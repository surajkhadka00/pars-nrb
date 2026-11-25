# SYSTEM_ARCHITECTURE.md

## Actual current architecture

```
React SPA (frontend/, port 5173)
  └─ axios client (sessionStorage JWT, single-flight refresh)
       └─ Vite proxy /api → Spring Boot 8080

Spring Boot 3.4.5 / Java 17 (np.org.nrb.pars)
  ├─ SecurityConfig: management chain (/actuator/**) + main chain
  │    TraceIdFilter → LoginRateLimitFilter → JwtAuthenticationFilter
  │    CurrentUser principal → SecurityContext → @PreAuthorize
  ├─ Domain modules: auth, usermanagement, rbac, deal, mail, monitoring, system
  │    Controllers (DTO in/out) → Services (@Transactional) → JPA repositories → pars schema
  ├─ common: ApiResponse/ApiMeta, Exceptions/GlobalExceptionHandler, OData queries, audit
  ├─ ai (enabled by flag): chat/documents/admin controllers → RagAnswerService → AiQueryRouter
  │    ├─ LiveDataService (UserService/DealService, permission checked)
  │    ├─ HybridRetrievalService (pgvector + PostgreSQL FTS + RRF)
  │    ├─ AiPromptService → ChatClient → OpenAI or Ollama
  │    └─ AiIngestionWorker → extract → chunk → embed → ai.vector_store
  └─ nrb: Forex client/schedulers, controlled crawler, source registry, document ingestion
       └─ one vector store in ai schema (shared with AI module)

PostgreSQL: schema pars (business) + schema ai (AI/NRB metadata + vector_store)
pgvector extension installed in schema pars; dimension 768 in use locally
```

## Target modular boundaries (to be implemented incrementally)

| Layer | Package intent | Dependency rule |
|---|---|---|
| Existing business | `deal`, `usermanagement`, `rbac`, `auth`, `mail` | unchanged; no AI writes |
| Identity | `common.context`, `rbac.service` | sole source of identity/permissions |
| AI chat/router | `ai.chat`, `ai.router` (current `ai.service`, `ai.live`) | depends on identity + providers + retrieval |
| Provider | `ai.provider` interface; `AiModelConfiguration`/`OllamaModelConfiguration` adapters | business services depend only on interfaces |
| Retrieval | `ai.retrieval`, `ai.embedding` | one vector store; metadata filters server-side |
| Documents | `ai.documents`, `ai.ingestion` | private storage + jobs + approval workflow |
| NRB | `integrations.nrb.forex`, `integrations.nrb.website` | outbound HTTP only through policy client |
| Monitoring | `monitoring`, Actuator/Micrometer | no prompt/document text in labels |

Dependency inversion: introduce `ChatModelProvider`, `EmbeddingProvider`, `RerankerProvider`, `DocumentStorageProvider` interfaces only where they enable substitution/tests; keep current Spring AI abstractions otherwise.

## Trust boundaries

1. Browser → backend: JWT bearer; browser never supplies user id, permissions, URLs, or SQL.
2. Backend → NRB: allow-listed hosts, robots.txt, bounded crawling.
3. Retrieved content → model: untrusted data inside delimiters; no tool calling.
4. Model output → browser: plain text/sanitized Markdown; no HTML execution.
5. AI → PARS data: read-only service calls with permission checks; no workflow writes.

## Kill switch (to implement)

`pars.ai.enabled=false` already disables all AI beans/endpoints without touching core PARS. Extend with an operator-controlled runtime flag (persisted in `ad_sys_config`, admin endpoint, audit record) so AI generation and external ingestion can be disabled independently at runtime.
