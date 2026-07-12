# ADR 0009: Generowanie Archiwów ZIP

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Opisuje podejście do pobierania wielu plików i całych galerii.

## Stan obecny
- Brak implementacji pobierania archiwów.

## Stan docelowy
- Asynchroniczne generowanie ZIP z czasowym linkiem do pobrania.

## Kontekst
Tworzenie dużych archiwów może trwać długo i nie nadaje się do blokującego żądania HTTP.

## Decyzja
Archiwa ZIP są generowane jako background jobs i udostępniane przez czasowy, kontrolowany link.

## Konsekwencje
- Lepsza odporność na duże galerie.
- Potrzeba cleanupu wygasłych archiwów.

## Alternatywy
- Synchroniczne streamowanie ZIP na żądanie: odrzucone jako ryzykowne dla wydajności.

## Decyzje otwarte
- Domyślny czas życia gotowego archiwum ZIP wymaga zatwierdzenia operacyjnego.
