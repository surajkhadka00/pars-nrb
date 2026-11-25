import { api, unwrap } from "./client";
import type { ApiResponse, CountryRequest, CountryResponse, OptionItem } from "./types";

export const countriesApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<CountryResponse[]> => {
    const response = await api.get<ApiResponse<CountryResponse[]>>("/api/v1/countries", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load countries");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/countries/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load country options");
    return Array.isArray(body.data) ? body.data : [];
  },

  get: async (id: string): Promise<CountryResponse> => {
    const response = await api.get<ApiResponse<CountryResponse>>(`/api/v1/countries/${id}`);
    return unwrap(response);
  },

  create: async (request: CountryRequest): Promise<CountryResponse> => {
    const response = await api.post<ApiResponse<CountryResponse>>("/api/v1/countries", request);
    return unwrap(response);
  },

  update: async (id: string, request: CountryRequest): Promise<CountryResponse> => {
    const response = await api.put<ApiResponse<CountryResponse>>(`/api/v1/countries/${id}`, request);
    return unwrap(response);
  },

  remove: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/countries/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete country");
  },
};
