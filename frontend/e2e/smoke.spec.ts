import { expect, test } from '@playwright/test';

test('guest access to protected subscriptions routes is blocked', async ({ page }) => {
  await page.goto('/subscriptions');
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();
});

test('login route is accessible for users', async ({ page }) => {
  await page.goto('/login');
  await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Login with Chzzk' })).toBeVisible();
});
