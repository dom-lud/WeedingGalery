import { test, expect } from '@playwright/test';

test.describe('Authentication Flow E2E', () => {
  const password = 'password123';

  test('should register a new user successfully', async ({ page }) => {
    const uniqueEmail = `test-${Date.now()}@example.com`;
    
    const csrfPromise = page.waitForResponse(r => r.url().includes('/api/auth/csrf'));
    await page.goto('/register');
    await csrfPromise; // Czekamy aż CSRF ciastko spłynie
    
    await expect(page.locator('h1')).toContainText('Create Account');
    
    await page.fill('input[type="email"]', uniqueEmail);
    await page.fill('input[type="password"]', password);
    
    const responsePromise = page.waitForResponse(r => r.url().includes('/api/auth/register'));
    await page.click('button[type="submit"]');
    const response = await responsePromise;
    expect(response.status()).toBe(200); // Rejestracja zwraca 200 OK w AuthController.java

    await expect(page.locator('text=Registration successful!')).toBeVisible();
    await expect(page).toHaveURL(/.*\/login/, { timeout: 5000 });
  });

  test('should login and access dashboard', async ({ page }) => {
    const loginEmail = `login-${Date.now()}@example.com`;
    
    const csrfPromise = page.waitForResponse(r => r.url().includes('/api/auth/csrf'));
    await page.goto('/register');
    await csrfPromise;

    await page.fill('input[type="email"]', loginEmail);
    await page.fill('input[type="password"]', password);
    const responsePromise = page.waitForResponse(r => r.url().includes('/api/auth/register'));
    await page.click('button[type="submit"]');
    await responsePromise;
    
    await expect(page).toHaveURL(/.*\/login/, { timeout: 8000 });

    await page.fill('input[type="email"]', loginEmail);
    await page.fill('input[type="password"]', password);
    const loginResponsePromise = page.waitForResponse(r => r.url().includes('/api/auth/login'));
    await page.click('button[type="submit"]');
    await loginResponsePromise;

    await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 5000 });
    await expect(page.locator('h1')).toContainText('My Galleries');
  });
  
  test('should block access to dashboard for unauthenticated user', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/.*\/login/);
  });
});
