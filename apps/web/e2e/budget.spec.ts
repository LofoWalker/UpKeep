import { test, expect } from './fixtures';

test.describe('Budget Management', () => {
  test.beforeEach(async ({ page, registerPage, loginPage }) => {
    const timestamp = Date.now();
    const email = `budget-test-${timestamp}@example.com`;

    await registerPage.navigate();
    await registerPage.register(email, 'SecurePass123', 'COMPANY');

    await page.waitForURL('/onboarding');

    const companyName = `Budget Test Company ${timestamp}`;
    await page.getByLabel(/company name/i).fill(companyName);
    await page.getByLabel(/company slug/i).fill(`budget-test-${timestamp}`);
    await page.getByRole('button', { name: /create company/i }).click();

    await page.waitForURL('/dashboard');
  });

  test('should display empty state when no budget is set', async ({ budgetPage, page }) => {
    await page.goto('/dashboard/budget');

    await expect(budgetPage.heading).toBeVisible();
    await expect(page.getByText(/set your monthly budget/i)).toBeVisible();
    await expect(budgetPage.amountInput).toBeVisible();
    await expect(budgetPage.currencySelect).toBeVisible();
  });

  test('should set budget successfully', async ({ budgetPage, page }) => {
    await page.goto('/dashboard/budget');

    await budgetPage.setBudget('500', 'EUR');

    await page.waitForSelector('text=/budget set successfully/i', { timeout: 5000 });

    await expect(page.getByText(/monthly budget/i)).toBeVisible();
    await expect(page.getByText(/500/)).toBeVisible();
  });

  test('should validate minimum budget amount', async ({ budgetPage, page }) => {
    await page.goto('/dashboard/budget');

    await budgetPage.amountInput.fill('0.50');
    await budgetPage.setBudgetButton.click();

    await expect(page.getByText(/budget must be at least/i)).toBeVisible();
  });

  test('should persist budget after page reload', async ({ budgetPage, page }) => {
    await page.goto('/dashboard/budget');

    await budgetPage.setBudget('750', 'USD');
    await page.waitForSelector('text=/budget set successfully/i', { timeout: 5000 });

    await page.reload();

    await expect(page.getByText(/750/)).toBeVisible();
    await expect(page.getByText(/USD/)).toBeVisible();
  });

  test('should display budget bar after setting budget', async ({ budgetPage, page }) => {
    await page.goto('/dashboard/budget');

    await budgetPage.setBudget('1000', 'EUR');
    await page.waitForSelector('text=/budget set successfully/i', { timeout: 5000 });

    await expect(page.getByText(/0% allocated/i)).toBeVisible();
    await expect(page.getByText(/1000.*remaining/i)).toBeVisible();
  });

  test('should support different currencies', async ({ budgetPage, page }) => {
    const currencies = ['EUR', 'USD', 'GBP'];

    for (const currency of currencies) {
      const timestamp = Date.now();
      const email = `budget-currency-${currency.toLowerCase()}-${timestamp}@example.com`;

      await page.goto('/register');
      await page.getByLabel(/email/i).fill(email);
      await page.getByLabel(/^password$/i).fill('SecurePass123');
      await page.getByLabel(/confirm password/i).fill('SecurePass123');
      await page.getByLabel(/company/i).check();
      await page.getByRole('button', { name: /create account/i }).click();

      await page.waitForURL('/onboarding');

      await page.getByLabel(/company name/i).fill(`Company ${currency} ${timestamp}`);
      await page.getByLabel(/company slug/i).fill(`company-${currency.toLowerCase()}-${timestamp}`);
      await page.getByRole('button', { name: /create company/i }).click();

      await page.waitForURL('/dashboard');
      await page.goto('/dashboard/budget');

      await budgetPage.setBudget('500', currency);
      await page.waitForSelector('text=/budget set successfully/i', { timeout: 5000 });

      await expect(page.getByText(new RegExp(currency))).toBeVisible();

      await page.goto('/login');
    }
  });

  test('should navigate from dashboard to budget page', async ({ page }) => {
    await page.goto('/dashboard');

    const setBudgetButton = page.getByRole('button', { name: /set budget/i }).first();
    await expect(setBudgetButton).toBeVisible();
    await setBudgetButton.click();

    await expect(page).toHaveURL(/\/dashboard\/budget/);
  });
});
