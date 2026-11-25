# AI Troubleshooting — PARS NRB

| Symptom | Cause | Fix |
|---|---|---|
| `/api/v1/ai/chat` returns 404 | `pars.ai.enabled=false` (AI beans not registered) | Set `PARS_AI_ENABLED=true` with a valid key and restart. |
| App fails: `PARS_AI_API_KEY is required` | AI enabled without key | Supply key or disable AI. No mock fallback exists by design. |
| `PARS AI: pgvector extension NOT available` at startup | Local PostgreSQL lacks pgvector | Install `pgvector` (DBA) and re-run migration; AI remains disabled meanwhile. |
| Upload rejected as "File content does not match extension" | MIME/magic mismatch | Re-export the file; scanned/renamed files must be genuine PDF/DOCX/XLSX/TXT/HTML. |
| Job stuck in `FAILED` | Extraction/chunking error (e.g. scanned PDF, encrypted file) | Read `error_message` from `/admin/jobs` or `ai_ingestion_jobs`; upload an extractable file. |
| Document not appearing in answers | Not PUBLISHED / active_version false / access filter | Publish after `PENDING_REVIEW`; check access classification and grants. |
| Answer says insufficient evidence | Retrieval below threshold or filters exclude all chunks | Check `ai.vector_store` rows, metadata `status`, `active`, `access`, `deny`. |
| Dimension mismatch on startup | Local Ollama uses 768 (`nomic-embed-text`); OpenAI uses 1536. Set `PARS_AI_EMBEDDING_DIMENSIONS` to match `ai.vector_store.embedding`, then reindex |
| 429 responses | Daily quota or concurrency semaphore exhausted | Reduce load, raise `PARS_AI_DAILY_LIMIT` / `PARS_AI_MAX_CONCURRENT_REQUESTS`. |
| Frontend AI pages not in sidebar | Missing permission | Grant `AI_CHAT` / `AI_DOCUMENT_READ` / `AI_ADMIN` deliberately; admin has them by default. |
| 401 loop after login | Existing JWT/session expiry | Use the existing frontend apiClient refresh logic; never create a second token store. |

Useful SQL:

```sql
SELECT id, title, publication_status, active_version, access_classification FROM ai.ai_documents;
SELECT id, status, stage, attempts, error_message FROM ai.ai_ingestion_jobs ORDER BY created_at DESC LIMIT 20;
SELECT metadata->>'title', metadata->>'access', metadata->>'status', metadata->>'active' FROM ai.vector_store LIMIT 10;
```
