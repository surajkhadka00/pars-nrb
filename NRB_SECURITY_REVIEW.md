# NRB Security Review

| Risk | Control | Status |
|---|---|---|
| SSRF / arbitrary URL fetch | `NrbUrlPolicy` allow-list, scheme check, no credentials, private/loopback IP rejection, redirect re-validation; browser never supplies a URL | Implemented + 6 unit tests |
| robots.txt / site terms | Fetched before crawling; unavailable ⇒ skip; disallow paths honoured; crawl-delay respected | Implemented |
| Crawl abuse | Bounded depth/pages, delay, single-threaded, descriptive User-Agent, no CAPTCHA/auth bypass | Implemented |
| Malicious files | Extension + MIME + magic-byte + size validation; no execution | Implemented |
| Prompt injection from web/PDF | Evidence delimited as untrusted data; system prompt forbids instruction override; no tools | Implemented |
| Data privacy | No secrets in config; secrets env-only; no prompt text in metric labels | Implemented |
| Authorization | `AI_FOREX_READ`, `AI_SOURCE_READ`, `AI_SOURCE_MANAGE`, `AI_DOCUMENT_SYNC` seeded ADMIN-only by V17; `@PreAuthorize` on all new endpoints | Implemented |
| Conversation ownership | Existing owner checks in `AiChatService` / repositories unchanged | Preserved |
| SQL access | All AI data access via parameterized JDBC/JPA; model has no SQL tool | Implemented |
| Unauthorized retrieval | Retrieval filters applied in SQL/vector query before context construction | Implemented |

Known limitations: OCR for scanned PDFs is not enabled (4 failures recorded). Some very large spreadsheets still exceed the embedding model context (10 failures recorded) — they are recorded in `ai.nrb_ingestion_errors`, never silently skipped. Production should review the third-party Windows pgvector binary and obtain data-governance approval before any external provider use; the current provider is local Ollama.
