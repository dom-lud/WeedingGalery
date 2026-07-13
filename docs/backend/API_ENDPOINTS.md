# Endpointy API

## Cel dokumentu
Prezentuje docelową strukturę endpointów REST dla wszystkich głównych obszarów systemu.

## Status dokumentu
- Status: draft
- Zakres: katalog planowanych endpointów
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Endpointy opisane w tym dokumencie są planowane; nie należy zakładać ich istnienia w kodzie.

## Stan docelowy
- Spójne REST API dla użytkowników, gości i administratorów.

## Authentication
- `POST /api/auth/register` - utworzenie konta przez administratora
- `POST /api/auth/verify-email`
- `POST /api/auth/login`
- `POST /api/auth/logout`
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
- `GET /api/events`
- `POST /api/events`
- `GET /api/events/{eventId}`
- `PATCH /api/events/{eventId}`
- `POST /api/events/{eventId}/archive`
- `DELETE /api/events/{eventId}`
- `GET /api/events/{eventId}/statistics`

## Memberships i invitations
- `GET /api/events/{eventId}/members`
- `POST /api/events/{eventId}/members/invitations`
- `POST /api/events/{eventId}/members/invitations/{invitationId}/resend`
- `DELETE /api/events/{eventId}/members/invitations/{invitationId}`
- `POST /api/invitations/{token}/accept`
- `POST /api/invitations/{token}/reject`
- `PATCH /api/events/{eventId}/members/{membershipId}`
- `DELETE /api/events/{eventId}/members/{membershipId}`
- `POST /api/events/{eventId}/ownership-transfer`

## Galleries
- `GET /api/events/{eventId}/galleries`
- `POST /api/events/{eventId}/galleries`
- `GET /api/galleries/{galleryId}`
- `PATCH /api/galleries/{galleryId}`
- `DELETE /api/galleries/{galleryId}`
- `POST /api/galleries/{galleryId}/publish`
- `POST /api/galleries/{galleryId}/disable-upload`
- `POST /api/galleries/{galleryId}/enable-upload`
- `POST /api/galleries/{galleryId}/qr`

## Public galleries
- `GET /api/public/galleries/{slug}`
- `POST /api/public/galleries/{slug}/access`
- `GET /api/public/galleries/{slug}/media`
- `GET /api/public/media/{mediaId}`
- `POST /api/public/galleries/{slug}/favorite`

## Uploads
- `POST /api/uploads`
- `POST /api/uploads/{uploadSessionId}/files`
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
- `GET /api/galleries/{galleryId}/customization`
- `PUT /api/galleries/{galleryId}/customization`
- `POST /api/galleries/{galleryId}/cover`

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
- `POST /api/admin/users/{userId}/block`
- `POST /api/admin/users/{userId}/unblock`
- `GET /api/admin/events`
- `GET /api/admin/galleries`
- `GET /api/admin/media`
- `POST /api/admin/media/{mediaId}/reprocess`
- `GET /api/admin/storage`
- `GET /api/admin/system-settings`
- `PUT /api/admin/system-settings/{key}`
- `GET /api/admin/audit`

## Audit
- `GET /api/audit/me`
- `GET /api/admin/audit`

## Uwagi kontraktowe
- Wszystkie listy wspierają filtrowanie i paginację.
- Operacje publiczne muszą być ograniczane przez token, slug, kod dostępu i reguły galerii.
- Endpointy administracyjne wymagają odrębnych ról systemowych.

## Powiązane dokumenty
- [API_CONVENTIONS.md](API_CONVENTIONS.md)
- [AUTHENTICATION_AND_AUTHORIZATION.md](AUTHENTICATION_AND_AUTHORIZATION.md)
- [../product/FUNCTIONAL_REQUIREMENTS.md](../product/FUNCTIONAL_REQUIREMENTS.md)

## Decyzje otwarte
- Czy endpoint uploadu dla dużych plików będzie od początku podzielony na `init`, `part`, `complete`.
