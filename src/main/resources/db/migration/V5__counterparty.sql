CREATE TABLE p_counterparty (
    id UUID PRIMARY KEY,
    value VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(2000),
    address VARCHAR(2000),
    ad_user_id UUID REFERENCES ad_user (id),
    bloomberg_code VARCHAR(40),
    c_country_id UUID,
    comments VARCHAR(2000),
    contact_name VARCHAR(120),
    email VARCHAR(100),
    investment_grade VARCHAR(40),
    is_own_bank BOOLEAN NOT NULL DEFAULT FALSE,
    limit_type VARCHAR(20),
    limit_value NUMERIC,
    location VARCHAR(120),
    phone VARCHAR(30),
    position VARCHAR(100),
    reuters_dealing_code VARCHAR(40),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_p_counterparty_value UNIQUE (value)
);

CREATE INDEX ix_p_counterparty_user ON p_counterparty (ad_user_id);

CREATE TABLE p_counterpartylimit (
    id UUID PRIMARY KEY,
    p_counterparty_id UUID NOT NULL REFERENCES p_counterparty (id),
    date_from TIMESTAMPTZ,
    date_to TIMESTAMPTZ,
    description VARCHAR(255),
    limit_criteria VARCHAR(40),
    limit_type VARCHAR(20),
    limit_value NUMERIC,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_p_counterpartylimit_counterparty ON p_counterpartylimit (p_counterparty_id);

ALTER TABLE p_nostro
    ADD CONSTRAINT fk_p_nostro_counterparty
    FOREIGN KEY (p_counterparty_id) REFERENCES p_counterparty (id);

ALTER TABLE p_deal
    ADD CONSTRAINT fk_p_deal_counterparty
    FOREIGN KEY (p_counterparty_id) REFERENCES p_counterparty (id);

ALTER TABLE p_deal
    ADD CONSTRAINT fk_p_deal_ii_counterparty
    FOREIGN KEY (ii_counterparty_id) REFERENCES p_counterparty (id);

ALTER TABLE p_deal
    ADD CONSTRAINT fk_p_deal_iii_counterparty
    FOREIGN KEY (iii_counterparty_id) REFERENCES p_counterparty (id);

CREATE INDEX ix_p_nostro_counterparty ON p_nostro (p_counterparty_id);
CREATE INDEX ix_p_deal_counterparty ON p_deal (p_counterparty_id);

INSERT INTO ad_permission (id, code, name, description, created_at, updated_at, version)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb13', 'COUNTERPARTY_READ', 'Read counterparties', 'View counterparties', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb14', 'COUNTERPARTY_WRITE', 'Write counterparties', 'Create and update counterparties', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb15', 'COUNTERPARTYLIMIT_READ', 'Read counterparty limits', 'View counterparty limits', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb16', 'COUNTERPARTYLIMIT_WRITE', 'Write counterparty limits', 'Create and update counterparty limits', NOW(), NOW(), 0);

INSERT INTO ad_role_permission (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id
FROM ad_permission
WHERE code IN (
    'COUNTERPARTY_READ', 'COUNTERPARTY_WRITE',
    'COUNTERPARTYLIMIT_READ', 'COUNTERPARTYLIMIT_WRITE'
);
