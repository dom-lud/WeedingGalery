import { test, expect } from './fixtures/auth.fixture'

test.describe('Authentication Flow E2E', () => {
  const password = 'password123'

  test('should register a new user successfully', async ({ page, registerPage }) => {
    const uniqueEmail = `test-${Date.now()}@example.com`

    await registerPage.goto()
    await expect(page.locator('h1')).toContainText('Create Account')

    const response = await registerPage.register(uniqueEmail, password)
    expect(response.status()).toBe(200)

    await expect(page.locator('text=Registration successful!')).toBeVisible()
    await expect(page).toHaveURL(/.*\/login/, { timeout: 5000 })
  })

  test('should login and access dashboard', async ({
    page,
    registerPage,
    loginPage,
    dashboardPage,
  }) => {
    const loginEmail = `login-${Date.now()}@example.com`

    await registerPage.goto()
    await registerPage.register(loginEmail, password)

    await expect(page).toHaveURL(/.*\/login/, { timeout: 8000 })

    await loginPage.login(loginEmail, password)
    await dashboardPage.verifyIsLoaded()
  })

  test('should block access to dashboard for unauthenticated user', async ({
    page,
    dashboardPage,
  }) => {
    await dashboardPage.goto()
    await expect(page).toHaveURL(/.*\/login/)
  })
})
