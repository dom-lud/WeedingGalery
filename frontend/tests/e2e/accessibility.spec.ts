import AxeBuilder from '@axe-core/playwright'
import { expect, test } from '@playwright/test'
import type { Page } from '@playwright/test'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'

const wcagTags = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']

async function expectNoWcagViolations(page: Page) {
  const results = await new AxeBuilder({ page }).withTags(wcagTags).analyze()
  expect(results.violations, JSON.stringify(results.violations, null, 2)).toEqual([])
}

test.describe('Automated accessibility baseline', () => {
  test('login has no WCAG A/AA violations', async ({ page }) => {
    await new LoginPage(page).goto()
    await expectNoWcagViolations(page)
  })

  test('authenticated dashboard has no WCAG A/AA violations', async ({ page }) => {
    const login = new LoginPage(page)
    const dashboard = new DashboardPage(page)
    await login.goto()
    expect((await login.login('admin@example.com', 'password123')).status()).toBe(200)
    await dashboard.verifyIsLoaded()
    await expectNoWcagViolations(page)
  })
})
