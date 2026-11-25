import { api, clearTokens, setAccessToken, setRefreshToken } from "./client";
import type { ApiResponse, FailedAttemptResponse, ForgotPasswordStatusResponse, MeResponse, SessionResponse, TokenResponse } from "./types";

export interface LoginResult {
  tokens: TokenResponse;
  user: MeResponse;
}

export const authApi = {
  async login(username: string, password: string): Promise<LoginResult> {
    const response = await api.post<ApiResponse<TokenResponse>>("/api/v1/auth/login", { username, password });
    const body = response.data;
    if (!body.success || !body.data?.accessToken || !body.data?.refreshToken) {
      throw new Error(body.message || "Login failed");
    }
    setAccessToken(body.data.accessToken);
    setRefreshToken(body.data.refreshToken);
    const me = await authApi.me();
    return { tokens: body.data, user: me };
  },

  async refresh(refreshToken: string): Promise<TokenResponse> {
    const response = await api.post<ApiResponse<TokenResponse>>("/api/v1/auth/refresh", { refreshToken });
    const body = response.data;
    if (!body.success || !body.data?.accessToken || !body.data?.refreshToken) {
      throw new Error(body.message || "Refresh failed");
    }
    setAccessToken(body.data.accessToken);
    setRefreshToken(body.data.refreshToken);
    return body.data;
  },

  async me(): Promise<MeResponse> {
    const response = await api.get<ApiResponse<MeResponse>>("/api/v1/auth/me");
    const body = response.data;
    if (!body.success) {
      throw new Error(body.message || "Failed to load user");
    }
    return body.data;
  },

  async logout(): Promise<void> {
    try {
      await api.post("/api/v1/auth/logout");
    } catch {
      // Session may already be gone; always clear local tokens.
    }
    clearTokens();
  },

  async logoutAll(): Promise<void> {
    try {
      await api.post("/api/v1/auth/logout-all");
    } catch {
      // ignore
    }
    clearTokens();
  },

  async listSessions(): Promise<SessionResponse[]> {
    const response = await api.get<ApiResponse<SessionResponse[]>>("/api/v1/auth/sessions");
    const body = response.data;
    if (!body.success) {
      throw new Error(body.message || "Failed to load sessions");
    }
    return body.data;
  },

  async revokeSession(sessionId: string): Promise<void> {
    await api.delete(`/api/v1/auth/sessions/${sessionId}`);
  },

  async revokeSessionAdmin(sessionId: string): Promise<void> {
    await api.delete(`/api/v1/auth/admin/sessions/${sessionId}`);
  },

  async listFailedAttempts(): Promise<FailedAttemptResponse[]> {
    const response = await api.get<ApiResponse<FailedAttemptResponse[]>>("/api/v1/failed-login-attempts");
    const body = response.data;
    if (!body.success) {
      throw new Error(body.message || "Failed to load failed attempts");
    }
    return body.data;
  },

  async forgotPasswordStatus(token: string): Promise<ForgotPasswordStatusResponse> {
    const response = await api.get<ApiResponse<ForgotPasswordStatusResponse>>("/api/v1/auth/forgot-password", {
      params: { token },
    });
    const body = response.data;
    if (!body.success) {
      throw new Error(body.message || "Failed to load password reset status");
    }
    return body.data;
  },

  async requestPasswordReset(email: string): Promise<void> {
    await api.post("/api/v1/auth/forgot-password", { email });
  },

  async resetPassword(token: string, newPassword: string): Promise<void> {
    await api.post("/api/v1/auth/reset-password", { token, newPassword });
  },

  async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    await api.post("/api/v1/auth/change-password", { currentPassword, newPassword });
  },
};
