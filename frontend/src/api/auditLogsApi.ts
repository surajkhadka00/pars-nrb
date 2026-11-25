import { api } from "./client";
import type { ApiResponse, AuditLogResponse } from "./types";

export const auditLogsApi = {
  list: async (page: number, size: number, sort?: string, filter?: string): Promise<AuditLogResponse[]> => {
    const response = await api.get<ApiResponse<AuditLogResponse[]>>("/api/v1/audit-logs", {
      params: { page, size, ...(sort ? { sort } : {}), ...(filter ? { $filter: filter } : {}) },
    });
    const body = response.data;
    if (!body.success) throw new Error(body.message || "Failed to load audit logs");
    return Array.isArray(body.data) ? body.data : [];
  },
};
