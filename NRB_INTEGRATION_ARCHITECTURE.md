# NRB Integration — Architecture Proposal (Phase 0)

Status: **proposal only. No database, configuration, or production code changed by this phase.**

## 1. Modules (all under `np.org.nrb.pars.nrb`)

```
nrb/
├── config/     NrbProperties (@ConfigurationProperties "nrb"), NrbHttpConfiguration (RestClient bean)
├── client/     NrbForexApiClient (typed /rates client)
├── domain/     NrbForexRate, NrbForexDay, NrbForexResponse, NrbSourceDefinition, ...
├── crawler/    NrbWebsiteCrawler, NrbDocumentDiscoveryService, NrbDocumentDownloader,
│               NrbContentChangeDetector, NrbSourceHealthService
├── ingest/     NrbDocumentParser, NrbDocumentMetadataService, NrbRagIngestionService
├── service/    NrbForexService, NrbSourceService, NrbSyncScheduler
└── repository/ JDBC repositories for source registry, forex snapshots, documents, jobs
```

Design principles:
- Every source comes from a **configurable registry row** (name, URL, type, category, enabled, hashes, dates, status, errors). No hardcoded site structure.
- All outbound calls go through one `RestClient` bean with connect/response timeouts from `nrb.*` config.
- Crawler restricted to an allow-list of official hosts; redirects validated; SSRF blocked.
- Crawler and heavy parsing/embedding run in `@Scheduled` jobs, never in HTTP request threads.
- Existing `ApiResponse<T>` / `ApiMeta`, `Exceptions.*` and `@PreAuthorize` conventions are reused.
- A new `AiQueryRouter` branch (FOREX_LIVE / FOREX_HISTORICAL / NRB_DOCUMENT) reuses the existing RAG answer service.

## 2. Data flow

```
React /ai/chat ──POST /api/v1/ai/chat──▶ AiChatController ─▶ RagAnswerService
                                              ├── SELF / DEALS / APP_GUIDE (existing LiveDataService)
                                              ├── FOREX_LIVE ──▶ NrbForexService (cached live /rates)
                                              ├── FOREX_HISTORICAL ──▶ stored forex snapshots (DB)
                                              ├── NRB_DOCUMENT ──▶ HybridRetrievalService (pgvector + FTS)
                                              └── DOCUMENTS (existing)

NrbSyncScheduler ─▶ NrbWebsiteCrawler (robots + allow-list + delay)
                 ─▶ Discovery ─▶ hash change detection ─▶ Downloader (size/signature validation)
                 ─▶ Parser (PDF/DOCX/XLSX/HTML/TXT/CSV, Nepali Unicode preserved)
                 ─▶ Chunker ─▶ EmbeddingModel ─▶ ai.vector_store (768 dims, Ollama)
                 ─▶ PUBLISH only after all steps succeed
```

## 3. Proposed Flyway additions (V17+, after approval)

New tables in schema `ai` (no changes to V1–V16):
- `ai.nrb_source_registry` — source metadata described in the prompt.
- `ai.nrb_source_sync_jobs` — job state, progress, attempts, timestamps, errors.
- `ai.nrb_forex_snapshots` + `ai.nrb_forex_rates` — raw NRB payload fields preserved (date, published_on, modified_on, iso3, name, unit, buy/sell as NUMERIC(22,8), fetched_at, source_url, content_hash).
- `ai.nrb_documents` / `ai.nrb_document_versions` / `ai.nrb_document_chunks` / `ai.nrb_ingestion_errors`.
- `ai.ai_embeddings` metadata table only if needed for model/version tracking — **no duplicate vector storage**; vectors stay in `ai.vector_store`.
- Reuse existing `ai_retrieval_logs`, `ai_conversations`, `ai_messages`, `ai_feedback`.

Add permissions via a new migration (not by editing V14): `AI_SOURCE_READ`, `AI_SOURCE_MANAGE`, `AI_DOCUMENT_SYNC`; grant only to ADMIN.

## 4. Proposed endpoints (all with existing ApiResponse format)

Authenticated (JWT + permission):
- `GET /api/v1/nrb/forex/latest` (`AI_CHAT` or new `AI_FOREX_READ` — decision needed)
- `GET /api/v1/nrb/forex?from=&to=&page=&per_page=` (historical, stored snapshots)
- `GET /api/v1/nrb/forex/{currencyCode}` (ISO3)
- `GET /api/v1/nrb/sources`, `GET /api/v1/nrb/sources/{id}` (`AI_SOURCE_READ`)
- `GET /api/v1/nrb/documents`, `GET /api/v1/nrb/documents/{id}` (`AI_DOCUMENT_READ`)
- `GET /api/v1/ai/sources/status`

Admin only:
- `POST /api/v1/admin/nrb/sources/{id}/sync`, `POST /api/v1/admin/nrb/sync`,
- `POST /api/v1/admin/nrb/documents/{id}/reindex`, `PATCH /api/v1/admin/nrb/sources/{id}` (`AI_SOURCE_MANAGE`/`AI_DOCUMENT_SYNC`)

No route conflicts: `/api/v1/nrb/**` and `/api/v1/admin/nrb/**` are currently unused.

## 5. Configuration (follow `pars.*` kebab-case convention)

`nrb.forex-api-base-url`, `nrb.website-base-url`, `nrb.crawler.enabled`, `nrb.crawler.user-agent`, `nrb.crawler.delay-ms`, `nrb.sync-cron`, `nrb.forex-cache-ttl`, `nrb.max-download-size`, `nrb.allowed-domains`, plus request/response timeout and max pages. Environment variables use uppercase `NRB_*` names as requested. AI variables already exist as `PARS_AI_*`; map `AI_PROVIDER`/`AI_MODEL`/`AI_EMBEDDING_MODEL` to the existing `pars.ai.*` names rather than introducing a second convention (documented mapping).

## 6. Security decisions

- No URL from the browser is ever fetched; source IDs map to registry rows only.
- Allow-list validated on every redirect/hop; private IP/link-local targets rejected (SSRF).
- robots.txt honoured; 403/429/5xx → backoff and recorded error, never bypassed.
- Uploaded/downloaded files validated by extension, MIME, magic bytes and size; no execution.
- Prompts treat all web/PDF content as untrusted data with delimiters; no tool calling; no SQL access.
- Every answer cites source URL + title + publication date + page/section where available.
- Indicative NRB rates are explicitly labelled as such, never as bank transaction rates.
- Secrets only from environment; never logged.

## 7. Implementation order (after approval)

1. Config + HTTP client + Forex DTOs/client/tests (no DB changes).
2. V17 migration + Forex persistence + `/nrb/forex/**` endpoints + tests.
3. Source registry + crawler policy/health + V18 migration.
4. Downloader/parser/metadata/change detection + V19 migration.
5. RAG router branches + citations + answer tests.
6. React pages (Forex current/historical, source status, citation links).
7. Security hardening, Testcontainers tests, evaluation dataset, docs.
