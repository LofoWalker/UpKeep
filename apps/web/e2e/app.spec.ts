import { test, expect } from './fixtures';

test.describe('Home Page', () => {
  test('displays the main heading and navigation links', async ({ homePage }) => {
    await homePage.navigate();

    await expect(homePage.heading).toBeVisible();
    await expect(homePage.signInLink).toBeVisible();
    await expect(homePage.createAccountLink).toBeVisible();
  });

  test('navigates to login page when clicking Sign In', async ({ homePage, page }) => {
    await homePage.navigate();
    await homePage.clickSignIn();

    await expect(page).toHaveURL(/\/login/);
  });

  test('navigates to register page when clicking Create Account', async ({ homePage, page }) => {
    await homePage.navigate();
    await homePage.clickCreateAccount();

    await expect(page).toHaveURL(/\/register/);
  });
});

test.describe('Login Page', () => {
  test('displays login form elements', async ({ loginPage }) => {
    await loginPage.navigate();

    await expect(loginPage.emailInput).toBeVisible();
    await expect(loginPage.passwordInput).toBeVisible();
    await expect(loginPage.submitButton).toBeVisible();
  });

  test('shows validation error for empty form submission', async ({ loginPage }) => {
    await loginPage.navigate();
    await loginPage.submitButton.click();

    await expect(loginPage.page.getByText('Invalid email address')).toBeVisible();
  });
});

test.describe('Register Page', () => {
  test('displays registration form elements', async ({ registerPage }) => {
    await registerPage.navigate();

    await expect(registerPage.emailInput).toBeVisible();
    await expect(registerPage.passwordInput).toBeVisible();
    await expect(registerPage.submitButton).toBeVisible();
  });

  test('shows validation error for weak password', async ({ registerPage }) => {
    await registerPage.navigate();
    await registerPage.emailInput.fill('test@example.com');
    await registerPage.passwordInput.fill('password123');

    const confirmPassword = registerPage.page.getByLabel(/confirm.*password/i);
    if (await confirmPassword.isVisible()) {
      await confirmPassword.fill('password123');
    }

    await registerPage.submitButton.click();

    await expect(registerPage.page.getByText(/uppercase/i)).toBeVisible();
  });
});
