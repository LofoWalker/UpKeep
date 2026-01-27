import {apiRequest} from '@/lib/api';

export enum Role {
    OWNER = 'OWNER',
    MEMBER = 'MEMBER',
}

export enum InvitationStatus {
    PENDING = 'PENDING',
    ACCEPTED = 'ACCEPTED',
    DECLINED = 'DECLINED',
    EXPIRED = 'EXPIRED',
}

export interface Company {
    id: string;
    name: string;
    slug: string;
}

export interface CompanyWithRole extends Company {
    role: Role;
}

export interface Membership {
    id: string;
    role: Role;
}

export interface CompanyResponse {
    id: string;
    name: string;
    slug: string;
    membership: Membership;
}

export interface DashboardStats {
    totalMembers: number;
    hasBudget: boolean;
    hasPackages: boolean;
    hasAllocations: boolean;
}

export interface CompanyDashboard {
    id: string;
    name: string;
    slug: string;
    userRole: Role;
    stats: DashboardStats;
}

export interface MemberInfo {
    membershipId: string;
    customerId: string;
    email: string;
    role: Role;
    joinedAt: string;
}

export interface InvitationInfo {
    id: string;
    email: string;
    role: Role;
    status: InvitationStatus;
    expiresAt: string;
}

export interface InvitationDetails {
    id: string;
    companyName: string;
    role: Role;
    status: InvitationStatus;
    isExpired: boolean;
    expiresAt: string;
}

export interface AcceptInvitationResult {
    companyId: string;
    companyName: string;
    companySlug: string;
    membershipId: string;
    role: Role;
}

export interface CreateCompanyRequest {
    name: string;
    slug?: string;
}

export interface InviteUserRequest {
    email: string;
    role: Role;
}

export interface UpdateMemberRoleRequest {
    role: Role;
}

export async function createCompany(data: CreateCompanyRequest): Promise<CompanyResponse> {
    return apiRequest<CompanyResponse>('/api/companies', {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

export async function getUserCompanies(): Promise<CompanyWithRole[]> {
    return apiRequest<CompanyWithRole[]>('/api/companies', {
        method: 'GET',
    });
}

export async function getCompanyDashboard(companyId: string): Promise<CompanyDashboard> {
    return apiRequest<CompanyDashboard>(`/api/companies/${companyId}/dashboard`, {
        method: 'GET',
    });
}

export async function getCompanyMembers(companyId: string): Promise<MemberInfo[]> {
    return apiRequest<MemberInfo[]>(`/api/companies/${companyId}/members`, {
        method: 'GET',
    });
}

export async function inviteUser(companyId: string, data: InviteUserRequest): Promise<InvitationInfo> {
    return apiRequest<InvitationInfo>(`/api/companies/${companyId}/invitations`, {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

export async function updateMemberRole(
    companyId: string,
    membershipId: string,
    data: UpdateMemberRoleRequest
): Promise<{ membershipId: string; previousRole: Role; newRole: Role }> {
    return apiRequest(`/api/companies/${companyId}/members/${membershipId}`, {
        method: 'PATCH',
        body: JSON.stringify(data),
    });
}

export async function getInvitationDetails(token: string): Promise<InvitationDetails> {
    return apiRequest<InvitationDetails>(`/api/invitations/${token}`, {
        method: 'GET',
    });
}

export async function acceptInvitation(token: string): Promise<AcceptInvitationResult> {
    return apiRequest<AcceptInvitationResult>(`/api/invitations/${token}/accept`, {
        method: 'POST',
    });
}
