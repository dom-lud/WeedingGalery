# Roadmapa Funkcjonalna

## Cel dokumentu
Porządkuje rozwój pełnej platformy w logiczne etapy implementacyjne bez redukowania wizji produktu do MVP.

## Status dokumentu
- Status: draft
- Zakres: roadmapa wdrażania funkcji i procesów
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Roadmapa opisuje planowany rozwój. Żaden etap nie powinien być traktowany jako ukończony wyłącznie na podstawie dokumentacji.

## Stan docelowy
- Sekwencja etapów prowadząca od dokumentacji i procesów do stabilnej produkcji pełnej platformy wieloużytkownikowej.

## Etap 0 - Dokumentacja i proces
- Cel: zbudowanie podstaw procesu pracy, backlogu, checklist, ADR i materiałów dla agentów AI.
- Zależności: brak.
- Rezultat: spójna dokumentacja sterująca dalszym rozwojem.
- Kryterium ukończenia: istnieją workflow, backlog, Definition of Ready, Definition of Done, prompt templates, checklisty i mapa dokumentacji.
- Główne ryzyka: dryf terminologii, duplikacja dokumentacji, brak aktualizacji AGENTS i mapy dokumentacji.

## Etap 1 - Fundament techniczny
- Cel: przygotowanie podstaw aplikacji i środowisk uruchomieniowych.
- Zależności: etap 0.
- Rezultat: działający szkielet backendu, frontendu, bazy, migracji i podstawowych health checks.
- Kryterium ukończenia: repozytorium ma spójny szkielet techniczny zgodny z dokumentacją i ADR.
- Główne ryzyka: rozjazd między implementacją a architekturą, błędne decyzje startowe dla konfiguracji i storage.

## Etap 2 - Identity
- Cel: wdrożenie tożsamości użytkownika i bezpiecznego logowania.
- Zależności: etap 1, ADR dot. auth.
- Rezultat: rejestracja, logowanie, sesje lub JWT, weryfikacja e-mail, reset hasła, profil użytkownika oraz transakcyjna obsługa e-maili dla rejestracji, weryfikacji konta i odzyskiwania dostępu.
- Kryterium ukończenia: użytkownik może utworzyć konto, zalogować się i zarządzać podstawową tożsamością.
- Główne ryzyka: zła strategia auth, niedoszacowanie wymagań bezpieczeństwa i sesji.

## Etap 3 - Wydarzenia i członkowie
- Cel: wdrożenie rdzenia domenowego wydarzeń i współzarządzania.
- Zależności: etap 2.
- Rezultat: wydarzenia, owner, manager, role, zaproszenia i ownership.
- Kryterium ukończenia: właściciel może tworzyć wydarzenia i zarządzać członkami.
- Główne ryzyka: błędy ownership, niespójne role, brak audytu zmian członkostwa.

## Etap 4 - Galerie
- Cel: wdrożenie galerii i podstawowego modelu publikacji.
- Zależności: etap 3, ADR dot. dostępu do galerii.
- Rezultat: galerie per wydarzenie, slug, dostęp publiczny, ustawienia prywatności i QR.
- Kryterium ukończenia: wydarzenie może posiadać wiele galerii z konfigurowalnym dostępem.
- Główne ryzyka: enumeracja galerii, błędny model widoczności i slugów.

## Etap 5 - Upload i storage
- Cel: przyjmowanie zdjęć i filmów z walidacją i ograniczeniami.
- Zależności: etap 4, ADR dot. storage.
- Rezultat: upload zdjęć i filmów, walidacja, limity, storage abstraction i upload sessions.
- Kryterium ukończenia: gość lub użytkownik może bezpiecznie przesłać media do właściwej galerii.
- Główne ryzyka: upload security, limity miejsca, niespójność baza-storage.

## Etap 6 - Przetwarzanie mediów
- Cel: uruchomienie przetwarzania asynchronicznego po uploadzie.
- Zależności: etap 5, ADR dot. background jobs i media processing.
- Rezultat: miniatury, metadane, statusy, retry i zadania asynchroniczne.
- Kryterium ukończenia: media po uploadzie przechodzą pełny pipeline przetwarzania z monitoringiem błędów.
- Główne ryzyka: przeciążenie serwera, brak idempotencji jobów, błędy retry.

## Etap 7 - Publiczna galeria
- Cel: udostępnienie galerii do przeglądania na urządzeniach mobilnych i desktopach.
- Zależności: etap 6.
- Rezultat: widok galerii, lightbox, filmy, lazy loading, filtrowanie i pobieranie.
- Kryterium ukończenia: gość może wejść do galerii i komfortowo przeglądać opublikowane media.
- Główne ryzyka: wydajność mobilna, ochrona prywatności, duży transfer danych.

## Etap 8 - Moderacja
- Cel: wdrożenie publikacji sterowanej przez organizatora.
- Zależności: etap 7.
- Rezultat: zatwierdzanie, ukrywanie, usuwanie i operacje zbiorcze.
- Kryterium ukończenia: właściciel lub manager może zarządzać widocznością materiałów.
- Główne ryzyka: niespójne statusy mediów, brak audytu decyzji moderacyjnych.

## Etap 9 - Personalizacja
- Cel: wprowadzenie bezpiecznej konfiguracji wyglądu galerii.
- Zależności: etap 7.
- Rezultat: motywy, kolory, zdjęcie okładkowe, teksty i układ galerii.
- Kryterium ukończenia: wydarzenie ma kontrolowane opcje personalizacji bez dowolnego HTML/CSS/JS.
- Główne ryzyka: XSS, nadmierna złożoność UI, niespójność motywów.

## Etap 10 - Panel administratora
- Cel: wdrożenie operacyjnego panelu zarządzania platformą.
- Zależności: etapy 2-9.
- Rezultat: użytkownicy, wydarzenia, galerie, storage, limity, audyt i konfiguracja.
- Kryterium ukończenia: administrator może obsługiwać najważniejsze przypadki operacyjne z pełnym audytem.
- Główne ryzyka: nadużycie uprawnień administratora, brak separacji ścieżek administracyjnych.

## Etap 11 - Statystyki i powiadomienia
- Cel: dostarczenie informacji zwrotnej dla użytkowników i operatorów.
- Zależności: etapy 5-10.
- Rezultat: dashboard, wykorzystanie miejsca, powiadomienia produktowe i operacyjne, alerty o limitach oraz pozostałe notyfikacje niezwiązane z podstawowym flow tożsamości.
- Kryterium ukończenia: właściciel i administrator widzą kluczowe metryki i zdarzenia.
- Główne ryzyka: naruszenie prywatności przez statystyki, spam notyfikacyjny.

## Etap 12 - Plany i komercjalizacja
- Cel: przygotowanie platformy pod limity i komercyjne warianty oferty.
- Zależności: etapy 2-11.
- Rezultat: plany, limity, subskrypcje i przygotowanie pod płatności.
- Kryterium ukończenia: system potrafi wymuszać limity i przypisywać plany bez konieczności wdrożonych płatności.
- Główne ryzyka: limity sprzeczne z logiką domenową, zbyt wczesne komplikowanie modelu sprzedaży.

## Etap 13 - Stabilizacja produkcyjna
- Cel: przygotowanie platformy do bezpiecznej eksploatacji produkcyjnej.
- Zależności: wszystkie poprzednie etapy.
- Rezultat: backup, monitoring, logi, bezpieczeństwo, testy wydajnościowe i disaster recovery.
- Kryterium ukończenia: platforma ma udokumentowane i sprawdzone procedury operacyjne dla wdrożenia produkcyjnego.
- Główne ryzyka: niedoszacowanie wymagań storage, backupów i obciążenia uploadem.

## Zasady roadmapy
- Etapy są logiczne, nie kontraktowe.
- Funkcje wdrażane później nadal są częścią jednej docelowej wizji produktu.
- Zmiana kolejności etapów wymaga aktualizacji zależności i ryzyk.

## Powiązane dokumenty
- [PRODUCT_VISION.md](PRODUCT_VISION.md)
- [BACKLOG.md](BACKLOG.md)
- [../development/WORKFLOW.md](../development/WORKFLOW.md)
- [../architecture/MODULES.md](../architecture/MODULES.md)

## Decyzje otwarte
- Po którym etapie powinna nastąpić pierwsza wersja publicznie dostępna dla użytkowników zewnętrznych.
