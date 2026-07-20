import { test, expect } from './fixtures/auth.fixture'

test.describe('Events and memberships E2E', () => {
  test('owner creates an event, adds a manager and transfers ownership', async ({
    page,
    loginPage,
    dashboardPage,
  }) => {
    const suffix = `${Date.now()}-${test.info().workerIndex}`
    const managerEmail = `manager-${suffix}@example.com`
    const eventName = `Stage 3 event ${suffix}`

    await loginPage.goto()
    expect((await loginPage.login('admin@example.com', 'password123')).status()).toBe(200)
    await dashboardPage.verifyIsLoaded()

    const csrfToken = await page.evaluate(() => {
      const cookie = document.cookie.split('; ').find((entry) => entry.startsWith('XSRF-TOKEN='))
      return cookie ? decodeURIComponent(cookie.split('=')[1]) : null
    })
    expect(csrfToken).not.toBeNull()
    const registerResponse = await page.request.post('/api/auth/register', {
      headers: { 'X-XSRF-TOKEN': csrfToken! },
      data: { email: managerEmail, password: 'password123' },
    })
    expect(registerResponse.status()).toBe(200)

    expect((await dashboardPage.createEvent(eventName)).status()).toBe(201)
    await expect(page.getByRole('heading', { name: eventName, level: 3 })).toBeVisible()
    await dashboardPage.manageEvent(eventName)
    expect((await dashboardPage.addManager(managerEmail)).status()).toBe(201)
    await expect(page.getByText(managerEmail, { exact: true })).toBeVisible()

    await dashboardPage.logout()
    await loginPage.login(managerEmail, 'password123')
    await dashboardPage.verifyIsLoaded()
    await expect(
      page.getByRole('heading', { name: eventName, exact: true, level: 3 }),
    ).toBeVisible()
    await dashboardPage.manageEvent(eventName)
    await dashboardPage.openPeople()
    await expect(page.getByLabel('Manager email')).toHaveCount(0)
    await dashboardPage.openSettings()
    await expect(page.getByRole('button', { name: /delete event/i })).toHaveCount(0)

    await dashboardPage.logout()
    await loginPage.login('admin@example.com', 'password123')
    await dashboardPage.manageEvent(eventName)
    await dashboardPage.openPeople()
    const managerRow = page.getByText(managerEmail, { exact: true }).locator('xpath=ancestor::li')
    const transferResponsePromise = page.waitForResponse(
      (response) =>
        response.url().includes('/ownership-transfer') && response.request().method() === 'POST',
    )
    await managerRow.getByRole('button', { name: /transfer ownership/i }).click()
    await page
      .getByRole('dialog', { name: 'Transfer ownership?' })
      .getByRole('button', { name: 'Transfer ownership' })
      .click()
    expect((await transferResponsePromise).status()).toBe(200)

    await dashboardPage.logout()
    await loginPage.login(managerEmail, 'password123')
    await dashboardPage.manageEvent(eventName)
    await dashboardPage.openPeople()
    await expect(page.getByLabel('Manager email')).toBeVisible()
    await dashboardPage.openSettings()
    await expect(page.getByRole('button', { name: /delete event/i })).toBeVisible()
  })
})
