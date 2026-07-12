-- Seed data dla środowiska developerskiego
INSERT INTO users (id, email, password_hash, system_role, failed_login_attempts, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin@example.com', '$2a$10$ikl3ddSI2Mil0Zdq57fzQuTBiPXjM3lOnJhhi6bznAAf3ODMJjrvq', 'ADMIN', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
