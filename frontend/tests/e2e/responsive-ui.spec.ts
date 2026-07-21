import { expect, test } from '@playwright/test'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { adminEmail, adminPassword } from './fixtures/testCredentials'

test.describe('Responsive UI at 360px', () => {
  test.use({ viewport: { width: 360, height: 800 }, reducedMotion: 'reduce' })

  test('keeps login immediately keyboard-operable without horizontal overflow', async ({
    page,
  }) => {
    await page.goto('/login')

    const emailInput = page.locator('#login-email')
    await expect(emailInput).toBeVisible()
    await page.keyboard.press('Tab')
    await expect(emailInput).toBeFocused()
    await page.keyboard.type('admin@example.com')
    await page.keyboard.press('Tab')
    await page.keyboard.type(adminPassword)

    const loginMetrics = await page.evaluate(() => ({
      documentWidth: document.documentElement.scrollWidth,
      viewportWidth: document.documentElement.clientWidth,
      buttonHeights: Array.from(document.querySelectorAll('button'))
        .filter((button) => button.getClientRects().length > 0)
        .map((button) => Math.round(button.getBoundingClientRect().height)),
    }))
    expect(loginMetrics.documentWidth).toBeLessThanOrEqual(loginMetrics.viewportWidth)
    expect(loginMetrics.buttonHeights).not.toHaveLength(0)
    expect(Math.min(...loginMetrics.buttonHeights)).toBeGreaterThanOrEqual(44)

    const loginResponse = page.waitForResponse((response) =>
      response.url().includes('/api/auth/login'),
    )
    await page.keyboard.press('Enter')
    expect((await loginResponse).status()).toBe(200)
    await expect(page).toHaveURL(/.*\/dashboard/)

    const dashboardMetrics = await page.evaluate(() => ({
      documentWidth: document.documentElement.scrollWidth,
      viewportWidth: document.documentElement.clientWidth,
      buttonHeights: Array.from(document.querySelectorAll('button'))
        .filter((button) => button.getClientRects().length > 0)
        .map((button) => Math.round(button.getBoundingClientRect().height)),
    }))
    expect(dashboardMetrics.documentWidth).toBeLessThanOrEqual(dashboardMetrics.viewportWidth)
    expect(dashboardMetrics.buttonHeights).not.toHaveLength(0)
    expect(Math.min(...dashboardMetrics.buttonHeights)).toBeGreaterThanOrEqual(44)

    const originatingManageButton = page
      .getByRole('button', { name: 'Manage', exact: true })
      .first()
    await originatingManageButton.click()
    const eventWorkspace = page.getByRole('region', { name: /^Manage / })
    await expect(eventWorkspace).toBeVisible()
    const detailHeading = eventWorkspace.getByRole('heading', { level: 2 }).first()
    await expect(detailHeading).toBeFocused()
    expect(await detailHeading.evaluate((node) => getComputedStyle(node).borderLeftColor)).not.toBe(
      'rgba(0, 0, 0, 0)',
    )
    await expect(page.getByRole('button', { name: 'Back to events' })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Events', exact: true })).toHaveCount(0)

    const galleriesTab = page.getByRole('tab', { name: 'Galleries' })
    await expect(galleriesTab).toHaveAttribute('aria-controls', 'event-panel-galleries')
    await galleriesTab.focus()
    await page.keyboard.press('ArrowRight')
    const peopleTab = page.getByRole('tab', { name: 'People' })
    await expect(peopleTab).toBeFocused()
    await page.keyboard.press('Enter')
    await expect(page.getByRole('tabpanel', { name: 'People' })).toBeVisible()

    await page.getByRole('button', { name: 'Back to events' }).click()
    await expect(page.getByRole('heading', { name: 'Events', exact: true })).toBeVisible()
    await expect(originatingManageButton).toBeFocused()
  })
})

test.describe('Responsive UI at 1024px', () => {
  test.use({ viewport: { width: 1024, height: 900 }, reducedMotion: 'reduce' })

  test('uses a single list-detail mode without stacking the event list above details', async ({
    page,
  }) => {
    const login = new LoginPage(page)
    await login.goto()
    expect((await login.login(adminEmail, adminPassword)).status()).toBe(200)

    await expect(page.getByRole('heading', { name: 'Events', exact: true })).toBeVisible()
    await page.getByRole('button', { name: 'Manage', exact: true }).first().click()
    await expect(page.getByRole('region', { name: /^Manage / })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Events', exact: true })).toHaveCount(0)
    await expect(page.getByRole('button', { name: 'Back to events' })).toBeVisible()
  })
})

test.describe('Responsive UI at 390px', () => {
  test.use({ viewport: { width: 390, height: 844 }, reducedMotion: 'reduce' })

  test('uses a full-screen event dialog and keeps the dashboard aligned', async ({ page }) => {
    const login = new LoginPage(page)
    const dashboard = new DashboardPage(page)
    await login.goto()
    expect((await login.login(adminEmail, adminPassword)).status()).toBe(200)
    await dashboard.verifyIsLoaded()

    await page.getByRole('button', { name: 'Create event' }).click()
    const dialog = page.getByRole('dialog', { name: 'Create event' })
    await expect(dialog).toBeVisible()
    const bounds = await dialog.boundingBox()
    expect(bounds?.width).toBe(390)
    expect(bounds?.height).toBe(844)
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(390)
  })
})
