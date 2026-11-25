# CI/CD Setup Guide — PARS NRB (No AWS)

## Local Git setup (one time)

```powershell
cd C:\Users\WELCOME\Downloads\pars-nrb
git init
git branch -M main
git add .
git commit -m "Initial PARS NRB with AI and NRB integration"
```

Create a personal GitHub repository (e.g. `pars-nrb`) and push:

```powershell
git remote add origin https://github.com/<your-username>/pars-nrb.git
git push -u origin main
```

## What the CI workflow does

`.github/workflows/ci.yml` runs on push/PR to `main` and manual dispatch:

1. **backend** — `./mvnw -B -ntp clean verify` on Temurin 17, with Maven caching and surefire report upload on failure.
2. **frontend** — `npm ci`, `npm run lint`, `npx tsc -b`, `npm test`, `npm run build`.
3. **docker** — builds `pars-nrb:ci` with the Dockerfile and scans with Trivy.

`.github/workflows/codeql.yml` runs CodeQL on Java + JavaScript weekly and on push/PR.

`.github/dependabot.yml` checks Maven and npm weekly.

## Required GitHub configuration (names only)

Repository Settings → Secrets and variables → Actions:

| Type | Name | Purpose |
|---|---|---|
| Secret | `PARS_DB_PASSWORD` | PostgreSQL password (dev/compose) |
| Secret | `PARS_JWT_SECRET` | JWT signing secret (>=32 bytes) |
| Variable | `PARS_AI_ENABLED` | `true`/`false` |
| Variable | `PARS_AI_PROVIDER` | `ollama` or `openai` |
| Variable | `PARS_AI_CHAT_MODEL` | e.g. `llama3.2:3b` or `gpt-4o-mini` |
| Variable | `PARS_AI_EMBEDDING_MODEL` | e.g. `nomic-embed-text` or `text-embedding-3-small` |
| Variable | `PARS_AI_EMBEDDING_DIMENSIONS` | `768` for Ollama, `1536` for OpenAI |
| Variable | `NRB_CRAWLER_ENABLED` | `true`/`false` |
| Variable | `PARS_CORS_ORIGINS` | e.g. `http://localhost:5173` |

For hosted OpenAI only (do not commit): `PARS_AI_API_KEY` as a Secret — only after data-governance approval.

## Local verification commands (Windows)

```powershell
cd C:\Users\WELCOME\Downloads\pars-nrb
.\mvnw.cmd clean verify
cd frontend
npm.cmd ci
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

## Notes

- No AWS resources are created. Deployment is local/Docker-based; `docker-compose up -d` starts PostgreSQL (pgvector) + backend.
- The Maven wrapper pins Maven 3.9.9.
- `.dockerignore` excludes `target/`, `frontend/node_modules/`, logs and `.env`.
- Docker image runs as non-root user `1001` with a health check.
- No AWS deployment workflows are included (per your request).
