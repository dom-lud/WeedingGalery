# Prompt: Etap 4 - GALLERY-001

## Cel dokumentu
Gotowy prompt wykonawczy do implementacji minimalnego, spojnego Etapu 4.

## Status dokumentu
- Status: ready
- Zakres: uwierzytelnione zarzadzanie galeriami
- Ostatnia aktualizacja: 2026-07-13

## Prompt
```md
Domknij minimalny Etap 4 w zakresie `GALLERY-001`.

Jako pierwszy krok jawnie zdecyduj o subagentach zgodnie z
`docs/development/SUBAGENT_ORCHESTRATION.md`. Dla tego zadania rozwaz co
najmniej niezalezne role Security/QA oraz Frontend lub Database, ale nie
pozwalaj kilku agentom edytowac tych samych plikow.

Przed implementacja przeczytaj w podanej kolejnosci:
1. `AGENTS.md`, `README.md` i aktualny `git status`.
2. `docs/DOCUMENTATION_MAP.md`.
3. `docs/product/BACKLOG.md` - tylko `GALLERY-001`.
4. `docs/checklists/STAGE_4_READINESS_CHECKLIST.md`.
5. `docs/development/DEFINITION_OF_READY.md` i `docs/DEFINITION_OF_DONE.md`.
6. `docs/architecture/DATA_MODEL.md`, `MULTI_TENANCY.md` i `MODULES.md`.
7. `docs/product/USER_ROLES.md` i `PERMISSIONS_MATRIX.md`.
8. `docs/backend/API_CONVENTIONS.md`, `ERROR_HANDLING.md` oraz
   `AUTHENTICATION_AND_AUTHORIZATION.md`.
9. ADR 0001, 0005, 0006, 0008, 0011 i 0012.
10. Istniejacy kod events/memberships/gallery oraz testy Etapu 3.

Preflight jest twardym gate:
- przerwij, jesli istnieja konflikty merge lub markery konfliktow;
- potwierdz, ze w `db/migration` istnieje jeden baseline V1;
- nie zakladaj, ze obecny `GalleryService` jest bezpieczna implementacja - to
  tylko historyczny szkielet bez ownership i poprawnego API;
- sprawdz regresje Etapu 3 przed rozszerzaniem domeny.

Przed kodem:
1. Porownaj planowany kontrakt `GALLERY-001` w
   `api-contract/API_CONTRACT.md` z wymaganiami i skoryguj go, jesli trzeba.
2. Zapisz macierz scenariuszy testowych: happy path, negatywne, edge cases,
   IDOR, CSRF, concurrency i audit failure.
3. Dopiero potem przygotuj migracje V2 i implementacje.

Zakres wymagany:
- wiele galerii per wydarzenie;
- management API zagniezdzone pod `/api/events/{eventId}/galleries`;
- list, create, get, update, archive i soft delete;
- pola publicznego kontraktu: `id`, `eventId`, `name`, stabilny `slug`,
  `description`, `sortOrder`, `status`, `currentUserRole`, timestampy;
- slug generowany przez serwer, globalnie unikalny, nieedytowalny i nigdy
  nietraktowany jako dowod dostepu;
- owner i aktywny manager: list/read/create/update;
- tylko owner: archive/delete;
- gallery ownership zawsze dziedziczone przez `Gallery.event_id -> Event`;
- scoped queries sprawdzajace jednoczesnie `eventId`, `galleryId` i relacje
  uzytkownika do wydarzenia;
- outsider, nieistniejacy, usuniety i cross-event galleryId maja ten sam `404`;
- mutacje wymagaja sesji i CSRF;
- optimistic version i stabilny `409` dla konfliktu;
- wymagany, fail-closed audyt create/update/archive/delete bez payloadow i
  sekretow;
- UI Material UI jako jedyny glowny system UI, theme-first, mobile-first,
  z loading, error, empty, disabled i confirmation states;
- warstwa API frontendu pozostaje scentralizowana, bez rozproszonego `fetch`.

Poza zakresem - nie implementuj:
- `GALLERY-002`, `PUBLIC-001`, publicznych endpointow ani publicznego widoku;
- tokenow, kodow dostepu, publicznych grantow i QR;
- uploadu, downloadu, mediow i storage;
- moderacji, cover media, personalizacji i statystyk;
- bypassu ownership dla roli systemowej `ADMIN`;
- dowolnego HTML, CSS lub JavaScript w danych galerii.

Minimalne wymagania danych:
- utworz forward migration V2; nie modyfikuj zatwierdzonego V1;
- uzgodnij encje z kontraktem: `sort_order`, `archived_at`, `deleted_at`,
  `version` i wymagane indeksy;
- zachowaj globalna rezerwacje slugu rowniez po soft delete;
- nie wystawiaj nieaktywnych flag GALLERY-002 w API ani UI.

Testy musza wynikac z kontraktu, nie z kodu:
- unit: role, lifecycle, slug, walidacja, audit failure;
- integracja: realne logowanie, sesja, CSRF, owner/manager/outsider,
  removed-manager, cross-event IDOR, soft delete i optimistic locking;
- migracja: V1 -> V2 na H2/MySQL contract oraz czysta baza MySQL;
- frontend unit: loading/error/empty, walidacja i uprawnienia kontrolek;
- Playwright E2E: owner tworzy dwie galerie, manager edytuje i zmienia
  kolejnosc, nie moze archiwizowac/usuwac, removed manager traci dostep;
- utrzymaj testy API/integration i Playwright jako osobne suite;
- wykonaj pelna regresje Etapu 3.

Lokalna weryfikacja ma isc w tej kolejnosci:
1. `backend\mvnw.cmd -DskipTests package`
2. `docker compose up --build`
3. dopiero potem backend tests, frontend lint/test/build i Playwright przez
   `http://localhost`
4. sprawdz `docker compose ps`, health, log Flyway i brak nowych ERROR.

Po implementacji wykonuj self-review w swiezym kontekscie jako bezlitosny
reviewer. Sprawdz security, IDOR, CSRF, transakcje, audit, kontrakt, migracje,
jakosc testow, MUI/mobile/a11y oraz dokumentacje. Kazdy finding napraw i
powtorz testy oraz review az nie zostana findings albo jawny blocker.

Na koncu:
- zaktualizuj backlog, roadmap, API_ENDPOINTS, DATA_MODEL, MULTI_TENANCY,
  E2E_SCENARIOS, LOGGING i completion checklist Etapu 4;
- sprawdz DoD i wszystkie wymagania punkt po punkcie;
- podaj decyzje o subagentach, zmiany, komendy i wyniki, findings review,
  ostrzezenia, nieweryfikowane obszary oraz blockery.
```

## Powiazane dokumenty
- [../checklists/STAGE_4_READINESS_CHECKLIST.md](../checklists/STAGE_4_READINESS_CHECKLIST.md)
- [FEATURE_IMPLEMENTATION.md](FEATURE_IMPLEMENTATION.md)
- [../../api-contract/API_CONTRACT.md](../../api-contract/API_CONTRACT.md)
