# Wymagania Funkcjonalne

## Cel dokumentu
Zbiera docelowe wymagania funkcjonalne produktu w podziale na obszary domenowe.

## Status dokumentu
- Status: draft
- Zakres: pełny stan docelowy
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Wymagania opisują system docelowy; nie są jeszcze odwzorowane w implementacji.

## Stan docelowy
- Pełna platforma wieloużytkownikowa, modularna, gotowa do obsługi wielu wydarzeń i galerii.

## Tożsamość użytkownika
- Rejestracja, weryfikacja e-mail, logowanie, wylogowanie.
- Reset hasła, zmiana hasła, blokada konta, dezaktywacja i usunięcie konta.
- Sesje użytkownika z możliwością wylogowania z innych urządzeń.
- Gotowość pod przyszłe logowanie społecznościowe.

## Profil użytkownika
- Dane podstawowe, zdjęcie profilowe opcjonalne, język, powiadomienia.
- Lista wydarzeń, zaproszeń i wykorzystania miejsca.

## Wydarzenia
- Tworzenie, edycja, archiwizacja, usuwanie.
- Nazwa, typ, data, opis, status, prywatność, daty aktywności i wygaśnięcia.
- Wsparcie dla wielu wydarzeń na jednym koncie.

## Członkostwo i zaproszenia
- Zapraszanie po e-mailu.
- Akceptacja, odrzucenie, cofnięcie zaproszenia.
- Zmiana roli członka i transfer własności.
- Historia zmian członkostwa.

## Galerie
- Wiele galerii w ramach wydarzenia.
- Slug, opis, okładka, status, kolejność, publikacja, wygaśnięcie.
- Ustawienia uploadu, pobierania, moderacji i widoczności.

## Publiczny dostęp
- Dostęp przez slug, token, QR i opcjonalny kod.
- Możliwość wyłączenia uploadu przy zachowaniu przeglądania.
- Możliwość wyłączenia całej galerii lub samego publicznego widoku.

## Upload plików
- Zdjęcia i filmy, wiele plików, drag and drop, upload mobilny.
- Postęp dla pliku, retry, anulowanie, walidacja klienta i serwera.
- Limity rozmiaru, liczby plików, galerii, użytkownika i storage.
- Gotowość na resumable lub multipart upload dla dużych plików.

## Media
- Metadane, checksum, status, ownership, powiązanie z wydarzeniem i galerią.
- Miniatury, techniczne metadane, autor gość opcjonalnie, soft delete.

## Moderacja
- Tryby: automatyczna publikacja, publikacja po zatwierdzeniu, galeria prywatna.
- Statusy plików: `UPLOADING`, `PROCESSING`, `UPLOADED`, `PENDING_APPROVAL`, `APPROVED`, `HIDDEN`, `REJECTED`, `FAILED`, `DELETED`.
- Operacje pojedyncze i zbiorcze.

## Przetwarzanie mediów
- Miniatury zdjęć, podglądy filmów opcjonalnie, korekta orientacji, ekstrakcja metadanych.
- Retry nieudanych zadań i śledzenie statusów.

## Galeria publiczna
- Responsive, lazy loading, paginacja lub infinite scroll.
- Filtrowanie, sortowanie, fullscreen, odtwarzanie filmów, pobieranie.
- Obsługa pustej galerii i komunikatów o niedostępności.

## Pobieranie
- Pojedyncze pliki, wiele plików, cała galeria.
- Asynchroniczne tworzenie archiwów ZIP.
- Czasowy link do pobrania z wygasaniem.

## Personalizacja
- Bezpieczne motywy, kolory, teksty, tła, układy, okładki i opcjonalne logo.
- Brak możliwości wstrzykiwania dowolnego HTML, CSS lub JavaScript.

## QR i udostępnianie
- QR dla wydarzenia i galerii, eksport PNG/SVG, wersja do druku.

## Statystyki
- Dla organizatora: media, uploady, wyświetlenia, pobrania, storage, błędy, aktywność.
- Dla administratora: użytkownicy, wydarzenia, galerie, storage, błędy, największe galerie.

## Powiadomienia
- Obowiązkowe: weryfikacja e-mail, reset hasła, zaproszenia.
- Opcjonalne: limity, wygasanie galerii, gotowy ZIP, błędy przetwarzania, alerty administracyjne.

## Plany i limity
- Wbudowane w domenę plany `FREE`, `BASIC`, `PREMIUM`, `ADMIN_ASSIGNED`, `CUSTOM`.
- Limity wydarzeń, galerii, storage, wielkości plików, retencji, pobrań ZIP, personalizacji i liczby współzarządzających.

## Panel administratora
- Dashboard, użytkownicy, wydarzenia, galerie, pliki, limity, konfiguracja, audyt, retry zadań.

## Audyt
- Rejestrowanie krytycznych działań użytkowników i administratorów z wynikiem, kontekstem i identyfikatorem zasobu.

## Powiązane dokumenty
- [NON_FUNCTIONAL_REQUIREMENTS.md](NON_FUNCTIONAL_REQUIREMENTS.md)
- [FEATURE_ROADMAP.md](FEATURE_ROADMAP.md)
- [../backend/API_ENDPOINTS.md](../backend/API_ENDPOINTS.md)

## Decyzje otwarte
- Czy pierwsza wersja pobierania wielu plików poza ZIP będzie realizowana synchronicznie czy przez pakowanie po stronie serwera.
