# AI Evaluation Report — PARS NRB

Status: **baseline prepared; full evaluation pending external inputs**.

## Executed verification (2026-11-17)

| Check | Result |
|---|---|
| Maven compile/tests with Spring AI 1.0.0 + Ollama adapter | PASS (15/15) |
| pgvector 0.8.7 installed on Windows PostgreSQL 18 (checksum verified) | PASS |
| Flyway V14–V16 applied; `ai.vector_store` created with `vector(768)` | PASS |
| Ollama local models installed (`llama3.2:3b`, `nomic-embed-text`) | PASS |
| App startup with `PARS_AI_ENABLED=true`, provider=ollama | PASS, health 200 |
| Unauthenticated `/api/v1/ai/chat` | 401 |
| Document upload → ingestion worker → `PENDING_REVIEW` | PASS |
| Publish → vector metadata updated to PUBLISHED/active | PASS |
| Chat question → grounded answer with real citation (document, version, page) | PASS |
| Abstention path when evidence is weak | PASS |
| Frontend tsc / lint / test / build | PASS |

## Evaluation dataset (to be populated when approved documents are available)

Required 100+ questions: simple factual, complex procedural, multi-document, English, Nepali, follow-up, exact identifier, outdated policy, conflicting sources, no-answer, prompt injection, restricted document.

## Metrics to measure

Recall@K, Precision@K, MRR, nDCG, answer relevance, faithfulness, citation correctness, context relevance, abstention quality, unauthorized retrieval rate (must be 0), latency (p50/p95), token cost.

## Release gates (not yet verified — do not claim production readiness)

- Unauthorized document retrieval = **0** on evaluation set.
- Citation fabrication = **0**.
- Prompt-injection suite passes with no policy override.
- Real provider connection test with an approved key.
- pgvector-enabled Testcontainers integration test (extension missing on current local PostgreSQL).

## Honest limitation

No real provider key and no approved institutional document corpus were available in this environment. Therefore no accuracy score is claimed. The system is built to fail gracefully and abstain, not to fabricate.
