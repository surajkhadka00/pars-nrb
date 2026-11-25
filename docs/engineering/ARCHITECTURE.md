# ARCHITECTURE.md

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
pgvector extension in schema pars; dimension 768 in use locally
```

## Trust boundaries

1. Browser → backend: JWT bearer; browser never supplies user id, permissions, URLs, or SQL.
2. Backend → NRB: allow-listed hosts, robots.txt, bounded crawling.
3. Retrieved content → model: untrusted data inside delimiters; no tool calling.
4. Model output → browser: plain text/sanitized Markdown; no HTML execution.
5. AI → PARS data: read-only service calls with permission checks; no workflow writes.

## Kill switch

`pars.ai.enabled=false` disables all AI beans/endpoints without touching core PARS. `AiKillSwitchService` adds a runtime toggle persisted in `ad_sys_config`, recorded in audit log, checked before every chat request.

## Target modular boundaries (incremental)

| Layer | Package intent | Dependency rule |
|---|---|---|
| Existing business | `deal`, `usermanagement`, `rbac`, `auth`, `mail` | unchanged; no AI writes |
| Identity | `common.context`, `rbac.service` | sole source of identity/permissions |
| AI chat/router | `ai.service`, `ai.live` | depends on identity + providers + retrieval |
| Provider | `ai.provider` interface; adapters isolated | business services depend only on interfaces |
| Retrieval | `ai.retrieval`, `ai.embedding` | one vector store; filters server-side |
| Documents | `ai.documents`, `ai.ingestion` | private storage + jobs + approval |
| NRB | `integrations.nrb.forex`, `integrations.nrb.website` | outbound HTTP only through policy client |
| Monitoring | `monitoring`, Actuator/Micrometer | no prompt/document text in labels |

## Interfaces to introduce only where they enable substitution/tests

`ChatModelProvider`, `EmbeddingProvider`, `RerankerProvider`, `DocumentStorageProvider` — only after evaluation demonstrates need; current Spring AI abstractions suffice.

## Not implemented yet (out of scope of this phase)

SSE streaming, global search backend, Nepali localization full resources, CI, HTTPS reverse proxy, Testcontainers tests, evaluation harness.
