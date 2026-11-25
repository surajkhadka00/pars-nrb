# NRB Test Report

Executed 2026-11-19.

| Suite | Result |
|---|---|
| `mvn test` (backend) | **37/37 pass** — existing 19 + Forex client 6 + Forex service 3 + URL policy 7 + router 5 + chunking/RRF etc. |
| Frontend `tsc -b` | PASS |
| Frontend `npm run lint` | PASS |
| Frontend `npm run test` | PASS (1/1) |
| Flyway V17, V18 | Applied cleanly on PostgreSQL 18.6 |
| Live NRB Forex API | `GET /rates` verified with real 2026-11-16…18 data |
| `/nrb/forex/latest` | 22 currencies, real source URL and dates |
| `/nrb/forex` historical | 3 days × 22 rates with distinct per-date values |
| `/nrb/forex/{ISO3}` | USD buy/sell with unit |
| Admin sync | `savedDays: 2` then idempotent 0 |
| AI chat live Forex route | Real 2026-11-19 rates + official URL citation |
| Controlled crawler | 42 documents, 1,473 chunks indexed; second sync 0 duplicates |
| robots.txt/SSRF unit tests | 7 pass (domain restriction, credentials, private IP, robots rules) |
| Nepali Unicode | Language detection implemented; dedicated Testcontainers evaluation pending |

Not verified: full 100-question evaluation corpus, live provider keys, Docker deployment, production DBA approval of pgvector, OCR for scanned PDFs.

Failures encountered and fixed: DTO currency nesting, Mockito/Java 26 incompatibility (replaced with fakes), anchor-link `href="#"` crash, oversized embedding input (hard-split), jar lock on Windows repackage.
