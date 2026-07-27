CREATE TABLE usage_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(40) NOT NULL,
    event_id VARCHAR(36) NULL,
    gallery_id VARCHAR(36) NULL,
    actor_user_id VARCHAR(36) NULL,
    public_access_id VARCHAR(36) NULL,
    quantity BIGINT NOT NULL DEFAULT 1,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usage_event_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_usage_event_gallery FOREIGN KEY (gallery_id) REFERENCES galleries(id),
    CONSTRAINT fk_usage_event_actor FOREIGN KEY (actor_user_id) REFERENCES users(id),
    CONSTRAINT fk_usage_event_public_access FOREIGN KEY (public_access_id) REFERENCES gallery_accesses(id)
);

