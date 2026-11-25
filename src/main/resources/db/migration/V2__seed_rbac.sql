-- Seed roles and permissions (users are loaded from the database)

INSERT INTO ad_role (id, code, name, description, created_at, updated_at, version)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'ADMIN', 'Administrator', 'Full system access', NOW(), NOW(), 0),
    ('22222222-2222-2222-2222-222222222222', 'USER', 'User', 'Standard authenticated user', NOW(), NOW(), 0);

INSERT INTO ad_permission (id, code, name, description, created_at, updated_at, version)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'USER_READ', 'Read users', 'View users', NOW(), NOW(), 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'USER_WRITE', 'Write users', 'Create and update users', NOW(), NOW(), 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'ROLE_READ', 'Read roles', 'View roles', NOW(), NOW(), 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'ROLE_WRITE', 'Write roles', 'Create and update roles', NOW(), NOW(), 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'PERMISSION_READ', 'Read permissions', 'View permissions', NOW(), NOW(), 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 'SESSION_REVOKE', 'Revoke sessions', 'Revoke user sessions', NOW(), NOW(), 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', 'FAILED_ATTEMPT_READ', 'Read failed attempts', 'View login failures', NOW(), NOW(), 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', 'API_ACCESS_WRITE', 'Write API access', 'Grant or deny user API access', NOW(), NOW(), 0);

INSERT INTO ad_role_permission (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id FROM ad_permission;
