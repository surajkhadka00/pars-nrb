-- V14: AI/RAG schema. Additive only; existing pars objects are untouched.
-- All AI tables live in schema "ai". Business schema remains "pars".

CREATE SCHEMA IF NOT EXISTS ai;

-- hstore and uuid-ossp are required by Spring AI's PGvector store.
-- Production DBA note: extensions must be pre-approved and installed by the DBA.
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- pgvector is optional at migration time: AI stays disabled when the extension
-- is absent. The vector table is created only when the extension is available.
DO $ai_vector$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'vector') THEN
        CREATE EXTENSION IF NOT EXISTS vector;
        EXECUTE 'CREATE TABLE IF NOT EXISTS ai.vector_store (
                    id uuid PRIMARY KEY,
                    content text,
                    metadata json,
                    embedding vector(1536)
                 )';
        RAISE NOTICE 'PARS AI: pgvector available; ai.vector_store is ready';
    ELSE
        RAISE NOTICE 'PARS AI: pgvector extension NOT available; AI vector store will be disabled';
    END IF;
END
$ai_vector$;

CREATE TABLE IF NOT EXISTS ai.ai_documents (
    id UUID PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    original_filename VARCHAR(500) NOT NULL,
    mime_type VARCHAR(150) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    file_size BIGINT NOT NULL,
    category VARCHAR(120),
    department VARCHAR(120),
    access_classification VARCHAR(30) NOT NULL DEFAULT 'INTERNAL',
    publication_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    doc_version INTEGER NOT NULL DEFAULT 1,
    effective_date DATE,
    expiry_date DATE,
    uploaded_by UUID NOT NULL REFERENCES pars.ad_user (id),
    approved_by UUID REFERENCES pars.ad_user (id),
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ,
    last_indexed_at TIMESTAMPTZ,
    embedding_model VARCHAR(150),
    embedding_model_version VARCHAR(80),
    embedding_dimensions INTEGER,
    storage_reference TEXT NOT NULL,
    active_version BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    row_version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS ix_ai_documents_status ON ai.ai_documents (publication_status, active_version);
CREATE INDEX IF NOT EXISTS ix_ai_documents_class ON ai.ai_documents (access_classification);
CREATE INDEX IF NOT EXISTS ix_ai_documents_checksum ON ai.ai_documents (checksum);
CREATE INDEX IF NOT EXISTS ix_ai_documents_category ON ai.ai_documents (category);

CREATE TABLE IF NOT EXISTS ai.ai_ingestion_jobs (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES ai.ai_documents (id),
    status VARCHAR(30) NOT NULL DEFAULT 'QUEUED',
    stage VARCHAR(30),
    progress INTEGER NOT NULL DEFAULT 0,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    error_message TEXT,
    claimed_by VARCHAR(120),
    claimed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    row_version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS ix_ai_jobs_claim ON ai.ai_ingestion_jobs (status, created_at);

CREATE TABLE IF NOT EXISTS ai.ai_conversations (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES pars.ad_user (id),
    title VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ,
    row_version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS ix_ai_conversations_owner ON ai.ai_conversations (owner_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS ai.ai_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES ai.ai_conversations (id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    sources JSONB,
    insufficient_evidence BOOLEAN NOT NULL DEFAULT FALSE,
    trace_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_ai_messages_conversation ON ai.ai_messages (conversation_id, created_at);

CREATE TABLE IF NOT EXISTS ai.ai_feedback (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES ai.ai_messages (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES pars.ad_user (id),
    rating INTEGER NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_ai_feedback_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE IF NOT EXISTS ai.ai_retrieval_logs (
    id UUID PRIMARY KEY,
    conversation_id UUID,
    user_id UUID NOT NULL REFERENCES pars.ad_user (id),
    question_sha256 VARCHAR(64),
    retrieval_mode VARCHAR(40),
    chunk_ids JSONB,
    scores JSONB,
    trace_id VARCHAR(64),
    duration_ms BIGINT,
    result_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_ai_retrieval_user ON ai.ai_retrieval_logs (user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS ai.ai_document_access (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES ai.ai_documents (id) ON DELETE CASCADE,
    user_id UUID REFERENCES pars.ad_user (id),
    role_code VARCHAR(80),
    effect VARCHAR(10) NOT NULL DEFAULT 'GRANT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_ai_doc_access_effect CHECK (effect IN ('GRANT', 'DENY')),
    CONSTRAINT ck_ai_doc_access_target CHECK (user_id IS NOT NULL OR role_code IS NOT NULL)
);
CREATE INDEX IF NOT EXISTS ix_ai_doc_access_doc ON ai.ai_document_access (document_id);

CREATE TABLE IF NOT EXISTS ai.ai_usage_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES pars.ad_user (id),
    event_type VARCHAR(40) NOT NULL,
    tokens_in INTEGER NOT NULL DEFAULT 0,
    tokens_out INTEGER NOT NULL DEFAULT 0,
    estimated_cost NUMERIC(14, 6) NOT NULL DEFAULT 0,
    trace_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_ai_usage_user_day ON ai.ai_usage_events (user_id, created_at DESC);

-- AI permissions: follow the existing uppercase code convention.
-- Deliberately granted to ADMIN only; other roles must be assigned explicitly.
INSERT INTO pars.ad_permission (id, code, name, description, created_at, updated_at, version)
VALUES
    ('cccccccc-cccc-cccc-cccc-ccccccccccc1', 'AI_CHAT', 'Use AI assistant', 'Ask questions through the PARS AI assistant', NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc2', 'AI_CONVERSATION_READ', 'Read AI conversations', 'View own AI conversations', NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc3', 'AI_DOCUMENT_READ', 'Read AI documents', 'View AI knowledge documents', NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc4', 'AI_DOCUMENT_UPLOAD', 'Upload AI documents', 'Upload knowledge documents for ingestion', NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc5', 'AI_DOCUMENT_APPROVE', 'Approve AI documents', 'Publish or archive knowledge documents', NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc6', 'AI_DOCUMENT_DELETE', 'Delete AI documents', 'Delete knowledge documents', NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc7', 'AI_DOCUMENT_REINDEX', 'Reindex AI documents', 'Re-run ingestion for a document', NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc8', 'AI_FEEDBACK', 'Provide AI feedback', 'Rate AI answers', NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc9', 'AI_ADMIN', 'AI administration', 'Manage AI configuration and jobs', NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccca', 'AI_METRICS_READ', 'Read AI metrics', 'View AI usage and retrieval metrics', NOW(), NOW(), 0);

INSERT INTO pars.ad_role_permission (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id
FROM pars.ad_permission
WHERE code LIKE 'AI_%';
