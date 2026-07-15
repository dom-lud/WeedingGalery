# Checklista Zakonczenia Etapow 4B i 5

## Status dokumentu
- Status: completed
- Zakres: `GALLERY-002`, `PUBLIC-001`, `UPLOAD-001`, minimalny `UPLOAD-002` i uploadowa czesc `STORAGE-001`
- Ostatnia aktualizacja: 2026-07-15

## Funkcjonalnosc i kontrakt
- [x] Kontrakt FE-BE zostal zaprojektowany w `api-contract/API_CONTRACT.md`.
- [x] Owner zarzadza publikacja, tokenem i kodem; manager nie omija owner-only.
- [x] Guest otrzymuje czasowy grant do jednej galerii i moze wysylac manifest oraz pliki.
- [x] Retry, partial failure, idempotency, cancel i status sesji maja jawna semantyke.

## Security i dane
- [x] Token raw nie trafia do bazy; kod jest hashowany BCrypt.
- [x] Cross-gallery/session IDOR, CSRF, rate limiting i okna publikacji zostaly sprawdzone.
- [x] Walidacja laczy rozszerzenie, MIME, parser/strukture formatu, rozmiar i manifest.
- [x] Quota obejmuje stored + reserved, a expiry/revocation zwalnia rezerwacje.
- [x] Migracje V3/V4 sa addytywne i przechodza test kontraktu Flyway.
- [x] Storage blokuje traversal/symlinki, uzywa pliku tymczasowego i atomowego move.
- [x] Zaplanowany cleanup obsluguje wygasle sesje, przerwane `RECEIVING` i stare `.tmp`.

## Jakosc
- [x] Testy backendowe, frontendowe, lint i build przechodza.
- [x] Docker Compose config/build/health i Playwright E2E zostaly wykonane.
- [x] Self-review i niezalezny review security zostaly wykonane; findings naprawiono i zweryfikowano ponownie.
- [x] Dokumentacja, ADR, API SSOT, roadmapa i backlog sa spojne.

## Jawnie odroczone
- QR (`QR-001`), przegladanie/download i signed links (Etap 7/`DOWNLOAD-001`).
- Chunk/resumable upload, analiza kodeka MP4 i skan antywirusowy (hardening/Etap 6).
- Wspoldzielony limiter wymagany przed skalowaniem horyzontalnym.
