import { api, unwrap } from "./client";
import type { ApiResponse, FiscalYearResponse, OptionItem } from "./types";

export const fiscalYearsApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<FiscalYearResponse[]> => {
    const response = await api.get<ApiResponse<FiscalYearResponse[]>>("/api/v1/fiscal-years", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load fiscal years");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/fiscal-years/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load fiscal year options");
    return Array.isArray(body.data) ? body.data : [];
  },

  get: async (id: string): Promise<FiscalYearResponse> => {
    const response = await api.get<ApiResponse<FiscalYearResponse>>(`/api/v1/fiscal-years/${id}`);
    return unwrap(response);
  },

  create: async (request: import("./types").FiscalYearRequest): Promise<FiscalYearResponse> => {
    const response = await api.post<ApiResponse<FiscalYearResponse>>("/api/v1/fiscal-years", request);
    return unwrap(response);
  },

  update: async (id: string, request: import("./types").FiscalYearRequest): Promise<FiscalYearResponse> => {
    const response = await api.put<ApiResponse<FiscalYearResponse>>(`/api/v1/fiscal-years/${id}`, request);
    return unwrap(response);
  },

  remove: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/fiscal-years/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete fiscal year");
  },
};
