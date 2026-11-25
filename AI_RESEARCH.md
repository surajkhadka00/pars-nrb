# AI Research Report — PARS NRB RAG System

Access date: 2026-11-17. Primary sources: official documentation and Maven Central metadata.

## 1. Spring AI / Spring Boot compatibility gate (verified)

| Artifact inspected | Boot dependency declared | Verdict |
|---|---|---|
| `spring-ai-starter-model-openai:1.0.0` POM | `spring-boot-starter:3.4.5` | **EXACT MATCH** with our parent 3.4.5 |
| `spring-ai-starter-model-openai:1.0.9` POM | `spring-boot-starter:3.5.15` | Incompatible — would force Boot upgrade |
| Spring AI `2.0.2` Getting Started | "2.0.x supports Spring Boot 4.0.x and 4.1.x" | Incompatible |
| Maven Central metadata | 1.0.0 … 1.0.9, 1.1.0…1.1.8, 2.0.x, 2.1.0-RC2 available | 1.0.0 selected |

**Decision:** pin `spring-ai-bom:1.0.0`, never use a range. Add no other Boot-managed overrides. Verify with `mvn dependency:tree` and `mvn -q help:effective-pom`.

Sources:
- https://repo1.maven.org/maven2/org/springframework/ai/spring-ai-bom/maven-metadata.xml
- https://repo1.maven.org/maven2/org/springframework/ai/spring-ai-starter-model-openai/1.0.0/spring-ai-starter-model-openai-1.0.0.pom
- https://repo1.maven.org/maven2/org/springframework/ai/spring-ai-starter-model-openai/1.0.9/spring-ai-starter-model-openai-1.0.9.pom
- https://docs.spring.io/spring-ai/reference/getting-started.html
- https://docs.spring.io/spring-ai/reference/upgrade-notes.html

## 2. PGvector / Spring AI PGvector store

Official PGvector store docs confirm:
- Requires PostgreSQL extensions `vector`, `hstore`, `uuid-ossp`.
- Table columns: `id uuid`, `content text`, `metadata json`, `embedding vector(dimensions)`.
- `index-type`: `NONE` (exact), `IVFFlat`, `HNSW` (default). HNSW max 2000 dimensions.
- `initialize-schema` defaults to **false** — Flyway must own the schema.
- Configurable `schema-name`, `table-name`, `schema-validation=true` recommended for custom names.
- Metadata filters are translated into PostgreSQL JSON path expressions.
- **Important:** filtered ANN searches can lose recall with HNSW/IVFFlat; we choose `NONE` (exact) initially for correctness, then benchmark before enabling HNSW.

Sources:
- https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html
- https://github.com/pgvector/pgvector

## 3. Document readers

Spring AI 1.0.0 BOM manages `spring-ai-pdf-document-reader`, `spring-ai-tika-document-reader`, `spring-ai-jsoup-document-reader`. The existing project already has Apache POI 5.4.1 for XLSX. We use Tika for MIME detection, PDFBox/PDF reader for PDFs, POI for Office, Jsoup for HTML.

Sources:
- https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html
- https://pdfbox.apache.org/, https://tika.apache.org/, https://poi.apache.org/, https://jsoup.org/

## 4. Spring AI chat/RAG APIs

Spring AI 1.0.0 provides `ChatModel`, `EmbeddingModel`, `ChatClient`, `VectorStore`, `Document`, `SearchRequest`, `QuestionAnswerAdvisor`, `spring-ai-rag` module. Business services will depend only on these interfaces, providing provider independence.

Sources:
- https://docs.spring.io/spring-ai/reference/api/chatclient.html
- https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html
- https://docs.spring.io/spring-ai/reference/api/embeddings.html

## 5. Security research

OWASP Top 10 for LLM Applications (2025): prompt injection, sensitive information disclosure, supply chain, data/model poisoning, improper output handling, excessive agency, system prompt leakage, vector/embedding weaknesses, misinformation, unbounded consumption. Our threat model (AI_SECURITY_THREAT_MODEL.md) maps each to a concrete control.

Sources:
- https://owasp.org/www-project-top-10-for-large-language-model-applications/
- https://genai.owasp.org/

## 6. PostgreSQL / Testcontainers / React / Docker

- PostgreSQL FTS: `websearch_to_tsquery`, `to_tsvector`, GIN indexes; `pg_trgm` for fuzzy exact-ID matching.
- Testcontainers PostgreSQL module (already in pom at 1.20.6) supports `pgvector/pgvector:pg17` image.
- React 19 + TypeScript strict, Vite 6, TanStack Query 5 — already used by this project.
- Docker multi-stage build for `target/nrb-pars.jar` (Java 17/21 runtime base), `pgvector/pgvector` for dev DB.

Sources:
- https://www.postgresql.org/docs/current/textsearch.html
- https://java.testcontainers.org/modules/databases/postgres/
- https://react.dev/, https://www.typescriptlang.org/, https://vite.dev/, https://docs.docker.com/

## 7. Items that remain unverified until runtime

- Whether the local PostgreSQL installation permits `CREATE EXTENSION vector` with the dev user — must be checked with SQL in Phase 1 (DBA prerequisite otherwise).
- Actual embedding dimensions by chosen model — must match `vector(N)` in Flyway.
- Whether a real AI provider key is available; if not, AI stays disabled and tests use mock models only.
