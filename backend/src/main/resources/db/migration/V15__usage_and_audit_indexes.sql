CREATE INDEX idx_usage_events_event_time ON usage_events (event_id, occurred_at, event_type);
CREATE INDEX idx_usage_events_gallery_time ON usage_events (gallery_id, occurred_at, event_type);
CREATE INDEX idx_usage_events_type_time ON usage_events (event_type, occurred_at);
CREATE INDEX idx_audit_events_filter_time ON audit_events (event_type, actor_type, created_at);
CREATE INDEX idx_audit_events_gallery_time ON audit_events (gallery_id, created_at);
CREATE INDEX idx_audit_events_event_time ON audit_events (event_id, created_at);
CREATE INDEX idx_notifications_status_created ON notifications (audience, status, created_at);

