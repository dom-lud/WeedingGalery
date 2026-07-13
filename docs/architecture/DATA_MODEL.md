# Model Danych

## Cel dokumentu
Opisuje docelowy model danych, główne encje, relacje, indeksy i zasady ownership.

## Status dokumentu
- Status: draft
- Zakres: model danych dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Zaimplementowane sa tabele `users`, `events`, `event_memberships`, `galleries` i `audit_events` zarzadzane przez Flyway.
- Etap 3 dodal `privacy_mode`, lifecycle/soft delete i optimistic version wydarzenia oraz historyczne membership managerow.
- Etap 4 zaimplementowal `Gallery` w zakresie GALLERY-001: scoped management API, stabilny slug, kolejnosc, lifecycle, soft delete, optimistic version i audyt.
- Pozostale encje opisane ponizej nadal stanowia stan docelowy.

## Stan docelowy
- Relacyjny model danych zoptymalizowany pod wieloużytkownikowość, audyt i pracę na plikach.

## Zasady modelowania
- Każdy zasób biznesowy ma właściciela lub kontekst wydarzenia.
- Dane współdzielone między wydarzeniami są ograniczone do bytów systemowych.
- Soft delete stosujemy tam, gdzie potrzebna jest retencja, audyt lub przywracanie.
- Indeksy muszą wspierać typowe zapytania zawężane po `owner_id`, `event_id`, `gallery_id`, statusie i dacie.

## Encje
### User
- Przeznaczenie: konto użytkownika.
- Pola: `id`, `email`, `password_hash`, `first_name`, `last_name`, `status`, `locale`, `profile_image_key`, `created_at`, `updated_at`, `deleted_at`.
- Relacje: 1:N do `UserSession`, `Event`, `EventMembership`, `EventInvitation`, `UserSubscription`, `Notification`.
- Ograniczenia: unikalny `email`, status aktywności, soft delete.
- Indeksy: `email`, `status`.

### UserSession
- Przeznaczenie: aktywna lub historyczna sesja użytkownika.
- Pola: `id`, `user_id`, `token_hash`, `refresh_token_hash`, `device_info`, `ip_address`, `expires_at`, `revoked_at`, `created_at`.
- Relacje: N:1 do `User`.
- Indeksy: `user_id`, `expires_at`, `revoked_at`.

### EmailVerificationToken
- Przeznaczenie: potwierdzenie adresu e-mail.
- Pola: `id`, `user_id`, `token_hash`, `expires_at`, `used_at`, `created_at`.
- Ograniczenia: tylko aktywny token na użytkownika według polityki implementacyjnej.

### PasswordResetToken
- Przeznaczenie: reset hasła.
- Pola: `id`, `user_id`, `token_hash`, `expires_at`, `used_at`, `created_at`.

### Event
- Przeznaczenie: główny byt biznesowy grupujący galerie i członków.
- Pola zaimplementowane: `id`, `owner_user_id`, `name`, `type`, `event_date`, `description`, `status`, `privacy_mode`, `archived_at`, `created_at`, `updated_at`, `deleted_at`, `version`.
- Relacje: 1:N do `Gallery`, `EventMembership`, `EventInvitation`, `StorageUsage`.
- Indeksy: `owner_user_id`, `(owner_user_id, status)`, `event_date`.

### EventMembership
- Przeznaczenie: rola użytkownika w wydarzeniu.
- Pola zaimplementowane: `id`, `event_id`, `user_id`, `role`, `joined_at`, `removed_at`, `created_at`, `updated_at`, `version`.
- Aktualnie membership przechowuje `MANAGER`; `OWNER` pozostaje wyliczany z wydarzenia.
- Ograniczenia: unikalność `(event_id, user_id)`.
- Indeksy: `(user_id, role)`, `(event_id, role)`.

### EventInvitation
- Przeznaczenie: zaproszenie do wydarzenia.
- Pola: `id`, `event_id`, `invited_email`, `invited_user_id`, `role`, `token_hash`, `status`, `expires_at`, `accepted_at`, `created_by_user_id`, `created_at`.
- Indeksy: `(event_id, invited_email)`, `token_hash`, `status`.

### Gallery
- Przeznaczenie: galeria w ramach wydarzenia.
- Pola zaimplementowane: `id`, `event_id`, `name`, `slug`, `description`, `status`, `sort_order`, `archived_at`, `deleted_at`, `created_at`, `updated_at`, `version`.
- Pola planowane w kolejnych etapach: `cover_media_file_id`, `access_code_hash`, `visibility_mode`, `upload_enabled`, `download_enabled`, `public_view_enabled`, `moderation_mode`, `published_at`, `expires_at`, `theme_key`.
- Ograniczenia: `slug` jest globalnie unikalny i pozostaje zarezerwowany po soft delete; galeria zawsze nalezy do jednego wydarzenia.
- Indeksy: `(event_id, status)`, `(event_id, deleted_at, sort_order)`, `deleted_at`, unikalny `slug`.

### GalleryAccess
- Przeznaczenie: kontrola dostępu publicznego.
- Pola: `id`, `gallery_id`, `access_type`, `token_hash`, `requires_passcode`, `expires_at`, `revoked_at`, `created_at`.
- Indeksy: `(gallery_id, access_type)`, `token_hash`.

### MediaFile
- Przeznaczenie: metadane zdjęcia lub filmu.
- Pola: `id`, `owner_user_id`, `event_id`, `gallery_id`, `upload_session_id`, `original_filename`, `storage_filename`, `storage_key`, `mime_type`, `size_bytes`, `media_type`, `status`, `uploaded_by_guest_name`, `checksum_sha256`, `thumbnail_status`, `metadata_json`, `moderation_flag`, `uploaded_at`, `published_at`, `deleted_at`.
- Relacje: N:1 do `Event`, `Gallery`, `UploadSession`; 1:N do `MediaThumbnail`, `MediaProcessingJob`.
- Indeksy: `gallery_id`, `(gallery_id, status)`, `event_id`, `checksum_sha256`.

### MediaProcessingJob
- Przeznaczenie: zadanie przetwarzania mediów.
- Pola: `id`, `media_file_id`, `job_type`, `status`, `attempt_count`, `last_error_code`, `last_error_message`, `scheduled_at`, `started_at`, `finished_at`, `created_at`.
- Indeksy: `(status, scheduled_at)`, `(media_file_id, job_type)`.

### MediaThumbnail
- Przeznaczenie: zapis wygenerowanych miniaturek lub podglądów.
- Pola: `id`, `media_file_id`, `variant`, `storage_key`, `width`, `height`, `size_bytes`, `created_at`.
- Ograniczenia: unikalność `(media_file_id, variant)`.

### UploadSession
- Przeznaczenie: grupuje upload jednego lub wielu plików.
- Pola: `id`, `event_id`, `gallery_id`, `initiator_type`, `initiator_user_id`, `guest_display_name`, `status`, `total_files`, `uploaded_files`, `failed_files`, `client_fingerprint`, `created_at`, `completed_at`.
- Indeksy: `gallery_id`, `(event_id, created_at)`, `status`.

### DownloadArchive
- Przeznaczenie: asynchronicznie generowane archiwum ZIP.
- Pola: `id`, `event_id`, `gallery_id`, `requested_by_user_id`, `scope_type`, `status`, `storage_key`, `expires_at`, `download_token_hash`, `error_message`, `created_at`, `completed_at`.
- Indeksy: `(requested_by_user_id, created_at)`, `(status, created_at)`.

### StorageUsage
- Przeznaczenie: agregacja wykorzystania storage.
- Pola: `id`, `user_id`, `event_id`, `gallery_id`, `bytes_used`, `bytes_soft_deleted`, `calculated_at`.
- Indeksy: `user_id`, `event_id`, `gallery_id`.

### SubscriptionPlan
- Przeznaczenie: definicja planu i limitów.
- Pola: `id`, `code`, `name`, `status`, `max_storage_bytes`, `max_events`, `max_galleries_per_event`, `max_file_size_bytes`, `retention_days`, `zip_download_enabled`, `max_event_managers`, `customization_level`, `created_at`, `updated_at`.
- Ograniczenia: unikalny `code`.

### UserSubscription
- Przeznaczenie: przypisanie planu do użytkownika.
- Pola: `id`, `user_id`, `plan_id`, `status`, `starts_at`, `ends_at`, `assigned_by_admin_id`, `created_at`, `updated_at`.
- Indeksy: `user_id`, `(status, ends_at)`.

### Notification
- Przeznaczenie: zapis zdarzeń notyfikacyjnych.
- Pola: `id`, `user_id`, `channel`, `type`, `status`, `payload_json`, `sent_at`, `failed_at`, `created_at`.
- Indeksy: `(user_id, created_at)`, `(type, status)`.

### AuditLog
- Przeznaczenie: ślad audytowy.
- Pola: `id`, `actor_type`, `actor_user_id`, `action_type`, `resource_type`, `resource_id`, `event_id`, `result`, `ip_address`, `correlation_id`, `context_json`, `created_at`.
- Indeksy: `(resource_type, resource_id)`, `(actor_user_id, created_at)`, `event_id`, `action_type`.

### SystemSetting
- Przeznaczenie: ustawienia konfiguracyjne systemu.
- Pola: `id`, `setting_key`, `setting_value`, `value_type`, `updated_by_admin_id`, `updated_at`.
- Ograniczenia: unikalny `setting_key`.

### AdminAction
- Przeznaczenie: jawny zapis działań administracyjnych.
- Pola: `id`, `admin_user_id`, `action_type`, `target_type`, `target_id`, `reason`, `status`, `details_json`, `created_at`.
- Indeksy: `(admin_user_id, created_at)`, `(target_type, target_id)`.

## Diagram ERD
```mermaid
erDiagram
    User ||--o{ UserSession : has
    User ||--o{ EmailVerificationToken : verifies
    User ||--o{ PasswordResetToken : resets
    User ||--o{ Event : owns
    User ||--o{ EventMembership : participates
    User ||--o{ UserSubscription : subscribes
    User ||--o{ Notification : receives
    Event ||--o{ EventMembership : contains
    Event ||--o{ EventInvitation : contains
    Event ||--o{ Gallery : contains
    Event ||--o{ MediaFile : scopes
    Event ||--o{ UploadSession : scopes
    Event ||--o{ DownloadArchive : scopes
    Event ||--o{ StorageUsage : tracks
    Gallery ||--o{ GalleryAccess : exposes
    Gallery ||--o{ MediaFile : contains
    Gallery ||--o{ UploadSession : receives
    MediaFile ||--o{ MediaThumbnail : has
    MediaFile ||--o{ MediaProcessingJob : spawns
    UploadSession ||--o{ MediaFile : groups
    SubscriptionPlan ||--o{ UserSubscription : defines
    User ||--o{ AuditLog : produces
    User ||--o{ AdminAction : performs
```

## Zasady usuwania
- `User`, `Event`, `Gallery`, `MediaFile` wspierają soft delete.
- Hard delete plików wymaga skoordynowanego usunięcia rekordu i obiektu storage.
- `AuditLog` i `AdminAction` nie powinny być usuwane operacyjnie poza polityką retencji zgodną z wymaganiami prywatności.

## Ownership
- `Event.owner_user_id` jest główną osią ownership.
- `Gallery.event_id` dziedziczy ownership z wydarzenia.
- `MediaFile` musi wskazywać `event_id` i `gallery_id`; opcjonalnie `owner_user_id` wskazuje konto właściciela wydarzenia.
- Dostęp użytkownika jest wyliczany przez ownership albo `EventMembership`.

## Powiązane dokumenty
- [MULTI_TENANCY.md](MULTI_TENANCY.md)
- [../backend/DATABASE_CONVENTIONS.md](../backend/DATABASE_CONVENTIONS.md)
- [../product/PERMISSIONS_MATRIX.md](../product/PERMISSIONS_MATRIX.md)

## Decyzje otwarte
- Publiczne znaczenie slugu i dodatkowy alias moga zostac rozszerzone dopiero wraz z zaakceptowaniem ADR 0010; GALLERY-001 utrzymuje globalna unikalnosc.
