import { expect, Page, Response } from '@playwright/test'

export class LoginPage {
  readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async goto() {
    await this.page.goto('/login')
  }

  async gotoWithoutWaitingForCsrf() {
    await this.page.goto('/login')
  }

  async fillEmail(email: string) {
    await this.page.fill('input[type="email"]', email)
  }

  async fillPassword(password: string) {
    await this.page.fill('#login-password', password)
  }

  async submitLogin(): Promise<Response> {
    const loginResponsePromise = this.page.waitForResponse((r) =>
      r.url().includes('/api/auth/login'),
    )
    await this.page.click('button[type="submit"]')
    return await loginResponsePromise
  }

  async login(email: string, password: string): Promise<Response> {
    await this.fillEmail(email)
    await this.fillPassword(password)
    return await this.submitLogin()
  }

  async loginQuickly(email: string, password: string): Promise<Response> {
    await this.gotoWithoutWaitingForCsrf()
    await this.fillEmail(email)
    await this.fillPassword(password)
    return await this.submitLogin()
  }

  async expectPublicSignUpHidden() {
    await expect(this.page.getByRole('link', { name: /sign up/i })).toHaveCount(0)
  }

  async expectVisible() {
    await expect(this.page).toHaveURL(/.*\/login/)
    await expect(this.page.getByRole('heading', { name: /sign in to your gallery/i })).toBeVisible()
  }

  async expectPasswordHidden() {
    await expect(this.page.locator('#login-password')).toHaveAttribute('type', 'password')
  }

  async expectPasswordVisible() {
    await expect(this.page.locator('#login-password')).toHaveAttribute('type', 'text')
  }

  async togglePasswordVisibility() {
    const toggleButton = this.page.getByRole('button', {
      name: /show password|hide password/i,
    })
    await toggleButton.click()
  }

  async expectInvalidCredentialsError() {
    await expect(this.page.getByText(/invalid email or password/i)).toBeVisible()
  }
}
