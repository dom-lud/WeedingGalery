UPDATE media_files
SET gallery_id = (SELECT id FROM galleries WHERE slug = 'guest-uploads-demo')
WHERE gallery_id = (SELECT id FROM galleries WHERE slug = 'guest-uploads-demo-2945795a');

UPDATE upload_sessions
SET gallery_id = (SELECT id FROM galleries WHERE slug = 'guest-uploads-demo')
WHERE gallery_id = (SELECT id FROM galleries WHERE slug = 'guest-uploads-demo-2945795a');

UPDATE gallery_accesses
SET gallery_id = (SELECT id FROM galleries WHERE slug = 'guest-uploads-demo')
WHERE gallery_id = (SELECT id FROM galleries WHERE slug = 'guest-uploads-demo-2945795a');

UPDATE galleries
SET storage_used_bytes = (
        SELECT COALESCE(SUM(COALESCE(size_bytes, expected_size_bytes)), 0)
        FROM media_files
        WHERE media_files.gallery_id = galleries.id
          AND media_files.status IN ('STORED', 'PROCESSING', 'PROCESSED', 'PROCESSING_FAILED')
    ),
    storage_reserved_bytes = 0,
    updated_at = CURRENT_TIMESTAMP
WHERE slug = 'guest-uploads-demo';

UPDATE galleries
SET slug = 'guest-uploads-demo-retired-2945795a',
    public_view_enabled = FALSE,
    upload_enabled = FALSE,
    download_enabled = FALSE,
    archived_at = CURRENT_TIMESTAMP,
    deleted_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE slug = 'guest-uploads-demo-2945795a';

UPDATE galleries
SET slug = 'guest-uploads-demo-2945795a',
    public_view_enabled = TRUE,
    upload_enabled = TRUE,
    download_enabled = FALSE,
    moderation_mode = 'REQUIRED',
    access_code_hash = '$2a$10$d29D1xGpzpJcB.kh4rsl7u9IoDyGYfci8FL27UVMhpYgdSrQ1yT5u',
    updated_at = CURRENT_TIMESTAMP
WHERE slug = 'guest-uploads-demo';
