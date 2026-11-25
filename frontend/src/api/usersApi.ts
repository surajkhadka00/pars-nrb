import { api, unwrap } from "./client";
import type { ApiResponse, OptionItem, PermissionResponse, RoleResponse, UserApiAccessResponse, UserResponse } from "./types";

export const usersApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<UserResponse[]> => {
    const response = await api.get<ApiResponse<UserResponse[]>>("/api/v1/users", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load users");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/users/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load user options");
    return Array.isArray(body.data) ? body.data : [];
  },

  get: async (id: string): Promise<UserResponse> => {
    const response = await api.get<ApiResponse<UserResponse>>(`/api/v1/users/${id}`);
    return unwrap(response);
  },

  create: async (request: import("./types").CreateUserRequest): Promise<UserResponse> => {
    const response = await api.post<ApiResponse<UserResponse>>("/api/v1/users", request);
    return unwrap(response);
  },

  update: async (id: string, request: import("./types").UpdateUserRequest): Promise<UserResponse> => {
    const response = await api.put<ApiResponse<UserResponse>>(`/api/v1/users/${id}`, request);
    return unwrap(response);
  },

  lock: async (id: string): Promise<void> => {
    const response = await api.post<ApiResponse<UserResponse>>(`/api/v1/users/${id}/lock`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to lock user");
  },

  unlock: async (id: string): Promise<void> => {
    const response = await api.post<ApiResponse<UserResponse>>(`/api/v1/users/${id}/unlock`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to unlock user");
  },
};

export const rolesApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<RoleResponse[]> => {
    const response = await api.get<ApiResponse<RoleResponse[]>>("/api/v1/roles", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load roles");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/roles/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load role options");
    return Array.isArray(body.data) ? body.data : [];
  },

  create: async (request: import("./types").RoleRequest): Promise<RoleResponse> => {
    const response = await api.post<ApiResponse<RoleResponse>>("/api/v1/roles", request);
    return unwrap(response);
  },

  update: async (id: string, request: import("./types").RoleRequest): Promise<RoleResponse> => {
    const response = await api.put<ApiResponse<RoleResponse>>(`/api/v1/roles/${id}`, request);
    return unwrap(response);
  },

  remove: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/roles/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete role");
  },
};

export const permissionsApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<PermissionResponse[]> => {
    const response = await api.get<ApiResponse<PermissionResponse[]>>("/api/v1/permissions", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load permissions");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/permissions/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load permission options");
    return Array.isArray(body.data) ? body.data : [];
  },

  listUserAccess: async (userId: string): Promise<UserApiAccessResponse[]> => {
    const response = await api.get<ApiResponse<UserApiAccessResponse[]>>(`/api/v1/users/${userId}/api-access`);
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load user API access");
    return Array.isArray(body.data) ? body.data : [];
  },

  replaceUserAccess: async (userId: string, requests: import("./types").UserApiAccessRequest[]): Promise<UserApiAccessResponse[]> => {
    const response = await api.put<ApiResponse<UserApiAccessResponse[]>>(`/api/v1/users/${userId}/api-access`, requests);
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to update user API access");
    return Array.isArray(body.data) ? body.data : [];
  },
};

export const positionsApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<import("./types").PositionResponse[]> => {
    const response = await api.get<ApiResponse<import("./types").PositionResponse[]>>("/api/v1/positions", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load positions");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/positions/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load position options");
    return Array.isArray(body.data) ? body.data : [];
  },
};
