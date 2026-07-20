# Checklista Domkniecia Etapu 4

## Cel dokumentu
Zapisuje wynik Definition of Ready, Definition of Done oraz checklist dla
minimalnego zakresu `GALLERY-001`.

## Status dokumentu
- Status: completed
- Zakres: Etap 4 - uwierzytelnione zarzadzanie wieloma galeriami
- Ostatnia aktualizacja: 2026-07-20

## Zakres i decyzje
- [x] Jawnie zdecydowano o braku subagentow zgodnie z `SUBAGENT_ORCHESTRATION.md`; jedna petla utrzymala spojny kontrakt FE-BE i audyt.
- [x] Skill `stage-4-galleries` nie byl obecny w repo, dlatego zastosowano lokalne skills API, backend, migration, frontend, testing, security i documentation.
- [x] Zakres ograniczono do GALLERY-001; publiczne API, tokeny, kody, QR, upload, media, moderacja, download i personalizacja pozostaly poza zakresem.
- [x] Istniejacy kontrakt GALLERY-001 w `api-contract/API_CONTRACT.md` zostal uzyty jako SSOT przed implementacja.
- [x] Nowy ADR nie byl potrzebny; publiczny dostep nadal czeka na zaakceptowanie ADR 0010.

## Feature i API
- [x] DTO nie przyjmuja `eventId`, ownera, roli, slugu ani statusu.
- [x] Owner i aktywny manager moga listowac, odczytywac, tworzyc i edytowac metadane.
- [x] Tylko owner moze archiwizowac i wykonywac soft delete.
- [x] Slug jest generowany przez serwer, globalnie unikalny, stabilny i zarezerwowany po usunieciu.
- [x] Lista ma deterministyczna kolejnosc `sortOrder`, `createdAt`, `id`.
- [x] Cross-event `galleryId`, usunieta galeria i brak zasobu sa maskowane jako `404`.
- [x] Mutacje wymagaja sesji i CSRF, a bledy zachowuja wspolny format API.

## Baza, security i audyt
- [x] Dodano forward migration V2 bez zmiany zatwierdzonego V1.
- [x] V2 dodaje lifecycle, soft delete, optimistic version, kolejnosc, indeksy scoped queries i `gallery_id` audytu.
- [x] Ownership jest dziedziczone wylacznie z wydarzenia; rola systemowa `ADMIN` nie omija zwyklego management API.
- [x] Zapytania zasobu wymagaja jednoczesnego `eventId + galleryId`, co broni nested IDOR.
- [x] Audyt GALLERY jest fail-closed i nie zapisuje payloadow ani przyszlych sekretow dostepu.
- [x] Nie dodano sekretow, publicznych endpointow ani storage.

## Testy i quality gates
- [x] Backend integracja: realne sesje, CSRF, owner/manager/outsider, IDOR, sortowanie, walidacja, lifecycle, soft delete i audyt.
- [x] Flyway V1+V2 przechodzi na H2 w trybie MySQL oraz na istniejacym MySQL 8.4 w Docker Compose.
- [x] Frontend unit obejmuje empty/create, manager permissions oraz error/retry.
- [x] Frontend lint i production build przechodza.
- [x] Playwright obejmuje realny owner/manager/lifecycle flow przez glowny Nginx proxy FE-BE.
- [x] Docker Compose uruchamia zdrowe MySQL i backend oraz frontend i Nginx.

## Self-review
- [x] Petla 1 wykryla stary `updatedAt` w odpowiedzi przed flush JPA; timestamp jest teraz ustawiany jawnie w transakcji.
- [x] Petla 1 wykryla bledny domyslny entrypoint Playwright kierujacy do statycznego Nginx na 5173; domyslny E2E przechodzi przez glowny proxy na porcie 80.
- [x] Petla 1 wykryla nieprecyzyjny locator zagniezdzonych kart MUI; test wskazuje najblizsza karte galerii.
- [x] Usunieto przypadkowe zmiany zakonczen linii i wygenerowany raport Playwright.

## Nieweryfikowane i ostrzezenia
- Produkcyjny backup/restore i rollout pozostaja poza zakresem; migracje potwierdzono lokalnie na MySQL 8.4.
- Vite nadal raportuje ostrzezenie o chunku JS powyzej 500 kB; nie blokuje GALLERY-001, ale wymaga code splitting przy dalszej rozbudowie panelu.
- Ta checklista opisuje historyczny zakres GALLERY-001. Publiczny dostep i semantyka slugu zostaly pozniej wdrozone w Etapie 4B i sa opisane w [STAGE_5_COMPLETION_CHECKLIST.md](STAGE_5_COMPLETION_CHECKLIST.md); QR i listowanie mediow nadal pozostaja poza zakresem.
