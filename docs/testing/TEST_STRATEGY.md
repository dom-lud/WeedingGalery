# Strategia Testów

## Cel dokumentu
Opisuje docelową strategię testowania platformy na poziomie backendu, frontendu i end-to-end.

## Status dokumentu
- Status: draft
- Zakres: test strategy dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Istniejący szkielet projektu nie zapewnia pełnego pokrycia testowego.

## Stan docelowy
- Testy pokrywają krytyczne ścieżki biznesowe, bezpieczeństwo, ownership, storage i zadania asynchroniczne.

## Piramida testów
- Testy jednostkowe dla logiki domenowej i UI
- Testy integracyjne dla API, repozytoriów, storage i security
- Testy E2E dla głównych przepływów użytkownika

## Priorytety
- Ownership i autoryzacja
- Upload i przetwarzanie mediów
- Retencja i usuwanie danych
- Operacje administracyjne
- Stabilność kontraktów API

## Zasady jakości testów
- Test ma bronić wymagania, kontraktu albo regresji, nie aktualnej struktury kodu.
- Test powinien umieć obalić błędną implementację, a nie tylko potwierdzić szczęśliwą ścieżkę.
- Dla krytycznych zmian preferuj testy negatywne, graniczne i regresyjne obok testów pozytywnych.
- Nie pisz testu tylko dlatego, że łatwo go dopasować do obecnego kodu.
- Jeśli test jest zbyt mocno sprzężony z detalem implementacyjnym, trzeba to uzasadnić.

## Powiązane dokumenty
- [BACKEND_TESTING.md](BACKEND_TESTING.md)
- [FRONTEND_TESTING.md](FRONTEND_TESTING.md)
- [E2E_SCENARIOS.md](E2E_SCENARIOS.md)

## Decyzje otwarte
- Zakres automatycznych testów wydajnościowych przed pierwszą produkcją.
