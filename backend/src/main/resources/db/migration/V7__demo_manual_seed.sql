UPDATE users
SET password_hash = '$2a$10$d29D1xGpzpJcB.kh4rsl7u9IoDyGYfci8FL27UVMhpYgdSrQ1yT5u',
    system_role = 'ADMIN',
    failed_login_attempts = 0,
    locked_until = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'admin@example.com';

INSERT INTO users (id, email, password_hash, system_role, failed_login_attempts, created_at, updated_at)
SELECT '00000000-0000-0000-0000-000000000701',
       'admin@example.com',
       '$2a$10$d29D1xGpzpJcB.kh4rsl7u9IoDyGYfci8FL27UVMhpYgdSrQ1yT5u',
       'ADMIN',
       0,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@example.com');

UPDATE users
SET password_hash = '$2a$10$d29D1xGpzpJcB.kh4rsl7u9IoDyGYfci8FL27UVMhpYgdSrQ1yT5u',
    system_role = 'USER',
    failed_login_attempts = 0,
    locked_until = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE email IN ('owner@example.com', 'manager@example.com', 'guest-tester@example.com');

INSERT INTO users (id, email, password_hash, system_role, failed_login_attempts, created_at, updated_at)
SELECT '00000000-0000-0000-0000-000000000702',
       'owner@example.com',
       '$2a$10$d29D1xGpzpJcB.kh4rsl7u9IoDyGYfci8FL27UVMhpYgdSrQ1yT5u',
       'USER',
       0,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'owner@example.com');

INSERT INTO users (id, email, password_hash, system_role, failed_login_attempts, created_at, updated_at)
SELECT '00000000-0000-0000-0000-000000000703',
       'manager@example.com',
       '$2a$10$d29D1xGpzpJcB.kh4rsl7u9IoDyGYfci8FL27UVMhpYgdSrQ1yT5u',
       'USER',
       0,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'manager@example.com');

INSERT INTO users (id, email, password_hash, system_role, failed_login_attempts, created_at, updated_at)
SELECT '00000000-0000-0000-0000-000000000704',
       'guest-tester@example.com',
       '$2a$10$d29D1xGpzpJcB.kh4rsl7u9IoDyGYfci8FL27UVMhpYgdSrQ1yT5u',
       'USER',
       0,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'guest-tester@example.com');

UPDATE events
SET owner_user_id = (SELECT id FROM users WHERE email = 'owner@example.com'),
    name = 'Demo Wedding Manual',
    type = 'WEDDING',
    event_date = '2026-08-15',
    description = 'Dane demo do recznego klikania',
    status = 'DRAFT',
    privacy_mode = 'PRIVATE',
    archived_at = NULL,
    deleted_at = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE id = '00000000-0000-0000-0000-000000000711';

INSERT INTO events (id, name, type, event_date, description, status, owner_user_id, privacy_mode, archived_at,
                    deleted_at, created_at, updated_at, version)
SELECT '00000000-0000-0000-0000-000000000711',
       'Demo Wedding Manual',
       'WEDDING',
       '2026-08-15',
       'Dane demo do recznego klikania',
       'DRAFT',
       users.id,
       'PRIVATE',
       NULL,
       NULL,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       0
FROM users
WHERE users.email = 'owner@example.com'
  AND NOT EXISTS (SELECT 1 FROM events WHERE id = '00000000-0000-0000-0000-000000000711');

UPDATE event_memberships
SET role = 'MANAGER',
    removed_at = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE event_id = '00000000-0000-0000-0000-000000000711'
  AND user_id = (SELECT id FROM users WHERE email = 'manager@example.com');

INSERT INTO event_memberships (id, event_id, user_id, role, joined_at, removed_at, created_at, updated_at, version)
SELECT '00000000-0000-0000-0000-000000000712',
       events.id,
       users.id,
       'MANAGER',
       CURRENT_TIMESTAMP,
       NULL,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       0
FROM events
JOIN users ON users.email = 'manager@example.com'
WHERE events.id = '00000000-0000-0000-0000-000000000711'
  AND NOT EXISTS (
      SELECT 1
      FROM event_memberships
      WHERE event_id = '00000000-0000-0000-0000-000000000711'
        AND user_id = users.id
  );

UPDATE galleries
SET event_id = '00000000-0000-0000-0000-000000000711',
    slug = 'guest-uploads-demo',
    name = 'Guest Uploads Demo',
    description = 'Galeria demo z publicznym uploadem',
    status = 'ACTIVE',
    sort_order = 10,
    archived_at = NULL,
    deleted_at = NULL,
    public_view_enabled = TRUE,
    upload_enabled = TRUE,
    download_enabled = FALSE,
    moderation_mode = 'REQUIRED',
    access_code_hash = '$2a$10$d29D1xGpzpJcB.kh4rsl7u9IoDyGYfci8FL27UVMhpYgdSrQ1yT5u',
    published_at = NULL,
    expires_at = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE id = '00000000-0000-0000-0000-000000000721'
   OR slug = 'guest-uploads-demo';

INSERT INTO galleries (id, event_id, slug, name, description, status, sort_order, archived_at, deleted_at, version,
                       public_view_enabled, upload_enabled, download_enabled, moderation_mode, access_code_hash,
                       published_at, expires_at, storage_used_bytes, storage_reserved_bytes, created_at, updated_at)
SELECT '00000000-0000-0000-0000-000000000721',
       '00000000-0000-0000-0000-000000000711',
       'guest-uploads-demo',
       'Guest Uploads Demo',
       'Galeria demo z publicznym uploadem',
       'ACTIVE',
       10,
       NULL,
       NULL,
       0,
       TRUE,
       TRUE,
       FALSE,
       'REQUIRED',
       '$2a$10$d29D1xGpzpJcB.kh4rsl7u9IoDyGYfci8FL27UVMhpYgdSrQ1yT5u',
       NULL,
       NULL,
       0,
       0,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM galleries WHERE slug = 'guest-uploads-demo');

UPDATE gallery_accesses
SET gallery_id = (SELECT id FROM galleries WHERE slug = 'guest-uploads-demo'),
    token_hash = '7203fdb1bf057575dd9c46070322fa9819d70219e686d399eb822e3cb171e28e',
    revoked_at = NULL
WHERE id = '00000000-0000-0000-0000-000000000731'
   OR token_hash = '7203fdb1bf057575dd9c46070322fa9819d70219e686d399eb822e3cb171e28e';

INSERT INTO gallery_accesses (id, gallery_id, token_hash, revoked_at, created_at, version)
SELECT '00000000-0000-0000-0000-000000000731',
       galleries.id,
       '7203fdb1bf057575dd9c46070322fa9819d70219e686d399eb822e3cb171e28e',
       NULL,
       CURRENT_TIMESTAMP,
       0
FROM galleries
WHERE galleries.slug = 'guest-uploads-demo'
  AND NOT EXISTS (
      SELECT 1
      FROM gallery_accesses
      WHERE token_hash = '7203fdb1bf057575dd9c46070322fa9819d70219e686d399eb822e3cb171e28e'
  );
