# PROJECT_AUDIT.md — PARS NRB Phase 0

Audit date: 2026-11-20. Method: direct file inspection of `C:\Users\WELCOME\Downloads\pars-nrb`. No production or database changes made in Phase 0.

## 1. Scope snapshot

| Area | Measured |
|---|---|
| Main Java files | 221 |
| Controllers | 27 (134 mapped methods) |
| Backend tests | 8 files / 37 `@Test` |
| Flyway migrations | V1–V18 |
| Frontend `src` files | 64 |
| Frontend tests | 1 file / 1 test |
| Root markdown docs | 25 |

## 2. Findings register

Severity: **C**ritical, **H**igh, **M**edium, **L**ow. Status: **C**onfirmed by file/runtime evidence, **R** needs human review.

| ID | File / line | Current behavior | Risk / severity | Why it matters | Recommended correction | Dependencies | Verification test | Status |
|---|---|---|---|---|---|---|---|---|
| AUD-001 | `application-dev.yml:5` | `PARS_DB_PASSWOR` typo; default password `123456` | H | Env override silently ignored; weak dev credential | Rename to `PARS_DB_PASSWORD`; remove default in prod-like profiles; document rotation | None | Assert binding with `PARS_DB_PASSWORD` env | C |
| AUD-002 | `SecurityConfig.java:113–117` | Empty CORS list → `allowedOriginPatterns("*")` with `allowCredentials(true)` | H | Any origin is reflected; credentialed cross-origin access risk | Fail closed: empty list = no CORS; explicit allow-list per profile; keep dev Vite origin explicit | Frontend origin config | `SecurityConfigCorsTest` rewritten to assert fail-closed | C |
| AUD-003 | `JwtService.java:23–29` | Short secrets zero-padded to 32 bytes; accepted silently | H | Weak HMAC key weakens token integrity | Reject secrets <32 bytes at startup with clear error | Secret management | Unit test asserts startup failure on short secret | C |
| AUD-004 | `JwtService.java:32–51` | No `iss`, `aud`, `jti`, `nbf`; only signature+expiry validated | M | No audience isolation or replay protection | Add issuer/audience claims and validation; consider `jti` for chat idempotency | Token contract change; frontend refresh | JWT unit tests | C |
| AUD-005 | `application-dev.yml:14` | Public default JWT secret | H | Anyone can forge tokens if dev config is reused | Require env secret in all non-local profiles | Deployment config | Config test | C |
| AUD-006 | `SecurityConfig.java:67–72` | No Content-Security-Policy or Permissions-Policy | M | XSS/clickjacking defense-in-depth absent | Add CSP (strict, self-only, no unsafe-inline), Permissions-Policy | Frontend asset hashes/nonce | Header integration test | C |
| AUD-007 | `AuthService.java:140–164` | Refresh check-then-write not atomic; only one prior token generation | H | Concurrent refresh or replay of older token can evade family revocation | Atomic conditional update; store hash chain with bounded history | DB migration | Concurrency + replay test | C |
| AUD-008 | `AuthService.java:356–362`, `LoginRateLimitFilter.java:60–66` | `X-Forwarded-For` trusted unconditionally | M | Rate-limit/session IP spoofing | Trusted-proxy list; use `RemoteAddr` unless proxy configured | Deployment topology | Filter unit test | C |
| AUD-009 | No `UserDetailsService` bean | Boot auto-creates in-memory `user` with generated password printed at startup | M | Credential noise; potential attack surface if basic auth enabled later | Exclude `UserDetailsServiceAutoConfiguration` or define explicit no-op provider | None | Startup log assertion | C |
| AUD-010 | `AiConversationRepository.java:88–90` | Persisted `sources` JSON not reloaded on conversation open | M | Citations lost after reload — trust/audit gap | Parse JSONB into `AiSourceResponse` list | Frontend type already supports | Repository test | C |
| AUD-011 | `RagAnswerService.java` | RPM config unused; quota check-then-insert racy; usage not recorded on capability/forex routes | M | Quotas bypassable; inconsistent cost accounting | Atomic quota reservation; central usage recording for all routes | DB constraints | Concurrency test | C |
| AUD-012 | `AiCommonConfiguration.java` | `indexType NONE` (exact scan), dimension default 1536 vs local 768 | M | Recall/latency at scale; dimension mismatch can break indexing | Benchmark HNSW with permission filters; single source of truth for dimensions + migration guard | DBA review | pgvector integration test | C |
| AUD-013 | `NrbUrlPolicy.java` / `NrbWebsiteCrawler.java` | DNS checked once, redirects followed without re-validation | H | SSRF bypass via redirect or DNS rebinding | Disable redirects and re-validate each hop; resolve+connect via pinned address | HTTP client config | Redirect-to-internal test | C |
| AUD-014 | `NrbDocumentDownloader.java:52–59` | Whole body buffered before size check | M | OOM on large/malicious file | Stream with cap + `Content-Length` pre-check | None | Oversized-stream test | C |
| AUD-015 | `NrbRagIngestionService.java` | NRB docs auto-published without human review | M | Unreviewed external content becomes authoritative | Add review state for external docs; require approval like uploads | Workflow design | Publication test | R |
| AUD-016 | `AuditLogService.java:32` | Bare `@Async` → unbounded executor | M | Thread exhaustion under load | Qualify named bounded executor | None | Load test | C |
| AUD-017 | V14 vs V15/V16 | `vector(1536)` vs `vector(768)` `IF NOT EXISTS` | H | Dimension mismatch; silent no-op migration | Single canonical dimension migration + startup validation + reindex | DBA | Migration test on clean DB | C |
| AUD-018 | V18 | Missing indexes on FK/join columns (`nrb_forex_rates.snapshot_id`, etc.) | L/M | Slow joins as data grows | Add indexes in V19 | None | `EXPLAIN ANALYZE` | C |
| AUD-019 | `application-dev.yml:7` | `validate-on-migrate:false` | M | Schema drift undetected | Enable validation in all profiles | None | Startup with drifted schema | C |
| AUD-020 | Build scripts / CI | All builds use `-DskipTests`; no GitHub Actions workflow | H | Regressions ship unnoticed | CI runs full test suite; deployment gates | CI platform | Pipeline run | C |
| AUD-021 | Frontend `AppLayout.tsx:82` | Sidebar hidden on mobile with no drawer | H | No navigation below `md` | Mobile drawer + focus management | Design system phase | 320px viewport test | C |
| AUD-022 | Frontend global | No ErrorBoundary, dark mode, i18n, global search, command menu | M | UX/accessibility/robustness gaps vs target | Implement in design-system phases | Multiple | Component/a11y tests | C |
| AUD-023 | Frontend `api.ts` + `api/client.ts`, `Spinner`/`Spinner2` | Duplicate clients/components | L | Drift risk | Remove legacy duplicates after verifying no imports | None | Grep/build | C |
| AUD-024 | `index.html` | Mojibake in title (`PARS A?...`) | L | Branded UX defect | Fix UTF-8 title | None | Visual check | C |
| AUD-025 | Dockerfile | Runs as root; no healthcheck; no storage volumes | M | Container hardening gap | Non-root user, HEALTHCHECK, volumes | Infra | Container test | C |
| AUD-026 | `docker-compose.yml` | DB port 5432 published; no TLS/proxy/backups | M | Dev-only posture; not production ready | Internalize DB, add reverse proxy/TLS, backups | Infra | Deployment test | C |
| AUD-027 | `deploy/*` | Placeholder paths `[path]`, `xxx.jar` | M | Not deployable as-is | Parameterize with reviewed values | Ops | Staging deploy | C |
| AUD-028 | AI/NRB controllers | Hard-coded authority strings instead of `PermissionCodes` | L | Drift risk | Use constants | None | Grep | C |
| AUD-029 | `AiPromptService` / providers | External OpenAI path can receive business context | R/H | Data-governance exposure | Approved provider policy, redaction, local-only default | Governance decision | DLP review | R |
| AUD-030 | Deal workflow | No explicit transition matrix in code | R/H | Maker-checker/approval integrity unverified | Extract from code + obtain business sign-off | Banking owner | Workflow tests | R |

## 3. Positive controls confirmed

- Every business response uses DTO records inside `ApiResponse<T>`; no raw entities returned.
- `@PreAuthorize` present on 122 methods; ADMIN-only endpoints use `hasRole('ADMIN')`.
- `@EntityGraph` used on main repositories to mitigate N+1; `@Version` optimistic locking present and mapped in exception handler.
- Pagination clamped in `Pageables` (`size ≤ 100`) for JPA endpoints; Flyway `ddl-auto: none`; all migrations additive with `IF NOT EXISTS`.
- JWT refresh rotation with prior-token reuse detection exists (see AUD-007 for atomicity gap).
- AI enabled by default **false**; crawler enabled by default **false**; no mock AI fallback in production.
- No `dangerouslySetInnerHTML`, no sensitive `console.log`, no user-supplied SQL or URL fetching in chat.

## 4. Human decisions required

1. Banking workflow rules for deal submission/verification/approval/rejection (AUD-030).
2. Data governance for any hosted AI provider (AUD-029).
3. Whether unreviewed external NRB documents may be auto-published (AUD-015).
4. Production topology (trusted proxies, TLS, secret store) before header/CORS hardening (AUD-002, AUD-006, AUD-008).

## 5. Phase 0 conclusion

The application is functional and well-structured for its size, but not yet banking-grade: the highest-value corrections are CORS fail-closed, JWT secret validation and claims, atomic refresh rotation, SSRF redirect hardening, dimension/index consistency, CI with tests, and mobile navigation. None of these were changed in Phase 0.
