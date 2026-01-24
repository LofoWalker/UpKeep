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

    let response: Response;
    try {
        response = await fetch(url, {
            ...options,
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...options?.headers,
            },
        });
    } catch (error) {
        throw new ApiError(
            'NETWORK_ERROR',
            'Unable to connect to the server. Please check your network connection.',
            error instanceof Error ? error.message : undefined
        );
    }

    if (!response.ok && response.status >= 500) {
        throw new ApiError(
            'SERVER_ERROR',
            'The server encountered an error. Please try again later.',
            `HTTP ${response.status}: ${response.statusText}`
        );
    }

    let data: ApiResponse<T>;
    try {
        data = await response.json();
    } catch {
        throw new ApiError(
            'PARSE_ERROR',
            'Received an invalid response from the server.',
            `Expected JSON but received ${response.headers.get('content-type') || 'unknown'}`
        );
    }

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
