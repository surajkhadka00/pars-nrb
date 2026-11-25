# DEPENDENCY_COMPATIBILITY.md

## Verified baseline

- Java 17 target; Spring Boot 3.4.5 (parent); Spring Framework 6.2.6; Jackson 2.18.3; Reactor 3.7.5.
- Spring AI pinned **1.0.0** via BOM. Verified from Maven Central POMs: 1.0.0 declares Boot 3.4.5; 1.0.9 declares Boot 3.5.15; 1.1.x targets 3.5.x; 2.x targets Boot 4.x. **Do not upgrade** without documented approval.
- Spring AI modules in use: openai, ollama, client-chat, pgvector-store, pdf/tika/jsoup document readers, spring-ai-test.
- Other pins: jjwt 0.12.6, mapstruct 1.6.3, springdoc 2.8.6, bucket4j 8.14.0, lombok 1.18.38, POI 5.4.1, Testcontainers 1.20.6 (declared but unused).
- Outbound HTTP uses Spring `RestClient` from `spring-web` — no extra dependency needed.

## Compatibility rules for future work

1. Any new dependency must be checked against Boot 3.4.5's managed versions (`mvn dependency:tree`, `help:effective-pom`).
2. Add `Maven Enforcer` rules to fail on unapproved version drift (AUD-001 area).
3. Testcontainers must be actually used before relying on DB integration claims.
4. npm side: React 19/Vite 6/Tailwind 4/Radix versions are mutually consistent; the `cors` npm package is unnecessary in a browser bundle and should be removed after verifying no import.
5. SSE streaming can use Spring MVC `SseEmitter` (spring-webmvc already present) — no WebFlux dependency required.
6. OCR, if added, must be evaluated for Java 17 compatibility and license before inclusion; current scanned PDFs are rejected with a recorded error.

## Dependency risks

| Risk | Severity | Mitigation |
|---|---|---|
| Spring AI upgrade by convenience | H | Enforcer rule + this document |
| Large transitive trees (Tika/POI/JSoup) | M | Review CVEs with dependency-check plugin |
| Testcontainers declared but unused | M | Add real DB tests or remove |
| No dependency scanning in CI | H | OWASP Dependency-Check in pipeline |
| Frontend `cors` package | L | Remove |

## Verification performed

`mvn dependency:tree` showed Boot 3.4.5, Spring AI 1.0.0, Jackson 2.18.3, Reactor 3.7.5 with no conflicting versions. Frontend `npm run build`, `lint`, `test` all pass.
