ALTER TABLE media_files ADD COLUMN width INT NULL;
ALTER TABLE media_files ADD COLUMN height INT NULL;
ALTER TABLE media_files ADD COLUMN processed_at TIMESTAMP NULL;

CREATE TABLE media_processing_jobs (
    id VARCHAR(36) PRIMARY KEY,
    media_file_id VARCHAR(36) NOT NULL,
    job_type VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    last_error_code VARCHAR(100) NULL,
    last_error_message VARCHAR(500) NULL,
    scheduled_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP NULL,
    finished_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    locked_by VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_media_processing_job_type UNIQUE (media_file_id, job_type),
    CONSTRAINT fk_media_processing_job_media FOREIGN KEY (media_file_id) REFERENCES media_files(id)
);

CREATE INDEX idx_media_processing_due ON media_processing_jobs (status, scheduled_at);
CREATE INDEX idx_media_processing_lock ON media_processing_jobs (status, locked_at);

CREATE TABLE media_thumbnails (
    id VARCHAR(36) PRIMARY KEY,
    media_file_id VARCHAR(36) NOT NULL,
    variant VARCHAR(40) NOT NULL,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    width INT NOT NULL,
    height INT NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_media_thumbnail_variant UNIQUE (media_file_id, variant),
    CONSTRAINT fk_media_thumbnail_media FOREIGN KEY (media_file_id) REFERENCES media_files(id)
);
