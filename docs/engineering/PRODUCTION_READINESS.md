# PRODUCTION_READINESS.md

## Current state: NOT production-ready

The application is functional and well-tested at unit level, but the production gates below are not yet satisfied. No regulatory or compliance claim is made.

## Gate status

| Gate | Status | Evidence / gap |
|---|---|---|
| A — Functional | Partial | Core PARS + AI + NRB flows verified; SSE, global search, exports pending |
| B — Security | Partial | CORS/JWT/SSRF/header fixes done; no CSP header tests, no CI security scan, no penetration test |
| C — AI quality | Partial | 44 tests; no 100-question evaluation corpus or metrics |
| D — Reliability | Partial | Retries bounded; cancellation works for chat? (no SSE stop yet); backups not rehearsed |
| E — UX/Accessibility | Partial | Design system + mobile drawer + dark theme; no axe tests, no 320px verification, no i18n full |
| F — Release | Not started | No CI, no staging deploy, no ops docs beyond deployment guide |

## Required before production

- `mvn test` and frontend `npm test` green in CI (no `-DskipTests`).
- PostgreSQL + pgvector integration tests.
- Backup and restore rehearsal including private storage.
- Independent security review and penetration test.
- Data-governance approval for any hosted AI provider.
- Banking-domain approval of deal workflow rules.
- Secret management via environment/secret store (no defaults).
- TLS reverse proxy and HTTPS.
- Monitoring, alerting, kill-switch verification.
- Remaining risks from `SECURITY_RISK_REGISTER.md` closed or explicitly risk-accepted.

## Kill switch

`pars.ai.enabled=false` disables all AI beans/endpoints. Runtime toggle `ad_sys_config.ai.kill.switch` (ADMIN endpoint `POST /api/v1/ai/admin/kill-switch`) disables generation with audit record. External ingestion gating is pending.
