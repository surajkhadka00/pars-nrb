# SECURITY_RISK_REGISTER.md

## Risk register (selected high/medium)

| ID | Asset | Threat | Likelihood | Impact | Mitigation status |
|---|---|---|---|---|---|
| SEC-001 | JWT tokens | Weak secret / no issuer / audience | Medium | High | Partial — AUD-003 done; AUD-004 open |
| SEC-002 | Sessions | Refresh replay | Medium | High | Partial — rotation atomic; family revocation verified |
| SEC-003 | Authentication endpoint | Rate-limit bypass via XFF spoofing | Medium | Medium | Open (AUD-008) |
| SEC-004 | CORS | Cross-origin credentialed access | Low | High | Fixed (AUD-002) |
| SEC-005 | Browser XSS | Script injection in UI | Low | High | CSP added; no unsafe HTML found |
| SEC-006 | File upload | Malicious file / path traversal | Medium | High | Fixed validation + traversal guard |
| SEC-007 | NRB crawler | SSRF / DNS rebinding | Medium | High | Partial (redirects disabled, DNS still TOCTOU) |
| SEC-008 | AI retrieval | Unauthorized chunk in context | Low | Critical | Filter in SQL/vector query; tests needed |
| SEC-009 | AI generation | Prompt injection via documents | Medium | High | Delimited untrusted data; no tool calling |
| SEC-010 | Hosted provider | Data egress of business context | Low | High | Blocked pending approval (AUD-029) |
| SEC-011 | External docs | Unreviewed content authoritative | Medium | High | Blocked pending approval (AUD-015) |
| SEC-012 | Audit log | Unbounded async executor | Medium | Medium | Open (AUD-016) |
| SEC-013 | Secrets | Generated in-memory user | Low | Medium | Open (AUD-009) |
| SEC-014 | Container | Root runtime, no volumes | Low | Medium | Open (AUD-025/026) |
| SEC-015 | CI | Tests skipped | Medium | High | Open (AUD-020) |

## Security tests currently present

- `NrbUrlPolicyTest` (7): allow-list, credentials, private IPs, robots.
- `SecurityConfigCorsTest` (6): updated to assert fail-closed.
- `JwtServiceTest` (3): secret validation + signing.

## Missing security tests (must be added in later phases)

JWT forgery, algorithm confusion, refresh replay/concurrency, `@PreAuthorize` denial for each role, IDOR on conversations/documents/deals, malicious upload (MIME spoof, path traversal, decompression bomb, oversized), prompt injection corpus, unauthorized vector retrieval, cache isolation, rate-limit bypass, XSS, CSP header assertion, audit record creation, conversation ownership, share-link authorization.

## Approval required

S4 (hosted provider data governance), S5 (external document publication), B1 (deal workflow rules). See `PROJECT_AUDIT.md` §4.
