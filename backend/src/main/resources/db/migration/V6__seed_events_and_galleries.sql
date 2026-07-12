-- Seed script for events and galleries
-- Wymaga istniejącego admin@example.com (z V3__seed_data.sql) o UUID '00000000-0000-0000-0000-000000000001'

INSERT INTO events (id, name, type, event_date, description, status, owner_id, created_at, updated_at)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Wesele Ani i Tomka',
    'WEDDING',
    '2026-08-15',
    'Nasze wielkie wesele. Prosimy o dodawanie zdjęć!',
    'PUBLISHED',
    '00000000-0000-0000-0000-000000000001',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO galleries (id, event_id, slug, title, description, status, visibility, allow_upload, allow_download, require_approval, expires_at, created_at, updated_at)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'aniatomek2026',
    'Główna Galeria Weselna',
    'Wrzucajcie tutaj wszystkie zdjęcia z zabawy!',
    'ACTIVE',
    'PUBLIC',
    TRUE,
    TRUE,
    FALSE,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
