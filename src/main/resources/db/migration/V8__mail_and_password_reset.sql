CREATE TABLE ad_mail_template (
    code VARCHAR(64) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE ad_password_reset (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES ad_user (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ad_password_reset_token UNIQUE (token_hash)
);

CREATE INDEX ix_ad_password_reset_user ON ad_password_reset (user_id);

INSERT INTO ad_sys_config (name, value, created_at, updated_at)
VALUES
    ('mail.enabled', 'false', NOW(), NOW()),
    ('mail.host', '', NOW(), NOW()),
    ('mail.port', '587', NOW(), NOW()),
    ('mail.protocol', 'smtp', NOW(), NOW()),
    ('mail.username', '', NOW(), NOW()),
    ('mail.password', '', NOW(), NOW()),
    ('mail.from', 'noreply@nrb.org.np', NOW(), NOW()),
    ('mail.from-name', 'PARS NRB', NOW(), NOW()),
    ('mail.smtp.auth', 'true', NOW(), NOW()),
    ('mail.smtp.starttls', 'true', NOW(), NOW()),
    ('mail.app-base-url', 'http://localhost:3000', NOW(), NOW()),
    ('mail.reset-token-minutes', '30', NOW(), NOW());

INSERT INTO ad_mail_template (code, name, subject, body, created_at, updated_at)
VALUES
    (
        'FORGOT_PASSWORD_REQUEST',
        'Forgot password request',
        'Reset your PARS password',
        '<p>Hello {{fullName}},</p><p>We received a request to reset the password for <strong>{{username}}</strong>.</p><p><a href="{{resetLink}}">Reset password</a></p><p>This link expires in {{expiresMinutes}} minutes. If you did not ask for this, you can ignore this mail.</p>',
        NOW(), NOW()
    ),
    (
        'PASSWORD_RESET',
        'Password reset',
        'Your PARS password was reset',
        '<p>Hello {{fullName}},</p><p>The password for <strong>{{username}}</strong> was reset. Sign in with the new password.</p><p>If you did not reset it, contact your administrator.</p>',
        NOW(), NOW()
    ),
    (
        'PASSWORD_CHANGED',
        'Password changed',
        'Your PARS password was changed',
        '<p>Hello {{fullName}},</p><p>The password for <strong>{{username}}</strong> was changed. Existing sessions were signed out.</p><p>If you did not change it, contact your administrator.</p>',
        NOW(), NOW()
    );
