import { test, expect } from './fixtures/auth.fixture'

test.describe('Authentication Flow E2E', () => {
  test('should login and access dashboard', async ({ loginPage, dashboardPage }) => {
    await loginPage.goto()
    const response = await loginPage.login('admin@example.com', 'password123!')
    expect(response.status()).toBe(200)
    await dashboardPage.verifyIsLoaded()
  })

  test('should login successfully on the first quick click without waiting for page stabilization', async ({
    loginPage,
    dashboardPage,
  }) => {
    const response = await loginPage.loginQuickly('ADMIN@EXAMPLE.COM', 'password123!')
    expect(response.status()).toBe(200)
    await dashboardPage.verifyIsLoaded()
  })

  test('should not expose public sign up entry point', async ({ page, loginPage }) => {
    await loginPage.goto()
    await loginPage.expectPublicSignUpHidden()

    await page.goto('/register')
    await loginPage.expectVisible()
  })

  test('should allow toggling password visibility on login', async ({ loginPage }) => {
    await loginPage.goto()
    await loginPage.expectPasswordHidden()
    await loginPage.togglePasswordVisibility()
    await loginPage.expectPasswordVisible()
    await loginPage.togglePasswordVisibility()
    await loginPage.expectPasswordHidden()
  })

  test('should show an error and stay on login page for invalid credentials', async ({
    loginPage,
  }) => {
    await loginPage.goto()
    const response = await loginPage.login('admin@example.com', 'wrongpass')

    expect(response.status()).toBe(401)
    await loginPage.expectVisible()
    await loginPage.expectInvalidCredentialsError()
  })

  test('should block access to dashboard for unauthenticated user', async ({ dashboardPage }) => {
    await dashboardPage.goto()
    await dashboardPage.expectRedirectedToLogin()
  })

  test('should logout, invalidate session and redirect back to login', async ({
    loginPage,
    dashboardPage,
  }) => {
    await loginPage.goto()
    const loginResponse = await loginPage.login('admin@example.com', 'password123!')
    expect(loginResponse.status()).toBe(200)
    await dashboardPage.verifyIsLoaded()

    const logoutResponse = await dashboardPage.logout()
    expect(logoutResponse.status()).toBe(200)
    await dashboardPage.expectRedirectedToLogin()

    await dashboardPage.goto()
    await dashboardPage.expectRedirectedToLogin()
  })
})
