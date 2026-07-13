ALTER TABLE galleries ADD COLUMN public_view_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE galleries ADD COLUMN upload_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE galleries ADD COLUMN download_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE galleries ADD COLUMN moderation_mode VARCHAR(30) NOT NULL DEFAULT 'REQUIRED';
ALTER TABLE galleries ADD COLUMN access_code_hash VARCHAR(100) NULL;
ALTER TABLE galleries ADD COLUMN published_at TIMESTAMP NULL;
ALTER TABLE galleries ADD COLUMN expires_at TIMESTAMP NULL;
ALTER TABLE galleries ADD COLUMN storage_used_bytes BIGINT NOT NULL DEFAULT 0;
ALTER TABLE galleries ADD COLUMN storage_reserved_bytes BIGINT NOT NULL DEFAULT 0;

CREATE TABLE gallery_accesses (
    id VARCHAR(36) PRIMARY KEY,
    gallery_id VARCHAR(36) NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_gallery_access_gallery FOREIGN KEY (gallery_id) REFERENCES galleries(id)
);

CREATE INDEX idx_gallery_access_gallery_active ON gallery_accesses (gallery_id, revoked_at);

ALTER TABLE audit_events MODIFY COLUMN user_email VARCHAR(255) NULL;
ALTER TABLE audit_events ADD COLUMN actor_type VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE audit_events ADD COLUMN public_access_id VARCHAR(36) NULL;

CREATE TABLE upload_sessions (
    id VARCHAR(36) PRIMARY KEY,
    gallery_id VARCHAR(36) NOT NULL,
    public_access_id VARCHAR(36) NOT NULL,
    grant_fingerprint CHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_fingerprint CHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_files INT NOT NULL,
    total_bytes BIGINT NOT NULL,
    reserved_bytes BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    cancelled_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_upload_access_idempotency UNIQUE (public_access_id, idempotency_key),
    CONSTRAINT fk_upload_session_gallery FOREIGN KEY (gallery_id) REFERENCES galleries(id),
    CONSTRAINT fk_upload_session_access FOREIGN KEY (public_access_id) REFERENCES gallery_accesses(id)
);

CREATE TABLE media_files (
    id VARCHAR(36) PRIMARY KEY,
    upload_session_id VARCHAR(36) NOT NULL,
    gallery_id VARCHAR(36) NOT NULL,
    client_file_id VARCHAR(100) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    expected_size_bytes BIGINT NOT NULL,
    size_bytes BIGINT NULL,
    declared_content_type VARCHAR(100) NOT NULL,
    detected_content_type VARCHAR(100) NULL,
    media_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    checksum_sha256 CHAR(64) NULL,
    failure_code VARCHAR(100) NULL,
    stored_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_media_session_client_file UNIQUE (upload_session_id, client_file_id),
    CONSTRAINT fk_media_upload_session FOREIGN KEY (upload_session_id) REFERENCES upload_sessions(id),
    CONSTRAINT fk_media_gallery FOREIGN KEY (gallery_id) REFERENCES galleries(id)
);

CREATE INDEX idx_upload_gallery_status ON upload_sessions (gallery_id, status, created_at);
CREATE INDEX idx_upload_access_status ON upload_sessions (public_access_id, status, created_at);
CREATE INDEX idx_media_session_status ON media_files (upload_session_id, status);
CREATE INDEX idx_media_gallery_status ON media_files (gallery_id, status, stored_at);
