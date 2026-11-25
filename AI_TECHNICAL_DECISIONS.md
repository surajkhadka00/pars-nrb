# AI Technical Decisions — PARS NRB

| # | Decision | Rationale | Rejected alternative |
|---|---|---|---|
| TD-01 | Pin Spring AI BOM **1.0.0** exactly | Its POM declares Boot 3.4.5 — exact match. Verified from Maven Central. | 1.0.9 (Boot 3.5.15), 1.1.x, 2.x (Boot 4) — would require unsupported Boot upgrade |
| TD-02 | Additive package `np.org.nrb.pars.ai`, no edits to existing business services | Preserves existing behavior and Flyway checksums | Modifying existing controllers/services |
| TD-03 | New Flyway migrations V14+ only; never edit V1–V13 | Flyway checksum safety | Editing existing migrations |
| TD-04 | Separate `ai` schema; business stays in `pars` | Clear ownership; Hibernate default schema remains `pars` | Putting AI tables in `pars` |
| TD-05 | `PgVectorStore` with Flyway-created `ai.vector_store`, `initialize-schema=false`, `schema-validation=true` | One vector persistence implementation; no runtime DDL as privileged DB user | Spring AI auto-schema creation at startup |
| TD-06 | `index-type: NONE` (exact search) initially | Permission filters plus approximate indexes can reduce recall — correctness first; benchmark before HNSW | HNSW default |
| TD-07 | Spring AI interfaces (`ChatModel`, `EmbeddingModel`, `VectorStore`) in all services | Provider switching without touching business logic | Provider-specific SDK calls in services |
| TD-08 | Provider selection via `pars.ai.provider` + conditional starter beans; no mock fallback in production | Explicit configuration errors instead of silent fake answers | Automatic mock provider |
| TD-09 | AI disabled by default (`PARS_AI_ENABLED=false`) | No key → no accidental external calls; existing app unaffected | Always-on AI |
| TD-10 | Files stored in private filesystem dir `pars.ai.storage.path`, metadata in DB | Reuses proven pattern of deal attachments (magic-byte checks, path containment) without sharing lifecycle | Reusing deal-attachment storage |
| TD-11 | PostgreSQL job queue with `SKIP LOCKED`, no message broker yet | Existing infrastructure is sufficient; no unproven scaling requirement | RabbitMQ/Kafka |
| TD-12 | Hybrid retrieval: vector + PostgreSQL FTS + RRF | Exact identifiers (document numbers, codes) fail with vector-only search | Vector-only |
| TD-13 | Chunking 600 tokens / 100 overlap, heading-aware, configurable | Starting point from requirements; evaluation tunes it | Fixed hard-coded values |
| TD-14 | Abstention with fixed sentence + retrieval threshold, not LLM confidence alone | Deterministic, testable | Model self-reported confidence |
| TD-15 | AI permissions added via Flyway, granted only to ADMIN initially | No automatic broad granting | Grant-all |
| TD-16 | `DEPARTMENT` access classification documented but not implemented | `AppUser` has no department attribute; do not invent relationships | Fake department field |
| TD-17 | React AI pages inside existing frontend, same JWT/sessionStorage client | No second token system | Separate SPA |
| TD-18 | No streaming in first release; SSE listed as future extension | Simpler authorization, error handling and audit in v1 | Streaming from day one |

## Open items requiring external input

1. Approved model provider and API key (data governance approval for external data transfer).
2. PostgreSQL DBA confirmation that `vector`, `hstore`, `uuid-ossp` extensions can be created in target environments.
3. Business owner approval of AI permission codes and which roles receive them.
4. Chunking/evaluation parameters require a representative approved document set.
