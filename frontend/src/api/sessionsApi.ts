import { api } from "./client";
import type { ApiResponse, FailedAttemptResponse, SessionResponse } from "./types";

export const sessionsApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<SessionResponse[]> => {
    const response = await api.get<ApiResponse<SessionResponse[]>>("/api/v1/auth-sessions", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load sessions");
    return Array.isArray(body.data) ? body.data : [];
  },

  revoke: async (sessionId: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/auth/sessions/${sessionId}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to revoke session");
  },

  revokeAdmin: async (sessionId: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/auth/admin/sessions/${sessionId}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to revoke session");
  },
};

export const failedAttemptsApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<FailedAttemptResponse[]> => {
    const response = await api.get<ApiResponse<FailedAttemptResponse[]>>("/api/v1/failed-login-attempts", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load failed attempts");
    return Array.isArray(body.data) ? body.data : [];
  },
};
