# Routing Frontendu

## Cel dokumentu
Opisuje planowane trasy SPA i zasady ochrony widoków.

## Status dokumentu
- Status: draft
- Zakres: routing paneli i galerii publicznej
- Ostatnia aktualizacja: 2026-07-20

## Stan obecny
- Dzialaja trasy `/login`, chroniony panel `/dashboard`, przekierowanie `/` oraz publiczny entrypoint `/g/:slug`.
- Trasy profilu, resetu hasla, zaproszen, publicznego przegladania mediow i panelu administratora pozostaja planowane.

## Stan docelowy
- Czytelny podział tras publicznych, użytkownika i administratora.

## Trasy publiczne
- `/`
- `/login`
- `/password-reset`
- `/g/:slug` (zaimplementowana; token jest przyjmowany we fragmencie URL, a kod w flow dostepu)
- `/gallery/:slug` i `/gallery/:slug/access` (historyczne propozycje, zastapione przez `/g/:slug`)

## Trasy użytkownika
- `/dashboard` (zaimplementowana)
- `/app` (planowany docelowy prefiks; jeszcze niezaimplementowany)
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
