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
    await expect(this.page.locator('h1')).toContainText('My Events')
  }

  async createEvent(name: string) {
    await this.page.getByRole('button', { name: /create event/i }).click()
    await this.page.getByLabel('Event name').fill(name)
    const responsePromise = this.page.waitForResponse((response) =>
      response.url().endsWith('/api/events') && response.request().method() === 'POST',
    )
    await this.page.getByRole('button', { name: /^save$/i }).click()
    return await responsePromise
  }

  async manageEvent(name: string) {
    const card = this.page.getByRole('heading', { name, level: 6 }).locator('..').locator('..')
    await card.getByRole('button', { name: /^manage$/i }).click()
  }

  async addManager(email: string) {
    await this.page.getByLabel('Manager email').fill(email)
    const responsePromise = this.page.waitForResponse((response) =>
      response.url().includes('/members') && response.request().method() === 'POST',
    )
    await this.page.getByRole('button', { name: /add manager/i }).click()
    return await responsePromise
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
