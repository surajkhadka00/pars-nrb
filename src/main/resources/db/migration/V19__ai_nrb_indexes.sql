-- V19: indexes for NRB/AI query paths identified in DATABASE_AUDIT.md.
-- Additive only; no existing data or constraints are changed.

CREATE INDEX IF NOT EXISTS ix_nrb_forex_snapshot_fk ON ai.nrb_forex_rates (snapshot_id);
CREATE INDEX IF NOT EXISTS ix_nrb_documents_url ON ai.nrb_documents (canonical_url);
CREATE INDEX IF NOT EXISTS ix_nrb_documents_source ON ai.nrb_documents (source_id);
CREATE INDEX IF NOT EXISTS ix_nrb_errors_document ON ai.nrb_ingestion_errors (document_id);
CREATE INDEX IF NOT EXISTS ix_ai_embeddings_chunk ON ai.ai_embeddings (chunk_id);
CREATE INDEX IF NOT EXISTS ix_nrb_doc_versions_doc ON ai.nrb_document_versions (document_id);
