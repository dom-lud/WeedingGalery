# Obsługa Błędów

## Cel dokumentu
Opisuje strategię mapowania wyjątków, błędów domenowych i odpowiedzi HTTP.

## Status dokumentu
- Status: draft
- Zakres: błędy backendowe i kontrakt API
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Strategia nie jest jeszcze zaimplementowana.

## Stan docelowy
- Jednolity, przewidywalny format błędu dla API prywatnego, publicznego i administracyjnego.

## Kategorie błędów
- Walidacja wejścia
- Uwierzytelnienie
- Autoryzacja
- Ownership i brak zasobu
- Konflikt stanu
- Limit planu lub storage
- Błąd integracji
- Błąd przetwarzania asynchronicznego
- Błąd wewnętrzny

## Format odpowiedzi
- `code`: stabilny kod maszynowy
- `message`: komunikat dla klienta lub panelu
- `details`: opcjonalna lista szczegółów
- `correlationId`: identyfikator do logów
- `timestamp`: czas błędu

## Mapowanie przykładowe
| Sytuacja | HTTP | Kod |
| --- | --- | --- |
| niepoprawne hasło | `401` | `INVALID_CREDENTIALS` |
| brak dostępu do wydarzenia | `403` lub `404` | `EVENT_ACCESS_DENIED` |
| przekroczony limit storage | `409` | `STORAGE_LIMIT_EXCEEDED` |
| plik za duży | `413` | `FILE_TOO_LARGE` |
| nieobsługiwany typ pliku | `422` | `UNSUPPORTED_MEDIA_TYPE` |
| błąd generowania ZIP | `202` dla statusu joba lub `500` przy odczycie błędnego joba | `ARCHIVE_GENERATION_FAILED` |

## Zasady bezpieczeństwa
- Nie ujawniamy stack trace ani ścieżek systemowych.
- Błędy logowania i resetu hasła nie mogą ujawniać, czy konto istnieje.
- Dla zasobów prywatnych dopuszczalne jest zwracanie `404` zamiast `403`.

## Powiązane dokumenty
- [API_CONVENTIONS.md](API_CONVENTIONS.md)
- [AUTHENTICATION_AND_AUTHORIZATION.md](AUTHENTICATION_AND_AUTHORIZATION.md)
- [../adr/0008-api-error-format.md](../adr/0008-api-error-format.md)

## Decyzje otwarte
- Czy odpowiedzi walidacyjne mają zawierać kody lokalizacyjne gotowe do tłumaczeń frontendu.
