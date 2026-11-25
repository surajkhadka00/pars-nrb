import { api, unwrap } from "./client";
import type {
  ApiResponse,
  CounterpartyLimitRequest,
  CounterpartyLimitResponse,
  CounterpartyRequest,
  CounterpartyResponse,
  OptionItem,
} from "./types";

export const counterpartiesApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<CounterpartyResponse[]> => {
    const response = await api.get<ApiResponse<CounterpartyResponse[]>>("/api/v1/counterparties", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load counterparties");
    return Array.isArray(body.data) ? body.data : [];
  },

  options: async (): Promise<OptionItem[]> => {
    const response = await api.get<ApiResponse<OptionItem[]>>("/api/v1/counterparties/options");
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load counterparty options");
    return Array.isArray(body.data) ? body.data : [];
  },

  get: async (id: string): Promise<CounterpartyResponse> => {
    const response = await api.get<ApiResponse<CounterpartyResponse>>(`/api/v1/counterparties/${id}`);
    return unwrap(response);
  },

  create: async (request: CounterpartyRequest): Promise<CounterpartyResponse> => {
    const response = await api.post<ApiResponse<CounterpartyResponse>>("/api/v1/counterparties", request);
    return unwrap(response);
  },

  update: async (id: string, request: CounterpartyRequest): Promise<CounterpartyResponse> => {
    const response = await api.put<ApiResponse<CounterpartyResponse>>(`/api/v1/counterparties/${id}`, request);
    return unwrap(response);
  },

  remove: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/counterparties/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete counterparty");
  },

  listLimits: async (page: number, size: number, sort?: string, filter?: string): Promise<CounterpartyLimitResponse[]> => {
    const response = await api.get<ApiResponse<CounterpartyLimitResponse[]>>("/api/v1/counterparty-limits", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load counterparty limits");
    return Array.isArray(body.data) ? body.data : [];
  },

  createLimit: async (request: CounterpartyLimitRequest): Promise<CounterpartyLimitResponse> => {
    const response = await api.post<ApiResponse<CounterpartyLimitResponse>>("/api/v1/counterparty-limits", request);
    return unwrap(response);
  },

  updateLimit: async (id: string, request: CounterpartyLimitRequest): Promise<CounterpartyLimitResponse> => {
    const response = await api.put<ApiResponse<CounterpartyLimitResponse>>(`/api/v1/counterparty-limits/${id}`, request);
    return unwrap(response);
  },

  removeLimit: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/counterparty-limits/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete counterparty limit");
  },
};
