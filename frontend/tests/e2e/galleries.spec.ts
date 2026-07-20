import { test, expect, type Page } from '@playwright/test'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'

async function createGallery(page: Page, name: string, order: number) {
  await page.getByRole('button', { name: 'Create gallery' }).click()
  await page.getByRole('textbox', { name: /Gallery name/ }).fill(name)
  await page.getByRole('spinbutton', { name: 'Gallery order' }).fill(String(order))
  const response = page.waitForResponse(
    (candidate) =>
      candidate.url().includes('/galleries') && candidate.request().method() === 'POST',
  )
  await page.getByRole('button', { name: 'Save gallery' }).click()
  expect((await response).status()).toBe(201)
}

test.describe('Galleries E2E', () => {
  test('owner and manager manage metadata while only owner controls lifecycle', async ({
    page,
  }) => {
    const suffix = `${Date.now()}-${test.info().workerIndex}`
    const eventName = `Stage 4 event ${suffix}`
    const managerEmail = `gallery-manager-${suffix}@example.com`
    const ownerGallery = `Reception ${suffix}`
    const managerGallery = `Preparations ${suffix}`
    const loginPage = new LoginPage(page)
    const dashboardPage = new DashboardPage(page)

    await loginPage.goto()
    expect((await loginPage.login('admin@example.com', 'password123')).status()).toBe(200)
    await dashboardPage.verifyIsLoaded()

    const csrfToken = await page.evaluate(() => {
      const cookie = document.cookie.split('; ').find((entry) => entry.startsWith('XSRF-TOKEN='))
      return cookie ? decodeURIComponent(cookie.split('=')[1]) : null
    })
    expect(csrfToken).not.toBeNull()
    expect(
      (
        await page.request.post('/api/auth/register', {
          headers: { 'X-XSRF-TOKEN': csrfToken! },
          data: { email: managerEmail, password: 'password123' },
        })
      ).status(),
    ).toBe(200)

    expect((await dashboardPage.createEvent(eventName)).status()).toBe(201)
    await dashboardPage.manageEvent(eventName)
    await createGallery(page, ownerGallery, 20)
    await expect(page.getByText(ownerGallery, { exact: true })).toBeVisible()
    expect((await dashboardPage.addManager(managerEmail)).status()).toBe(201)

    await dashboardPage.logout()
    await loginPage.login(managerEmail, 'password123')
    await dashboardPage.manageEvent(eventName)
    await createGallery(page, managerGallery, 10)
    await expect(page.getByText(managerGallery, { exact: true })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Archive gallery' })).toHaveCount(0)
    await expect(page.getByRole('button', { name: 'Delete gallery' })).toHaveCount(0)

    const managerCard = dashboardPage.galleryCard(managerGallery)
    await managerCard.getByRole('button', { name: /More actions for/ }).click()
    await page.getByRole('menuitem', { name: 'Edit gallery' }).click()
    await page.getByRole('textbox', { name: /Gallery name/ }).fill(`${managerGallery} edited`)
    const updateResponse = page.waitForResponse(
      (candidate) =>
        candidate.url().includes('/galleries/') && candidate.request().method() === 'PUT',
    )
    await page.getByRole('button', { name: 'Save gallery' }).click()
    expect((await updateResponse).status()).toBe(200)
    await expect(
      page.getByRole('button', { name: `More actions for ${managerGallery} edited` }),
    ).toBeFocused()

    await dashboardPage.logout()
    await loginPage.login('admin@example.com', 'password123')
    await dashboardPage.manageEvent(eventName)
    const ownerCard = dashboardPage.galleryCard(ownerGallery)
    const archiveResponse = page.waitForResponse(
      (candidate) =>
        candidate.url().endsWith('/archive') && candidate.request().method() === 'POST',
    )
    await ownerCard.getByRole('button', { name: /More actions for/ }).click()
    await page.getByRole('menuitem', { name: 'Archive gallery' }).click()
    await page
      .getByRole('dialog', { name: 'Archive gallery?' })
      .getByRole('button', { name: 'Archive gallery' })
      .click()
    expect((await archiveResponse).status()).toBe(200)
    await expect(ownerCard.getByText('Archived', { exact: true })).toBeVisible()
    await expect(
      ownerCard.getByRole('button', { name: `More actions for ${ownerGallery}` }),
    ).toBeFocused()

    const editedCard = dashboardPage.galleryCard(`${managerGallery} edited`)
    const deleteResponse = page.waitForResponse(
      (candidate) =>
        candidate.url().includes('/galleries/') && candidate.request().method() === 'DELETE',
    )
    await editedCard.getByRole('button', { name: /More actions for/ }).click()
    await page.getByRole('menuitem', { name: 'Delete gallery' }).click()
    await page
      .getByRole('dialog', { name: 'Delete gallery?' })
      .getByRole('button', { name: 'Delete gallery' })
      .click()
    expect((await deleteResponse).status()).toBe(204)
    await expect(page.getByText(`${managerGallery} edited`, { exact: true })).toHaveCount(0)
    await expect(page.getByRole('button', { name: 'Create gallery' })).toBeFocused()
  })
})
