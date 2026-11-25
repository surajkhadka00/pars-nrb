# AI_RAG_AUDIT.md

## Current implementation

- 25 AI classes + 18 NRB classes; all additive, AI disabled by default.
- Router: 8 deterministic routes (DOCUMENTS, SELF, DEALS, APP_GUIDE, CAPABILITY, FOREX_LIVE, FOREX_HISTORICAL, NRB_DOCUMENT).
- Retrieval: pgvector cosine (topK 10, threshold 0.35) + PostgreSQL FTS, RRF to 5 passages; server-side filters from JWT.
- Generation: OpenAI or Ollama via Spring AI; system prompt treats evidence as untrusted; fixed abstention sentence; citations from metadata (title/version/page/sourceUrl).
- Ingestion: upload → validate → extract → chunk → embed → `PENDING_REVIEW` → explicit publish; NRB crawler auto-publishes external docs (AUD-015).
- Conversations: owner-checked; feedback; daily quota; in-process semaphore.

## Gaps vs banking-grade target (full IDs in PROJECT_AUDIT.md)

| ID | Gap | Impact | Required work |
|---|---|---|---|
| AUD-010 | Citations not reloaded on conversation open | Audit/trust gap | Parse JSONB into typed responses |
| AUD-011 | Quota racy; RPM config unused; usage inconsistent | Quota bypass; cost blind spots | Atomic reservation + central accounting |
| AUD-012 | Exact vector scan; dimension conflict | Latency/recall; index failure | Benchmark + canonical dimension |
| AUD-015 | External docs auto-published | Unreviewed content authoritative | Review/approval state |
| AI-005 | No streaming/SSE, stop, retry, regenerate, branch UX | Modern chat expectations | Backend SSE + cancellation + frontend |
| AI-006 | No prompt version registry or telemetry | Untraceable behavior | Versioned prompt resources |
| AI-007 | No reranker or grounded-answer verification pass | Faithfulness risk | Evaluation-gated reranker + claim check |
| AI-008 | No offline evaluation dataset/script | No measurable quality | 100-question set + metrics |
| AI-009 | No per-user/role quota or cost reporting | Cost control gap | Usage tables + admin reports |
| AI-010 | External OpenAI path may receive business context | Data governance | Redaction/approved-provider policy |

## Positive

- No tool calling; no SQL access; no workflow writes; AI cannot approve deals.
- Unauthorized chunks filtered in the vector/SQL query before context construction.
- Insufficient evidence produces the fixed abstention sentence.
- Live Forex answers carry date, published/modified, fetch time and official URL; historical data is distinct.

## Evaluation status

No approved corpus; no Recall/MRR/nDCG/faithfulness/citation scores claimed. See `AI_EVALUATION_REPORT.md` and `NRB_TEST_REPORT.md`.
