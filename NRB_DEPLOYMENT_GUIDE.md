# NRB Deployment & Troubleshooting

## Run (Windows)

```powershell
cd C:\Users\WELCOME\Downloads\pars-nrb
$env:PARS_AI_ENABLED='true'
$env:PARS_AI_PROVIDER='ollama'
$env:PARS_AI_CHAT_MODEL='llama3.2:3b'
$env:PARS_AI_EMBEDDING_MODEL='nomic-embed-text'
$env:PARS_AI_EMBEDDING_DIMENSIONS='768'
$env:NRB_CRAWLER_ENABLED='true'   # optional; off by default
mvn spring-boot:run
```

Frontend: `cd frontend; npm.cmd run dev` → http://localhost:5173.

## Environment variables

See `NRB_API_RESEARCH.md` and `.env.example`. Key ones: `NRB_FOREX_API_BASE_URL`, `NRB_WEBSITE_BASE_URL`, `NRB_CRAWLER_ENABLED`, `NRB_CRAWLER_USER_AGENT`, `NRB_CRAWLER_DELAY_MS`, `NRB_SYNC_CRON`, `NRB_FOREX_CACHE_TTL`, `NRB_MAX_DOWNLOAD_SIZE`, `NRB_ALLOWED_DOMAINS`. AI variables use the existing `PARS_AI_*` names (the requested `AI_PROVIDER`/`AI_MODEL` names map to `PARS_AI_PROVIDER`/`PARS_AI_CHAT_MODEL`).

## Production notes

- Crawler must stay disabled until robots.txt and site terms are reviewed by the institution.
- pgvector extension must be installed/approved by the DBA; V16 places it in schema `pars`.
- Keep `AI_SOURCE_MANAGE` / `AI_DOCUMENT_SYNC` limited to ADMIN.
- Back up `pars` and `ai` schemas and the document storage directory.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `/nrb/**` returns 403 | Grant `AI_FOREX_READ` / `AI_SOURCE_READ` to the user |
| Admin sync returns 404 | Set `NRB_CRAWLER_ENABLED=true` and `PARS_AI_ENABLED=true`, then restart |
| Crawler indexes 0 documents | robots.txt unavailable/disallowed — check `ai.nrb_source_registry.error_details`; do not bypass |
| Ingestion errors "no extractable text" | Scanned PDF — OCR not enabled; document recorded in `ai.nrb_ingestion_errors` |
| "Tokens in a single document exceeds…" | Reduce `PARS_AI_CHUNK_SIZE` / raise local model context |
| Windows repackage fails | Stop the running backend (jar file locked) before `mvn package` |
| Rate value looks stale | Check the `date`, `publishedOn` and `fetchedAt` fields; historical data is never presented as current |
