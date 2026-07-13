ALTER TABLE events RENAME COLUMN owner_id TO owner_user_id;

ALTER TABLE events ADD COLUMN privacy_mode VARCHAR(50) NOT NULL DEFAULT 'PRIVATE';
ALTER TABLE events ADD COLUMN archived_at TIMESTAMP NULL;
ALTER TABLE events ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE events ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_events_owner_status ON events (owner_user_id, status);
CREATE INDEX idx_events_deleted_at ON events (deleted_at);

CREATE TABLE event_memberships (
    id VARCHAR(36) PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    removed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_event_membership_event_user UNIQUE (event_id, user_id),
    CONSTRAINT fk_event_memberships_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_event_memberships_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_event_memberships_user_role ON event_memberships (user_id, role);
CREATE INDEX idx_event_memberships_event_role ON event_memberships (event_id, role);
CREATE INDEX idx_event_memberships_event_removed ON event_memberships (event_id, removed_at);

ALTER TABLE audit_events ADD COLUMN event_id VARCHAR(36) NULL;
ALTER TABLE audit_events ADD COLUMN target_user_id VARCHAR(36) NULL;

CREATE INDEX idx_audit_events_event_id ON audit_events (event_id);
