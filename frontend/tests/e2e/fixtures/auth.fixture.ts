import { test as base } from '@playwright/test'
import { LoginPage } from '../pages/LoginPage'
import { DashboardPage } from '../pages/DashboardPage'

// Declare the types of your fixtures.
type AuthFixtures = {
  loginPage: LoginPage
  dashboardPage: DashboardPage
}

// Extend base test by providing our custom fixtures.
// This new "test" can be used in multiple test files, and each of them will get the fixtures.
export const test = base.extend<AuthFixtures>({
  loginPage: async ({ page }, use) => {
    // Set up the fixture.
    const loginPage = new LoginPage(page)
    // Use the fixture value in the test.
    // eslint-disable-next-line react-hooks/rules-of-hooks
    await use(loginPage)
  },

  dashboardPage: async ({ page }, use) => {
    const dashboardPage = new DashboardPage(page)
    // eslint-disable-next-line react-hooks/rules-of-hooks
    await use(dashboardPage)
  },
})

export { expect } from '@playwright/test'
