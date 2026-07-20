ALTER TABLE galleries RENAME COLUMN title TO name;

ALTER TABLE galleries ADD COLUMN sort_order INT NOT NULL DEFAULT 0;
ALTER TABLE galleries ADD COLUMN archived_at TIMESTAMP NULL;
ALTER TABLE galleries ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE galleries ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE galleries SET status = 'ACTIVE' WHERE status <> 'ARCHIVED';

ALTER TABLE galleries DROP COLUMN visibility;
ALTER TABLE galleries DROP COLUMN allow_upload;
ALTER TABLE galleries DROP COLUMN allow_download;
ALTER TABLE galleries DROP COLUMN require_approval;
ALTER TABLE galleries DROP COLUMN expires_at;

ALTER TABLE audit_events ADD COLUMN gallery_id VARCHAR(36) NULL;

CREATE INDEX idx_galleries_event_deleted_order ON galleries (event_id, deleted_at, sort_order);
CREATE INDEX idx_galleries_deleted_at ON galleries (deleted_at);
CREATE INDEX idx_audit_events_gallery_id ON audit_events (gallery_id);
