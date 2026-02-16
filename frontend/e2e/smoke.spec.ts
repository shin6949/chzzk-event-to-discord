import { expect, test } from '@playwright/test';

test('guest access to protected app routes is blocked', async ({ page }) => {
  await page.goto('/app/dashboard');
  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByRole('heading', { name: 'Chzzk Event to Discord' })).toBeVisible();
});

test('mock oauth callback can open authenticated dashboard', async ({ page }) => {
  await page.goto('/auth/chzzk/login');
  await page.getByRole('button', { name: 'Simulate USER callback' }).click();
  await page.getByRole('button', { name: 'Complete sign in' }).click();
  await expect(page).toHaveURL(/\/app\/dashboard$/);
  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
});
