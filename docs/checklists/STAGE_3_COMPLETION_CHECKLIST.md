# Checklista Domkniecia Etapu 3

## Cel dokumentu
Zapisuje wynik wykonania Definition of Ready, Definition of Done oraz checklist
dla minimalnego zakresu `EVENT-001` i `MEMBER-001`.

## Status dokumentu
- Status: completed
- Zakres: Etap 3 - wydarzenia, ownership i membership
- Ostatnia aktualizacja: 2026-07-13

## Zakres i decyzje
- [x] Jawnie wybrano Security i QA jako subagentow tylko do niezaleznej analizy.
- [x] Zakres ograniczono do events, manager membership, ownership i transferu.
- [x] Zaproszenia e-mail/token, galerie publiczne i uploady pozostaly poza zakresem.
- [x] `api-contract/API_CONTRACT.md` zaktualizowano przed kodem.
- [x] Nowy ADR nie byl potrzebny; ADR 0005 zaakceptowano po wdrozeniu decyzji.

## Feature i API
- [x] Owner pochodzi wylacznie z authenticated principal.
- [x] DTO oddzielaja payload API od encji JPA i nie przyjmuja `ownerId`.
- [x] Owner i manager maja jawnie rozdzielone uprawnienia.
- [x] Obcy i nieistniejacy `eventId` sa maskowane jako `404`.
- [x] Duplikat aktywnego membership zwraca `409`.
- [x] Usuniecie membership natychmiast odbiera dostep.
- [x] Transfer ownership jest transakcyjny i chroniony optimistic version.
- [x] Mutacje wymagaja sesji i CSRF.
- [x] Bledy domenowe maja stabilny kod, details, correlationId i timestamp.

## Baza i migracje
- [x] Pierwotnie dowieziono forward migrations V3/V4, a przed Etapem 4 - przy braku jakiegokolwiek wdrozenia produkcyjnego - swiadomie skonsolidowano aktualny schemat do nowego V1.
- [x] Dodano unique `(event_id, user_id)`, foreign keys i indeksy scoped queries.
- [x] Dodano soft delete wydarzenia, `removed_at` membership i optimistic version.
- [x] Nowy baseline V1 jest wykonywany na pustej bazie bez `baseline-on-migrate`.
- [x] Kompletny V1 jest broniony testem kontraktowym Flyway na H2 w trybie MySQL i weryfikowany na czystym MySQL 8.4 w Docker Compose.
- [x] Backup/restore nie byl wykonywany; zmiana dotyczy lokalnego srodowiska developerskiego, a produkcyjny rollout pozostaje poza zakresem.

## Security i audyt
- [x] Brak bypassu ownership dla zwyklego endpointu przez role `ADMIN`.
- [x] Scoped queries i use case bronia przed IDOR oraz cross-event nested IDOR.
- [x] Manager nie moze zarzadzac lifecycle, membership ani transferem.
- [x] Audyt EVENT/MEMBER jest fail-closed w transakcji biznesowej.
- [x] Best-effort audit auth jest izolowany w osobnej transakcji.
- [x] Audyt nie zapisuje hasel, tokenow CSRF ani payloadow wydarzenia.
- [x] Nie dodano sekretow ani publicznych endpointow.

## Testy i quality gates
- [x] Backend unit: principal ownership, maskowanie IDOR, propagacja awarii wymaganego audytu.
- [x] Integracja: realne logowanie, sesja, CSRF, owner/manager/outsider, duplikat, remove/reactivate, transfer, archive i audit.
- [x] Frontend lint przechodzi.
- [x] Frontend unit: 8/8 przechodzi.
- [x] Backend: 23/23 przechodzi, w tym kontrakt skonsolidowanego V1 na H2.
- [x] Frontend production build przechodzi.
- [x] Playwright E2E przez `http://localhost`: 8/8 przechodzi na 6 workerach.
- [x] Docker Compose: MySQL i backend healthy, frontend i Nginx uruchomione.
- [x] Po resecie przed Etapem 4 zweryfikowano health oraz Flyway schema history rowna V1.

## Self-review
- [x] Pierwsza petla wykryla brak zgodnosci API MUI 9 i zostala naprawiona.
- [x] Druga petla historycznie wykryla brak historii Flyway na lokalnym wolumenie; problem przestal istniec po swiadomym resecie wszystkich lokalnych danych i konsolidacji V1.
- [x] E2E historycznie wykryl MySQL ENUM audytu; skonsolidowany V1 od poczatku tworzy `event_type` jako `VARCHAR(255)`.
- [x] Review audytu wykryl rollback-only mimo catch; best-effort auth audit przeniesiono do osobnej transakcji.
- [x] Po poprawkach powtorzono build, Docker, testy i E2E do zielonego wyniku.

## Nieweryfikowane i ostrzezenia
- Produkcyjny backup/restore i rollout nie byly czescia zadania.
- Vite raportuje ostrzezenie o chunku JS powyzej 500 kB; nie blokuje Etapu 3, ale wymaga code splitting przed rozbudowa kolejnych ekranow.
- Testy integracyjne uzywaja H2, natomiast zgodnosc migracji i runtime zostala osobno potwierdzona na MySQL Docker.
