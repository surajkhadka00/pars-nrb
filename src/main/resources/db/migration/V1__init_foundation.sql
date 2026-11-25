-- PARS foundation schema (ad_ prefix for user/auth tables; flyway_schema_history is unmanaged)

CREATE TABLE ad_user (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    expired BOOLEAN NOT NULL DEFAULT FALSE,
    failed_count INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    locked_at TIMESTAMPTZ,
    password_changed_at TIMESTAMPTZ,
    last_successful_login_at TIMESTAMPTZ,
    inactive_expire_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ad_user_username UNIQUE (username),
    CONSTRAINT uk_ad_user_email UNIQUE (email)
);

CREATE TABLE ad_role (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ad_role_code UNIQUE (code)
);

CREATE TABLE ad_permission (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ad_permission_code UNIQUE (code)
);

CREATE TABLE ad_user_role (
    user_id UUID NOT NULL REFERENCES ad_user (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES ad_role (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE ad_role_permission (
    role_id UUID NOT NULL REFERENCES ad_role (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES ad_permission (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE ad_user_api_access (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES ad_user (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES ad_permission (id) ON DELETE CASCADE,
    effect VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ad_user_api_access UNIQUE (user_id, permission_id),
    CONSTRAINT ck_ad_user_api_access_effect CHECK (effect IN ('GRANT', 'DENY'))
);

CREATE TABLE ad_auth_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES ad_user (id) ON DELETE CASCADE,
    family_id UUID NOT NULL,
    refresh_token_hash VARCHAR(64) NOT NULL,
    previous_refresh_token_hash VARCHAR(64),
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    issued_at TIMESTAMPTZ NOT NULL,
    last_activity_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ad_auth_session_refresh_hash UNIQUE (refresh_token_hash)
);

CREATE INDEX ix_ad_auth_session_user_id ON ad_auth_session (user_id);
CREATE INDEX ix_ad_auth_session_family_id ON ad_auth_session (family_id);

CREATE TABLE ad_auth_failed_attempt (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    ip_address VARCHAR(64),
    attempted_at TIMESTAMPTZ NOT NULL,
    reason VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_ad_auth_failed_attempt_username ON ad_auth_failed_attempt (username, attempted_at DESC);

CREATE TABLE ad_sys_config (
    name VARCHAR(128) PRIMARY KEY,
    value TEXT,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE ad_audit_log (
    id UUID PRIMARY KEY,
    method VARCHAR(16) NOT NULL,
    path VARCHAR(255) NOT NULL,
    status INTEGER NOT NULL,
    payload TEXT,
    duration_ms BIGINT NOT NULL,
    user_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_ad_audit_log_created_at ON ad_audit_log (created_at DESC);
