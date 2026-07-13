import { expect, test } from '@playwright/test'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { PublicGalleryPage } from './pages/PublicGalleryPage'

test('owner publishes a code-protected gallery and a guest uploads a file', async ({ page }) => {
  const suffix = `${Date.now()}-${test.info().workerIndex}`
  const eventName = `Public flow ${suffix}`
  const galleryName = `Guest photos ${suffix}`
  const login = new LoginPage(page)
  const dashboard = new DashboardPage(page)

  await login.goto()
  expect((await login.login('admin@example.com', 'password123')).status()).toBe(200)
  expect((await dashboard.createEvent(eventName)).status()).toBe(201)
  await dashboard.manageEvent(eventName)
  await page.getByRole('button', { name: 'Create gallery' }).click()
  await page.getByLabel('Gallery name').fill(galleryName)
  await page.getByRole('button', { name: 'Save gallery' }).click()
  const galleryCard = page
    .getByText(galleryName, { exact: true })
    .locator('xpath=ancestor::div[contains(@class,"MuiCard-root")][1]')
  await galleryCard.getByRole('button', { name: 'Access settings' }).click()
  await page.getByRole('button', { name: 'Rotate private share link' }).click()
  const shareLink = await page.getByLabel('New private share link').inputValue()
  await page.getByLabel('Enable guest view').check()
  await page.getByLabel('Allow guest uploads').check()
  await page.getByRole('button', { name: 'Save access settings' }).click()
  await page.getByLabel('New access code').fill('secret1')
  await page.getByRole('button', { name: 'Set code' }).click()

  await page.context().clearCookies()
  const publicGallery = new PublicGalleryPage(page)
  await publicGallery.open(shareLink)
  await publicGallery.enterCode('wrong11')
  await expect(page.getByText('That access code is not valid.')).toBeVisible()
  await publicGallery.enterCode('secret1')
  await publicGallery.expectOpened(galleryName)
  await publicGallery.uploadPng('guest-photo.png')
})
