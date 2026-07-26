# Endpointy API

## Cel dokumentu
Prezentuje docelową strukturę endpointów REST dla wszystkich głównych obszarów systemu.

## Status dokumentu
- Status: draft
- Zakres: katalog planowanych endpointów
- Ostatnia aktualizacja: 2026-07-20

## Stan obecny
- Endpointy auth, events, memberships, galleries, public access, upload oraz
  pierwsza iteracja etapow 7-10 oznaczone ponizej sa zaimplementowane.
- Pozostale endpointy sa planowane; ich obecność na liscie nie oznacza istnienia w runtime.

## Stan docelowy
- Spójne REST API dla użytkowników, gości i administratorów.

## Authentication
- `GET /api/auth/csrf` - zaimplementowany bootstrap CSRF dla SPA
- `GET /api/auth/me` - zaimplementowany odczyt bieżącej sesji
- `POST /api/auth/register` - zaimplementowane utworzenie konta przez administratora
- `POST /api/auth/verify-email`
- `POST /api/auth/login` - zaimplementowany
- `POST /api/auth/logout` - zaimplementowany
- `POST /api/auth/logout-all`
- `POST /api/auth/refresh`
- `POST /api/auth/password-reset/request`
- `POST /api/auth/password-reset/confirm`
- `POST /api/auth/change-password`

## Users i profile
- `GET /api/users/me`
- `PATCH /api/users/me`
- `DELETE /api/users/me`
- `GET /api/users/me/sessions`
- `DELETE /api/users/me/sessions/{sessionId}`
- `GET /api/profile/storage-usage`
- `GET /api/profile/invitations`

## Events
- `GET /api/events` - zaimplementowany
- `POST /api/events` - zaimplementowany
- `GET /api/events/{eventId}` - zaimplementowany
- `PUT /api/events/{eventId}` - zaimplementowany
- `POST /api/events/{eventId}/archive` - zaimplementowany
- `DELETE /api/events/{eventId}` - zaimplementowany jako soft delete
- `GET /api/events/{eventId}/statistics`

## Memberships i invitations
- `GET /api/events/{eventId}/members` - zaimplementowany
- `POST /api/events/{eventId}/members` - zaimplementowane bezposrednie dodanie istniejacego konta
- `POST /api/events/{eventId}/members/invitations`
- `POST /api/events/{eventId}/members/invitations/{invitationId}/resend`
- `DELETE /api/events/{eventId}/members/invitations/{invitationId}`
- `POST /api/invitations/{token}/accept`
- `POST /api/invitations/{token}/reject`
- `PATCH /api/events/{eventId}/members/{membershipId}`
- `DELETE /api/events/{eventId}/members/{membershipId}` - zaimplementowany
- `POST /api/events/{eventId}/ownership-transfer` - zaimplementowany

## Galleries
- `GET /api/events/{eventId}/galleries` - zaimplementowany GALLERY-001
- `POST /api/events/{eventId}/galleries` - zaimplementowany GALLERY-001
- `GET /api/events/{eventId}/galleries/{galleryId}` - zaimplementowany GALLERY-001
- `PUT /api/events/{eventId}/galleries/{galleryId}` - zaimplementowany GALLERY-001
- `POST /api/events/{eventId}/galleries/{galleryId}/archive` - zaimplementowany GALLERY-001
- `DELETE /api/events/{eventId}/galleries/{galleryId}` - zaimplementowany soft delete GALLERY-001
- `GET /api/events/{eventId}/galleries/{galleryId}/settings` - zaimplementowany Etap 4B
- `PUT /api/events/{eventId}/galleries/{galleryId}/settings` - zaimplementowany Etap 4B, owner-only
- `POST /api/events/{eventId}/galleries/{galleryId}/access-token/rotate` - zaimplementowany Etap 4B, owner-only
- `PUT /api/events/{eventId}/galleries/{galleryId}/access-code` - zaimplementowany Etap 4B, owner-only
- `DELETE /api/events/{eventId}/galleries/{galleryId}/access-code` - zaimplementowany Etap 4B, owner-only
- `GET /api/events/{eventId}/galleries/{galleryId}/media` - zaimplementowany Etap 6, owner/manager, zwraca bezpieczne URL-e podgladu mediow
- `GET /api/events/{eventId}/galleries/{galleryId}/media/{mediaId}/thumbnail` - zaimplementowany Etap 6, owner/manager, kontrolowany streaming miniatury albo oryginalu jako fallback
- `GET /api/events/{eventId}/galleries/{galleryId}/media/{mediaId}/content` - zaimplementowany Etap 6, owner/manager, kontrolowany streaming oryginalu
- `GET /api/events/{eventId}/galleries/{galleryId}/download` - zaimplementowany Etap 6, owner-only ZIP galerii
- `POST /api/galleries/{galleryId}/publish`
- `POST /api/galleries/{galleryId}/disable-upload`
- `POST /api/galleries/{galleryId}/enable-upload`
- `POST /api/galleries/{galleryId}/qr`

## Public galleries
- `GET /api/public/galleries/{slug}` - zaimplementowany Etap 7, wymaga grantu sesyjnego i zwraca tylko zatwierdzone media
- `POST /api/public/galleries/{slug}/access` - zaimplementowany Etap 4B, token + opcjonalny kod
- `GET /api/public/galleries/{slug}/media/{mediaId}/thumbnail` - zaimplementowany Etap 7, wymaga grantu sesyjnego i statusu `APPROVED`
- `GET /api/public/galleries/{slug}/media/{mediaId}/content` - zaimplementowany Etap 7, wymaga grantu sesyjnego i statusu `APPROVED`
- `GET /api/public/galleries/{slug}/media` - zaimplementowany Etap 7, limit 500 i tylko `APPROVED`
- `GET /api/public/media/{mediaId}`
- `POST /api/public/galleries/{slug}/favorite`

## Uploads
- `POST /api/public/galleries/{slug}/upload-sessions` - zaimplementowany Etap 5, manifest + idempotency
- `GET /api/public/galleries/{slug}/upload-sessions/{uploadSessionId}` - zaimplementowany Etap 5
- `PUT /api/public/galleries/{slug}/upload-sessions/{uploadSessionId}/files/{clientFileId}` - zaimplementowany Etap 5, jeden multipart
- `POST /api/public/galleries/{slug}/upload-sessions/{uploadSessionId}/cancel` - zaimplementowany Etap 5
- `POST /api/uploads/{uploadSessionId}/files/{fileId}/retry`
- `POST /api/uploads/{uploadSessionId}/cancel`
- `GET /api/uploads/{uploadSessionId}`

## Media
- `GET /api/galleries/{galleryId}/media`
- `GET /api/media/{mediaId}`
- `DELETE /api/media/{mediaId}`
- `POST /api/media/{mediaId}/approve`
- `POST /api/media/{mediaId}/reject`
- `POST /api/media/{mediaId}/hide`
- `POST /api/media/{mediaId}/restore`
- `POST /api/media/bulk-actions`

## Downloads
- `POST /api/downloads`
- `GET /api/downloads/{archiveId}`
- `GET /api/downloads/{archiveId}/file`
- `DELETE /api/downloads/{archiveId}`

## Customization
- `GET /api/galleries/{galleryId}/customization` - Etap 9, owner/manager odczyt; obecny kontrakt nie niesie eventId
- `PUT /api/galleries/{galleryId}/customization` - Etap 9, owner zapis, optimistic version; cross-event scoping pozostaje gate'em security

## Statistics
- `GET /api/events/{eventId}/statistics/media`
- `GET /api/events/{eventId}/statistics/views`
- `GET /api/events/{eventId}/statistics/storage`

## Notifications i subscriptions
- `GET /api/notifications`
- `PATCH /api/notifications/preferences`
- `GET /api/subscriptions/me`
- `POST /api/subscriptions/change-plan`

## Admin
- `GET /api/admin/dashboard`
- `GET /api/admin/users`
- `GET /api/admin/users/{userId}`
- `POST /api/admin/users/{userId}/lock` - Etap 10, audytowana akcja
- `POST /api/admin/users/{userId}/unlock` - Etap 10, audytowana akcja
- `GET /api/admin/events`
- `GET /api/admin/galleries`
- `GET /api/admin/media`
- `POST /api/admin/events/{eventId}/archive` - Etap 10, audytowana akcja
- `POST /api/admin/media/{mediaId}/hide` - Etap 10, audytowana akcja
- `GET /api/admin/storage`
- `GET /api/admin/system-settings`
- `PUT /api/admin/system-settings/{key}`
- `GET /api/admin/audit`

## Audit
- `GET /api/audit/me`
- `GET /api/admin/audit`

## Uwagi kontraktowe
- SSOT dokladnych payloadow, statusow i rol dla zaimplementowanych Etapow 3, 4, 4B i 5 to [../../api-contract/API_CONTRACT.md](../../api-contract/API_CONTRACT.md).
- Zwykly endpoint wydarzen nie daje administratorowi bypassu ownership.
- Zaproszenia e-mail i tokeny pozostaja poza Etapem 3.
- Docelowo listy wspieraja filtrowanie i paginacje; minimalne `GET /api/events` i `GET /api/events/{eventId}/members` w Etapie 3 zwracaja pelne tablice dostepnego zakresu bez paginacji.
- Operacje publiczne muszą być ograniczane przez token, slug, kod dostępu i reguły galerii.
- Endpointy administracyjne wymagają odrębnych ról systemowych.

## Powiązane dokumenty
- [API_CONVENTIONS.md](API_CONVENTIONS.md)
- [AUTHENTICATION_AND_AUTHORIZATION.md](AUTHENTICATION_AND_AUTHORIZATION.md)
- [../product/FUNCTIONAL_REQUIREMENTS.md](../product/FUNCTIONAL_REQUIREMENTS.md)

## Implemented first iteration: Stages 7-10

- Moderation uses scoped endpoints under
  `/api/events/{eventId}/galleries/{galleryId}/media/...`; bulk actions are
  limited to 100 media and are audited.
- Customization uses whitelist validation and optimistic `version` updates. The
  current endpoint is galleryId-scoped; eventId scoping is an explicitly open
  security gate and was not changed in this documentation-only audit.
- Admin operations are isolated under `/api/admin/**`, require `ADMIN`, and
  audit mutations. Production evidence is tracked in
  [STAGES_7_10_PRODUCTION_CHECKLIST.md](../checklists/STAGES_7_10_PRODUCTION_CHECKLIST.md).

## Decyzje otwarte
- Czy endpoint uploadu dla dużych plików będzie od początku podzielony na `init`, `part`, `complete`.
