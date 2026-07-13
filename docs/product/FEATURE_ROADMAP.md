# Roadmapa Funkcjonalna

## Cel dokumentu
Porzadkuje rozwoj pelnej platformy w logiczne etapy implementacyjne bez redukowania wizji produktu do MVP.

## Status dokumentu
- Status: draft
- Zakres: roadmapa wdrazania funkcji i procesow
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Roadmapa opisuje planowany rozwoj.
- Etap 0 jest zamkniety na poziomie dokumentacji i procesu.
- Etap 1 zostal uruchomiony jako techniczny fundament repozytorium.
- Etap 2 jest domkniety w zakresie podstawowej identity.
- Etap 3 jest domkniety w minimalnym zakresie `EVENT-001` i `MEMBER-001`.

## Stan docelowy
- Sekwencja etapow prowadzaca od dokumentacji i procesow do stabilnej produkcji pelnej platformy wielouzytkownikowej.

## Etap 0 - Dokumentacja i proces
- Cel: zbudowanie podstaw procesu pracy, backlogu, checklist, ADR i materialow dla agentow AI.
- Zaleznosci: brak.
- Rezultat: spojna dokumentacja sterujaca dalszym rozwojem.
- Kryterium ukonczenia: istnieja workflow, backlog, Definition of Ready, Definition of Done, prompt templates, checklisty i mapa dokumentacji.
- Glowne ryzyka: dryf terminologii, duplikacja dokumentacji, brak aktualizacji AGENTS i mapy dokumentacji.

## Etap 1 - Fundament techniczny
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
- Cel: wdrozenie galerii i podstawowego modelu publikacji.
- Zaleznosci: etap 3, ADR dot. dostepu do galerii.
- Rezultat: galerie per wydarzenie, slug, dostep publiczny, ustawienia prywatnosci i QR.
- Kryterium ukonczenia: wydarzenie moze posiadac wiele galerii z konfigurowalnym dostepem.
- Glowne ryzyka: enumeracja galerii, bledny model widocznosci i slugow.

## Etap 5 - Upload i storage
- Cel: przyjmowanie zdjec i filmow z walidacja i ograniczeniami.
- Zaleznosci: etap 4, ADR dot. storage.
- Rezultat: upload zdjec i filmow, walidacja, limity, storage abstraction i upload sessions.
- Kryterium ukonczenia: gosc lub uzytkownik moze bezpiecznie przeslac media do wlasciwej galerii.
- Glowne ryzyka: upload security, limity miejsca, niespojnosc baza-storage.

## Etap 6 - Przetwarzanie mediow
- Cel: uruchomienie przetwarzania asynchronicznego po uploadzie.
- Zaleznosci: etap 5, ADR dot. background jobs i media processing.
- Rezultat: miniatury, metadane, statusy, retry i zadania asynchroniczne.
- Kryterium ukonczenia: media po uploadzie przechodza pelny pipeline przetwarzania z monitoringiem bledow.
- Glowne ryzyka: przeciazenie serwera, brak idempotencji jobow, bledy retry.

## Etap 7 - Publiczna galeria
- Cel: udostepnienie galerii do przegladania na urzadzeniach mobilnych i desktopach.
- Zaleznosci: etap 6.
- Rezultat: widok galerii, lightbox, filmy, lazy loading, filtrowanie i pobieranie.
- Kryterium ukonczenia: gosc moze wejsc do galerii i komfortowo przegladac opublikowane media.
- Glowne ryzyka: wydajnosc mobilna, ochrona prywatnosci, duzy transfer danych.

## Etap 8 - Moderacja
- Cel: wdrozenie publikacji sterowanej przez organizatora.
- Zaleznosci: etap 7.
- Rezultat: zatwierdzanie, ukrywanie, usuwanie i operacje zbiorcze.
- Kryterium ukonczenia: wlasciciel lub manager moze zarzadzac widocznoscia materialow.
- Glowne ryzyka: niespojne statusy mediow, brak audytu decyzji moderacyjnych.

## Etap 9 - Personalizacja
- Cel: wprowadzenie bezpiecznej konfiguracji wygladu galerii.
- Zaleznosci: etap 7.
- Rezultat: motywy, kolory, zdjecie okladkowe, teksty i uklad galerii.
- Kryterium ukonczenia: wydarzenie ma kontrolowane opcje personalizacji bez dowolnego HTML/CSS/JS.
- Glowne ryzyka: XSS, nadmierna zlozonosc UI, niespojnosc motywow.

## Etap 10 - Panel administratora
- Cel: wdrozenie operacyjnego panelu zarzadzania platforma.
- Zaleznosci: etapy 2-9.
- Rezultat: uzytkownicy, wydarzenia, galerie, storage, limity, audyt i konfiguracja.
- Kryterium ukonczenia: administrator moze obslugiwac najwazniejsze przypadki operacyjne z pelnym audytem.
- Glowne ryzyka: naduzycie uprawnien administratora, brak separacji sciezek administracyjnych.

## Etap 11 - Statystyki i powiadomienia
- Cel: dostarczenie informacji zwrotnej dla uzytkownikow i operatorow.
- Zaleznosci: etapy 5-10.
- Rezultat: dashboard, wykorzystanie miejsca, powiadomienia produktowe i operacyjne, alerty o limitach oraz pozostale notyfikacje niezwiązane z podstawowym flow tozsamosci.
- Kryterium ukonczenia: wlasciciel i administrator widza kluczowe metryki i zdarzenia.
- Glowne ryzyka: naruszenie prywatnosci przez statystyki, spam notyfikacyjny.

## Etap 12 - Plany i komercjalizacja
- Cel: przygotowanie platformy pod limity i komercyjne warianty oferty.
- Zaleznosci: etapy 2-11.
- Rezultat: plany, limity, subskrypcje i przygotowanie pod platnosci.
- Kryterium ukonczenia: system potrafi wymuszac limity i przypisywac plany bez koniecznosci wdrozonych platnosci.
- Glowne ryzyka: limity sprzeczne z logika domenowa, zbyt wczesne komplikowanie modelu sprzedazy.

## Etap 13 - Stabilizacja produkcyjna
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
