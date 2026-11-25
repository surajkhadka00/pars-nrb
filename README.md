# PARS NRB — Spring Boot + React + AI/RAG

Portfolio Analysis and Reporting System for Nepal Rastra Bank.

This repository now contains the original Spring Boot application (unchanged business modules), the React frontend, and an additive AI/RAG module that is **disabled by default**.

## Modules

- `src/main/java/np/org/nrb/pars/**` — existing PARS application.
- `src/main/java/np/org/nrb/pars/ai/**` — AI/RAG module (additive).
- `frontend/` — React 19 + TypeScript + Vite application incl. AI chat/document/admin pages.
- `src/main/resources/db/migration/V14__ai_schema.sql` — new AI schema + permissions.

## Run locally (Windows)

```powershell
# Backend (from project root)
mvn clean package
java -jar target\nrb-pars.jar

# Frontend (new terminal)
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. The Vite proxy forwards `/api` to `http://localhost:8080`.

## FREE local AI (no key, no external data transfer)

```powershell
cd C:\Users\WELCOME\Downloads\pars-nrb

# 1. Install pgvector (asks for Administrator; verifies SHA-256)
powershell -ExecutionPolicy Bypass -File scripts\install-pgvector.ps1

# 2. Install Ollama + llama3.2:3b + nomic-embed-text
powershell -ExecutionPolicy Bypass -File scripts\setup-ollama.ps1

# 3. Run backend with local provider
$env:PARS_AI_ENABLED='true'
$env:PARS_AI_PROVIDER='ollama'
$env:PARS_AI_CHAT_MODEL='llama3.2:3b'
$env:PARS_AI_EMBEDDING_MODEL='nomic-embed-text'
$env:PARS_AI_EMBEDDING_DIMENSIONS='768'
mvn spring-boot:run
```

Then start the frontend normally (`cd frontend; npm.cmd run dev`) and log in as ADMIN.

See `AI_ENVIRONMENT_VARIABLES.md`, `AI_DEPLOYMENT_GUIDE.md`, `AI_TROUBLESHOOTING.md`.

## Tests

```powershell
mvn test                       # backend unit tests
cd frontend
npm run build                  # TypeScript + production build
```

## Documentation index

- `AI_ARCHITECTURE.md` … `AI_TROUBLESHOOTING.md` (existing RAG module docs)
- `AI_IMPLEMENTATION_AUDIT.md`, `NRB_INTEGRATION_ARCHITECTURE.md`, `NRB_API_RESEARCH.md`, `NRB_CRAWLER_POLICY.md` (Phase 0)
- `NRB_API_DOCUMENTATION.md`, `NRB_DATA_SOURCE_CATALOG.md`, `NRB_INGESTION_PIPELINE.md`, `NRB_RAG_DESIGN.md`, `NRB_SECURITY_REVIEW.md`, `NRB_TEST_REPORT.md`, `NRB_DEPLOYMENT_GUIDE.md` (delivery)
