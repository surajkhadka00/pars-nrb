CREATE TABLE p_deal_attachment (
    id UUID PRIMARY KEY,
    p_deal_id UUID NOT NULL REFERENCES p_deal (id),
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(80) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by_id UUID NOT NULL REFERENCES ad_user (id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_p_deal_attachment_stored_name UNIQUE (stored_file_name),
    CONSTRAINT ck_p_deal_attachment_size_positive CHECK (file_size > 0)
);

CREATE INDEX ix_p_deal_attachment_deal ON p_deal_attachment (p_deal_id);
CREATE INDEX ix_p_deal_attachment_user ON p_deal_attachment (uploaded_by_id);
