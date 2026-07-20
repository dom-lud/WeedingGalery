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
    await expect(this.page.getByRole('heading', { name: 'Your events', level: 1 })).toBeVisible()
  }

  async createEvent(name: string) {
    await this.page.getByRole('button', { name: /create event/i }).click()
    await this.page.getByLabel('Event name').fill(name)
    const responsePromise = this.page.waitForResponse(
      (response) =>
        response.url().endsWith('/api/events') && response.request().method() === 'POST',
    )
    await this.page.getByRole('button', { name: /^save$/i }).click()
    return await responsePromise
  }

  async manageEvent(name: string) {
    const workspace = this.page.getByRole('region', { name: `Manage ${name}`, exact: true })
    if (await workspace.isVisible()) return
    const cardHeading = this.page.getByRole('heading', { name, level: 3 })
    const visibleTarget = await Promise.race([
      workspace.waitFor({ state: 'visible' }).then(() => 'workspace' as const),
      cardHeading.waitFor({ state: 'visible' }).then(() => 'card' as const),
    ])
    if (visibleTarget === 'workspace') return
    const card = cardHeading.locator('xpath=ancestor::article[1]')
    await card.getByRole('button', { name: /^(manage|selected)$/i }).click()
  }

  galleryCard(name: string) {
    return this.page.getByRole('article', { name: `Gallery ${name}`, exact: true })
  }

  async addManager(email: string) {
    await this.openPeople()
    await this.page.getByLabel('Manager email').fill(email)
    const responsePromise = this.page.waitForResponse(
      (response) => response.url().includes('/members') && response.request().method() === 'POST',
    )
    await this.page.getByRole('button', { name: /add manager/i }).click()
    return await responsePromise
  }

  async openPeople() {
    await this.page.getByRole('tab', { name: 'People', exact: true }).click()
  }

  async openSettings() {
    await this.page.getByRole('tab', { name: 'Settings', exact: true }).click()
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
