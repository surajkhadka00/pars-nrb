# NRB Ingestion Pipeline

1. Administrator enables a source (`PATCH /nrb/sources/{id}?enabled=true`).
2. `POST /admin/nrb/sources/{id}/sync` starts a job row in `ai.nrb_source_sync_jobs`.
3. `NrbWebsiteCrawler` fetches `robots.txt` first. If unavailable or disallowing a path, nothing is crawled — never bypassed. Bounded depth/pages, crawl delay, allow-list, redirects re-validated.
4. Discovered document links are classified by extension; off-domain links ignored.
5. `NrbDocumentDownloader` validates allow-list, robots, size cap, extension, MIME and magic bytes. No execution of downloaded files.
6. `NrbDocumentMetadataService` extracts title, document number, department, fiscal year, language (Nepali Unicode preserved, no translation), publication date.
7. `DocumentExtractionService` extracts text with page markers (PDFBox) and Office/HTML/Tika. Oversized single paragraphs are hard-split by `ChunkingService`.
8. SHA-256 hash check makes ingestion idempotent — unchanged documents are skipped.
9. Chunks are embedded with `nomic-embed-text` (768 dimensions) and written ONLY to `ai.vector_store`, with metadata: `sourceUrl`, `docId`, `title`, `version`, `page`, `section`, `status=PUBLISHED`, `active=true`, `access=PUBLIC`.
10. Document metadata and errors are stored in `ai.nrb_documents` / `ai.nrb_ingestion_errors`.

Forex ingestion is separate: `NrbForexApiClient` → `NrbForexService` → `ai.nrb_forex_snapshots` / `ai.nrb_forex_rates`, with content hash dedupe and scheduled/manual sync.
