-- V15: Create the pgvector table after pgvector is installed.
-- V14 ran when the extension was absent, so no ai.vector_store table exists locally.
-- Dimension 768 matches the FREE local embedding model "nomic-embed-text" (Ollama).
-- If you switch to OpenAI embeddings (1536), ALTER the column and reindex all documents.

DO $ai_vector$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'vector') THEN
        CREATE EXTENSION IF NOT EXISTS vector;
        EXECUTE 'CREATE TABLE IF NOT EXISTS ai.vector_store (
                    id uuid PRIMARY KEY,
                    content text,
                    metadata json,
                    embedding vector(768)
                 )';
        RAISE NOTICE 'PARS AI: ai.vector_store ready with vector(768) for local Ollama embeddings';
    ELSE
        RAISE NOTICE 'PARS AI: pgvector still unavailable; vector store not created';
    END IF;
END
$ai_vector$;
