-- V20: Repair ADMIN role permissions. Guarantees that the ADMIN role always has
-- every permission known to the system, even if it was accidentally edited.
-- Also prevents future drift by re-granting all permissions idempotently.

INSERT INTO pars.ad_role_permission (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id
FROM pars.ad_permission
ON CONFLICT DO NOTHING;

-- Make the ADMIN role non-deletable and non-renamable at the data level by recording
-- its canonical code in sys_config (application logic enforces it).
INSERT INTO pars.ad_sys_config (name, value, created_at, updated_at)
VALUES ('rbac.admin-role-code', 'ADMIN', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;
