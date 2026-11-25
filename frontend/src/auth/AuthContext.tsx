import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { authApi } from "../api/authApi";
import { clearTokens, getAccessToken, ApiError } from "../api/client";
import type { MeResponse } from "../api/types";

export interface AuthContextValue {
  status: "loading" | "authenticated" | "unauthenticated";
  user: MeResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  logoutAll: () => Promise<void>;
  hasPermission: (code: string) => boolean;
  hasAnyPermission: (codes: string[]) => boolean;
  hasRole: (role: string) => boolean;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<MeResponse | null>(null);
  const [status, setStatus] = useState<"loading" | "authenticated" | "unauthenticated">("loading");

  const applyUser = useCallback(async () => {
    if (!getAccessToken()) {
      setUser(null);
      setStatus("unauthenticated");
      return;
    }
    try {
      const me = await authApi.me();
      setUser(me);
      setStatus("authenticated");
    } catch {
      clearTokens();
      setUser(null);
      setStatus("unauthenticated");
    }
  }, []);

  useEffect(() => {
    // Session hydration must happen once after mount from the JWT in sessionStorage.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void applyUser();
  }, [applyUser]);

  const login = useCallback(async (username: string, password: string) => {
    const result = await authApi.login(username, password);
    setUser(result.user);
    setStatus("authenticated");
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();
    setUser(null);
    setStatus("unauthenticated");
  }, []);

  const logoutAll = useCallback(async () => {
    await authApi.logoutAll();
    setUser(null);
    setStatus("unauthenticated");
  }, []);

  const refreshUser = useCallback(async () => {
    await applyUser();
  }, [applyUser]);

  const hasPermission = useCallback(
    (code: string) => !!user?.permissions?.includes(code),
    [user],
  );

  const hasAnyPermission = useCallback(
    (codes: string[]) => codes.some((code) => user?.permissions?.includes(code)),
    [user],
  );

  const hasRole = useCallback(
    (role: string) => !!user?.roles?.some((r) => r === role),
    [user],
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      status,
      user,
      isAuthenticated: status === "authenticated",
      isLoading: status === "loading",
      login,
      logout,
      logoutAll,
      hasPermission,
      hasAnyPermission,
      hasRole,
      refreshUser,
    }),
    [status, user, login, logout, logoutAll, hasPermission, hasAnyPermission, hasRole, refreshUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}

export function authErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message || "The request failed";
  }
  if (error instanceof Error) {
    const message = error.message;
    if (message === "Network Error" || message === "Network Error.") {
      return "Unable to reach the PARS server. Please check your connection and try again.";
    }
    return message || "The request failed";
  }
  return "The request failed";
}
