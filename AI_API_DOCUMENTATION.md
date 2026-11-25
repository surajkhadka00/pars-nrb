# AI API Documentation — PARS NRB

Base path: `/api/v1/ai`. All endpoints use the existing JWT `Authorization: Bearer <token>` header and return `ApiResponse<T>`. AI endpoints are only registered when `pars.ai.enabled=true`.

## Chat

| Method | Path | Permission | Request | Response |
|---|---|---|---|---|
| POST | `/chat` | `AI_CHAT` | `{ "question": string (max 8000), "conversationId"?: uuid }` | `AiChatResponse` |
| GET | `/conversations` | `AI_CONVERSATION_READ` | `?page&size` | `List<AiConversationResponse>` |
| GET | `/conversations/{id}` | `AI_CONVERSATION_READ` | — | `List<AiMessageResponse>` |
| DELETE | `/conversations/{id}` | `AI_CONVERSATION_READ` | — | `null` |
| POST | `/feedback` | `AI_FEEDBACK` | `{messageId, rating 1..5, comment?}` | `null` |

`AiChatResponse`:
```json
{
  "answer": "...",
  "conversationId": "uuid",
  "messageId": "uuid",
  "sources": [{"documentId":"uuid","documentTitle":"...","docVersion":1,"page":4,"section":"3.2","chunkId":"uuid","score":0.83,"sourceStatus":"PUBLISHED"}],
  "insufficientEvidence": false,
  "traceId": "...",
  "model": "gpt-4o-mini"
}
```

Abstention returns `insufficientEvidence=true` with the fixed sentence, no fabricated citations.

## Documents

| Method | Path | Permission | Notes |
|---|---|---|---|
| GET | `/documents` | `AI_DOCUMENT_READ` | paged; metadata only |
| GET | `/documents/{id}` | `AI_DOCUMENT_READ` | metadata |
| POST | `/documents` | `AI_DOCUMENT_UPLOAD` | `multipart/form-data`: `file` + `request` JSON part |
| GET | `/documents/{id}/jobs` | `AI_DOCUMENT_READ` | ingestion status |
| POST | `/documents/{id}/publish` | `AI_DOCUMENT_APPROVE` | only from `PENDING_REVIEW` |
| POST | `/documents/{id}/archive` | `AI_DOCUMENT_APPROVE` | removes from retrieval |
| POST | `/documents/{id}/reindex` | `AI_DOCUMENT_REINDEX` | creates a new job |
| DELETE | `/documents/{id}` | `AI_DOCUMENT_DELETE` | deletes metadata, vectors, private file |

`request` JSON part: `{title, description, category, accessClassification, docVersion}`.
Accepted file types: PDF, DOCX, XLSX, TXT, HTML; max size default 20 MB.

## Administration

| Method | Path | Permission | Notes |
|---|---|---|---|
| GET | `/admin/status` | `AI_ADMIN` | config-safe status (no secrets) |
| GET | `/admin/metrics` | `AI_METRICS_READ` | counts and average feedback |
| GET | `/admin/jobs` | `AI_ADMIN` | job overview |
| GET | `/admin/config` | `AI_ADMIN` | non-secret config values |

Health: `GET /actuator/health/ai` → `UP` with `status=DISABLED` when AI is off.

## Error mapping

Uses the existing `GlobalExceptionHandler`: 400 `BAD_REQUEST`, 401 `UNAUTHORIZED`, 403 `FORBIDDEN`, 404 `NOT_FOUND`, 409 `CONFLICT`, 429 `RATE_LIMITED`. Network timeouts are returned as 504/500 with a message; the React client never destroys the session for temporary failures.
