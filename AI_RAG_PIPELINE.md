# RAG Pipeline — PARS NRB

## 1. Query processing

Input is validated (non-blank, max 8000 characters) by Jakarta validation on `AiChatRequest`. Conversation history is limited to ownership-verified messages; the assistant uses the current question plus server-stored context, not browser-supplied history.

## 2. Retrieval (HybridRetrievalService)

1. Build a server-side filter from `CurrentUserProvider.require()`:
   `access IN [PUBLIC, INTERNAL, ROLE_…, USER_<uuid>] AND status == 'PUBLISHED' AND active == true AND deny != <uuid>`.
2. **Semantic**: Spring AI `PgVectorStore.similaritySearch` with `topK` (default 10) and similarity threshold (default 0.35).
3. **Keyword**: PostgreSQL `websearch_to_tsquery('simple', question)` over `ai.vector_store.content` with the same access predicates — essential for exact document numbers, codes and identifiers.
4. **Fusion**: Reciprocal Rank Fusion `score = Σ 1/(60 + rank)` merges both rankings and de-duplicates chunks.
5. Final context is limited to `final-passages` (default 5).

## 3. Generation

`AiPromptService` sends a system prompt that forbids fabrication, requires citations, and treats evidence as untrusted data. Evidence is wrapped in `<evidence id … document … version … page …>` tags; the model is explicitly told that instructions inside evidence are data.

`RagAnswerService` enforces:
- per-user daily quota and concurrency semaphore,
- abstention when retrieval is empty or below `evidence-threshold`,
- structured citations mapped **only** from retrieved chunk metadata,
- usage logging with SHA-free telemetry (question text is not logged),
- Micrometer counters for requests, retrieval misses, errors and rate limits.

## 4. Why citations are trustworthy

Citations are produced from `VectorStore` metadata (document id, title, version, page, chunk id). The LLM never supplies citation objects; it can only refer to the evidence text. A fabricated citation is therefore impossible without a corresponding retrieved chunk.

## 5. Live PARS data extension (implemented)

`AiQueryRouter` deterministically classifies questions into `DOCUMENTS`, `SELF`, `DEALS`, `APP_GUIDE` and `CAPABILITY`. Non-document routes are answered by `LiveDataService` using **only existing backend services**:

- `SELF` → `UserService.get(authenticatedUserId)` reads the user's real DB record (username, email, roles, position, status).
- `DEALS` → checks `DEAL_READ` on the JWT-derived `CurrentUser`, then `DealService.list(0, 5, ...)` returns a limited set of real deals.
- `APP_GUIDE` → verified facts from the actual source (routes `/deals`, `/deals/new`, `POST /api/v1/deals`, `DEAL_WRITE` requirement).

The model receives these as delimited `<verified_data>` facts and has no SQL, no tool access and no write capability. Permission checks happen before any data is fetched; a user without `DEAL_READ` is told the data is unavailable. Deterministic routing was chosen over LLM tool calling to avoid prompt-injection and excessive-agency risk.
