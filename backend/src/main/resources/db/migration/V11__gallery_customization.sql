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
