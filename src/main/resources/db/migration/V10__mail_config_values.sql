INSERT INTO ad_sys_config (name, value, created_at, updated_at)
VALUES
    ('mail.enabled', 'false', NOW(), NOW()),
    ('mail.host', 'localhost', NOW(), NOW()),
    ('mail.port', '587', NOW(), NOW()),
    ('mail.protocol', 'smtp', NOW(), NOW()),
    ('mail.username', 'pars', NOW(), NOW()),
    ('mail.password', 'change-me', NOW(), NOW()),
    ('mail.from', 'noreply@nrb.org.np', NOW(), NOW()),
    ('mail.from-name', 'PARS NRB', NOW(), NOW()),
    ('mail.smtp.auth', 'true', NOW(), NOW()),
    ('mail.smtp.starttls', 'true', NOW(), NOW()),
    ('mail.app-base-url', 'http://localhost:3000', NOW(), NOW()),
    ('mail.reset-token-minutes', '30', NOW(), NOW())
ON CONFLICT (name) DO UPDATE
SET value = EXCLUDED.value,
    updated_at = NOW()
WHERE ad_sys_config.value IS NULL OR btrim(ad_sys_config.value) = '';
