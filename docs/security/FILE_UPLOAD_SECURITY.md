# Bezpieczeństwo Uploadu Plików

## Cel dokumentu
Opisuje zagrożenia i zabezpieczenia związane z przyjmowaniem zdjęć i filmów od gości oraz użytkowników.

## Status dokumentu
- Status: draft
- Zakres: upload security dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Mechanizm uploadu nie jest jeszcze zaimplementowany.

## Stan docelowy
- Upload wieloplikowy bezpieczny dla backendu, storage i odbiorców galerii.

## Główne ryzyka
- Fałszywe MIME types
- Niebezpieczne SVG
- Pliki wykonywalne lub skrypty pod zmienionym rozszerzeniem
- Path traversal
- Zip bombs
- Zbyt duże żądania i brute force upload
- Wstrzyknięcie złośliwych metadanych

## Wymagania walidacyjne
- Lista dozwolonych typów plików.
- Walidacja rozszerzenia, nagłówka MIME i magic bytes.
- Limit rozmiaru pliku i liczby plików.
- Limit czasu uploadu i liczby jednoczesnych sesji.
- Odrzucanie plików wykonywalnych i archiwów, jeśli nie są obsługiwane.
- Ograniczenie lub całkowite zablokowanie SVG do czasu pełnego modelu sanitizacji.

## Zasady storage
- Zapis pod losowym kluczem, nigdy pod nazwą użytkownika.
- Brak bezpośredniego zapisu do ścieżki wskazanej przez klienta.
- Odczyt pliku tylko po autoryzacji biznesowej.

## Powiązane dokumenty
- [SECURITY_REQUIREMENTS.md](SECURITY_REQUIREMENTS.md)
- [../architecture/FILE_STORAGE.md](../architecture/FILE_STORAGE.md)
- [../backend/MEDIA_PROCESSING.md](../backend/MEDIA_PROCESSING.md)

## Decyzje otwarte
- Czy w pierwszej wersji dopuścić wyłącznie popularne formaty zdjęć i MP4/H.264 dla filmów.
