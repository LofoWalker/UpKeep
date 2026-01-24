import { apiRequest } from '../../lib/api';

export enum AccountType {
  COMPANY = 'COMPANY',
  MAINTAINER = 'MAINTAINER',
  BOTH = 'BOTH',
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
  accountType: AccountType;
}

export interface RegisterResponse {
  customerId: string;
  email: string;
  accountType: AccountType;
}

export async function registerCustomer(data: RegisterRequest): Promise<RegisterResponse> {
  return apiRequest<RegisterResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
