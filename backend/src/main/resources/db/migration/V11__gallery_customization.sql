CREATE TABLE gallery_customizations (
    id VARCHAR(36) NOT NULL,
    gallery_id VARCHAR(36) NOT NULL,
    theme_key VARCHAR(20) NOT NULL,
    layout_mode VARCHAR(20) NOT NULL,
    primary_color CHAR(7) NOT NULL,
    accent_color CHAR(7) NOT NULL,
    welcome_text VARCHAR(1000) NULL,
    cover_media_id VARCHAR(36) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_gallery_customization_gallery UNIQUE (gallery_id),
    CONSTRAINT fk_gallery_customization_gallery FOREIGN KEY (gallery_id) REFERENCES galleries(id),
    CONSTRAINT fk_gallery_customization_cover FOREIGN KEY (cover_media_id) REFERENCES media_files(id)
);

CREATE INDEX idx_gallery_customization_cover ON gallery_customizations (cover_media_id);

-- The public gallery read model keeps the first-iteration appearance flags on
-- galleries so the existing gallery/public-access projections can read them
-- without joining a private customization aggregate.
ALTER TABLE galleries ADD COLUMN theme VARCHAR(20) NOT NULL DEFAULT 'EDITORIAL';
ALTER TABLE galleries ADD COLUMN layout VARCHAR(20) NOT NULL DEFAULT 'GRID';
ALTER TABLE galleries ADD COLUMN primary_color CHAR(7) NOT NULL DEFAULT '#74465A';
ALTER TABLE galleries ADD COLUMN accent_color CHAR(7) NOT NULL DEFAULT '#B47B4C';
ALTER TABLE galleries ADD COLUMN background_color CHAR(7) NOT NULL DEFAULT '#F7F4F2';
ALTER TABLE galleries ADD COLUMN welcome_text VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE galleries ADD COLUMN show_title BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE galleries ADD COLUMN show_upload BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE galleries ADD COLUMN show_download BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE galleries ADD COLUMN cover_media_id VARCHAR(36) NULL;
