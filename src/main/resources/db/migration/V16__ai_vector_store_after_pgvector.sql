-- V16: Create ai.vector_store now that pgvector is installed.
-- V14/V15 ran while pgvector was absent, so this table does not exist yet.
-- 768 dimensions = free local embedding model nomic-embed-text (Ollama).
-- The vector extension and its type/operators must live in the "pars" schema,
-- because the JDBC URL sets currentSchema=pars (search_path = pars only).

DO $ai_vector$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'vector') THEN
        CREATE EXTENSION IF NOT EXISTS vector SCHEMA pars;
        IF (SELECT n.nspname FROM pg_extension e JOIN pg_namespace n ON n.oid = e.extnamespace
            WHERE e.extname = 'vector') <> 'pars' THEN
            ALTER EXTENSION vector SET SCHEMA pars;
        END IF;
        EXECUTE 'CREATE TABLE IF NOT EXISTS ai.vector_store (
                    id uuid PRIMARY KEY,
                    content text,
                    metadata json,
                    embedding vector(768)
                 )';
        RAISE NOTICE 'PARS AI: ai.vector_store ready with vector(768) in pars schema';
    END IF;
END
$ai_vector$;
