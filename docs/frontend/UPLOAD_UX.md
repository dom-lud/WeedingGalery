# UX Uploadu

## Cel dokumentu
Opisuje docelowe doświadczenie użytkownika podczas uploadu zdjęć i filmów.

## Status dokumentu
- Status: draft
- Zakres: UX uploadu w galerii publicznej i panelu użytkownika
- Ostatnia aktualizacja: 2026-07-15

## Stan obecny
- Publiczna strona `/g/:slug` jest mobile-first i obsługuje token z fragmentu URL, opcjonalny kod, wybór wielu plików, status per plik, częściowe błędy, retry i anulowanie.
- Klient wysyła manifest, a następnie pliki równolegle z ograniczoną współbieżnością; pojedynczy błąd nie blokuje kolejki.
- Stan aktywnej sesji można odczytać z backendu, ale po pełnym odświeżeniu przeglądarki klient nie przywraca jeszcze automatycznie lokalnej kolejki.

## Stan docelowy
- Stabilny, czytelny upload wieloplikowy z obsługą telefonów i częściowych błędów.

## Wymagania UX
- Wybór wielu plików
- Drag and drop na desktopie
- Wybór z aparatu lub galerii na telefonie
- Pasek postępu per plik i globalny
- Statusy: oczekuje, wysyłanie, przetwarzanie, gotowe, błąd, anulowane
- Retry dla plików nieudanych
- Komunikaty o limitach i nieobsługiwanych plikach

## Zasady
- Nie blokować całej kolejki przez jeden błędny plik.
- Zachować czytelny stan po odświeżeniu, jeśli to możliwe.
- Przy słabym łączu preferować prostotę i informację o statusie nad ciężkie animacje.

## Powiązane dokumenty
- [STATE_MANAGEMENT.md](STATE_MANAGEMENT.md)
- [RESPONSIVE_DESIGN.md](RESPONSIVE_DESIGN.md)
- [../architecture/BACKGROUND_JOBS.md](../architecture/BACKGROUND_JOBS.md)

## Decyzje otwarte
- Automatyczne odtworzenie lokalnej kolejki po odświeżeniu oraz upload chunked/resumable pozostają rozszerzeniem Etapu 5.
