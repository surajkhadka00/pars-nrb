# DATABASE_AUDIT.md

## Current state

- PostgreSQL 18.6; schemas `pars` (business) and `ai` (AI/NRB); Flyway V1–V18, `hibernate.ddl-auto=none`.
- JDBC URL uses `currentSchema=pars`; unqualified business tables resolve to `pars`; AI/NRB SQL explicitly qualifies `ai.`.
- pgvector 0.8.7 installed in schema `pars` (V16 moves it there); `ai.vector_store` uses `vector(768)` locally.

## Migrations

| Version | Summary | Risk |
|---|---|---|
| V1–V13 | Foundation, RBAC, deals/masters, attachments, mail, fiscal year, country, positions | Additive; no undo files |
| V14 | `ai` schema, 8 AI tables, 10 AI permissions → ADMIN | `vector(1536)` declared here |
| V15/V16 | Conditional `vector(768)` create; extension move to `pars` | `IF NOT EXISTS` ⇒ no-op when table already exists (AUD-017) |
| V17 | Forex snapshots/rates + 4 AI permissions | Idempotent `ON CONFLICT` |
| V18 | Source registry, sync jobs, NRB documents/versions/errors, embeddings metadata | Seeds 7 disabled sources |

## Positive

- All AI/NRB inserts are idempotent via unique constraints and `ON CONFLICT DO NOTHING`.
- Audit/version columns on business tables; `@Version` used by JPA; no destructive `DROP`.
- Parameterized JDBC throughout; no concatenated user input in SQL.

## Findings (see PROJECT_AUDIT.md IDs)

| ID | Finding | Severity | Correction |
|---|---|---|---|
| AUD-017 | 1536 vs 768 dimension conflict and silent no-op migrations | H | One canonical dimension migration; startup validator; reindex procedure |
| AUD-018 | Missing indexes: `ai.nrb_forex_rates(snapshot_id)`, `ai.nrb_documents(canonical_url, source_id)`, `ai.nrb_ingestion_errors(document_id)`, `ai.ai_embeddings(chunk_id)` | M | V19 index migration; verify with `EXPLAIN ANALYZE` |
| AUD-019 | dev `validate-on-migrate:false` | M | Enable validation; CI migration test |
| DB-004 | `ai.vector_store` has no index (`indexType NONE`) | M (scale) | Benchmark HNSW with permission filters before enabling |
| DB-005 | No undo migrations or documented restore test | M | Backup/restore runbook + restore rehearsal |

## Transaction/locking notes

- `pg_advisory_xact_lock` used for document numbering — good.
- AuthSession activity bump intentionally bypasses `@Version` via bulk JPQL update to avoid concurrent 500s.
- Refresh rotation check-then-write is not atomic (AUD-007) — needs DB-level conditional update.
- AI/NRB JDBC writes are not always in explicit transactions; ingestion stages update status field-by-field — a crash can leave a job in an intermediate state (recoverable by re-sync, but record as a risk).

## Verification plan

Testcontainers PostgreSQL + pgvector; clean-DB migration test; dimension validation test; idempotency tests; `EXPLAIN ANALYZE` for forex joins and vector search with metadata filters; backup/restore rehearsal.
