# AI Implementation Plan — PARS NRB

## Phase 0 — Source audit & research ✅ COMPLETE
- Audited 177 Java files, all controllers/services/repositories/entities/DTOs, security, 13 Flyway migrations, YAML, tests, storage, frontend, deployment.
- Verified Spring AI compatibility: **1.0.0 GA ↔ Boot 3.4.5** (exact). Rejected 1.0.9, 1.1.x, 2.x.
- Confirmed no existing AI code/tables/deps; frontend exists at `frontend/`.
- Created AI_ARCHITECTURE.md, AI_RESEARCH.md, AI_TECHNICAL_DECISIONS.md, AI_SECURITY_THREAT_MODEL.md.

## Phase 1 — AI foundation
- [ ] Add Spring AI 1.0.0 BOM + pinned starters to `pom.xml`; resolve dependency tree; confirm no Boot/Jackson/Reactor conflicts.
- [ ] Add `ParsAiProperties` (`@ConfigurationProperties(prefix="pars.ai")`), `AiModelConfiguration` conditional on `pars.ai.enabled`.
- [ ] Add AI config to `application.yml` + `application-dev.yml` (`PARS_AI_*` env overrides), no secrets committed.
- [ ] Flyway `V14__ai_schema.sql`: `ai` schema, `vector/hstore/uuid-ossp` extension check, AI tables, permission codes seeded + ADMIN grants.
- [ ] Verify PostgreSQL version + pgvector availability with SQL; document DBA prerequisite.
- [ ] `AiHealthIndicator` at `/actuator/health/ai`.
- Tests: Maven build + existing 8 tests still green + dependency-tree check.

## Phase 2 — Database & security
- [ ] JPA entities + repositories for `ai` schema (documents, jobs, conversations, messages, feedback, access grants, retrieval logs).
- [ ] `AiPermissionService` wrapping existing `CurrentUserProvider`/`PermissionResolver`.
- [ ] Section 104 of SecurityConfig unchanged; new endpoints annotated `@PreAuthorize("hasAuthority('AI_...')")`.
- Tests: permission resolution unit tests.

## Phase 3 — Document ingestion
- [ ] File validation service (extension, MIME, magic bytes, size, traversal).
- [ ] Private storage service.
- [ ] Extraction service (PDF/Tika/POI/Jsoup), heading-aware chunker with configurable size/overlap.
- [ ] `AiIngestionWorker` @Scheduled with SKIP LOCKED, retries, status transitions, atomic publication.
- [ ] Document controller CRUD + publish/archive/reindex.
- Tests: extraction, chunking, validation, job state tests.

## Phase 4 — RAG engine
- [ ] `HybridRetrievalService`: pgvector similarity (Spring AI) + PostgreSQL FTS + RRF.
- [ ] Server-side access filters (PUBLISHED, active version, classification, grants).
- [ ] `AiPromptService` (configurable system prompt, untrusted document delimiters).
- [ ] `RagAnswerService` with citations, confidence threshold, fixed abstention.
- Tests: retrieval security, citation correctness, abstention.

## Phase 5 — Chat API
- [ ] `AiChatController`: chat, conversations CRUD, feedback.
- [ ] Owner checks, retention, quotas, timeouts, Micrometer meters.
- Tests: auth, ownership, quotas, error handling.

## Phase 6 — React application
- [ ] `src/api/aiApi.ts` typed client (existing token/refresh client).
- [ ] AI chat page (conversation sidebar, sources, feedback, states).
- [ ] Document admin page (upload, status, publish/archive/reindex/delete).
- [ ] AI admin page (config status, metrics, jobs).
- [ ] Router + permission-aware nav.
- Tests: Vitest/RTL for chat and admin views.

## Phase 7 — Security hardening
- [ ] Prompt injection, XSS, log-redaction, key-leak, path-traversal tests.
- [ ] Audit integration with existing `ad_audit_log`.

## Phase 8 — Testing & evaluation
- [ ] Testcontainers PostgreSQL + pgvector integration tests.
- [ ] 100-question evaluation dataset when approved documents are available; Recall@K, MRR, nDCG, faithfulness, abstention, unauthorized retrieval = 0.

## Phase 9 — Deployment
- [ ] Dockerfile, docker-compose (pgvector dev DB), env examples, backup strategy, production checklist.
- [ ] AI_EVALUATION_REPORT.md, AI_DEPLOYMENT_GUIDE.md, AI_ENVIRONMENT_VARIABLES.md, AI_TROUBLESHOOTING.md, README.md.

Legend: ✅ complete, ⚠️ implemented but unverified against a real provider/dataset, [ ] pending.

## Status after implementation pass (2026-11-17)
- Phase 0 ✅ | Phase 1 ✅ | Phase 2 ✅ | Phase 3 ✅ | Phase 4 ✅ | Phase 5 ✅ | Phase 6 ✅ | Phase 7 ⚠️ | Phase 8 ⚠️ | Phase 9 ⚠️
- Verified: `mvn test` 19/19 pass; frontend `tsc`, `lint`, `test`, `build` pass; app boots with AI enabled; unauthenticated AI endpoints return 401.
- Verified end to end: document RAG with citations; live self/deals/app-guide queries; shared refresh/concurrency fix.
- Unverified by design: real hosted provider connection, full prompt-injection suite, 100-question evaluation corpus, Docker deployment.
