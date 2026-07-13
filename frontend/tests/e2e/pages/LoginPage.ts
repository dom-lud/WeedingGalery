import { Page, Response } from '@playwright/test'

export class LoginPage {
  readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async goto() {
    const csrfPromise = this.page.waitForResponse((r) => r.url().includes('/api/auth/csrf'))
    await this.page.goto('/login')
    await csrfPromise
  }

  async login(email: string, password: string): Promise<Response> {
    await this.page.fill('input[type="email"]', email)
    await this.page.fill('input[type="password"]', password)
    const loginResponsePromise = this.page.waitForResponse((r) =>
      r.url().includes('/api/auth/login'),
    )
    await this.page.click('button[type="submit"]')
    return await loginResponsePromise
  }
}
