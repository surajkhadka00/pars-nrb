import { api, unwrap } from "./client";
import type { ApiMeta, ApiResponse, DealAttachmentResponse, DealRequest, DealResponse } from "./types";

export interface DealPage {
  items: DealResponse[];
  meta: ApiMeta;
}

export const dealsApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<DealPage> => {
    const response = await api.get<ApiResponse<DealResponse[]>>("/api/v1/deals", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load deals");
    const items = Array.isArray(body.data) ? body.data : [];
    return { items, meta: body.meta ?? { page, size, totalElements: items.length, totalPages: 1, generatedAt: null } };
  },

  get: async (id: string): Promise<DealResponse> => {
    const response = await api.get<ApiResponse<DealResponse>>(`/api/v1/deals/${id}`);
    return unwrap(response);
  },

  create: async (request: DealRequest): Promise<DealResponse> => {
    const response = await api.post<ApiResponse<DealResponse>>("/api/v1/deals", request);
    return unwrap(response);
  },

  update: async (id: string, request: DealRequest): Promise<DealResponse> => {
    const response = await api.put<ApiResponse<DealResponse>>(`/api/v1/deals/${id}`, request);
    return unwrap(response);
  },

  async remove(id: string): Promise<void> {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/deals/${id}`);
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete deal");
  },

  listAttachments: async (dealId: string): Promise<DealAttachmentResponse[]> => {
    const response = await api.get<ApiResponse<DealAttachmentResponse[]>>("/api/v1/deal-attachments", {
      params: { dealId },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load attachments");
    return Array.isArray(body.data) ? body.data : [];
  },

  uploadAttachment: async (dealId: string, file: File): Promise<DealAttachmentResponse> => {
    const formData = new FormData();
    formData.append("dealId", dealId);
    formData.append("file", file);
    const response = await api.post<ApiResponse<DealAttachmentResponse>>("/api/v1/deal-attachments", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to upload attachment");
    return body.data;
  },

  async downloadAttachment(dealId: string, attachmentId: string): Promise<void> {
    const response = await api.get<Blob>(`/api/v1/deal-attachments/${attachmentId}`, {
      params: { dealId },
      responseType: "blob",
    });
    const blob = new Blob([response.data], {
      type: (response.headers?.["content-type"] as string) ?? "application/octet-stream",
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.rel = "noopener";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },

  async removeAttachment(dealId: string, attachmentId: string): Promise<void> {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/deal-attachments/${attachmentId}`, { params: { dealId } });
    if (!response.data.success) throw new Error(response.data.message || "Failed to delete attachment");
  },
};
