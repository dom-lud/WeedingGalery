ALTER TABLE upload_sessions DROP INDEX uk_upload_access_idempotency;
ALTER TABLE upload_sessions
    ADD CONSTRAINT uk_upload_grant_idempotency UNIQUE (grant_fingerprint, idempotency_key);
