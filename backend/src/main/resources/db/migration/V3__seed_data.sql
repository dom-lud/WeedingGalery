-- Seed data dla środowiska developerskiego
INSERT INTO users (id, email, password_hash, system_role, failed_login_attempts, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin@example.com', '$2a$10$c1eO4x0R9HqV.H9hRj2E7.Nq05S6Jc.sZ3v4mR/W4Xm6D0cZ9f7dO', 'ADMIN', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
