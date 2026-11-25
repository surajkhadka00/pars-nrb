INSERT INTO ad_sys_config (name, value, created_at, updated_at)
SELECT 'mail.protocol', 'smtp', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ad_sys_config WHERE name = 'mail.protocol');
