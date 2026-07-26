ALTER TABLE gallery_customizations
    ADD COLUMN lock_version BIGINT NOT NULL DEFAULT 0;
