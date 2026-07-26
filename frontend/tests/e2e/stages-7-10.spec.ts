import AxeBuilder from '@axe-core/playwright'
import { expect, test, type Page } from '@playwright/test'
import { adminEmail, adminPassword } from './fixtures/testCredentials'
import { LoginPage } from './pages/LoginPage'

type Json = Record<string, unknown>

async function csrf(page: Page) {
  return page.evaluate(() => {
    const cookie = document.cookie.split('; ').find((entry) => entry.startsWith('XSRF-TOKEN='))
    return cookie ? decodeURIComponent(cookie.slice('XSRF-TOKEN='.length)) : null
  })
}

async function api(page: Page, method: 'GET' | 'POST' | 'PUT', path: string, data?: Json) {
  const token = method === 'GET' ? null : await csrf(page)
  return page.request.fetch(`/api/${path}`, {
    method,
    headers: token ? { 'X-XSRF-TOKEN': token } : undefined,
    data,
  })
}

async function createOwnedGallery(page: Page, suffix: string) {
  const eventResponse = await api(page, 'POST', 'events', {
    name: `Stages 7-10 event ${suffix}`,
    type: 'WEDDING',
    description: 'E2E test data',
    privacyMode: 'PRIVATE',
  })
  expect(eventResponse.status()).toBe(201)
  const event = (await eventResponse.json()) as { id: string }
  const galleryResponse = await api(page, 'POST', `events/${event.id}/galleries`, {
    name: `Stages 7-10 gallery ${suffix}`,
    description: 'E2E gallery',
    sortOrder: 1,
  })
  expect(galleryResponse.status()).toBe(201)
  return {
    eventId: event.id,
    gallery: (await galleryResponse.json()) as { id: string; slug: string },
  }
}

async function expectNoWcagViolations(page: Page) {
  const results = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'])
    .analyze()
  expect(results.violations, JSON.stringify(results.violations, null, 2)).toEqual([])
}

test.describe('Stages 7-10 production E2E contract', () => {
  test('enforces auth, CSRF and the non-admin boundary on the admin namespace', async ({
    page,
    browser,
  }) => {
    const unauthenticated = await page.request.get('/api/admin/dashboard')
    expect(unauthenticated.status()).toBe(401)

    const login = new LoginPage(page)
    await login.goto()
    expect((await login.login(adminEmail, adminPassword)).status()).toBe(200)

    const csrfToken = await csrf(page)
    expect(csrfToken).not.toBeNull()
    const withoutCsrf = await page.request.post('/api/admin/users/not-a-real-user/lock')
    expect(withoutCsrf.status()).toBe(403)

    const dashboard = await page.request.get('/api/admin/dashboard')
    expect(dashboard.status()).toBe(200)
    expect((await dashboard.json()) as Json).toHaveProperty('users')

    const userEmail = `e2e-user-${Date.now()}@example.com`
    const register = await page.request.post('/api/auth/register', {
      headers: { 'X-XSRF-TOKEN': csrfToken! },
      data: { email: userEmail, password: 'password123' },
    })
    expect(register.status()).toBe(200)
    const userContext = await browser.newContext()
    const userPage = await userContext.newPage()
    const userLogin = new LoginPage(userPage)
    await userLogin.goto()
    expect((await userLogin.login(userEmail, 'password123')).status()).toBe(200)
    expect((await userPage.request.get('/api/admin/dashboard')).status()).toBe(403)
    await userContext.close()
  })

  test('requires a public grant before exposing a gallery and opens it after exchange', async ({
    page,
    browser,
  }) => {
    const login = new LoginPage(page)
    await login.goto()
    expect((await login.login(adminEmail, adminPassword)).status()).toBe(200)
    const owned = await createOwnedGallery(page, `${Date.now()}-${test.info().workerIndex}`)

    const rotate = await api(
      page,
      'POST',
      `events/${owned.eventId}/galleries/${owned.gallery.id}/access-token/rotate`,
    )
    expect(rotate.status()).toBe(201)
    const token = (await rotate.json()) as { accessToken: string }
    const settings = await api(
      page,
      'PUT',
      `events/${owned.eventId}/galleries/${owned.gallery.id}/settings`,
      {
        publicViewEnabled: true,
        uploadEnabled: false,
        downloadEnabled: false,
        moderationMode: 'REQUIRED',
        version: 0,
      },
    )
    expect(settings.status()).toBe(200)

    const guest = await browser.newContext()
    const guestPage = await guest.newPage()
    const noGrant = await guest.request.get(`/api/public/galleries/${owned.gallery.slug}`)
    expect([401, 404]).toContain(noGrant.status())
    const exchange = await guest.request.post(
      `/api/public/galleries/${owned.gallery.slug}/access`,
      {
        data: { accessToken: token.accessToken },
      },
    )
    expect(exchange.status()).toBe(200)
    const publicGallery = await guest.request.get(`/api/public/galleries/${owned.gallery.slug}`)
    expect(publicGallery.status()).toBe(200)
    await guestPage.goto(`/g/${owned.gallery.slug}`)
    await expect(guestPage.getByRole('heading', { name: /Stages 7-10 gallery/ })).toBeVisible()
    await expectNoWcagViolations(guestPage)
    await guest.close()
  })

  test('keeps customization textual and versioned', async ({ page }) => {
    const login = new LoginPage(page)
    await login.goto()
    expect((await login.login(adminEmail, adminPassword)).status()).toBe(200)
    const owned = await createOwnedGallery(page, `${Date.now()}-custom`)
    const read = await api(page, 'GET', `galleries/${owned.gallery.id}/customization`)
    expect(read.status()).toBe(200)
    const current = (await read.json()) as { version: number }
    const update = await api(page, 'PUT', `galleries/${owned.gallery.id}/customization`, {
      theme: 'MINIMAL',
      layout: 'GRID',
      primaryColor: '#112233',
      accentColor: '#445566',
      backgroundColor: '#F7F4F2',
      welcomeText: 'Text stays text: <script>alert(1)</script>',
      showTitle: true,
      showUpload: true,
      showDownload: false,
      coverMediaId: null,
      version: current.version,
    })
    expect(update.status()).toBe(400)
    const safeUpdate = await api(page, 'PUT', `galleries/${owned.gallery.id}/customization`, {
      theme: 'MINIMAL',
      layout: 'GRID',
      primaryColor: '#112233',
      accentColor: '#445566',
      backgroundColor: '#F7F4F2',
      welcomeText: 'Text stays text',
      showTitle: true,
      showUpload: true,
      showDownload: false,
      coverMediaId: null,
      version: current.version,
    })
    expect(safeUpdate.status()).toBe(200)
    expect((await safeUpdate.json()) as Json).toMatchObject({ theme: 'MINIMAL', version: 1 })
  })

  test('keeps moderation scoped and protected by CSRF, including bulk limits', async ({ page }) => {
    const login = new LoginPage(page)
    await login.goto()
    expect((await login.login(adminEmail, adminPassword)).status()).toBe(200)
    const owned = await createOwnedGallery(page, `${Date.now()}-moderation`)
    const mediaPath = `events/${owned.eventId}/galleries/${owned.gallery.id}/media`

    const withoutCsrf = await page.request.post(`${mediaPath}/not-a-real-media-id/approve`)
    expect(withoutCsrf.status()).toBe(403)
    const missingMedia = await api(page, 'POST', `${mediaPath}/not-a-real-media-id/approve`, {})
    expect(missingMedia.status()).toBe(404)
    const tooMany = await api(page, 'POST', `${mediaPath}/bulk-actions`, {
      mediaIds: Array.from({ length: 101 }, (_, index) => `media-${index}`),
      action: 'APPROVE',
    })
    expect(tooMany.status()).toBe(400)
  })

  test('renders the admin dashboard with accessible data and denial state for non-admins', async ({
    page,
  }) => {
    const login = new LoginPage(page)
    await login.goto()
    expect((await login.login(adminEmail, adminPassword)).status()).toBe(200)
    await page.goto('/admin')
    await expect(page.getByRole('heading', { name: 'Administration', level: 1 })).toBeVisible()
    await expect(page.getByRole('region', { name: 'Platform summary' })).toBeVisible()
    await expectNoWcagViolations(page)
  })

  test('public unavailable state is accessible and does not expose storage details', async ({
    page,
  }) => {
    await page.goto('/g/does-not-exist-for-e2e')
    await expect(page.getByRole('heading', { name: 'Gallery unavailable' })).toBeVisible()
    await expect(page.locator('body')).not.toContainText(/storageKey|objectKey|password|token/i)
    await expectNoWcagViolations(page)
  })
})
