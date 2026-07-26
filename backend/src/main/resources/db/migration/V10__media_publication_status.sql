ALTER TABLE media_files ADD COLUMN publication_status VARCHAR(20) NULL;

UPDATE media_files
SET publication_status = CASE WHEN (SELECT moderation_mode FROM galleries WHERE galleries.id = media_files.gallery_id) = 'NONE'
    THEN 'APPROVED' ELSE 'PENDING' END
WHERE publication_status IS NULL;

ALTER TABLE media_files MODIFY COLUMN publication_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
CREATE INDEX idx_media_gallery_publication_status ON media_files (gallery_id, publication_status, status, stored_at);
