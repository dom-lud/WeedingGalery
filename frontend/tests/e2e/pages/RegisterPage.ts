import { Page } from '@playwright/test'

export class RegisterPage {
  readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async goto() {
    const csrfPromise = this.page.waitForResponse((r) => r.url().includes('/api/auth/csrf'))
    await this.page.goto('/register')
    await csrfPromise
  }

  async register(email: string, password: string) {
    await this.page.fill('input[type="email"]', email)
    await this.page.fill('input[type="password"]', password)
    const responsePromise = this.page.waitForResponse((r) => r.url().includes('/api/auth/register'))
    await this.page.click('button[type="submit"]')
    return await responsePromise
  }
}
