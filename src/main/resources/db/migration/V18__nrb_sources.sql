-- V18: NRB source registry, sync jobs, discovered documents and ingestion errors.
-- Vectors remain ONLY in ai.vector_store; this migration stores metadata and audit data.

CREATE TABLE IF NOT EXISTS ai.nrb_source_registry (
    id UUID PRIMARY KEY,
    source_name VARCHAR(200) NOT NULL,
    official_url TEXT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    category VARCHAR(80) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_successful_sync TIMESTAMPTZ,
    last_attempted_sync TIMESTAMPTZ,
    content_hash VARCHAR(64),
    publication_date DATE,
    last_modified_date TIMESTAMPTZ,
    canonical_url TEXT,
    ingestion_status VARCHAR(40) DEFAULT 'NOT_SYNCED',
    error_details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_nrb_source_url UNIQUE (official_url)
);

CREATE TABLE IF NOT EXISTS ai.nrb_source_sync_jobs (
    id UUID PRIMARY KEY,
    source_id UUID REFERENCES ai.nrb_source_registry(id) ON DELETE CASCADE,
    job_type VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'QUEUED',
    discovered INTEGER NOT NULL DEFAULT 0,
    downloaded INTEGER NOT NULL DEFAULT 0,
    failed INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_nrb_sync_jobs_created ON ai.nrb_source_sync_jobs (created_at DESC);

CREATE TABLE IF NOT EXISTS ai.nrb_documents (
    id UUID PRIMARY KEY,
    source_id UUID REFERENCES ai.nrb_source_registry(id) ON DELETE SET NULL,
    canonical_url TEXT NOT NULL,
    document_number VARCHAR(120),
    department VARCHAR(150),
    title TEXT NOT NULL,
    category VARCHAR(80),
    language VARCHAR(20),
    fiscal_year VARCHAR(20),
    publication_date DATE,
    last_modified TIMESTAMPTZ,
    content_hash VARCHAR(64) NOT NULL,
    file_name VARCHAR(500),
    mime_type VARCHAR(150),
    file_size BIGINT,
    storage_reference TEXT,
    ingestion_status VARCHAR(40) NOT NULL DEFAULT 'DISCOVERED',
    published BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    fetched_at TIMESTAMPTZ,
    error_details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_nrb_document_hash UNIQUE (content_hash)
);

CREATE TABLE IF NOT EXISTS ai.nrb_document_versions (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES ai.nrb_documents(id) ON DELETE CASCADE,
    version INTEGER NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    storage_reference TEXT,
    publication_date DATE,
    indexed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_nrb_doc_version UNIQUE (document_id, version)
);

CREATE TABLE IF NOT EXISTS ai.nrb_ingestion_errors (
    id UUID PRIMARY KEY,
    source_id UUID REFERENCES ai.nrb_source_registry(id) ON DELETE SET NULL,
    document_id UUID REFERENCES ai.nrb_documents(id) ON DELETE CASCADE,
    url TEXT,
    stage VARCHAR(40) NOT NULL,
    error_message TEXT,
    trace_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_nrb_ingestion_errors_created ON ai.nrb_ingestion_errors (created_at DESC);

-- Optional metadata for vectors produced by NRB sources (no vector data duplicated).
CREATE TABLE IF NOT EXISTS ai.ai_embeddings (
    id UUID PRIMARY KEY,
    chunk_id UUID NOT NULL,
    document_id UUID,
    source_url TEXT,
    embedding_model VARCHAR(150),
    embedding_model_version VARCHAR(80),
    dimensions INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Seed only verified official sources from NRB site navigation. All disabled by default.
INSERT INTO ai.nrb_source_registry
 (id, source_name, official_url, source_type, category, enabled, ingestion_status)
VALUES
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'NRB Homepage', 'https://www.nrb.org.np/', 'HTML', 'GENERAL', FALSE, 'NOT_SYNCED'),
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'NRB Circulars', 'https://www.nrb.org.np/category/circulars/?department=bfr', 'HTML', 'CIRCULARS', FALSE, 'NOT_SYNCED'),
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'NRB FX Circulars', 'https://www.nrb.org.np/category/fxm-circulars/?department=fxm', 'HTML', 'FX_REGULATION', FALSE, 'NOT_SYNCED'),
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'NRB Monetary Policy', 'https://www.nrb.org.np/category/monetary-policy/?department=ofg', 'HTML', 'MONETARY_POLICY', FALSE, 'NOT_SYNCED'),
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5', 'NRB Statistics & Data Links', 'https://www.nrb.org.np/departments/statistics-data-links/', 'HTML', 'STATISTICS', FALSE, 'NOT_SYNCED'),
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee6', 'NRB Economic Research', 'https://www.nrb.org.np/departments/red/', 'HTML', 'REPORTS', FALSE, 'NOT_SYNCED'),
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee7', 'NRB Forex API', 'https://www.nrb.org.np/api/forex/v1/', 'API', 'FOREX', FALSE, 'NOT_SYNCED')
ON CONFLICT (official_url) DO NOTHING;
