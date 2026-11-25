# Docker Hardening — PARS NRB

## Dockerfile review

- Multi-stage build: `maven:3.9-eclipse-temurin-17` → `eclipse-temurin:17-jre`.
- `mvn dependency:go-offline` before source copy for layer caching.
- `mvn clean package -DskipTests` builds `target/nrb-pars.jar`.
- Runtime stage runs as user `1001` (non-root), owns `/app` and `/data`.
- Volumes: `/data/attachments`, `/data/ai`.
- Healthcheck: `wget -qO- http://127.0.0.1:8080/actuator/health` every 30s, 5s timeout, 60s start period, 3 retries.
- JVM: `-XX:MaxRAMPercentage=75.0` (respects container memory limits).
- EXPOSE 8080.

## Hardening checklist

| Control | Implementation | Status |
|---|---|---|
| Non-root runtime | `USER 1001` | ✅ |
| Minimal base image | `eclipse-temurin:17-jre` (no JDK tools) | ✅ |
| Multi-stage | builder discarded, only jar copied | ✅ |
| Health check | `/actuator/health` | ✅ |
| Read-only FS | Not yet enabled (`/data` writable) — enable with `read_only: true` + tmpfs in compose after verification | ⚠️ |
| Resource limits | Set via `deploy.resources.limits` in compose override or orchestrator | ⚠️ |
| No secrets in layers | Secrets passed via env at runtime | ✅ |
| Graceful shutdown | Spring Boot default (30s) | ✅ |
| TLS/HTTPS | Provided by reverse proxy (see LOCAL_DEPLOYMENT.md) | ⚠️ |

## Local verification (requires Docker Desktop)

```powershell
cd C:\Users\WELCOME\Downloads\pars-nrb
docker build -t pars-nrb:local .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev -e PARS_DB_PASSWORD=change-me pars-nrb:local
```

Then `curl.exe http://localhost:8080/actuator/health` should return `{"status":"UP"}`.

## Compose (local dev)

```powershell
docker compose up -d
docker compose logs -f backend
docker compose ps
docker compose down
```

The `db` service uses `pgvector/pgvector:pg17` (pgvector extension available). The backend depends on `db` being healthy.
