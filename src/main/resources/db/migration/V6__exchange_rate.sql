-- Daily exchange rate. Base currency is always NPR (seeded in V4).

CREATE TABLE c_exchangerate (
    id UUID PRIMARY KEY,
    c_base_currency_id UUID NOT NULL REFERENCES c_currency (id),
    unit INTEGER NOT NULL,
    c_currency_id UUID NOT NULL REFERENCES c_currency (id),
    rate NUMERIC(22, 8) NOT NULL,
    rate_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_c_exchangerate_base_npr CHECK (
        c_base_currency_id = 'cccccccc-cccc-cccc-cccc-ccccccccccc1'
    ),
    CONSTRAINT ck_c_exchangerate_unit_positive CHECK (unit > 0),
    CONSTRAINT ck_c_exchangerate_rate_positive CHECK (rate > 0),
    CONSTRAINT ck_c_exchangerate_currency_diff CHECK (c_currency_id <> c_base_currency_id),
    CONSTRAINT uk_c_exchangerate_day UNIQUE (c_base_currency_id, c_currency_id, rate_date)
);

CREATE INDEX ix_c_exchangerate_date ON c_exchangerate (rate_date DESC);
CREATE INDEX ix_c_exchangerate_currency ON c_exchangerate (c_currency_id);
