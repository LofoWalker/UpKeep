import { apiRequest } from '@/lib/api';

export interface PackageItem {
  id: string;
  name: string;
  registry: string;
  importedAt: string;
}

export interface PackageListResponse {
  packages: PackageItem[];
  totalCount: number;
  page: number;
  size: number;
}

export interface ImportLockfileRequest {
  fileContent: string;
  filename: string;
}

export interface ImportLockfileResult {
  importedCount: number;
  skippedCount: number;
  totalParsed: number;
  importedNames: string[];
  skippedNames: string[];
}

export interface ImportListRequest {
  packageNames: string[];
}

export interface ImportListResult {
  importedCount: number;
  skippedCount: number;
  invalidCount: number;
  importedNames: string[];
  skippedNames: string[];
  invalidNames: string[];
}

export async function listPackages(
  companyId: string,
  page: number = 0,
  size: number = 50,
  search?: string
): Promise<PackageListResponse> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (search) {
    params.set('search', search);
  }
  return apiRequest<PackageListResponse>(
    `/api/companies/${companyId}/packages?${params.toString()}`
  );
}

export async function importFromLockfile(
  companyId: string,
  request: ImportLockfileRequest
): Promise<ImportLockfileResult> {
  return apiRequest<ImportLockfileResult>(
    `/api/companies/${companyId}/packages/import/lockfile`,
    { method: 'POST', body: JSON.stringify(request) }
  );
}

export async function importFromList(
  companyId: string,
  request: ImportListRequest
): Promise<ImportListResult> {
  return apiRequest<ImportListResult>(
    `/api/companies/${companyId}/packages/import/list`,
    { method: 'POST', body: JSON.stringify(request) }
  );
}

