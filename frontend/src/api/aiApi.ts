import { api, unwrap } from "./client";
import type { ApiResponse } from "./types";

export interface AiSource {
  documentId: string;
  documentTitle: string;
  docVersion: number;
  page: number;
  section: string;
  chunkId: string;
  score: number;
  sourceStatus: string;
  sourceUrl?: string | null;
}

export interface AiChatResponse {
  answer: string;
  conversationId: string;
  messageId: string;
  sources: AiSource[];
  insufficientEvidence: boolean;
  traceId: string | null;
  model: string;
}

export interface AiConversation {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
  messageCount: number;
}

export interface AiMessage {
  id: string;
  role: "USER" | "ASSISTANT";
  content: string;
  sources: AiSource[];
  insufficientEvidence: boolean;
  createdAt: string;
}

export interface AiDocument {
  id: string;
  title: string;
  originalFilename: string;
  mimeType: string;
  checksum: string;
  fileSize: number;
  category: string | null;
  accessClassification: string;
  publicationStatus: string;
  docVersion: number;
  activeVersion: boolean;
  uploadedAt: string;
  publishedAt: string | null;
  embeddingModel: string | null;
  lastIndexedAt: string | null;
}

export interface AiJob {
  id: string;
  documentId: string;
  status: string;
  stage: string | null;
  progress: number;
  attempts: number;
  errorMessage: string | null;
  createdAt: string;
  finishedAt: string | null;
}

export interface AiMetrics {
  totalQuestions: number;
  failedQuestions: number;
  publishedDocuments: number;
  failedJobs: number;
  feedbackCount: number;
  averageFeedback: number;
}

export interface NrbForexRate {
  iso3: string;
  name: string;
  unit: number;
  buy: number;
  sell: number;
  rawBuy: string;
  rawSell: string;
}

export interface NrbForexDay {
  date: string;
  publishedOn: string;
  modifiedOn: string;
  fetchedAt: string;
  sourceUrl: string;
  rates: NrbForexRate[];
}

export interface NrbSource {
  id: string;
  name: string;
  url: string;
  type: string;
  category: string;
  enabled: boolean;
  lastSync: string | null;
  status: string;
  error: string | null;
}

export interface NrbDocument {
  id: string;
  canonicalUrl: string;
  title: string;
  category: string;
  language: string;
  publicationDate: string | null;
  contentHash: string;
  status: string;
  published: boolean;
  version: number;
  storageReference: string;
}

export const nrbApi = {
  latest: async (): Promise<NrbForexDay> => {
    const response = await api.get<ApiResponse<NrbForexDay>>("/api/v1/nrb/forex/latest");
    return unwrap(response);
  },
  historical: async (from: string, to: string): Promise<NrbForexDay[]> => {
    const response = await api.get<ApiResponse<NrbForexDay[]>>("/api/v1/nrb/forex", { params: { from, to } });
    return unwrap(response);
  },
  byCurrency: async (code: string, date: string): Promise<NrbForexDay> => {
    const response = await api.get<ApiResponse<NrbForexDay>>(`/api/v1/nrb/forex/${encodeURIComponent(code)}`, {
      params: { date },
    });
    return unwrap(response);
  },
  sources: async (): Promise<NrbSource[]> => {
    const response = await api.get<ApiResponse<NrbSource[]>>("/api/v1/nrb/sources");
    return unwrap(response);
  },
  documents: async (): Promise<NrbDocument[]> => {
    const response = await api.get<ApiResponse<NrbDocument[]>>("/api/v1/nrb/documents", { params: { page: 0, size: 100 } });
    return unwrap(response);
  },
  status: async (): Promise<Record<string, unknown>> => {
    const response = await api.get<ApiResponse<Record<string, unknown>>>("/api/v1/nrb/sources/status");
    return unwrap(response);
  },
  setEnabled: async (id: string, enabled: boolean): Promise<void> => {
    await api.patch(`/api/v1/nrb/sources/${id}`, null, { params: { enabled } });
  },
  syncSource: async (id: string): Promise<void> => {
    await api.post(`/api/v1/admin/nrb/sources/${id}/sync`);
  },
  syncAll: async (): Promise<void> => {
    await api.post("/api/v1/admin/nrb/sync");
  },
};

export const aiApi = {
  chat: async (question: string, conversationId?: string): Promise<AiChatResponse> => {
    const response = await api.post<ApiResponse<AiChatResponse>>("/api/v1/ai/chat", {
      question,
      ...(conversationId ? { conversationId } : {}),
    });
    return unwrap(response);
  },

  conversations: async (page = 0, size = 50): Promise<AiConversation[]> => {
    const response = await api.get<ApiResponse<AiConversation[]>>("/api/v1/ai/conversations", { params: { page, size } });
    return unwrap(response);
  },

  messages: async (conversationId: string): Promise<AiMessage[]> => {
    const response = await api.get<ApiResponse<AiMessage[]>>(`/api/v1/ai/conversations/${conversationId}`);
    return unwrap(response);
  },

  deleteConversation: async (id: string): Promise<void> => {
    await api.delete(`/api/v1/ai/conversations/${id}`);
  },

  feedback: async (messageId: string, rating: number, comment?: string): Promise<void> => {
    await api.post("/api/v1/ai/feedback", { messageId, rating, comment: comment ?? null });
  },

  documents: async (page = 0, size = 50): Promise<AiDocument[]> => {
    const response = await api.get<ApiResponse<AiDocument[]>>("/api/v1/ai/documents", { params: { page, size } });
    return unwrap(response);
  },

  documentJobs: async (id: string): Promise<AiJob[]> => {
    const response = await api.get<ApiResponse<AiJob[]>>(`/api/v1/ai/documents/${id}/jobs`);
    return unwrap(response);
  },

  uploadDocument: async (file: File, title: string, classification: string, category: string): Promise<AiDocument> => {
    const form = new FormData();
    form.append("file", file);
    form.append("request", new Blob([JSON.stringify({
      title,
      description: null,
      category: category || null,
      accessClassification: classification,
      docVersion: 1,
    })], { type: "application/json" }));
    const response = await api.post<ApiResponse<AiDocument>>("/api/v1/ai/documents", form, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return unwrap(response);
  },

  publish: async (id: string): Promise<void> => {
    await api.post(`/api/v1/ai/documents/${id}/publish`);
  },

  archive: async (id: string): Promise<void> => {
    await api.post(`/api/v1/ai/documents/${id}/archive`);
  },

  reindex: async (id: string): Promise<void> => {
    await api.post(`/api/v1/ai/documents/${id}/reindex`);
  },

  deleteDocument: async (id: string): Promise<void> => {
    await api.delete(`/api/v1/ai/documents/${id}`);
  },

  metrics: async (): Promise<AiMetrics> => {
    const response = await api.get<ApiResponse<AiMetrics>>("/api/v1/ai/admin/metrics");
    return unwrap(response);
  },

  jobs: async (): Promise<AiJob[]> => {
    const response = await api.get<ApiResponse<AiJob[]>>("/api/v1/ai/admin/jobs");
    return unwrap(response);
  },

  config: async (): Promise<Record<string, unknown>> => {
    const response = await api.get<ApiResponse<Record<string, unknown>>>("/api/v1/ai/admin/config");
    return unwrap(response);
  },

  killSwitchStatus: async (): Promise<{ disabled: boolean; updatedBy: string; updatedAt: string }> => {
    const response = await api.get<ApiResponse<{ disabled: boolean; updatedBy: string; updatedAt: string }>>("/api/v1/ai/admin/kill-switch");
    return unwrap(response);
  },

  setKillSwitch: async (disabled: boolean, reason: string): Promise<void> => {
    await api.post("/api/v1/ai/admin/kill-switch", null, { params: { disabled, reason } });
  },
};
