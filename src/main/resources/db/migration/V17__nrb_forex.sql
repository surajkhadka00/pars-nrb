-- V17: NRB Forex snapshots/rates and read permissions for the NRB integration.
-- Additive only. Existing pars business tables and V1-V16 are untouched.

CREATE TABLE IF NOT EXISTS ai.nrb_forex_snapshots (
    id UUID PRIMARY KEY,
    source_url TEXT NOT NULL,
    rate_date DATE NOT NULL,
    published_on VARCHAR(40),
    modified_on VARCHAR(40),
    fetched_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    content_hash VARCHAR(64) NOT NULL,
    raw_payload JSONB,
    CONSTRAINT uk_nrb_forex_snapshot UNIQUE (rate_date, content_hash)
);
CREATE INDEX IF NOT EXISTS ix_nrb_forex_snapshot_date ON ai.nrb_forex_snapshots (rate_date DESC);

CREATE TABLE IF NOT EXISTS ai.nrb_forex_rates (
    id UUID PRIMARY KEY,
    snapshot_id UUID NOT NULL REFERENCES ai.nrb_forex_snapshots (id) ON DELETE CASCADE,
    iso3 VARCHAR(10) NOT NULL,
    name VARCHAR(150),
    unit INTEGER NOT NULL DEFAULT 1,
    buy NUMERIC(22, 8),
    sell NUMERIC(22, 8),
    raw_buy VARCHAR(40),
    raw_sell VARCHAR(40)
);
CREATE INDEX IF NOT EXISTS ix_nrb_forex_rates_iso3 ON ai.nrb_forex_rates (iso3);

-- New permissions follow the existing uppercase convention. ADMIN only by default.
INSERT INTO pars.ad_permission (id, code, name, description, created_at, updated_at, version)
VALUES
    ('dddddddd-dddd-dddd-dddd-ddddddddddd1', 'AI_FOREX_READ', 'Read NRB forex rates', 'View live and historical official NRB forex rates', NOW(), NOW(), 0),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd2', 'AI_SOURCE_READ', 'Read NRB sources', 'View configured NRB knowledge sources', NOW(), NOW(), 0),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd3', 'AI_SOURCE_MANAGE', 'Manage NRB sources', 'Configure and synchronize NRB sources', NOW(), NOW(), 0),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd4', 'AI_DOCUMENT_SYNC', 'Synchronize NRB documents', 'Run document discovery and reindex operations', NOW(), NOW(), 0);

INSERT INTO pars.ad_role_permission (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id
FROM pars.ad_permission
WHERE code IN ('AI_FOREX_READ', 'AI_SOURCE_READ', 'AI_SOURCE_MANAGE', 'AI_DOCUMENT_SYNC');
