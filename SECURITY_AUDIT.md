# SECURITY_AUDIT.md

Scope: file inspection + runtime log evidence on 2026-11-20. OWASP ASVS 5.0.0 used as a checklist; this is **not** a certification claim.

## Controls confirmed implemented

- JWT HMAC signing, 15-minute access tokens, refresh token rotation with previous-hash reuse detection.
- `@PreAuthorize` on 122 methods; all new AI/NRB endpoints permission-gated; ADMIN-only endpoints use `hasRole('ADMIN')`.
- CurrentUser derived from JWT/session in `JwtAuthenticationFilter` — never from browser input.
- Single-flight refresh in frontend; no second token store.
- Parameterized SQL everywhere; no user-supplied SQL/URL in AI chat.
- File validation: extension, MIME, magic bytes, size cap, path traversal guard; private storage outside web root.
- Crawler allow-list + robots.txt fail-closed + scheme/private-IP checks; no CAPTCHA/auth bypass.
- AI disabled by default; no mock provider fallback; evidence treated as untrusted data; no tool calling.
- No secrets committed; generic 500 without stack trace to client (except AUD-014-style message leakage noted below).

## High-priority gaps (full register in PROJECT_AUDIT.md)

| ID | Gap | Exploit scenario | Required test |
|---|---|---|---|
| AUD-002 | CORS wildcard + credentials | Malicious origin reflects and calls authenticated API | Cross-origin test with allowed/denied origins |
| AUD-003/005 | Weak dev secret zero-padded, public default | Forged JWT if dev config reused | Startup rejection + forged-token test |
| AUD-007 | Non-atomic refresh rotation | Concurrent refresh bypasses revocation | Two-thread replay test |
| AUD-013 | Redirects/DNS rebinding in crawler | SSRF to internal service | 302-to-localhost test |
| AUD-017 | Vector dimension mismatch | Index corruption or startup failure | Clean-DB migration test |
| AUD-020 | CI skips tests | Security regressions ship | CI gate requiring full suite |
| AUD-010 | Citations lost on reload | Audit trail incomplete | Conversation reload test |
| AUD-014 | `e.getMessage()` returned to client | Internal detail disclosure | Assert generic error contract |

## Headers

Present: `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`, HSTS. Missing: CSP, Permissions-Policy (AUD-006).

## Logging privacy

`ApiTimingFilter` redacts sensitive JSON keys before writing audit payloads; login logs the username (PII) at INFO; generated in-memory user password is printed at startup (AUD-009). No access tokens or provider keys in logs found.

## File access

Authorization on AI/NRB document download/preview must be re-verified against current role grants (requirement not fully implemented for per-document grants — see AUD-015 and document-level access policy; needs human review).

## Security tests currently present

`NrbUrlPolicyTest` (7 tests) — allow-list, credentials, private IPs, robots. `SecurityConfigCorsTest` (5) currently asserts the **unsafe** wildcard reflection and must be rewritten (AUD-002).

## Missing security tests

JWT forgery/expiry/algorithm confusion; refresh replay/concurrency; `@PreAuthorize` denial for every role; IDOR on conversations/documents/deals; upload malicious signatures/path traversal; prompt injection corpus; unauthorized vector retrieval; cache isolation after permission revocation; rate-limit bypass; XSS; CSP header assertion; audit record creation.

## Conclusion

The architecture has a sound identity/permission backbone, but the gaps above must be closed and tested before handling real banking data. No formal compliance claim is made.
