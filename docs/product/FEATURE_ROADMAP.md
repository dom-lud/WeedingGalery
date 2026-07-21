# Roadmapa Funkcjonalna

## Cel dokumentu
Porzadkuje rozwoj pelnej platformy w logiczne etapy implementacyjne bez redukowania wizji produktu do MVP.

## Status dokumentu
- Status: draft
- Zakres: roadmapa wdrazania funkcji i procesow
- Ostatnia aktualizacja: 2026-07-20

## Stan obecny
- Roadmapa opisuje planowany rozwoj.
- Etap 0 jest zamkniety na poziomie dokumentacji i procesu.
- Etap 1 jest zakonczony jako techniczny fundament repozytorium.
- Etap 2 jest domkniety w zakresie podstawowej identity.
- Etap 3 jest domkniety w minimalnym zakresie `EVENT-001` i `MEMBER-001`.
- Etapy 4 i 4B sa domkniete, a Etap 5 jest domkniety w pierwszym zakresie upload/storage.
- Closure sprint 0-5 neutralizuje historyczny credential przez forward-only V5, dodaje jawny bootstrap pierwszego admina, wymusza sekrety prod oraz zabezpiecza cookies i konfiguracje CORS bez rozszerzania zakresu produktowego.
- Najblizszym etapem produktowym jest Etap 6; trwa jego analiza architektoniczna.
- Etap 13 ma wdrozony baseline CI/health/Compose, ale nie jest produkcyjnie zakonczony.

## Status etapow

| Etap | Status | Zakres / następny warunek |
| --- | --- | --- |
| 0 - Dokumentacja i proces | `DONE` | Dokumentacja sterująca, backlog, ADR, checklisty i workflow istnieją. |
| 1 - Fundament techniczny | `DONE` | Backend, frontend, MySQL, Flyway, Compose, health i CI są działające. |
| 2 - Core Identity | `DONE` | `AUTH-001`; `AUTH-002` pozostaje osobnym zadaniem. |
| 3 - Wydarzenia i członkowie | `DONE` | `EVENT-001`, `MEMBER-001`; zaproszenia czekają na `AUTH-002`. |
| 4/4B - Galerie i public access | `DONE` | `GALLERY-001`, `GALLERY-002`, `PUBLIC-001`; bez QR, listowania mediów i moderacji. |
| 5 - Upload i storage | `DONE` | `UPLOAD-001`, minimalny `UPLOAD-002`, uploadowa część `STORAGE-001`. |
| 6 - Przetwarzanie mediów | `ANALYSIS` | Zaakceptować ADR 0004/0007, zamrozić model joba, retry i statusów. |
| 7 - Publiczna galeria | `DEFERRED` | Oczekuje na Etap 6; zaprojektować listowanie, lightbox, download i budżety wydajnościowe. |
| 8 - Moderacja | `DEFERRED` | Oczekuje na media i widok publiczny. |
| 9 - Personalizacja | `IDEA` | Oczekuje na stabilny publiczny widok galerii. |
| 10 - Panel administratora | `IDEA` | Wymaga osobnego admin API, audytu i modelu jawnych akcji. |
| 11 - Statystyki i powiadomienia | `IDEA` | Wymaga media/download oraz rozszerzonej identity. |
| 12 - Plany i komercjalizacja | `IDEA` | Wymaga profilu, statystyk i decyzji produktowej o planach. |
| 13 - Stabilizacja produkcyjna | `IN_PROGRESS` | Baseline CI/Compose/health istnieje; brak backup/restore, alertów, HTTPS rollout i testów obciążeniowych. |

## Stan docelowy
- Sekwencja etapow prowadzaca od dokumentacji i procesow do stabilnej produkcji pelnej platformy wielouzytkownikowej.

## Etap 0 - Dokumentacja i proces
- Status: zakonczony.
- Cel: zbudowanie podstaw procesu pracy, backlogu, checklist, ADR i materialow dla agentow AI.
- Zaleznosci: brak.
- Rezultat: spojna dokumentacja sterujaca dalszym rozwojem.
- Kryterium ukonczenia: istnieja workflow, backlog, Definition of Ready, Definition of Done, prompt templates, checklisty i mapa dokumentacji.
- Glowne ryzyka: dryf terminologii, duplikacja dokumentacji, brak aktualizacji AGENTS i mapy dokumentacji.

## Etap 1 - Fundament techniczny
- Status: zakonczony.
- Cel: przygotowanie podstaw aplikacji i srodowisk uruchomieniowych.
- Zaleznosci: etap 0.
- Rezultat: dzialajacy szkielet backendu, frontendu, bazy, migracji i podstawowych health checks.
- Kryterium ukonczenia: repozytorium ma spojny szkielet techniczny zgodny z dokumentacja i ADR.
- Glowne ryzyka: rozjazd miedzy implementacja a architektura, bledne decyzje startowe dla konfiguracji i storage.

## Etap 2 - Core Identity
- Cel: wdrozenie tozsamosci uzytkownika i bezpiecznego logowania.
- Zaleznosci: etap 1, ADR dot. auth.
- Rezultat: administracyjne tworzenie kont, logowanie, wylogowanie, sesje, CSRF dla SPA, `/api/auth/me`, blokada po wielu blednych logowaniach oraz podstawowy audyt auth.
- Status: zakonczony.
- Potwierdzenie:
  - wdrozone sa sesje serwerowe, `GET /api/auth/csrf`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me` oraz administracyjne `POST /api/auth/register`,
  - frontend korzysta z flow logowania bez publicznego przycisku rejestracji,
  - istnieje audyt auth dla rejestracji, logowania, nieudanych logowan, blokady konta i wylogowania,
  - flow jest pokryte testami backendowymi, frontendowymi i E2E.
- Poza zamknietym zakresem:
  - weryfikacja e-mail,
  - reset hasla,
  - maile transakcyjne,
  - zarzadzanie wieloma sesjami i logout-all.
- Glowne ryzyka po etapie: dalsza rozbudowa identity nie moze rozszczelnic braku enumeracji kont, CSRF ani audytu.

## Etap 3 - Wydarzenia i czlonkowie
- Cel: wdrozenie rdzenia domenowego wydarzen i wspolzarzadzania.
- Zaleznosci: etap 2.
- Rezultat: wydarzenia, owner, manager, role wydarzenia i ownership.
- Zakres pierwszego wejscia do etapu:
  - `EVENT-001` jako tworzenie i zarzadzanie wydarzeniami,
  - `MEMBER-001` jako role i czlonkostwo w wydarzeniu,
  - bez e-mailowych zaproszen, dopoki `AUTH-002` nie dostarczy pelnego flow mail/token.
- Kryterium ukonczenia: wlasciciel moze tworzyc wydarzenia i zarzadzac czlonkami, a ownership jest egzekwowany w API i logice biznesowej.
- Status: zakonczony.
- Potwierdzenie:
  - owner wynika wylacznie z `events.owner_user_id`, a aktywne membership reprezentuje managera,
  - owner i manager widza tylko wydarzenia wynikajace z ownership albo aktywnego membership,
  - manager moze edytowac metadane, ale nie moze zarzadzac lifecycle, membership ani transferem,
  - usuniecie membership natychmiast odbiera dostep, a transfer ownership jest atomowy,
  - mutacje sa chronione sesja i CSRF oraz zapisuja audyt biznesowy,
  - flow zostal potwierdzony testami backendowymi, integracyjnymi i Playwright E2E na Docker Compose.
- Poza zamknietym zakresem: zaproszenia e-mail/token, publiczna galeria, uploady i osobny tor administracyjny wydarzen.
- Glowne ryzyka: bledy ownership, niespojne role, brak audytu zmian czlonkostwa.

## Etap 4 - Galerie
- Cel: wdrozenie bezpiecznego zarzadzania wieloma galeriami w kontekscie wydarzenia.
- Zaleznosci: etap 3. ADR 0010 jest wymagany dopiero dla publicznego dostepu w kolejnym rozszerzeniu.
- Status: zakonczony w zakresie `GALLERY-001` oraz rozszerzenia 4B (`GALLERY-002`, `PUBLIC-001`).
- Pierwszy zakres: uwierzytelnione listowanie, tworzenie, odczyt, edycja, kolejnosc, archiwizacja i soft delete galerii z ownership dziedziczonym po wydarzeniu.
- Rozszerzenie 4B: ustawienia publikacji i uploadu, rotowany token, opcjonalny kod BCrypt, grant sesyjny ograniczony do galerii, rate limiting oraz publiczny mobile-first entrypoint `/g/:slug`.
- Poza zakonczonym zakresem: QR, listowanie mediow, download, moderacja i personalizacja.
- Kryterium ukonczenia pierwszego zakresu: owner i manager moga zarzadzac dozwolonymi metadanymi wielu galerii, owner kontroluje lifecycle, a API i UI bronia scoping `eventId + galleryId`.
- Potwierdzenie:
  - management API dziedziczy ownership i role z wydarzenia oraz maskuje cross-event IDOR,
  - owner i aktywny manager moga listowac, odczytywac, tworzyc i edytowac metadane,
  - tylko owner moze idempotentnie archiwizowac i wykonywac soft delete,
  - slug jest generowany przez serwer, globalnie unikalny, stabilny i pozostaje zarezerwowany po usunieciu,
  - mutacje sa chronione sesja i CSRF oraz zapisuja wymagany audyt,
  - flow zostal potwierdzony testami backendowymi, frontendowymi i Playwright E2E na MySQL/Docker Compose.
- Glowne ryzyka: enumeracja galerii, bledny model widocznosci i slugow.

## Etap 5 - Upload i storage
- Cel: przyjmowanie zdjec i filmow z walidacja i ograniczeniami.
- Zaleznosci: etap 4, ADR dot. storage.
- Rezultat: upload zdjec i filmow, walidacja, limity, storage abstraction i upload sessions.
- Kryterium ukonczenia: gosc lub uzytkownik moze bezpiecznie przeslac media do wlasciwej galerii.
- Status: zakonczony w pierwszym zakresie `UPLOAD-001`, minimalnego `UPLOAD-002` i uploadowej czesci `STORAGE-001`.
- Potwierdzenie:
  - manifest i upload pojedynczych plikow dzialaja w ramach sesji z idempotencja, retry, anulowaniem i statusem per plik,
  - serwer sprawdza rozszerzenie, deklarowany MIME, magic bytes, rozmiar, liczbe plikow, aktywne sesje i quota galerii,
  - zapis przechodzi przez `StorageService`, losowy klucz, plik tymczasowy i atomowa publikacje bez nadpisywania w trwalym volume,
  - publiczny grant, upload i negatywne sciezki sa pokryte testami backend, frontend i E2E.
- Poza zakonczonym zakresem: resumable/chunk upload, analiza kodeka MP4, skan antywirusowy, przetwarzanie mediow oraz pobieranie/signed links.
- Glowne ryzyka: upload security, limity miejsca, niespojnosc baza-storage.

## Etap 6 - Przetwarzanie mediow
- Status: analiza; implementacja nie zostala rozpoczeta.
- Najblizszy krok: zaakceptowac ADR 0004 i ADR 0007, wybrac mechanizm jobow oraz zapisac kontrakt statusow, retry i idempotencji.
- Cel: uruchomienie przetwarzania asynchronicznego po uploadzie.
- Zaleznosci: etap 5, ADR dot. background jobs i media processing.
- Rezultat: miniatury, metadane, statusy, retry i zadania asynchroniczne.
- Kryterium ukonczenia: media po uploadzie przechodza pelny pipeline przetwarzania z monitoringiem bledow.
- Glowne ryzyka: przeciazenie serwera, brak idempotencji jobow, bledy retry.

## Etap 7 - Publiczna galeria
- Status: odroczony do zakonczenia Etapu 6.
- Najblizszy krok: po stabilizacji modelu mediow zaprojektowac API listowania, streaming/signed links, lightbox i lazy loading.
- Cel: udostepnienie galerii do przegladania na urzadzeniach mobilnych i desktopach.
- Zaleznosci: etap 6.
- Rezultat: widok galerii, lightbox, filmy, lazy loading, filtrowanie i pobieranie.
- Kryterium ukonczenia: gosc moze wejsc do galerii i komfortowo przegladac opublikowane media.
- Glowne ryzyka: wydajnosc mobilna, ochrona prywatnosci, duzy transfer danych.

## Etap 8 - Moderacja
- Status: odroczony; zalezy od Etapow 6 i 7.
- Najblizszy krok: zdefiniowac statusy publikacji, operacje bulk, ownership i katalog audytu.
- Cel: wdrozenie publikacji sterowanej przez organizatora.
- Zaleznosci: etap 7.
- Rezultat: zatwierdzanie, ukrywanie, usuwanie i operacje zbiorcze.
- Kryterium ukonczenia: wlasciciel lub manager moze zarzadzac widocznoscia materialow.
- Glowne ryzyka: niespojne statusy mediow, brak audytu decyzji moderacyjnych.

## Etap 9 - Personalizacja
- Status: idea; implementacja nie zostala rozpoczeta.
- Najblizszy krok: po Etapie 7 zamrozic bezpieczny schemat motywow bez dowolnego HTML/CSS/JS.
- Cel: wprowadzenie bezpiecznej konfiguracji wygladu galerii.
- Zaleznosci: etap 7.
- Rezultat: motywy, kolory, zdjecie okladkowe, teksty i uklad galerii.
- Kryterium ukonczenia: wydarzenie ma kontrolowane opcje personalizacji bez dowolnego HTML/CSS/JS.
- Glowne ryzyka: XSS, nadmierna zlozonosc UI, niespojnosc motywow.

## Etap 10 - Panel administratora
- Status: idea; implementacja nie zostala rozpoczeta.
- Najblizszy krok: zaprojektowac osobny admin API i jawne, audytowane akcje bez omijania ownership w API biznesowym.
- Cel: wdrozenie operacyjnego panelu zarzadzania platforma.
- Zaleznosci: etapy 2-9.
- Rezultat: uzytkownicy, wydarzenia, galerie, storage, limity, audyt i konfiguracja.
- Kryterium ukonczenia: administrator moze obslugiwac najwazniejsze przypadki operacyjne z pelnym audytem.
- Glowne ryzyka: naduzycie uprawnien administratora, brak separacji sciezek administracyjnych.

## Etap 11 - Statystyki i powiadomienia
- Status: idea; implementacja nie zostala rozpoczeta.
- Najblizszy krok: po media/download zdefiniowac agregaty, prywatnosc danych i kanaly powiadomien.
- Cel: dostarczenie informacji zwrotnej dla uzytkownikow i operatorow.
- Zaleznosci: etapy 5-10.
- Rezultat: dashboard, wykorzystanie miejsca, powiadomienia produktowe i operacyjne, alerty o limitach oraz pozostale notyfikacje niezwiązane z podstawowym flow tozsamosci.
- Kryterium ukonczenia: wlasciciel i administrator widza kluczowe metryki i zdarzenia.
- Glowne ryzyka: naruszenie prywatnosci przez statystyki, spam notyfikacyjny.

## Etap 12 - Plany i komercjalizacja
- Status: idea; implementacja nie zostala rozpoczeta.
- Najblizszy krok: podjac decyzje produktowa o planach i oddzielic limity techniczne od entitlementow komercyjnych.
- Cel: przygotowanie platformy pod limity i komercyjne warianty oferty.
- Zaleznosci: etapy 2-11.
- Rezultat: plany, limity, subskrypcje i przygotowanie pod platnosci.
- Kryterium ukonczenia: system potrafi wymuszac limity i przypisywac plany bez koniecznosci wdrozonych platnosci.
- Glowne ryzyka: limity sprzeczne z logika domenowa, zbyt wczesne komplikowanie modelu sprzedazy.

## Etap 13 - Stabilizacja produkcyjna
- Status: w toku w zakresie baseline; etap nie jest zakonczony produkcyjnie.
- Wykonane: CI, coverage gates, Docker Compose, health checks, podstawowa konfiguracja i logi z correlation ID.
- Najblizszy krok: backup/restore, RPO/RTO, HTTPS rollout/rollback, monitoring i alerty, skan security oraz testy obciazeniowe.
- Cel: przygotowanie platformy do bezpiecznej eksploatacji produkcyjnej.
- Zaleznosci: wszystkie poprzednie etapy.
- Rezultat: backup, monitoring, logi, bezpieczenstwo, testy wydajnosciowe i disaster recovery.
- Kryterium ukonczenia: platforma ma udokumentowane i sprawdzone procedury operacyjne dla wdrozenia produkcyjnego.
- Glowne ryzyka: niedoszacowanie wymagan storage, backupow i obciazenia uploadem.

## Zasady roadmapy
- Etapy sa logiczne, nie kontraktowe.
- Funkcje wdrazane pozniej nadal sa czescia jednej docelowej wizji produktu.
- Zmiana kolejnosci etapow wymaga aktualizacji zaleznosci i ryzyk.

## Powiazane dokumenty
- [PRODUCT_VISION.md](PRODUCT_VISION.md)
- [BACKLOG.md](BACKLOG.md)
- [../development/WORKFLOW.md](../development/WORKFLOW.md)
- [../architecture/MODULES.md](../architecture/MODULES.md)

## Decyzje otwarte
- Po ktorym etapie powinna nastapic pierwsza wersja publicznie dostepna dla uzytkownikow zewnetrznych.
