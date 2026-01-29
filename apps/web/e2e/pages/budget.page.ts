import {Locator, Page} from '@playwright/test';

export class BudgetPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly amountInput: Locator;
  readonly currencySelect: Locator;
  readonly setBudgetButton: Locator;
  readonly budgetBar: Locator;
  readonly budgetTotal: Locator;
  readonly budgetRemaining: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { name: /budget management/i });
    this.amountInput = page.getByLabel(/monthly budget/i);
    this.currencySelect = page.getByRole('combobox', { name: /currency/i });
    this.setBudgetButton = page.getByRole('button', { name: /set budget/i });
    this.budgetBar = page.locator('[class*="budgetbar"]').first();
    this.budgetTotal = page.getByText(/total/i);
    this.budgetRemaining = page.getByText(/remaining/i);
  }

  async navigate() {
    await this.page.goto(`/dashboard/budget`);
  }

  async setBudget(amount: string, currency: string = 'EUR') {
    await this.amountInput.fill(amount);
    await this.currencySelect.click();
    await this.page.getByRole('option', { name: new RegExp(currency, 'i') }).click();
    await this.setBudgetButton.click();
  }

  async waitForSuccess() {
    await this.page.waitForSelector('[role="status"]', { state: 'visible', timeout: 5000 });
  }
}
