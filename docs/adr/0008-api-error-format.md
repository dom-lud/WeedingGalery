# ADR 0008: Format Błędów API

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Opisuje wspólny format błędów HTTP.

## Stan obecny
- Brak ustalonego kontraktu.

## Stan docelowy
- Stabilny format błędu używany we wszystkich API.

## Kontekst
Frontend, panel administratora i publiczna galeria potrzebują spójnych komunikatów oraz identyfikatorów błędów.

## Decyzja
Wprowadzamy standardową odpowiedź z polami `code`, `message`, `details`, `correlationId`, `timestamp`.

## Konsekwencje
- Spójniejsza obsługa po stronie klientów.
- Konieczność dyscypliny przy mapowaniu wyjątków.

## Alternatywy
- Domyślne odpowiedzi frameworka: odrzucone.

## Decyzje otwarte
- Czy katalog kodów błędów będzie utrzymywany w osobnym pliku referencyjnym.
