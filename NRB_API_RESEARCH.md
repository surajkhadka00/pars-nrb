# NRB API Research (verified 2026-11-18)

## Official documentation

- Site: `https://www.nrb.org.np/`
- API docs: `https://www.nrb.org.np/api-docs-v1/` (retrieved successfully)
- Base URL: `https://www.nrb.org.np/api/forex/v1/`
- Only documented endpoint: `GET /rates` with required params `page`, `per_page` (max 100), `from`, `to` (YYYY-MM-DD).

Documentation response fields: `status.code` (200/400), `errors.validation`, `params`, `data.payload[]` (`date`, `published_on`, `modified_on`, `rates[]`), `rates[].currency.{unit,name,ISO3}`, `buy`, `sell`, `pagination`.

## Live verification (actual response)

Requested `?page=1&per_page=3&from=2026-11-16&to=2026-11-18` → HTTP 200.

Observed differences from the prose documentation — implement against the **actual** JSON:

- `data.payload` is an array of **day objects**, each containing `rates[]`.
- The currency code field is lowercase **`iso3`**, not `ISO3` as written in the docs. Handle both defensively.
- `buy`/`sell` are **JSON strings** (e.g. `"149.26"`), not numbers. Parse to `BigDecimal`, never to double.
- `unit` is an integer (e.g. 100 for INR/JPY/KRW). Rate is per `unit` units of currency.
- `params` contains extra keys (`date`, `post_type`, `slug`, `q`) beyond the four documented.
- `pagination`: `{page, pages, per_page, total, links:{prev,next}}`.
- `published_on`/`modified_on` are `YYYY-MM-DD HH:mm:ss` strings in Nepal time (no timezone suffix) — parse with `Asia/Kathmandu`.
- Sample verified values: 2026-11-18 USD buy 149.26 / sell 149.86; INR per 100 = 160.00/160.15; JPY per 10.
- Error shape for bad params: `status.code=400` with `errors.validation` map.

## Domain / discovery findings (from the official site navigation, not invented)

Confirmed official pages/categories reachable from the homepage:
- `https://www.nrb.org.np/category/circulars/?department=bfr`
- `https://www.nrb.org.np/category/fxm-circulars/?department=fxm`
- `https://www.nrb.org.np/departments/statistics-data-links/`
- `https://www.nrb.org.np/departments/red/`
- `https://www.nrb.org.np/category/monetary-policy/?department=ofg`
- `https://www.nrb.org.np/category/notices/?department=ofg`
- `https://www.nrb.org.np/cmfm_rates/policy_rates`
- `https://www.nrb.org.np/forex/`
- `https://www.nrb.org.np/sitemap_index.xml`

These are candidate **configured sources**, not hardcoded crawler paths.

## robots.txt

Could not be retrieved from this environment (transport error for `https://www.nrb.org.np/robots.txt`). The crawler must fetch and honour robots.txt at runtime on every sync; if it is unavailable or disallows a path, that source must be skipped with a recorded error. Never bypass.

## Dependency compatibility

- No new Maven dependency required: `RestClient` ships with Spring Framework 6.2 / Boot 3.4.5 via `spring-boot-starter-web`; Jackson already present.
- Spring AI remains pinned at 1.0.0. Testcontainers 1.20.6 already present for pgvector integration tests.
- Optional local cache: implement a simple TTL map first; introduce Caffeine only with documented approval.
