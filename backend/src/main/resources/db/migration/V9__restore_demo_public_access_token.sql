UPDATE gallery_accesses
SET gallery_id = (SELECT id FROM galleries WHERE slug = 'guest-uploads-demo-2945795a'),
    revoked_at = NULL
WHERE token_hash = '7203fdb1bf057575dd9c46070322fa9819d70219e686d399eb822e3cb171e28e';

INSERT INTO gallery_accesses (id, gallery_id, token_hash, revoked_at, created_at, version)
SELECT '00000000-0000-0000-0000-000000000731',
       galleries.id,
       '7203fdb1bf057575dd9c46070322fa9819d70219e686d399eb822e3cb171e28e',
       NULL,
       CURRENT_TIMESTAMP,
       0
FROM galleries
WHERE galleries.slug = 'guest-uploads-demo-2945795a'
  AND NOT EXISTS (
      SELECT 1
      FROM gallery_accesses
      WHERE token_hash = '7203fdb1bf057575dd9c46070322fa9819d70219e686d399eb822e3cb171e28e'
  );
