CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    system_role VARCHAR(50) NOT NULL DEFAULT 'USER',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    details TEXT,
    event_id VARCHAR(36) NULL,
    target_user_id VARCHAR(36) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    event_date DATE NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    privacy_mode VARCHAR(50) NOT NULL DEFAULT 'PRIVATE',
    archived_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_events_owner FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

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

CREATE TABLE galleries (
    id VARCHAR(36) PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    allow_upload BOOLEAN NOT NULL,
    allow_download BOOLEAN NOT NULL,
    require_approval BOOLEAN NOT NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_galleries_event FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE INDEX idx_events_owner_status ON events (owner_user_id, status);
CREATE INDEX idx_events_deleted_at ON events (deleted_at);
CREATE INDEX idx_event_memberships_user_role ON event_memberships (user_id, role);
CREATE INDEX idx_event_memberships_event_role ON event_memberships (event_id, role);
CREATE INDEX idx_event_memberships_event_removed ON event_memberships (event_id, removed_at);
CREATE INDEX idx_audit_events_event_id ON audit_events (event_id);
CREATE INDEX idx_galleries_event_status ON galleries (event_id, status);

INSERT INTO users (id, email, password_hash, system_role, failed_login_attempts, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@example.com',
    '$2a$10$ikl3ddSI2Mil0Zdq57fzQuTBiPXjM3lOnJhhi6bznAAf3ODMJjrvq',
    'ADMIN',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

