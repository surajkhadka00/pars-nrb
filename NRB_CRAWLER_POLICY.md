# NRB Crawler Policy (proposal)

## Scope

Only the official `nrb.org.np` domain (and explicitly approved subdomains from configuration). Every candidate URL must resolve to an allowed host before any request; redirects are re-validated at each hop. No user-supplied URL is ever fetched.

## Rules

1. Fetch and honour `robots.txt` before any crawl; cache per sync run. If robots.txt is unavailable, skip crawling and record an error — do not assume permission.
2. Descriptive User-Agent identifying PARS NRB (configurable), e.g. `PARS-NRB-KnowledgeBot/1.0 (+contact: pars@nrb.gov.np)`.
3. Minimum delay between requests (`nrb.crawler.delay-ms`, default ≥ 2000 ms); bounded concurrency (default 1 worker).
4. Respect HTTP 403/429 and Retry-After; back off with bounded exponential retry (max 3 attempts) then stop that source.
5. Max crawl depth and max pages per source from configuration; canonical URL normalization; visited-set prevents loops/duplicates.
6. Only public HTML pages and approved document types (PDF, DOCX, XLSX, CSV, TXT). Max download size from config (default 20 MB), streaming with a hard cap.
7. Validate extension, MIME type and magic bytes; never execute downloaded content; malware scanning hook if available.
8. Track `ETag`/`Last-Modified` and SHA-256 content hash; skip unchanged documents; preserve original URL, retrieval timestamp and version.
9. Stop completely if the site blocks access — never bypass authentication, CAPTCHA or rate limits.
10. Store every skip/failure in `ai.nrb_ingestion_errors` with reason and source URL.

## Source categories (discovered from official navigation — not invented APIs)

Circulars, FX circulars/notices, monetary policy, guidelines/manuals, acts/rules, economic research reports, statistics (macro, BFI, payment, interest rates), public notices, annual reports.

## Nepali language

Preserve original UTF-8 text; no translation during ingestion; detect language for metadata only; never corrupt Nepali Unicode. Tests must include Devanagari text.
