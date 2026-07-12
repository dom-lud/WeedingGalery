# ADR 0004: Strategia Przetwarzania Mediów

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Opisuje kierunek przetwarzania zdjęć i filmów po uploadzie.

## Stan obecny
- Brak implementacji pipeline'u.

## Stan docelowy
- Asynchroniczne przetwarzanie z zachowaniem oryginału i retry.

## Kontekst
Generowanie miniaturek i odczyt metadanych może być kosztowne i nie powinno opóźniać odpowiedzi HTTP.

## Decyzja
Przetwarzanie mediów odbywa się jako background jobs uruchamiane po udanym zapisie pliku.

## Konsekwencje
- Lepsza responsywność API.
- Konieczność śledzenia statusów i błędów jobów.

## Alternatywy
- Synchroniczne generowanie miniaturek: odrzucone jako mniej odporne.

## Decyzje otwarte
- Zakres obsługi preview i transkodowania wideo w pierwszej iteracji.
