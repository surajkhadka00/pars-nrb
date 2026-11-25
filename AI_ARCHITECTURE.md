# AI Architecture — PARS NRB Institutional Knowledge Assistant

## 1. System context

The AI system is an **additive module** inside the existing PARS NRB Spring Boot application. It reuses:

- JWT authentication (`JwtAuthenticationFilter`, `JwtService`)
- Spring Security + method security (`hasAuthority('<CODE>')`)
- `CurrentUserProvider` / `CurrentUser` for server-side identity
- RBAC (`PermissionResolver`, `RoleService`, `UserApiAccess`)
- `ApiResponse<T>` / `ApiMeta` response envelope
- Flyway, PostgreSQL (`pars` schema for business, new `ai` schema for AI)
- Actuator/Micrometer/Prometheus
- Existing React frontend (sessionStorage JWT + shared refresh)

```text
React (chat / document admin / AI dashboard)
    │  /api/v1/ai/**  (Bearer JWT via existing apiClient)
    ▼
Spring Boot AI REST API (same app, same SecurityContext)
    │
    ├─ AiChatController ─ AiChatService ─ AiConversationService (owner checks)
    │                          │
    │                          ▼
    │                  RagQueryService (router)
    │                   ├─ Document RAG (approved docs only)
    │                   └─ Extension point: read-only PARS data services (future)
    │                          │
    │                          ▼
    │      HybridRetrievalService
    │       ├─ pgvector similarity (Spring AI PgVectorStore) + server-side metadata filters
    │       ├─ PostgreSQL full-text (websearch_to_tsquery) + pg_trgm exact-ID fallback
    │       ├─ Reciprocal Rank Fusion (RRF)
    │       └─ optional reranker (config-gated)
    │                          │
    │                          ▼
    │      AiPromptService + ChatModel (Spring AI abstraction) → grounded answer + citations
    │
    ├─ AiDocumentController ─ AiDocumentService ─ AiIngestionWorker (@Scheduled, SKIP LOCKED)
    │       upload → validate → scan → extract → chunk → embed → index → review → publish
    │
    └─ AiAdminController ─ metrics, health, job overview
```

## 2. Package layout (additive)

```text
np.org.nrb.pars.ai
├── config/        ParsAiProperties, AiModelConfiguration, AiHealthIndicator
├── controller/    AiChatController, AiDocumentController, AiAdminController
├── domain/        entity, dto, enums
├── repository/    JPA repositories (ai schema) + AiRetrievalJdbcRepository
├── ingestion/     AiDocumentService, DocumentExtractionService, ChunkingService,
│                  AiIngestionWorker, DocumentStorageService, FileValidationService
├── rag/           RagQueryService, HybridRetrievalService, RrfFusionService,
│                  AiPromptService, RagAnswerService, CitationMapper, AbstentionPolicy
├── service/       AiConversationService, AiFeedbackService, AiPermissionService,
│                  AiAuditService, AiUsageMeters, AiRateLimitService
└── exception/     AiNotConfiguredException, AiDocumentException
```

## 3. Data ownership

| Concern | Owner | Notes |
|---|---|---|
| Business data | schema `pars` | Never copied wholesale into vectors |
| AI metadata/conversations | schema `ai` | New Flyway migration V14+ |
| Vector index | `ai.vector_store` (Spring AI `PgVectorStore`, Flyway-managed) | Single vector persistence implementation |
| Original uploaded files | private directory `${pars.ai.storage.path}` | Never under public/static resources; not deal-attachment storage |
| Identity & permissions | `pars.ad_*` via existing services | No second user table, no browser-supplied permissions |

## 4. Retrieval security model

Every search request builds its filters **on the server** from `CurrentUserProvider.require()`:

- `status = PUBLISHED` only
- `active_version = true`
- `expires_at IS NULL OR expires_at > now()`
- access classification:
  - `PUBLIC` → everyone
  - `INTERNAL` → authenticated users
  - `ROLE_BASED` → intersection with user's verified role codes
  - `RESTRICTED` → only explicit `ai_document_access` grants for the user id
  - `DEPARTMENT` → **not implemented** until a trusted department attribute exists on `AppUser`; documented gap
- per-user DENY overrides respected via `PermissionResolver` semantics

Filters are applied in SQL by `PgVectorStore` metadata filtering and the FTS query — never after retrieval in Java, and never sent from the browser.

## 5. Generation contract

- System prompt is a configurable resource; document text is delimited as untrusted data.
- Answer must use only retrieved evidence; citation references include document id, title, version, page, chunk id, score.
- Insufficient evidence → fixed abstention sentence + `insufficientEvidence=true`.
- No tool calling, no SQL access, no transaction actions exposed to the model.
- Output rendered as plain text with sanitized Markdown on the client; no HTML execution.

## 6. Asynchronous ingestion

PostgreSQL-backed job queue with states:
`UPLOADED → QUEUED → SCANNING → EXTRACTING → CHUNKING → EMBEDDING → INDEXING → PENDING_REVIEW → PUBLISHED`, terminal states `FAILED`, `ARCHIVED`.

Worker claims jobs with `FOR UPDATE SKIP LOCKED`, is idempotent, has max retries, and publishes atomically (new version indexed first, old version deactivated in the same transaction). A failed re-index never destroys a working published version.

## 7. Observability

Micrometer meters: `pars.ai.chat.requests`, `pars.ai.retrieval.duration`, `pars.ai.embedding.duration`, `pars.ai.model.duration`, `pars.ai.ingestion.duration`, `pars.ai.tokens`, `pars.ai.feedback`, `pars.ai.errors`, `pars.ai.ratelimit`. No prompt text, document text, tokens, or user identifiers in metric labels.

## 8. Disable path

`pars.ai.enabled=false` (default) → no AI beans, no AI endpoints active, no Flyway requirement for pgvector beyond migration being present. The rest of PARS is unaffected. `GET /actuator/health/ai` reports configuration status.
