# IMPLEMENTATION_ROADMAP.md

Phases from the master directive. Each phase lists entry gates, work, and exit evidence. Nothing below is claimed complete unless verified.

| Phase | Work | Exit evidence | Status |
|---|---|---|---|
| 0 | Repository audit + architecture + risk register | 8 audit docs + this roadmap | **Complete** (2026-11-20) |
| 1 | Security & data-flow threat model; fix CORS fail-closed, JWT secret validation, refresh atomicity, header hardening | Tests for each fix; app starts | Next |
| 2 | Shared design system components + tokens | Component tests; lint/build | Pending |
| 3 | Responsive AppShell, mobile drawer, breadcrumbs, titles | 320px + keyboard tests | Pending |
| 4 | DataTable/Filter/Pagination/States across pages | Page tests | Pending |
| 5 | Auth/workflow hardening (no AI writes; transition rules) | Workflow + security tests | Pending |
| 6 | Chat conversation architecture + SSE streaming + stop/cancel | Streaming tests | Pending |
| 7 | Provider interfaces, prompt registry, AI safety | Provider tests; injection corpus | Pending |
| 8 | NRB Forex connector hardening | Existing verified; add redirect/SSRF fix | Pending |
| 9 | NRB controlled discovery/ingestion hardening | Redirect + size-cap tests | Pending |
| 10 | OCR/extraction/lifecycle + approval gate | Publication tests | Pending |
| 11 | RAG retrieval + persisted citations | Citation reload test | Pending |
| 12 | Evaluation, reranker, grounded verification | Offline eval script + report | Pending |
| 13 | React chat/document admin redesign | UI/a11y tests | Pending |
| 14 | Observability, cost, notifications, kill switch | Metrics/audit tests | Pending |
| 15 | Nepali localization + accessibility | Locale + axe tests | Pending |
| 16 | Security/performance/resilience testing | Full test report | Pending |
| 17 | Deployment/docs/release readiness | Staging deploy + checklist | Pending |

## Safe dependency order

1. Security fixes that do not change public contracts (CORS, JWT secret validation, headers, SSRF redirects, formatter).
2. Database consistency (dimension migration, indexes) with backup first.
3. Backend contracts used by UI (citations reload, SSE endpoint, kill switch).
4. Design system → shell → pages.
5. AI quality (evaluation, reranker, verification) after stable contracts.
6. Deployment/infra hardening last (needs environment decisions).

## Human decisions required before implementation

- CORS policy per environment and trusted proxy topology (AUD-002/008).
- Deal workflow transition rules and maker-checker policy (AUD-030).
- AI provider/data-governance approval (AUD-029).
- Approval requirement for external NRB documents (AUD-015).
- Production backup/restore and secret-store choices.

## Current verified baseline (to preserve)

`mvn test` 37/37 pass; frontend `tsc`, `lint`, `test`, `build` pass; Flyway V18 applied; app starts and health returns 200; login through Vite proxy returns 200.
