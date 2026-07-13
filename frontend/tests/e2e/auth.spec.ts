import { test, expect } from './fixtures/auth.fixture'

test.describe('Authentication Flow E2E', () => {
  test('should login and access dashboard', async ({ loginPage, dashboardPage }) => {
    await loginPage.goto()
    await loginPage.login('admin@example.com', 'password123')
    await dashboardPage.verifyIsLoaded()
  })

  test('should not expose public sign up entry point', async ({ page, loginPage }) => {
    await loginPage.goto()
    await expect(page.getByRole('link', { name: /sign up/i })).toHaveCount(0)

    await page.goto('/register')
    await expect(page).toHaveURL(/.*\/login/)
    await expect(page.getByRole('heading', { name: /welcome back/i })).toBeVisible()
  })

  test('should allow toggling password visibility on login', async ({ page, loginPage }) => {
    await loginPage.goto()

    const passwordInput = page.locator('.password-input-wrapper input')
    await expect(passwordInput).toHaveAttribute('type', 'password')

    await page.getByRole('button', { name: /show password/i }).click()
    await expect(passwordInput).toHaveAttribute('type', 'text')

    await page.getByRole('button', { name: /hide password/i }).click()
    await expect(passwordInput).toHaveAttribute('type', 'password')
  })

  test('should block access to dashboard for unauthenticated user', async ({
    page,
    dashboardPage,
  }) => {
    await dashboardPage.goto()
    await expect(page).toHaveURL(/.*\/login/)
  })
})
