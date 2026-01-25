import { test as base } from '@playwright/test';
import { HomePage, LoginPage, RegisterPage } from '../pages';

type Fixtures = {
  homePage: HomePage;
  loginPage: LoginPage;
  registerPage: RegisterPage;
};

/**
 * Extended test fixture that provides page objects for all tests.
 * Usage: test('example', async ({ homePage, loginPage }) => { ... });
 */
export const test = base.extend<Fixtures>({
  homePage: async ({ page }, use) => {
    const homePage = new HomePage(page);
    await use(homePage);
  },

  loginPage: async ({ page }, use) => {
    const loginPage = new LoginPage(page);
    await use(loginPage);
  },

  registerPage: async ({ page }, use) => {
    const registerPage = new RegisterPage(page);
    await use(registerPage);
  },
});

export { expect } from '@playwright/test';
