# TEST_STRATEGY.md

## Current baseline (verified)
- `mvn test`: 44/44 pass; frontend `tsc`, `lint`, `test` (4/4), `build` pass.
- All tests are plain JUnit 5 / Vitest with no Spring context, no DB, no HTTP integration.

## Required test matrix

| Layer | Framework | Scope |
|---|---|---|
| Backend unit | JUnit 5 + AssertJ | Services, chunking, routing, RRF, validation |
| Backend security | Spring Security Test + MockMvc | JWT, RBAC, refresh replay, IP resolution, CORS |
| Backend integration | `@SpringBootTest` + Testcontainers (PostgreSQL + pgvector) | Migrations, repositories, pgvector search, concurrency |
| AI/RAG | JUnit + mocked providers + Testcontainers | Retrieval filters, citations, abstention, quotas |
| Crawler | JUnit + `MockRestServiceServer` | SSRF, robots, discovery, download, idempotency |
| Frontend unit | Vitest + RTL | Components, forms, permission rendering |
| Frontend a11y | axe-core | Keyboard, focus, contrast, zoom |
| E2E | Playwright (to add) | Login, deal lifecycle, upload/publish, chat, kill switch |
| Performance | JMeter/Gatling (to add) | Concurrent chat, ingestion, list endpoints |

## Rules
- CI must run the full suite; no `-DskipTests` in the normal path.
- No test passes by disabling authorization or validation.
- Every finding fix must add a regression test.
- Unauthorized retrieval rate and citation fabrication must be zero on the evaluation set.
- Tests use synthetic data only; no production confidential information.

## Priority order for adding tests
1. JWT claims/secret/proxy tests (S1, S2).
2. CORS/header tests.
3. `@PreAuthorize` denial tests per role.
4. Refresh replay/concurrency tests.
5. File upload signature/path traversal tests.
6. Crawler SSRF/robots tests (already 7).
7. AI retrieval permission-leakage test.
8. Testcontainers migration + pgvector test.
9. Frontend component/a11y tests.
10. E2E critical journeys.
