# Środowiska

## Cel dokumentu
Opisuje planowane środowiska uruchomieniowe i różnice konfiguracyjne.

## Status dokumentu
- Status: draft
- Zakres: development, test, production
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Środowiska nie są jeszcze sformalizowane.

## Stan docelowy
- Jasny podział na development, test i production ze wspólnym modelem konfiguracji.

## Development
- Lokalny backend i frontend
- Lokalna baza i storage
- Obniżone restrykcje operacyjne tylko tam, gdzie bezpieczne

## Test
- Środowisko dla testów integracyjnych i E2E
- Dane nietrwałe lub łatwo resetowalne
- Brak użycia produkcyjnych sekretów

## Production
- Jeden VPS na początek
- Backup, monitoring, log rotation, HTTPS
- Ograniczony dostęp administracyjny

## Powiązane dokumenty
- [DEPLOYMENT.md](DEPLOYMENT.md)
- [CONFIGURATION.md](CONFIGURATION.md)
- [MONITORING.md](MONITORING.md)

## Decyzje otwarte
- Czy przewidzieć środowisko staging przed pierwszym publicznym uruchomieniem.
