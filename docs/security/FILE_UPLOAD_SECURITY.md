# Bezpieczeństwo Uploadu Plików

## Cel dokumentu
Opisuje zagrożenia i zabezpieczenia związane z przyjmowaniem zdjęć i filmów od gości oraz użytkowników.

## Status dokumentu
- Status: draft
- Zakres: upload security dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-15

## Stan obecny
- Publiczny upload JPEG, PNG, WebP i MP4 jest zaimplementowany po uzyskaniu grantu do konkretnej galerii.
- Serwer porównuje rozszerzenie, deklarowany MIME i magic bytes; SVG, archiwa i nieznane formaty są odrzucane.
- Limity domyślne: 25 MiB na obraz, 500 MiB na wideo, 2 GiB na sesję, 3 aktywne sesje i 5 GiB quota galerii.
- Obraz ma dodatkowo limit 25 mln pikseli, a pelne dekodowanie JPEG/PNG jest ograniczone do dwoch rownoleglych operacji; brak slotu daje przejsciowe `503`.
- Sesja ma manifest maksymalnie 50 plików, TTL 30 minut oraz idempotency key; quota obejmuje bajty zajęte i zarezerwowane.
- Rate limiting jest rozdzielony per IP, operację i galerię: access 10, tworzenie sesji 30, upload pliku 200 w oknie 15 minut.
- Klucz storage jest generowany przez serwer; implementacja blokuje traversal i katalog root będący symlinkiem.
- Preflight przed parserem multipart wiaze slug, grant, sesje, plik manifestu i `Content-Length`; losowy session/file ID nie powoduje przyjecia duzego body.
- Reconciler rozroznia upload aktywny w aktualnym procesie od stanu `RECEIVING` odziedziczonego po restarcie i ponawia `CLEANUP_REQUIRED` bez ukrywania orphanow.

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
- Weryfikacja MP4 obejmuje kontener `ftyp`, ale nie potwierdza jeszcze kodeka H.264; analiza kodeka i skan antywirusowy należą do hardeningu/pipeline Etapu 6.
- Przed skalowaniem horyzontalnym limiter in-memory musi zostać zastąpiony współdzielonym mechanizmem.
