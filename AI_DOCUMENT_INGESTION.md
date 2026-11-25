# Document Ingestion — PARS NRB

## Flow

```
POST /api/v1/ai/documents (multipart)
   → validate extension/MIME/magic bytes/size/path
   → SHA-256 duplicate check
   → store original in private pars.ai.storage-path
   → insert ai.ai_documents (DRAFT) + ai.ai_ingestion_jobs (QUEUED)
   → return metadata immediately

Background @Scheduled worker (SKIP LOCKED):
   QUEUED → EXTRACTING → CHUNKING → EMBEDDING → INDEXING → PENDING_REVIEW
   failures → FAILED (retries up to max_attempts)

POST /{id}/publish → PUBLISHED + active_version = TRUE (atomic metadata update)
POST /{id}/archive → ARCHIVED, removed from retrieval
POST /{id}/reindex → new job; published version is only replaced when the new job is published
```

## Extraction

- PDF: Apache PDFBox 3 (`Loader.loadPDF`), page-by-page text with `[[PAGE n]]` markers. Encrypted PDFs are rejected; scanned PDFs with no text layer are rejected with an explicit error (OCR is a documented future extension).
- DOCX/XLSX/TXT/HTML: Apache Tika auto-detection.
- Empty documents, unsupported formats, malformed files and extraction errors are recorded as job failures with a reason.

## Chunking

Configurable `chunk-size-tokens` (default 600) and `chunk-overlap-tokens` (default 100); tokens are estimated as characters/4. Chunks respect page markers, paragraph boundaries and simple heading detection. Every chunk retains page, section and character offsets.

## Security controls

- Allow-listed extensions; Tika MIME check; magic-byte verification; 20 MB default limit.
- File names are replaced with UUID storage names; path traversal is rejected by containment checks.
- Uploaded files are never executed and are stored outside public/static resources.
- Malware scanning is a documented deployment hook (not enabled by default).
- Only `PENDING_REVIEW → PUBLISHED` documents enter the retrieval index.
