# Routing Frontendu

## Cel dokumentu
Opisuje planowane trasy SPA i zasady ochrony widoków.

## Status dokumentu
- Status: draft
- Zakres: routing paneli i galerii publicznej
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Routing docelowy nie jest jeszcze zaimplementowany.

## Stan docelowy
- Czytelny podział tras publicznych, użytkownika i administratora.

## Trasy publiczne
- `/`
- `/login`
- `/register`
- `/password-reset`
- `/gallery/:slug`
- `/gallery/:slug/access`

## Trasy użytkownika
- `/app`
- `/app/profile`
- `/app/events`
- `/app/events/:eventId`
- `/app/events/:eventId/galleries`
- `/app/events/:eventId/galleries/:galleryId`
- `/app/invitations`

## Trasy administratora
- `/admin`
- `/admin/users`
- `/admin/events`
- `/admin/galleries`
- `/admin/media`
- `/admin/storage`
- `/admin/audit`
- `/admin/settings`

## Guardy
- Guard sesji użytkownika
- Guard roli administratora
- Guard kontekstu wydarzenia
- Guard dostępu publicznego do galerii

## Powiązane dokumenty
- [FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md)
- [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md)
- [../product/USER_JOURNEYS.md](../product/USER_JOURNEYS.md)

## Decyzje otwarte
- Czy publiczna galeria ma pozostać w tym samym SPA, czy być odrębnym entrypointem buildowym.
