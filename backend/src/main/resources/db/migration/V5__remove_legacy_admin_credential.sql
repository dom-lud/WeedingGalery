UPDATE users
SET password_hash = 'BOOTSTRAP_DISABLED',
    failed_login_attempts = 0,
    locked_until = '2038-01-18 23:59:59',
    updated_at = CURRENT_TIMESTAMP
WHERE id = '00000000-0000-0000-0000-000000000001'
  AND system_role = 'ADMIN'
  AND password_hash = '$2a$10$ikl3ddSI2Mil0Zdq57fzQuTBiPXjM3lOnJhhi6bznAAf3ODMJjrvq';

DELETE FROM users
WHERE id = '00000000-0000-0000-0000-000000000001'
  AND password_hash = 'BOOTSTRAP_DISABLED'
  AND NOT EXISTS (
      SELECT 1 FROM events
      WHERE events.owner_user_id = '00000000-0000-0000-0000-000000000001'
  )
  AND NOT EXISTS (
      SELECT 1 FROM event_memberships
      WHERE event_memberships.user_id = '00000000-0000-0000-0000-000000000001'
  );
