# AI Deployment Guide — PARS NRB

## Prerequisites

1. Java 17+ runtime (project compiles with Java 17 target; verified on Java 26).
2. PostgreSQL with `vector`, `hstore`, `uuid-ossp` available. `CREATE EXTENSION` rights required once during installation (DBA task).
3. Approved model provider credentials and data-governance sign-off for external transfer, OR a local compatible provider.

## Build

```powershell
cd C:\Users\WELCOME\Downloads\pars-nrb
mvn clean package
cd frontend
npm install
npm run build
```

## Dev run (Windows)

```powershell
# Terminal 1: backend
$env:PARS_AI_ENABLED='false'   # keep off until configured
java -jar target\nrb-pars.jar

# Terminal 2: frontend
cd frontend
npm run dev
```

## Production run

- Use `SPRING_PROFILES_ACTIVE=prod`.
- Supply `PARS_DB_URL`, `PARS_DB_USER`, `PARS_DB_PASSWORD`, `PARS_JWT_SECRET`, `PARS_CORS_ORIGINS`.
- Supply AI variables only after approval; set `PARS_AI_ENABLED=true`.
- Run Flyway migration V14 during a reviewed maintenance window; back up `pars` and `ai` schemas first.
- Never enable wildcard credentialed CORS.
- Restrict AI permissions deliberately (ADMIN receives them by default in V14).

## Docker

`Dockerfile` builds `target/nrb-pars.jar` into a Java 17 runtime image. `docker-compose.yml` starts the app plus a `pgvector/pgvector` PostgreSQL for development.

## Health and monitoring

- `GET /actuator/health` and `/actuator/health/ai`.
- Prometheus metrics at `/actuator/prometheus` include `pars.ai.*` counters.
- No prompts, document text or tokens appear in metric labels.

## Backups

- `pars` schema: existing backup policy.
- `ai` schema: include in the same backup (metadata + retrieval logs).
- Private document storage: back up `PARS_AI_STORAGE_DIR` with the same retention policy as deal attachments.
- Restoring `ai.vector_store` requires the same embedding dimensions/model version recorded in `ai_documents`.

## Rollback

Set `PARS_AI_ENABLED=false` and restart: all AI beans and endpoints disappear, the rest of PARS works unchanged. Flyway V14 tables remain but are unused. Do not delete V14 from history.
