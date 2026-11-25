# REPOSITORY_AUDIT.md

Phase 0 audit of `C:\Users\WELCOME\Downloads\pars-nrb` (Spring Boot 3.4.5 / Java 17 / Maven, React 19 frontend, PostgreSQL + pgvector). Audit performed 2026-11-20. No production or database changes were made in Phase 0.

## 1. Project snapshot

| Area | Count |
|---|---|
| Main Java files | 221 |
| Controllers | 27 (134 mapped methods) |
| Backend tests | 8 files / 37 `@Test` |
| Flyway migrations | V1–V19 |
| Frontend `src` files | 64 |
| Frontend tests | 2 files / 4 tests |
| Root markdown docs | 31 |

## 2. Backend architecture

Packages: `auth` (controller 4, jwt 1, model 15, repository 3, security 4, service 4), `common` (api 3, audit 2, context 2, exception 3, query 4, web 2), `config` (5), `deal` (controller 12, model 11 + 24 DTOs, repository 13, service 13), `mail` (6), `monitoring` (7), `rbac` (controller 2, model 4 + 4 DTOs, repository 3, service 3), `system` (2), `usermanagement` (controller 2, model 2 + 5 DTOs, repository 2, service 2), `ai` (25 classes), `nrb` (18 classes), `ParsApplication`.

## 3. Findings (selected, full register in SECURITY_RISK_REGISTER.md)

| ID | File / line | Finding | Severity | Status | Human approval needed |
|---|---|---|---|---|---|
| AUD-001 | `application-dev.yml:5` | Env typo `PARS_DB_PASSWOR` | High | VERIFIED_FIXED (2026-11-20) | No |
| AUD-002 | `SecurityConfig.java:110–118` | CORS fail-open wildcard + credentials | High | VERIFIED_FIXED | No |
| AUD-003 | `JwtService.java:21–30` | Short secrets zero-padded | High | VERIFIED_FIXED | No |
| AUD-004 | `JwtService.java:32–51` | No iss/aud/jti/nbf | Medium | VERIFIED_OPEN | No |
| AUD-005 | `application-dev.yml:14` | Public default JWT secret | High | VERIFIED_OPEN | No (config) |
| AUD-006 | `SecurityConfig.java:67–72` | No CSP / Permissions-Policy | Medium | VERIFIED_FIXED | No |
| AUD-007 | `AuthService.java:140–164` | Non-atomic refresh rotation | High | VERIFIED_FIXED | No |
| AUD-008 | `AuthService.java:356–362`, `LoginRateLimitFilter.java:60–66` | X-Forwarded-For trusted | Medium | VERIFIED_OPEN | Deployment topology |
| AUD-009 | Security auto-config | Unintended in-memory user | Medium | VERIFIED_OPEN | No |
| AUD-010 | `AiConversationRepository.java:88–90` | Citations not reloaded | Medium | VERIFIED_FIXED | No |
| AUD-011 | `RagAnswerService.java` | Quota racy, RPM unused, inconsistent accounting | Medium | VERIFIED_OPEN | No |
| AUD-012 | `AiCommonConfiguration.java` | Exact vector scan, dimension conflict | Medium | VERIFIED_PARTIAL | No |
| AUD-013 | `NrbUrlPolicy.java`, `NrbDocumentDownloader.java` | SSRF redirect/DNS-rebinding, whole-file buffering | High/Medium | VERIFIED_FIXED | No |
| AUD-014 | `NrbAdminController.java:65` | Internal error detail returned | Medium | VERIFIED_OPEN | No |
| AUD-015 | `NrbRagIngestionService.java` | External docs auto-published | Medium | BLOCKED_PENDING_APPROVAL | Yes |
| AUD-016 | `AuditLogService.java:32` | Bare `@Async` | Medium | VERIFIED_OPEN | No |
| AUD-017 | V14/V15/V16 migrations | Dimension conflict, silent no-op | High | VERIFIED_FIXED (validator + V19) | No |
| AUD-018 | Missing indexes | Medium | VERIFIED_FIXED (V19) | No |
| AUD-019 | `application-dev.yml:7` | validate-on-migrate false | Medium | VERIFIED_OPEN | No |
| AUD-020 | Build/CI | All builds skip tests; no CI | High | VERIFIED_OPEN | No |
| AUD-021 | `AppLayout.tsx:82` | No mobile nav | High | VERIFIED_FIXED (AppShell) | No |
| AUD-022 | Frontend global | No ErrorBoundary/dark/i18n/search | Medium | VERIFIED_PARTIAL | No |
| AUD-023 | `api.ts`, `Spinner2` | Duplicates | Low | VERIFIED_OPEN | No |
| AUD-024 | `index.html` | Mojibake title | Low | VERIFIED_FIXED | No |
| AUD-025/026/027 | Docker/deploy | Root container, placeholders, no TLS | Medium | VERIFIED_OPEN | Ops |
| AUD-028 | AI/NRB controllers | Hard-coded permission strings | Low | VERIFIED_OPEN | No |
| AUD-029 | AI providers | Hosted provider data governance | High | BLOCKED_PENDING_APPROVAL | Yes |
| AUD-030 | Deal workflow | No explicit transition matrix | High | BLOCKED_PENDING_APPROVAL | Yes |

## 4. Test & build baseline

- `mvn test`: **44/44 pass**.
- Frontend: `tsc` pass, `lint` pass, `test` 4/4, `build` pass.
- Flyway: V1–V19 applied; app boots health 200.

## 5. Human decisions required

1. S4 — hosted AI data governance.
2. S5 — external NRB document publication approval.
3. B1 — deal workflow transition rules and maker-checker policy.
4. Trusted-proxy topology and CORS allow-list per environment.

## 6. Conclusion

The application is functional and well-structured. Highest-value corrections in Phase 0 are CORS fail-closed, JWT secret validation, refresh atomicity, citation reload, SSRF hardening, dimension guard, mobile navigation and design system. None of the Phase 0 audit items changed production behavior.
