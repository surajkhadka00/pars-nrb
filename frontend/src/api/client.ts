import axios, {
  type AxiosError,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from "axios";
import type { ApiErrorBody, ApiResponse } from "./types";

const ACCESS_TOKEN_KEY = "pars.accessToken";
const REFRESH_TOKEN_KEY = "pars.refreshToken";

export class ApiError extends Error {
  status: number;
  code: string;
  data?: unknown;
  traceId?: string | null;

  constructor(status: number, code: string, message: string, data?: unknown, traceId?: string | null) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.data = data;
    this.traceId = traceId ?? null;
  }
}

// ---------------------------------------------------------------------------
// Token storage (sessionStorage - tab scoped, never persisted to localStorage).
// ---------------------------------------------------------------------------

export function getAccessToken(): string | null {
  try {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setAccessToken(token: string): void {
  try {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, token);
  } catch {
    // Storage may be unavailable (private mode); treat as absent.
  }
}

export function getRefreshToken(): string | null {
  try {
    return sessionStorage.getItem(REFRESH_TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setRefreshToken(token: string): void {
  try {
    sessionStorage.setItem(REFRESH_TOKEN_KEY, token);
  } catch {
    // ignore
  }
}

export function clearTokens(): void {
  try {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  } catch {
    // ignore
  }
}

// ---------------------------------------------------------------------------
// Single in-flight refresh promise (shared across all concurrent 401s).
// ---------------------------------------------------------------------------

let refreshPromise: Promise<boolean> | null = null;

async function performRefresh(refreshToken: string): Promise<boolean> {
  try {
    const response = await axios.post<{ data: RefreshEnvelope }>(
      "/api/v1/auth/refresh",
      { refreshToken },
      { headers: { "Content-Type": "application/json" } },
    );
    const payload = response.data?.data;
    if (payload?.success && payload?.data?.accessToken && payload?.data?.refreshToken) {
      setAccessToken(payload.data.accessToken);
      setRefreshToken(payload.data.refreshToken);
      return true;
    }
    clearTokens();
    return false;
  } catch {
    clearTokens();
    return false;
  }
}

interface TokenResponseLike {
  accessToken?: string;
  refreshToken?: string;
}

interface RefreshEnvelope {
  success?: boolean;
  data?: TokenResponseLike;
}

function refreshAccessToken(): Promise<boolean> {
  const current = getRefreshToken();
  if (!current) {
    return Promise.resolve(false);
  }
  if (!refreshPromise) {
    refreshPromise = performRefresh(current).finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

// ---------------------------------------------------------------------------
// Axios instance
// ---------------------------------------------------------------------------

export const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
  timeout: 30_000,
  headers: { "Content-Type": "application/json" },
});

const PUBLIC_AUTH_ENDPOINTS = new Set([
  "/api/v1/auth/login",
  "/api/v1/auth/refresh",
  "/api/v1/auth/forgot-password",
  "/api/v1/auth/reset-password",
  "/api/v1/auth/register",
]);

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const url = (config.baseURL ? config.baseURL + (config.url ?? "") : config.url ?? "");
  const isPublic = PUBLIC_AUTH_ENDPOINTS.has(url);
  if (!isPublic) {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiErrorBody>) => {
    const status = error.response?.status;
    const config = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    const url: string = config?.baseURL ? config.baseURL + (config.url ?? "") : (config?.url ?? "");

    if (status === 401 && config && !config._retry && !PUBLIC_AUTH_ENDPOINTS.has(url)) {
      const refreshed = await refreshAccessToken();
      if (refreshed) {
        config._retry = true;
        return api.request(config);
      }
    }

    if (status === 401) {
      clearTokens();
    }

    const statusCode = status ?? 0;
    const body = error.response?.data as ApiErrorBody | undefined;
    const code = body?.code ?? "NETWORK_ERROR";
    const message = body?.message ?? error.message ?? "Network error";
    if (statusCode >= 400 && statusCode < 500) {
      throw new ApiError(statusCode, code, message, body?.data, body?.traceId);
    }
    throw new ApiError(statusCode || 0, "NETWORK_ERROR", message);
  },
);

// ---------------------------------------------------------------------------
// Convenience helpers
// ---------------------------------------------------------------------------

export async function unwrap<T>(response: { data: ApiResponse<T> }): Promise<T> {
  const body = response.data;
  if (!body.success) {
    throw new ApiError(400, body.code, body.message, body.data, body.traceId);
  }
  return body.data;
}
