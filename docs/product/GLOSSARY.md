# Słownik Pojęć

## Cel dokumentu
Utrzymuje spójną terminologię w całej dokumentacji.

## Status dokumentu
- Status: draft
- Zakres: pojęcia domenowe i techniczne
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Słownik jest punktem startowym i może być rozszerzany.

## Stan docelowy
- Jednolita terminologia stosowana w kodzie, API, testach i operacjach.

| Pojęcie | Definicja |
| --- | --- |
| Platforma | Cały system webowy opisany w dokumentacji |
| Użytkownik | Zarejestrowana osoba posiadająca konto systemowe |
| Gość | Osoba bez konta korzystająca z ograniczonego dostępu do galerii |
| Wydarzenie | Główny byt biznesowy grupujący galerie, członków i ustawienia |
| Galeria | Zbiór mediów w ramach wydarzenia z osobnymi ustawieniami dostępu |
| Membership | Relacja użytkownika do wydarzenia i jego rola w tym wydarzeniu |
| Invitation | Zaproszenie do dołączenia do wydarzenia |
| MediaFile | Zdjęcie lub film przesłany do galerii |
| UploadSession | Kontekst biznesowy pojedynczego procesu uploadu jednego lub wielu plików |
| DownloadArchive | Asynchronicznie przygotowywane archiwum ZIP |
| Ownership | Zasada, że zasób ma właściciela lub kontekst zdarzenia determinujący dostęp |
| Multi-tenancy | Logiczna separacja danych wielu niezależnych kont i wydarzeń |
| Storage | Warstwa odpowiedzialna za fizyczne przechowywanie plików |
| Public gallery | Widok galerii dostępny z użyciem slug, linku lub QR |
| Access token | Token nadający ograniczony dostęp do galerii lub operacji pobrania |
| Soft delete | Oznaczenie zasobu jako usuniętego bez natychmiastowego fizycznego kasowania |
| Hard delete | Fizyczne usunięcie danych z bazy lub storage |
| Retention | Zasady i czas przechowywania danych |
| Audit log | Rejestr istotnych działań użytkowników i administratorów |
| Background job | Zadanie wykonywane asynchronicznie poza ścieżką żądania HTTP |

## Zasady nazewnictwa
- W dokumentacji używamy terminu `wydarzenie`, nie `ślub`, gdy opis dotyczy modelu domenowego ogólnego.
- `Para młoda` jest głównym przypadkiem roli biznesowej `EventOwner`.
- `Galeria publiczna` nie oznacza braku kontroli dostępu; może nadal wymagać kodu lub tokenu.

## Powiązane dokumenty
- [PRODUCT_VISION.md](PRODUCT_VISION.md)
- [../architecture/DATA_MODEL.md](../architecture/DATA_MODEL.md)
- [../backend/API_CONVENTIONS.md](../backend/API_CONVENTIONS.md)

## Decyzje otwarte
- Czy w kodzie backendu stosować angielskie nazwy modułów i encji oraz polskie nazwy wyłącznie w dokumentacji i warstwie treści.
