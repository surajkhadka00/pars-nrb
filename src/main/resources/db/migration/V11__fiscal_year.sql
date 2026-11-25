CREATE TABLE p_fiscalyear (
    id UUID PRIMARY KEY,
    value VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_p_fiscalyear_value UNIQUE (value),
    CONSTRAINT ck_p_fiscalyear_dates CHECK (end_date >= start_date)
);

CREATE INDEX ix_p_fiscalyear_dates ON p_fiscalyear (start_date, end_date);

INSERT INTO ad_permission (id, code, name, description, created_at, updated_at, version)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb17', 'FISCALYEAR_READ', 'Read fiscal years', 'View fiscal years', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb18', 'FISCALYEAR_WRITE', 'Write fiscal years', 'Create, update, and delete fiscal years', NOW(), NOW(), 0);

INSERT INTO ad_role_permission (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id
FROM ad_permission
WHERE code IN ('FISCALYEAR_READ', 'FISCALYEAR_WRITE');
