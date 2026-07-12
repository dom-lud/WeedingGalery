# Roadmapa Funkcjonalna

## Cel dokumentu
Porządkuje rozwój produktu w logiczne etapy bez ograniczania końcowej wizji systemu.

## Status dokumentu
- Status: draft
- Zakres: etapy realizacji produktu
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Roadmapa opisuje planowany rozwój; etapy nie są ukończone.

## Stan docelowy
- Uporządkowany rozwój prowadzący do pełnej platformy produkcyjnej.

## Etap 1 - Fundamenty
- Monorepo, podstawowa struktura backendu i frontendu
- PostgreSQL, Flyway, konfiguracja środowisk
- Docker Compose, podstawowe health checks
- Minimalny CI i standardy kodu

## Etap 2 - Tożsamość użytkownika
- Rejestracja, logowanie, wylogowanie
- Weryfikacja e-mail
- Reset i zmiana hasła
- Profil użytkownika

## Etap 3 - Wydarzenia i członkowie
- Tworzenie wydarzeń
- Role wydarzenia
- Zaproszenia e-mail
- Współzarządzanie

## Etap 4 - Galerie
- Wiele galerii na wydarzenie
- Ustawienia widoczności i uploadu
- Publiczny dostęp
- Kody QR

## Etap 5 - Upload i storage
- Upload zdjęć i filmów
- Walidacja i limity
- Lokalny storage
- Metadane i ownership plików

## Etap 6 - Przetwarzanie mediów
- Miniatury
- Statusy i retry
- Zadania asynchroniczne
- Podstawowe metadane techniczne

## Etap 7 - Galeria publiczna
- Przeglądanie materiałów
- Fullscreen i odtwarzanie filmów
- Lazy loading
- Pobieranie pojedynczych plików

## Etap 8 - Moderacja
- Tryby publikacji
- Zatwierdzanie, ukrywanie i odrzucanie
- Operacje zbiorcze

## Etap 9 - Personalizacja
- Motywy, kolory, okładki
- Teksty powitalne
- Bezpieczne warianty układu

## Etap 10 - Panel administratora
- Dashboard i wyszukiwanie zasobów
- Zarządzanie użytkownikami, wydarzeniami i galeriami
- Audyt i retry zadań

## Etap 11 - Statystyki i powiadomienia
- Dashboard wydarzenia
- E-maile systemowe
- Alerty limitów i błędów

## Etap 12 - Plany i rozwój komercyjny
- Plany i limity
- Subskrypcje
- Gotowość do integracji płatności

## Etap 13 - Stabilizacja produkcyjna
- Backup i disaster recovery
- Monitoring i alerting
- Bezpieczeństwo produkcyjne
- Testy obciążeniowe i twarde quality gates

## Zasady roadmapy
- Etapy są logiczne, nie kontraktowe.
- Funkcje planowane później nadal należą do docelowej architektury.
- Zmiana kolejności wymaga aktualizacji dokumentacji zależności.

## Powiązane dokumenty
- [PRODUCT_VISION.md](PRODUCT_VISION.md)
- [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md)
- [../architecture/MODULES.md](../architecture/MODULES.md)

## Decyzje otwarte
- Zakres pierwszego wydania produkcyjnego po etapie 7 lub 8.
