export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export interface ApiResponse<T> {
  data: T | null;
  meta: {
    timestamp: string;
  } | null;
  error: {
    code: string;
    message: string;
    details: string | null;
    fields: Array<{
      field: string;
      message: string;
    }> | null;
  } | null;
}

export class ApiError extends Error {
  constructor(
    public code: string,
    message: string,
    public details?: string,
    public fields?: Array<{ field: string; message: string }>
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export async function apiRequest<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;

  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  const data: ApiResponse<T> = await response.json();

  if (data.error) {
    throw new ApiError(
      data.error.code,
      data.error.message,
      data.error.details || undefined,
      data.error.fields || undefined
    );
  }

  return data.data as T;
}
