import {Locator, Page, test as base} from '@playwright/test';

/**
 * Base page object providing common selectors and navigation methods.
 */
export class BasePage {
  constructor(readonly page: Page) {}

  async goto(path: string): Promise<void> {
    await this.page.goto(path);
  }


  getByTestId(testId: string): Locator {
    return this.page.getByTestId(testId);
  }

  getByRole(role: Parameters<Page['getByRole']>[0], options?: Parameters<Page['getByRole']>[1]): Locator {
    return this.page.getByRole(role, options);
  }

  getByText(text: string | RegExp): Locator {
    return this.page.getByText(text);
  }
}

/**
 * Page object for the Home page.
 */
export class HomePage extends BasePage {
  readonly signInLink: Locator;
  readonly createAccountLink: Locator;
  readonly heading: Locator;

  constructor(page: Page) {
    super(page);
    this.heading = page.getByRole('heading', { name: 'Upkeep', exact: true });
    this.signInLink = page.getByRole('link', { name: 'Sign In' });
    this.createAccountLink = page.getByRole('link', { name: 'Create Account' });
  }

  async navigate(): Promise<void> {
    await this.goto('/');
  }

  async clickSignIn(): Promise<void> {
    await this.signInLink.click();
  }

  async clickCreateAccount(): Promise<void> {
    await this.createAccountLink.click();
  }
}

/**
 * Page object for the Login page.
 */
export class LoginPage extends BasePage {
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;
  readonly registerLink: Locator;
  readonly githubButton: Locator;

  constructor(page: Page) {
    super(page);
    this.emailInput = page.getByLabel(/email/i);
    this.passwordInput = page.getByLabel(/password/i);
    this.submitButton = page.getByRole('button', { name: /sign in|login/i });
    this.registerLink = page.getByRole('link', { name: /create.*account|register|sign up/i });
    this.githubButton = page.getByRole('button', { name: /github/i });
  }

  async navigate(): Promise<void> {
    await this.goto('/login');
  }

  async login(email: string, password: string): Promise<void> {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }
}

/**
 * Page object for the Register page.
 */
export class RegisterPage extends BasePage {
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly confirmPasswordInput: Locator;
  readonly submitButton: Locator;
  readonly loginLink: Locator;

  constructor(page: Page) {
    super(page);
    this.emailInput = page.getByLabel(/email/i);
    this.passwordInput = page.getByLabel(/^password$/i);
    this.confirmPasswordInput = page.getByLabel(/confirm.*password/i);
    this.submitButton = page.getByRole('button', { name: /create.*account|register|sign up/i });
    this.loginLink = page.getByRole('link', { name: /sign in|login|already have/i });
  }

  async navigate(): Promise<void> {
    await this.goto('/register');
  }

  async register(email: string, password: string, confirmPassword?: string): Promise<void> {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.confirmPasswordInput.fill(confirmPassword ?? password);
    await this.submitButton.click();
  }
}

export { base as test };
