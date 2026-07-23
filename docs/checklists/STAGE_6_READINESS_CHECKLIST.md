# Checklista gotowosci i wykonania Etapu 6

## Status dokumentu
- Status: completed-first-iteration
- Zakres: wejscie i wykonanie pierwszej implementacji `MEDIA-001`
- Ostatnia aktualizacja: 2026-07-23

## Decyzja o subagentach
- [x] Decyzja podjeta zgodnie z `../development/SUBAGENT_ORCHESTRATION.md`.
- [x] Na etapie przygotowania nie uruchomiono subagentow: zakres byl dokumentacyjno-analityczny, a decyzje wymagaly jednej spojnej integracji ADR, API, testow i roadmapy.
- [x] Przy implementacji ponownie zdecydowano bez subagentow: zakres byl maly, ale przekrojowy i wymagal spojnego prowadzenia API, DB, backendu, frontendu oraz testow w jednym watku.

## Definition of Ready
- [x] Cel zadania jest jasno okreslony: asynchroniczne przetwarzanie mediow po uploadzie.
- [x] Zakres i poza zakresem sa opisane w `../backend/MEDIA_PROCESSING.md`.
- [x] Kryteria akceptacji wynikaja z `../product/BACKLOG.md` i `../testing/STAGE_6_TEST_BRIEF.md`.
- [x] Zaleznosci sa znane: Etap 5, storage abstraction, ADR 0004, ADR 0007.
- [x] Wplyw na API jest opisany w `../../api-contract/API_CONTRACT.md`.
- [x] Wplyw na baze jest ustalony: `media_processing_jobs`, `media_thumbnails`, pola/indeksy processingu.
- [x] Wymagania bezpieczenstwa sa okreslone: brak ujawniania sciezek storage, tokenow i prywatnych linkow.
- [x] Autoryzacja i ownership sa opisane jako brak nowych publicznych odczytow mediow w Etapie 6.
- [x] Wymagane testy sa wskazane w `../testing/STAGE_6_TEST_BRIEF.md`.
- [x] Potrzebne ADR-y zostaly zidentyfikowane i zaakceptowane: ADR 0004 oraz ADR 0007.
- [x] Krytyczne decyzje blokujace implementacje zostaly domkniete dla pierwszej iteracji.

## Zakres implementacji
- [x] Dodac migracje forward-only po V5.
- [x] Dodac encje/repozytoria jobow i thumbnaili.
- [x] Dodac serwis tworzenia joba po udanym zapisie oryginalu.
- [x] Dodac worker processingu z retry/backoff i kontrola wspolbieznosci wsadowej.
- [x] Dodac idempotentny zapis thumbnaili/metadanych.
- [x] Rozszerzyc statusy w backendzie i frontendzie.
- [x] Zaktualizowac audyt/logowanie bez danych wrazliwych.

## Testy wymagane przy implementacji
- [x] Test design brief pozostaje aktualny przed pisaniem testow.
- [x] Testy unit dla retry/statusow/klasyfikacji bledow.
- [x] Testy integracyjne upload -> job w dzialajacym backendzie testowym.
- [x] Test integracyjny worker -> DB -> storage w pelnym runtime.
- [x] Testy MySQL dla migracji oraz krytycznych ograniczen jobow/thumbnaili.
- [x] Testy storage failure/kompensacji/braku orphanow na poziomie service.
- [x] Testy frontendowe statusow processingu.
- [x] Playwright E2E smoke na Docker Compose dla public upload po zmianie statusow.
- [x] Accessibility baseline WCAG A/AA na Docker Compose dla login/dashboard.
- [x] Coverage backend i frontend utrzymuje progi 90%.

## Jawnie odroczone
- Publiczna galeria mediow i lightbox: Etap 7.
- Download, streaming, signed links i ZIP: `DOWNLOAD-001`.
- Moderacja: Etap 8.
- Pelne preview/transkodowanie wideo: osobna decyzja po pomiarach.
- Admin retry endpoint: admin API albo osobny zakres operacyjny.

## Zamkniecie pierwszej iteracji
- [x] Roadmapa oznacza Etap 6 jako `DONE` dla pierwszej iteracji `MEDIA-001`.
- [x] Backlog oznacza `MEDIA-001` jako `DONE` dla pierwszej iteracji Etapu 6.
- [x] Test brief Etapu 6 ma status `completed-first-iteration`.
- [x] Ostatnia lokalna weryfikacja backendu: `./mvnw.cmd verify`, 115 testow, 0 failures/errors, 1 skip, JaCoCo branch coverage 90.06%.
