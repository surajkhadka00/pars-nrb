# AI Database Schema — PARS NRB

Business data remains in `pars`. All AI objects live in schema `ai`, created by `V14__ai_schema.sql`. Hibernate `ddl-auto: none` and default schema `pars` are unchanged.

| Table | Purpose | Key columns |
|---|---|---|
| `ai_documents` | Document metadata, versioning, publication state | id, title, checksum, access_classification, publication_status, doc_version, active_version, uploaded_by, storage_reference, embedding_model |
| `ai_ingestion_jobs` | Async ingestion queue | id, document_id, status, stage, progress, attempts, max_attempts, error_message, claimed_by |
| `ai_conversations` | Private conversation ownership | id, owner_id, title, expires_at |
| `ai_messages` | Chat turns with sources | id, conversation_id, role, content, sources jsonb, insufficient_evidence |
| `ai_feedback` | User ratings | id, message_id, user_id, rating (1..5) |
| `ai_retrieval_logs` | Retrieval audit (no question text stored — SHA-256 hash only) | user_id, question_sha256, chunk_ids, scores, trace_id |
| `ai_document_access` | Explicit GRANT/DENY per user or role | document_id, user_id, role_code, effect |
| `ai_usage_events` | Quota/cost telemetry | user_id, event_type, tokens_in, estimated_cost |
| `ai.vector_store` | Spring AI PGvector store (created only when extension available) | id, content, metadata json, embedding vector(1536) |

## Vector metadata contract

Each chunk stored by `AiIngestionWorker` carries:
`docId`, `title`, `version`, `page`, `section`, `classification`, `access`, `deny`, `status`, `active`.

`access` is one of `PUBLIC`, `INTERNAL`, `ROLE_<role>`, `USER_<uuid>`. Server-side retrieval filters require:
`access IN (verified tokens) AND status = 'PUBLISHED' AND active = true AND deny != <current user id>`.

## Index strategy

- `index-type: NONE` (exact search) initially: permission filters with approximate HNSW indexes can lose recall.
- FTS: `to_tsvector('simple', content)` with GIN-compatible expressions; `websearch_to_tsquery`.
- Re-evaluate HNSW/IVFFlat after measuring query latency with representative data.
- Production DBA note: the `vector` extension and its type/operators must be created in the **`pars` schema** (the JDBC URL sets `currentSchema=pars`, so `public.vector` is invisible to Flyway and to Spring AI SQL). `scripts/install-pgvector.ps1` calls `ALTER EXTENSION vector SET SCHEMA pars;` for this reason.

Vector dimensions are fixed at 768 in V16 to match the FREE local embedding model `nomic-embed-text` (Ollama). If you switch to OpenAI embeddings (1536), alter the column and reindex all published documents.
