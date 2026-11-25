# NRB RAG Design

- One vector index (`ai.vector_store`, 768 dims). NRB chunks carry `sourceUrl` metadata; uploaded PARS documents carry their own `docId`. No duplicate vector stores.
- `HybridRetrievalService` fuses pgvector cosine similarity with PostgreSQL full-text via Reciprocal Rank Fusion, applies server-side access filters (`status=PUBLISHED`, `active=true`, classification and deny grants), and returns at most `final-passages` chunks.
- `AiQueryRouter` chooses the correct source before generation: live Forex, historical Forex, NRB document RAG, live self/deal data, or capability.
- `AiPromptService` puts evidence inside `<verified_data>` tags and explicitly treats web/PDF content as untrusted data. No tool calling, no SQL, no write operations.
- `AiSourceResponse.sourceUrl` lets the React UI render clickable official citations. Each answer includes document title, version, page and official URL where available.
- "Latest" claims require verified source metadata; the assistant refused to name a "latest FX circular" when the indexed evidence carried no reliable publication dates — this is correct abstention, not a failure.
- Forex answers are labelled indicative and always show the rate date, published/modified timestamps, fetch time and source URL. Historical dates are never presented as current.
