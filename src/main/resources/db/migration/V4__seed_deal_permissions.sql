INSERT INTO ad_permission (id, code, name, description, created_at, updated_at, version)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'DEAL_READ', 'Read deals', 'View deals', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'DEAL_WRITE', 'Write deals', 'Create and update deals', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3', 'DEALTYPE_READ', 'Read deal types', 'View deal types', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4', 'DEALTYPE_WRITE', 'Write deal types', 'Create and update deal types', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb5', 'INSTRUMENT_READ', 'Read instrument codes', 'View instrument codes', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb6', 'INSTRUMENT_WRITE', 'Write instrument codes', 'Create and update instrument codes', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb7', 'DEALTYPELIMIT_READ', 'Read deal type limits', 'View deal type limits', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb8', 'DEALTYPELIMIT_WRITE', 'Write deal type limits', 'Create and update deal type limits', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb9', 'NOSTRO_READ', 'Read nostros', 'View nostro accounts', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb10', 'NOSTRO_WRITE', 'Write nostros', 'Create and update nostro accounts', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb11', 'CURRENCY_READ', 'Read currencies', 'View currencies', NOW(), NOW(), 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb12', 'CURRENCY_WRITE', 'Write currencies', 'Create and update currencies', NOW(), NOW(), 0);

INSERT INTO ad_role_permission (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id
FROM ad_permission
WHERE code IN (
    'DEAL_READ', 'DEAL_WRITE',
    'DEALTYPE_READ', 'DEALTYPE_WRITE',
    'INSTRUMENT_READ', 'INSTRUMENT_WRITE',
    'DEALTYPELIMIT_READ', 'DEALTYPELIMIT_WRITE',
    'NOSTRO_READ', 'NOSTRO_WRITE',
    'CURRENCY_READ', 'CURRENCY_WRITE'
);

INSERT INTO c_currency (id, iso_code, cur_symbol, description, std_precision, costing_precision, is_euro, is_emu_member, is_active, created_at, updated_at, version)
VALUES
    ('cccccccc-cccc-cccc-cccc-ccccccccccc1', 'NPR', 'Rs', 'Nepalese Rupee', 2, 4, FALSE, FALSE, TRUE, NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc2', 'USD', '$', 'US Dollar', 2, 4, FALSE, FALSE, TRUE, NOW(), NOW(), 0),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc3', 'EUR', '€', 'Euro', 2, 4, TRUE, TRUE, TRUE, NOW(), NOW(), 0);
