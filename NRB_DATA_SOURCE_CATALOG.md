# NRB Data Source Catalog

All sources are seeded disabled in `V18__nrb_sources.sql` and enabled individually by an administrator. Only the Forex API is an officially documented API; all other entries are official public web pages discovered from the NRB site navigation.

| ID (seed) | Name | URL | Type | Category |
|---|---|---|---|---|
| eeee…e1 | NRB Homepage | https://www.nrb.org.np/ | HTML | GENERAL |
| eeee…e2 | NRB Circulars | https://www.nrb.org.np/category/circulars/?department=bfr | HTML | CIRCULARS |
| eeee…e3 | NRB FX Circulars | https://www.nrb.org.np/category/fxm-circulars/?department=fxm | HTML | FX_REGULATION |
| eeee…e4 | NRB Monetary Policy | https://www.nrb.org.np/category/monetary-policy/?department=ofg | HTML | MONETARY_POLICY |
| eeee…e5 | Statistics & Data Links | https://www.nrb.org.np/departments/statistics-data-links/ | HTML | STATISTICS |
| eeee…e6 | Economic Research | https://www.nrb.org.np/departments/red/ | HTML | REPORTS |
| eeee…e7 | NRB Forex API | https://www.nrb.org.np/api/forex/v1/ | API | FOREX |

Verified result: enabling `NRB Circulars` and syncing discovered and indexed **42 official documents** with **1,473 vector chunks**, idempotently (second sync produced 0 duplicates). Failures were recorded: 10 documents exceeded the embedding model's context, 4 scanned PDFs had no text layer (OCR not enabled).

Scope rule: no invented NRB endpoints. Policy/circular/statistics content comes only from controlled public crawling of the pages above and links discovered from them.
