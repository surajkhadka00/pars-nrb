CREATE TABLE c_country (
    id UUID PRIMARY KEY,
    value VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_c_country_value UNIQUE (value)
);

INSERT INTO ad_permission (id, code, name, description, created_at, updated_at, version)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb19', 'COUNTRY_READ', 'Read countries', 'View countries', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb20', 'COUNTRY_WRITE', 'Write countries', 'Create, update, and delete countries', NOW(), NOW(), 0);

INSERT INTO ad_role_permission (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id
FROM ad_permission
WHERE code IN ('COUNTRY_READ', 'COUNTRY_WRITE');
