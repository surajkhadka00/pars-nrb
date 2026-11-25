import { api } from "./client";
import type { ApiResponse, ExchangeRateImportResponse, ExchangeRateResponse } from "./types";

export const exchangeRatesApi = {  list: async (page: number, size: number, sort?: string, filter?: string): Promise<ExchangeRateResponse[]> => {
    const response = await api.get<ApiResponse<ExchangeRateResponse[]>>("/api/v1/exchange-rates", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load exchange rates");
    return Array.isArray(body.data) ? body.data : [];
  },

  upload: async (file: File, rateDate: string): Promise<ExchangeRateImportResponse> => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("rateDate", rateDate);
    const response = await api.post<ApiResponse<ExchangeRateImportResponse>>("/api/v1/exchange-rates/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to import exchange rates");
    return body.data;
  },

  remove: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/exchange-rates/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete exchange rate");
  },
};
