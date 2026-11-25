# AI Implementation Audit — PARS NRB (Current State)

Date: 2026-11-18. Scope: complete source audit before NRB external data integration. No files were changed during this audit.

## 1. Build & runtime baseline

- `pom.xml`: parent `spring-boot-starter-parent:3.4.5`; `<java.version>17</java.version>`; `finalName=nrb-pars`.
- Version properties: jjwt 0.12.6, mapstruct 1.6.3, springdoc 2.8.6, bucket4j 8.14.0, lombok 1.18.38, testcontainers 1.20.6, poi 5.4.1, **spring-ai.version=1.0.0** (pinned, BOM imported).
- Spring AI 1.0.0 is the exact release whose POM declares Boot 3.4.5; 1.0.9 → Boot 3.5.15, 2.x → Boot 4.x. **Do not upgrade.**
- HTTP client: **none configured** for outbound calls. `RestClient` is available transitively via `spring-web` (part of `spring-boot-starter-web`); no WebFlux/HttpClient/OkHttp dependency and no bean. No new dependency is required for the Forex client.
- Caching: no `@EnableCaching`, no cache provider. Simple in-memory TTL cache can be implemented in the connector (or Caffeine added later with approval).
- `@EnableScheduling`, `@EnableAsync`, `@EnableJpaAuditing`, `@ConfigurationPropertiesScan` are already on `ParsApplication`.
- Tests: 5 Java test classes / 19 tests, all plain JUnit; Testcontainers deps exist but are **unused**; no `@SpringBootTest`/MockMvc. Frontend: 1 Vitest test.

## 2. Existing RAG implementation (complete inventory)

Package `np.org.nrb.pars.ai` (23 classes):

| Area | Classes |
|---|---|
| config | `ParsAiProperties` (prefix `pars.ai`), `AiModelConfiguration` (OpenAI), `OllamaModelConfiguration` (free local), `AiCommonConfiguration` (VectorStore + ChatClient), `AiHealthIndicator` |
| controllers | `AiChatController` `/api/v1/ai/**`, `AiDocumentController` `/api/v1/ai/documents/**`, `AiAdminController` `/api/v1/ai/admin/**` |
| domain | `AiDtos` (chat/document/job/metrics records) |
| ingestion | `FileValidationService`, `DocumentStorageService`, `DocumentExtractionService`, `ChunkingService`, `AiIngestionWorker` (`@Scheduled`, SKIP LOCKED) |
| live | `AiQueryRouter` (DOCUMENTS/SELF/DEALS/APP_GUIDE/CAPABILITY), `LiveDataService` (uses `UserService`/`DealService`) |
| rag | `HybridRetrievalService` (pgvector + PostgreSQL FTS + RRF), `AiPromptService`, `RagAnswerService` |
| repositories | JDBC repositories for `ai_documents`, `ai_ingestion_jobs`, `ai_conversations`/`messages`/`feedback` |

Capabilities already present: document upload/validation/extraction/chunking/embedding/indexing, hybrid retrieval with server-side permission filters, citations, abstention, conversation ownership, feedback, live self/deals/app-guide queries, dual provider (Ollama/OpenAI), health indicator, quotas and concurrency control.

## 3. PostgreSQL / pgvector

- Business schema `pars` (JDBC `currentSchema=pars`); AI schema `ai`, tables: `vector_store`, `ai_documents`, `ai_ingestion_jobs`, `ai_conversations`, `ai_messages`, `ai_feedback`, `ai_retrieval_logs`, `ai_document_access`, `ai_usage_events`.
- `hibernate.ddl-auto=none`; **all schema changes must be Flyway migrations** (latest is **V16**).
- pgvector 0.8.7 installed locally; V16 forces the extension into schema `pars` (the JDBC search path); `ai.vector_store.embedding` is `vector(768)` for Ollama `nomic-embed-text`. Java default `embeddingDimensions=1536` — must be set to 768 for local Ollama (already done in run commands).
- `AiCommonConfiguration` uses `initializeSchema(false)`, COSINE distance, exact search (`NONE`), schema `ai`, table `vector_store`.
- **No NRB/source-registry tables exist** (`source_registry`, `source_sync_jobs`, `nrb_documents`, `nrb_document_versions`, `nrb_document_chunks`, `nrb_ingestion_errors`, `ai_embeddings` are all new work requiring V17+).

## 4. Security & RBAC

- Spring Security with two chains; JWT `JwtAuthenticationFilter` builds authorities `"ROLE_"+role` and exact permission codes; `@PreAuthorize("hasAuthority('CODE')")` pattern.
- `CurrentUserProvider.require()` → `CurrentUser(userId, username, sessionId, roles, permissions)`.
- Existing permission codes: 30 business codes; AI codes added by V14: `AI_CHAT`, `AI_CONVERSATION_READ`, `AI_DOCUMENT_READ`, `AI_DOCUMENT_UPLOAD`, `AI_DOCUMENT_APPROVE`, `AI_DOCUMENT_DELETE`, `AI_DOCUMENT_REINDEX`, `AI_FEEDBACK`, `AI_ADMIN`, `AI_METRICS_READ`. V14 grants AI codes to ADMIN only.
- New integration should add (via new migration, not by editing V14): `AI_SOURCE_READ`, `AI_SOURCE_MANAGE`, `AI_DOCUMENT_SYNC` — following the `ad_permission` + `ad_role_permission` seed pattern. Do not grant to USER by default.
- `GlobalExceptionHandler` maps `ApiException`, validation, optimistic lock, AccessDenied, 500. All new endpoints must use `ApiResponse<T>` and `Exceptions.*` factories.
- Known existing issues to account for: dev datasource env typo `PARS_DB_PASSWOR` (missing D); `/api/v1/exchange-rates` has no GET list endpoint.

## 5. Frontend

- `frontend/` exists: React 19, TypeScript strict, Vite 6, React Router 7, TanStack Query 5, axios, Tailwind 4, Radix UI, RHF/Zod, Vitest/RTL. Scripts: dev/build/lint/test/test:coverage.
- `src/api/client.ts`: `baseURL` from `VITE_API_BASE_URL` (empty → relative `/api`), Bearer interceptor, single-flight refresh, `unwrap`. Tokens in `sessionStorage` (`pars.accessToken`/`pars.refreshToken`).
- Design system in `src/components/common/ui.tsx`; AI pages `AiChatPage`, `AiDocumentsPage`, `AiAdminPage` already exist and are permission-gated.
- Router: `/ai/chat`, `/ai/documents`, `/ai/admin`. New NRB pages must reuse this stack and client — no second token system.

## 6. Gaps for the requested NRB integration

1. No outbound HTTP client/config (timeouts, retry, cache, SSRF allow-list).
2. No NRB tables or Flyway migration (latest V16).
3. No source registry / sync job mechanism / change detection.
4. No Forex DTOs, client, endpoints, or storage of fetched rates.
5. No crawler, page discovery, document downloader, or URL allow-list enforcement.
6. No Nepali Unicode handling verification tests; no language detection.
7. No Testcontainers/pgvector integration tests; no NRB evaluation dataset.
8. AI router currently handles only SELF/DEALS/APP_GUIDE/DOCUMENTS/CAPABILITY — needs FOREX_LIVE, FOREX_HISTORICAL, NRB_DOCUMENT routes.
9. Frontend has no Forex views, source status, or citation links to official URLs.

## 7. Risks / constraints

- Crawling an external government site: must respect `robots.txt`, rate limits, terms; stop if blocked; never bypass access controls. In this environment `robots.txt` could not be retrieved (transport error) — **runtime check is mandatory before enabling the crawler**.
- Only the Forex `/rates` endpoint is officially documented. All other content must come from controlled discovery of official pages; no invented APIs.
- External HTML/PDF content is untrusted: prompt-injection and malware/file-validation risks; keep all processing in the backend.
- Embedding dimension mismatch and `currentSchema=pars` must be handled before adding a second vector index.
