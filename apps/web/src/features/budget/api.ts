import { apiRequest } from '@/lib/api';

export interface BudgetSummary {
  budgetId: string | null;
  totalCents: number;
  allocatedCents: number;
  remainingCents: number;
  currency: string;
  exists: boolean;
}

export interface SetBudgetRequest {
  amountCents: number;
  currency: string;
}

export interface BudgetResult {
  budgetId: string;
  amountCents: number;
  currency: string;
}

export async function getBudgetSummary(companyId: string): Promise<BudgetSummary> {
  return apiRequest<BudgetSummary>(`/api/companies/${companyId}/budget`);
}

export async function setBudget(
  companyId: string,
  request: SetBudgetRequest
): Promise<BudgetResult> {
  return apiRequest<BudgetResult>(`/api/companies/${companyId}/budget`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}
