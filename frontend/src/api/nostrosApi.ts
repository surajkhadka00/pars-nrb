import { api, unwrap } from "./client";
import type { ApiResponse, NostroResponse, OptionItem } from "./types";

export const nostrosApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<NostroResponse[]> => {
    const response = await api.get<ApiResponse<NostroResponse[]>>("/api/v1/nostros", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load nostro accounts");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/nostros/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load nostro options");
    return Array.isArray(body.data) ? body.data : [];
  },

  get: async (id: string): Promise<NostroResponse> => {
    const response = await api.get<ApiResponse<NostroResponse>>(`/api/v1/nostros/${id}`);
    return unwrap(response);
  },

  create: async (request: import("./types").NostroRequest): Promise<NostroResponse> => {
    const response = await api.post<ApiResponse<NostroResponse>>("/api/v1/nostros", request);
    return unwrap(response);
  },

  update: async (id: string, request: import("./types").NostroRequest): Promise<NostroResponse> => {
    const response = await api.put<ApiResponse<NostroResponse>>(`/api/v1/nostros/${id}`, request);
    return unwrap(response);
  },

  remove: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/nostros/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete nostro account");
  },
};
