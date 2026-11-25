CREATE TABLE ad_position (
    id UUID PRIMARY KEY,
    value VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    limit_type VARCHAR(20),
    limit_value NUMERIC,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ad_position_value UNIQUE (value)
);

ALTER TABLE ad_user
    ADD COLUMN mobile VARCHAR(30),
    ADD COLUMN address VARCHAR(500),
    ADD COLUMN image BYTEA,
    ADD COLUMN image_content_type VARCHAR(64),
    ADD COLUMN ad_position_id UUID REFERENCES ad_position (id);

CREATE INDEX ix_ad_user_position ON ad_user (ad_position_id);

INSERT INTO ad_permission (id, code, name, description, created_at, updated_at, version)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb21', 'POSITION_READ', 'Read positions', 'View positions', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb22', 'POSITION_WRITE', 'Write positions', 'Create, update, and delete positions', NOW(), NOW(), 0);

INSERT INTO ad_role_permission (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id
FROM ad_permission
WHERE code IN ('POSITION_READ', 'POSITION_WRITE');
