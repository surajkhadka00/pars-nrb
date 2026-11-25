# Local Deployment, Backup and Recovery — PARS NRB

## Run locally (Windows, no Docker)

```powershell
cd C:\Users\WELCOME\Downloads\pars-nrb
.\mvnw.cmd clean package -DskipTests
java -jar target\nrb-pars.jar

# Frontend
cd frontend
npm.cmd run dev
```

Open http://localhost:5173.

## Run with Docker Compose

```powershell
cd C:\Users\WELCOME\Downloads\pars-nrb
docker compose up -d
docker compose ps
docker compose logs -f backend
```

## Environment variables

See `.env.example` and `AI_ENVIRONMENT_VARIABLES.md` / `NRB_API_RESEARCH.md`. Never commit real values. Use Windows environment variables, IntelliJ run config, or a secret store.

## TLS / reverse proxy (recommended for production)

Use nginx or IIS with a TLS certificate from your institution's CA or Let's Encrypt. Terminate TLS at the proxy and forward to the backend on 8080. Set `PARS_CORS_ORIGINS` to the public origin and disable Swagger in prod (`springdoc.api-docs.enabled=false` already in prod profile).

## Health checks

- `GET /actuator/health` (200 when UP)
- `GET /actuator/health/ai` (DISABLED/CONFIGURED/UP)
- Container healthcheck uses `/actuator/health`.

## Backup

- Database: `pg_dump -Fc -h localhost -U postgres nrb_pars > nrb_pars.dump` (include both `pars` and `ai` schemas).
- Private storage: back up `PARS_DEAL_ATTACHMENT_DIR` and `PARS_AI_STORAGE_DIR` with the same retention policy as deal attachments.
- `ai.vector_store` requires the same embedding model and dimensions to be restored.

## Restore rehearsal (must be tested before production)

1. Create a separate restore database (never overwrite production).
2. `pg_restore -d nrb_pars_restore nrb_pars.dump`.
3. Run Flyway validation: `mvn flyway:validate` (or start the app against the restored DB with `validate-on-migrate=true`).
4. Verify counts in `pars.ad_user`, `p_deal`, `ai.ai_documents`, `ai.vector_store`.
5. Start the app against the restored DB on a non-production port and confirm `/actuator/health` is UP and login works.

## Rollback

- Application: redeploy the previous jar/container image.
- Database: Flyway has no automatic undo — prefer forward-fix migrations and documented restore. Never delete applied migration rows from `flyway_schema_history`.
- AI kill switch: set `ad_sys_config.ai.kill.switch=ON` (or `PARS_AI_ENABLED=false`) and restart; core PARS keeps working.

## AI kill switch verification

1. `POST /api/v1/ai/admin/kill-switch?disabled=true&reason=...` as ADMIN.
2. `GET /api/v1/ai/admin/kill-switch` → `{disabled:true}`.
3. `POST /api/v1/ai/chat` → 403.
4. `POST .../kill-switch?disabled=false&reason=...` → chat returns 200 again.
5. Confirm the change is in `ad_audit_log`.

## Monitoring

- Prometheus metrics at `/actuator/prometheus` include `pars.ai.*` counters and `pars.api.requests`.
- Logs include `traceId`, `userId`, `sessionId` in MDC.
- No secrets, tokens, prompts or document text in metric labels.

## Known limitations

- No CI workflow is enforced until the repo is pushed to GitHub.
- No Testcontainers integration tests yet.
- No automated accessibility or E2E tests.
- Container runs as non-root but read-only filesystem is not yet enabled.
- Backup/restore not yet rehearsed.
