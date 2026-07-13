import { Page, expect } from '@playwright/test'

export class DashboardPage {
  readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async goto() {
    await this.page.goto('/dashboard')
  }

  async verifyIsLoaded() {
    await expect(this.page).toHaveURL(/.*\/dashboard/, { timeout: 5000 })
    await expect(this.page.locator('h1')).toContainText('My Galleries')
  }

  async logout() {
    const logoutResponsePromise = this.page.waitForResponse((response) =>
      response.url().includes('/api/auth/logout'),
    )
    await this.page.getByRole('button', { name: /log out/i }).click()
    return await logoutResponsePromise
  }

  async expectRedirectedToLogin() {
    await expect(this.page).toHaveURL(/.*\/login/)
  }
}
