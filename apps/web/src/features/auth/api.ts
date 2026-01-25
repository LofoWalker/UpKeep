import {apiRequest} from '../../lib/api';

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

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    customerId: string;
    email: string;
    accountType: AccountType;
}

export interface User {
    id: string;
    email: string;
    accountType: AccountType;
}

export async function registerCustomer(data: RegisterRequest): Promise<RegisterResponse> {
    return apiRequest<RegisterResponse>('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(data),
        credentials: 'include',
    });
}

export async function loginCustomer(data: LoginRequest): Promise<LoginResponse> {
    return apiRequest<LoginResponse>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(data),
        credentials: 'include',
    });
}

export async function logoutCustomer(): Promise<void> {
    await apiRequest<string>('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
    });
}

export async function refreshToken(): Promise<void> {
    await apiRequest<string>('/api/auth/refresh', {
        method: 'POST',
        credentials: 'include',
    });
}

export interface MeResponse {
    customerId: string;
    email: string;
    accountType: AccountType;
}

export async function getCurrentUser(): Promise<User> {
    const response = await apiRequest<MeResponse>('/api/auth/me', {
        method: 'GET',
        credentials: 'include',
    });
    return {
        id: response.customerId,
        email: response.email,
        accountType: response.accountType,
    };
}

