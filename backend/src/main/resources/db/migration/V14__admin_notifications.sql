CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    audience VARCHAR(20) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    dedupe_key VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    resource_type VARCHAR(50) NULL,
    resource_id VARCHAR(36) NULL,
    created_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP NULL,
    acknowledged_by VARCHAR(255) NULL,
    version BIGINT NOT NULL DEFAULT 0
);

