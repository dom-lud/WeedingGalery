import { expect, type Page } from '@playwright/test'

export class PublicGalleryPage {
  constructor(readonly page: Page) {}

  async open(link: string) {
    await this.page.goto(link)
  }

  async enterCode(code: string) {
    await this.page.getByLabel('Access code').fill(code)
    await this.page.getByRole('button', { name: 'Open gallery' }).click()
  }

  async expectOpened(name: string) {
    await expect(this.page.getByRole('heading', { name })).toBeVisible()
    await expect(this.page).not.toHaveURL(/token=/)
  }

  async uploadPng(name: string) {
    const uploadResponse = this.page.waitForResponse(
      (response) =>
        response.request().method() === 'PUT' &&
        response.url().includes('/upload-sessions/') &&
        response.url().includes('/files/'),
    )
    await this.page.locator('input[type="file"]').setInputFiles({
      name,
      mimeType: 'image/png',
      buffer: Buffer.from(
        'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=',
        'base64',
      ),
    })
    expect((await uploadResponse).status()).toBe(200)
    await expect(this.page.getByRole('button', { name: `Open ${name}` })).toBeVisible({
      timeout: 15_000,
    })
    await expect(this.page.getByText('Your upload queue is empty.')).toBeVisible()
  }
}
