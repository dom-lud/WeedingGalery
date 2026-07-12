# Zarządzanie Stanem

## Cel dokumentu
Definiuje zasady zarządzania stanem UI, stanem serwera i stanem długich operacji.

## Status dokumentu
- Status: draft
- Zakres: stan frontendu dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Docelowy model stanu nie jest jeszcze ustalony w kodzie.

## Stan docelowy
- Rozdzielenie stanu formularzy, stanu sesji, stanu serwera i stanu uploadu.

## Kategorie stanu
- Sesja i aktualny użytkownik
- Dane serwerowe pobierane z API
- Stan formularzy
- Stan UI lokalny
- Stan uploadu plików z postępem i retry

## Zasady
- Dane serwerowe trzymamy w dedykowanej warstwie klienta API i cache.
- Formularze walidujemy po stronie klienta i serwera.
- Stan uploadu powinien być odporny na częściowe błędy i wspierać wznowienie ręczne.
- Nie duplikujemy tych samych danych w wielu store bez potrzeby.

## Powiązane dokumenty
- [UPLOAD_UX.md](UPLOAD_UX.md)
- [UI_COMPONENTS.md](UI_COMPONENTS.md)
- [../backend/API_CONVENTIONS.md](../backend/API_CONVENTIONS.md)

## Decyzje otwarte
- Wybór konkretnego narzędzia do cache i zarządzania stanem serwera.
