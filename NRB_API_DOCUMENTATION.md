# NRB Integration API Documentation

Base: `/api/v1`. All endpoints require JWT. Responses use `ApiResponse<T>` and `ApiMeta`.

## Forex (permission `AI_FOREX_READ`)

| Method | Path | Description |
|---|---|---|
| GET | `/nrb/forex/latest` | Latest published NRB forex day (bounded 7-day lookback). Contains `date`, `publishedOn`, `modifiedOn`, `fetchedAt`, `sourceUrl`, `rates[]`. |
| GET | `/nrb/forex?from=YYYY-MM-DD&to=YYYY-MM-DD` | Historical range. Stored snapshots first; fetched from official API only when DB has no data. |
| GET | `/nrb/forex/{ISO3}?date=YYYY-MM-DD` | One currency on a date. |
| POST | `/admin/nrb/forex/sync?from=&to=` (`AI_SOURCE_MANAGE`) | Fetch and persist; returns `savedDays`. |

Rates include `iso3`, `name`, `unit`, `buy`, `sell`, `rawBuy`, `rawSell`. Values are indicative NRB rates, not commercial-bank transaction rates.

## Sources (permission `AI_SOURCE_READ`)

| Method | Path | Description |
|---|---|---|
| GET | `/nrb/sources` | Configured official sources with status, hashes, dates, errors. |
| GET | `/nrb/sources/{id}` | One source. |
| GET | `/nrb/documents?page&size` | Discovered/indexed official documents. |
| GET | `/nrb/documents/{id}` | One document. |
| GET | `/nrb/sources/status` | Aggregate sync health. |
| PATCH | `/nrb/sources/{id}?enabled=true|false` (`AI_SOURCE_MANAGE`) | Enable/disable a source. |

## Admin (requires `AI_DOCUMENT_SYNC`; active only when AI and crawler are enabled)

| Method | Path | Description |
|---|---|---|
| POST | `/admin/nrb/sources/{id}/sync` | Controlled discovery + download + index of one source. |
| POST | `/admin/nrb/sync` | All enabled HTML sources. |
| POST | `/admin/nrb/documents/{id}/reindex` | Re-download and re-index one document. |

## Chat routing (`POST /api/v1/ai/chat`)

`AiQueryRouter` now routes: `FOREX_LIVE` (today/current/latest rate), `FOREX_HISTORICAL` (rate on a date), `NRB_DOCUMENT` (circulars/policy/reports), plus existing `SELF`, `DEALS`, `APP_GUIDE`, `CAPABILITY`, `DOCUMENTS`. Every source response includes `sourceUrl` for clickable official citations.
