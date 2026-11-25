# AI Environment Variables — PARS NRB

Never commit real values. Use Windows PowerShell `$env:` variables, IntelliJ run configuration, Docker secrets, or a secret manager.

| Variable | Default | Purpose |
|---|---|---|
| `PARS_AI_ENABLED` | `false` | Master switch. AI beans/endpoints exist only when `true`. |
| `PARS_AI_PROVIDER` | `openai` | `openai` (hosted/paid) or `ollama` (FREE, local, no data leaves your machine). |
| `PARS_AI_CHAT_MODEL` | `gpt-4o-mini` | Chat model identifier. |
| `PARS_AI_EMBEDDING_MODEL` | `text-embedding-3-small` | Embedding model identifier. |
| `PARS_AI_API_KEY` | *(empty)* | Provider key. Required when enabled; never logged or returned by API. |
| `PARS_AI_BASE_URL` | *(empty)* | Optional compatible endpoint override. |
| `PARS_AI_MAX_TOKENS` | `1500` | Model output token cap. |
| `PARS_AI_TEMPERATURE` | `0.2` | Deterministic-leaning generation. |
| `PARS_AI_TIMEOUT` | `30s` | Request/concurrency timeout. |
| `PARS_AI_MAX_CONCURRENT_REQUESTS` | `4` | Semaphore limit for chat requests. |
| `PARS_AI_EMBEDDING_DIMENSIONS` | `1536` | Must match `vector(1536)` in Flyway V14. |
| `PARS_AI_EMBEDDING_MODEL_VERSION` | `1` | Recorded on documents for migration tracking. |
| `PARS_AI_STORAGE_DIR` | `/var/data/nrb-pars-ai` | Private original-file storage. |
| `PARS_AI_MAX_FILE_BYTES` | `20971520` | Upload size limit. |
| `PARS_AI_CHUNK_SIZE` | `600` | Chunk size in estimated tokens. |
| `PARS_AI_CHUNK_OVERLAP` | `100` | Overlap in estimated tokens. |
| `PARS_AI_TOP_K` | `10` | Retrieval candidate count. |
| `PARS_AI_FINAL_PASSAGES` | `5` | Evidence passages sent to the model. |
| `PARS_AI_SIMILARITY_THRESHOLD` | `0.35` | Vector similarity floor. |
| `PARS_AI_EVIDENCE_THRESHOLD` | `0.45` | Abstention floor. |
| `PARS_AI_RERANKER_ENABLED` | `false` | Reserved for evaluation-gated reranker. |
| `PARS_AI_DAILY_LIMIT` | `100` | Per-user daily chat quota. |
| `PARS_AI_RPM` | `20` | Reserved rate-limit value for deployment gateway. |

Existing variables still apply: `PARS_DB_URL`, `PARS_DB_USER`, `PARS_DB_PASSWORD`, `PARS_JWT_SECRET`, `PARS_CORS_ORIGINS`, `PARS_PORT`.

### FREE local model (recommended for development / confidential data)

```powershell
# One-time setup (Administrator for pgvector, then Ollama install)
powershell -ExecutionPolicy Bypass -File scripts\install-pgvector.ps1
powershell -ExecutionPolicy Bypass -File scripts\setup-ollama.ps1

# Run backend
$env:PARS_AI_ENABLED='true'
$env:PARS_AI_PROVIDER='ollama'
$env:PARS_AI_CHAT_MODEL='llama3.2:3b'
$env:PARS_AI_EMBEDDING_MODEL='nomic-embed-text'
$env:PARS_AI_EMBEDDING_DIMENSIONS='768'
mvn spring-boot:run
```

No API key is needed with Ollama. `nomic-embed-text` produces 768-dimensional vectors, which Flyway `V15__ai_vector_store_local_model.sql` creates. If you switch back to OpenAI embeddings (1536 dimensions), you must alter the column and reindex.

### Windows quick start (hosted provider)

```powershell
$env:PARS_AI_ENABLED='true'
$env:PARS_AI_PROVIDER='openai'
$env:PARS_AI_API_KEY='<your-approved-key>'
```
