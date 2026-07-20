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
    await this.page.locator('input[type="file"]').setInputFiles({
      name,
      mimeType: 'image/png',
      buffer: Buffer.from(
        'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=',
        'base64',
      ),
    })
    await this.page.getByRole('button', { name: 'Upload pending files' }).click()
    await expect(this.page.getByText('1 uploaded.')).toBeVisible()
  }
}
