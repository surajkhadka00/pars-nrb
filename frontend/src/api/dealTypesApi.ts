import { api, unwrap } from "./client";
import type {
  ApiResponse,
  DealTypeLimitRequest,
  DealTypeLimitResponse,
  DealTypeRequest,
  DealTypeResponse,
  OptionItem,
} from "./types";

export const dealTypesApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<DealTypeResponse[]> => {
    const response = await api.get<ApiResponse<DealTypeResponse[]>>("/api/v1/deal-types", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load deal types");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/deal-types/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load deal type options");
    return Array.isArray(body.data) ? body.data : [];
  },

  get: async (id: string): Promise<DealTypeResponse> => {
    const response = await api.get<ApiResponse<DealTypeResponse>>(`/api/v1/deal-types/${id}`);
    return unwrap(response);
  },

  create: async (request: DealTypeRequest): Promise<DealTypeResponse> => {
    const response = await api.post<ApiResponse<DealTypeResponse>>("/api/v1/deal-types", request);
    return unwrap(response);
  },

  update: async (id: string, request: DealTypeRequest): Promise<DealTypeResponse> => {
    const response = await api.put<ApiResponse<DealTypeResponse>>(`/api/v1/deal-types/${id}`, request);
    return unwrap(response);
  },

  remove: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/deal-types/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete deal type");
  },

  listLimits: async (page: number, size: number, sort?: string, filter?: string): Promise<DealTypeLimitResponse[]> => {
    const response = await api.get<ApiResponse<DealTypeLimitResponse[]>>("/api/v1/deal-type-limits", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load deal type limits");
    return Array.isArray(body.data) ? body.data : [];
  },

  createLimit: async (request: DealTypeLimitRequest): Promise<DealTypeLimitResponse> => {
    const response = await api.post<ApiResponse<DealTypeLimitResponse>>("/api/v1/deal-type-limits", request);
    return unwrap(response);
  },

  updateLimit: async (id: string, request: DealTypeLimitRequest): Promise<DealTypeLimitResponse> => {
    const response = await api.put<ApiResponse<DealTypeLimitResponse>>(`/api/v1/deal-type-limits/${id}`, request);
    return unwrap(response);
  },

  removeLimit: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/deal-type-limits/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete deal type limit");
  },
};
