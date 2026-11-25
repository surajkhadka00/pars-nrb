import { api, unwrap } from "./client";
import type { ApiResponse, InstrumentCodeResponse, OptionItem } from "./types";

export const instrumentCodesApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<InstrumentCodeResponse[]> => {
    const response = await api.get<ApiResponse<InstrumentCodeResponse[]>>("/api/v1/instrument-codes", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load instrument codes");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/instrument-codes/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load instrument code options");
    return Array.isArray(body.data) ? body.data : [];
  },

  get: async (id: string): Promise<InstrumentCodeResponse> => {
    const response = await api.get<ApiResponse<InstrumentCodeResponse>>(`/api/v1/instrument-codes/${id}`);
    return unwrap(response);
  },

  create: async (request: import("./types").InstrumentCodeRequest): Promise<InstrumentCodeResponse> => {
    const response = await api.post<ApiResponse<InstrumentCodeResponse>>("/api/v1/instrument-codes", request);
    return unwrap(response);
  },

  update: async (id: string, request: import("./types").InstrumentCodeRequest): Promise<InstrumentCodeResponse> => {
    const response = await api.put<ApiResponse<InstrumentCodeResponse>>(`/api/v1/instrument-codes/${id}`, request);
    return unwrap(response);
  },

  remove: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/instrument-codes/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete instrument code");
  },
};
