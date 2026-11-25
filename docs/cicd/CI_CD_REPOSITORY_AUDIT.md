# CI/CD Repository Audit — PARS NRB

Date: 2026-11-25. Phase 1 only — no code or configuration changes made.

## 1. Repository & Git state

- `.git` directory: **absent** — this is not currently a Git repository. The `.github/` folder contains only a `modernize/java-upgrade` hook helper (`recordToolUse.ps1`/`.sh`); **no GitHub Actions workflows exist**.
- Branch: unknown (no Git history). Before writing workflow branch filters, you must run `git init`, commit, and push to a remote, or connect an existing remote.
- `.gitignore`: present and ignores `target/`, `frontend/node_modules/`, `frontend/dist/`, IDE files, `*.log`, `.env`, Maven wrapper jar.
- `.dockerignore`: **absent** — Docker builds will send the entire context (including `target/`, `frontend/node_modules` if present) to the daemon.
- Tracked secrets: none (no Git tracking exists at all). `.env` is gitignored. `.env.example` exists with placeholder values. `application-dev.yml` contains a weak default DB password (`123456`) and a public JWT secret default — acceptable only for local dev.

## 2. Backend build & test

- `pom.xml`: parent `spring-boot-starter-parent:3.4.5`, Java 17 target, finalName `nrb-pars`.
- No Maven wrapper (`mvnw`/`mvnw.cmd`/`.mvn/wrapper` absent). CI must use `setup-java` + runner-installed Maven (or add a wrapper as a first change).
- Build: `mvn clean package`. Test: `mvn test` (44 tests currently pass). All real runs in this project used `-DskipTests` only for packaging speed.
- Flyway: 19 migrations (V1–V19), `locations=classpath:db/migration`, `enabled=true`, `default_schema=pars`, `ddl-auto=none`.
- Health: `/actuator/health` (200 when up), `/actuator/health/ai` (DISABLED/CONFIGURED).
- Required env: `PARS_DB_URL`, `PARS_DB_USER`, `PARS_DB_PASSWORD`, `PARS_JWT_SECRET`, `PARS_CORS_ORIGINS`, `PARS_PORT`, plus `PARS_AI_*`, `PARS_DEAL_ATTACHMENT_DIR`, `NRB_*`.
- Note: dev profile has env typo `PARS_DB_PASSWOR` (already flagged; fix pending).

## 3. Frontend build & test

- `frontend/package.json`: scripts `dev`, `build` (`tsc -b && vite build`), `preview`, `lint` (eslint .), `test` (vitest run), `test:coverage`.
- `frontend/package-lock.json` present → use `npm ci`.
- Vite 6 proxies `/api` → `http://localhost:8080`; `VITE_API_BASE_URL` empty by default.
- Vitest: jsdom, setup file `src/tests/setup.ts`, 4 tests currently passing.
- Node: lockfile compatible with Node 20 LTS (recommended).

## 4. Containerization

- `Dockerfile`: multi-stage, `maven:3.9-eclipse-temurin-17` → `eclipse-temurin:17-jre`, `mvn -q -B dependency:go-offline`, `mvn -q -B clean package -DskipTests`, copies `target/nrb-pars.jar` → `/app/app.jar`, EXPOSE 8080, `ENTRYPOINT java -jar`.
- `docker-compose.yml`: `db` (pgvector/pgvector:pg17, port 5432, volume `pars_pgdata`), `backend` (build, port 8080, env for DB/AI/CORS). No frontend/nginx service, no TLS, no healthcheck, no storage volume, no pgvector extension init in compose (relies on Flyway at runtime — works because V16 conditionally creates the extension).
- No `.dockerignore`.

## 5. Database

- PostgreSQL 18 locally (Flyway warns it is newer than tested); production target should be pinned.
- pgvector extension installed in schema `pars`; AI tables in schema `ai`.
- No backup/restore scripts or restore rehearsal evidence.

## 6. Health & monitoring

- Actuator endpoints: `health,info,metrics,prometheus` (base); `show-details: when_authorized`.
- No readiness/liveness split documented; no centralized logging; no alerting.

## 7. Risks and gaps

1. No Git history and no CI workflows — cannot do pull-request gating or automated verification yet.
2. All build paths skip tests; no dependency/security scanning.
3. No `.dockerignore`; Docker builds include unnecessary context.
4. No frontend service or TLS in compose.
5. No backup/restore documentation or rehearsal.
6. No healthcheck in Dockerfile.
7. No secret management documentation for GitHub Actions / AWS.

## 8. Proposed first safe step (Phase 2 — CI, no code changes yet)

Before writing any workflow, you must:
- Initialize Git and create the first commit on the primary branch (name it explicitly, e.g. `main`), or connect an existing remote.
- Decide the primary branch name (GitHub Actions branch filters depend on it).
- Confirm whether you want me to add a Maven wrapper (`mvnw`) — recommended for reproducible CI.

Once you confirm the branch name and wrapper decision, I will create:
- `.github/workflows/ci.yml` (backend `mvn clean verify`, frontend `npm ci` + lint + typecheck + test + build, Docker build)
- `.gitignore` review (already present)
- `.dockerignore`

No production deployment, AWS resources, or database changes in Phase 2.

## Questions (only where repo inspection cannot answer)

1. What should the primary Git branch be called? (GitHub Actions filters need an exact name.)
2. Do you want me to add a Maven wrapper for reproducible builds?
3. Is GitHub Actions on your own repository, or an organization repository (affects OIDC/AWS later)?
4. Which AWS region should I plan for (cost/architecture estimate only, no resources created yet)?
5. Do you have a domain name available for the frontend (Route 53/ACM), or should I use the CloudFront default domain?

I will not create any files until you answer these and confirm Phase 2.
