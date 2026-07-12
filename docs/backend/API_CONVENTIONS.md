# Konwencje API

## Cel dokumentu
Opisuje wspólne zasady projektowania REST API dla całej platformy.

## Status dokumentu
- Status: draft
- Zakres: standardy HTTP, paginacji, filtrowania i błędów
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Docelowe API nie istnieje jeszcze w implementacji.

## Stan docelowy
- Spójne REST API dla panelu użytkownika, galerii publicznej i panelu administratora.

## Wersjonowanie
- Start bez publicznego prefiksu wersji w ścieżce, z gotowością do `v2`.
- Breaking changes wymagają ADR lub decyzji architektonicznej oraz planu migracji klienta.

## Prefiksy
- `/api/auth`
- `/api/users`
- `/api/profile`
- `/api/events`
- `/api/uploads`
- `/api/media`
- `/api/downloads`
- `/api/admin`
- `/api/public`

## Zasady zasobów
- Używamy rzeczowników, nie czasowników.
- Relacje modelujemy przez zagnieżdżone zasoby tam, gdzie podkreślają ownership, np. `/api/events/{eventId}/galleries`.
- Operacje niestandardowe dopuszczalne jako subresource, np. `/approve`, `/restore`, `/archive`.

## Paginacja
- Domyślnie `page`, `size`, `sort`.
- Dla galerii publicznej dopuszczalny cursor lub infinite scroll API.
- Odpowiedź listowa powinna zawierać metadane paginacji.

## Sortowanie i filtrowanie
- Sortowanie: `sort=createdAt,desc`
- Filtrowanie: query params, np. `status`, `mediaType`, `from`, `to`, `q`
- Nie wspieramy dowolnego filtrowania dynamicznego bez jawnego kontraktu.

## Idempotency
- Wrażliwe operacje tworzące o potencjale duplikacji mogą przyjmować `Idempotency-Key`.
- Dotyczy szczególnie uploadu, generowania ZIP i wysyłki zaproszeń.

## Optymistyczna współbieżność
- Dla zasobów edytowalnych przez wielu użytkowników dopuszczamy pole `version` lub `ETag`.
- Preferowane dla wydarzeń, galerii i ustawień.

## Statusy HTTP
| Status | Zastosowanie |
| --- | --- |
| `200 OK` | odczyt lub udana operacja synchroniczna |
| `201 Created` | utworzenie zasobu |
| `202 Accepted` | uruchomienie operacji asynchronicznej |
| `204 No Content` | usunięcie, wylogowanie, operacja bez payloadu |
| `400 Bad Request` | niepoprawna składnia lub reguła walidacyjna |
| `401 Unauthorized` | brak uwierzytelnienia |
| `403 Forbidden` | brak uprawnień |
| `404 Not Found` | brak zasobu lub celowe ukrycie istnienia |
| `409 Conflict` | konflikt stanu lub limitu |
| `413 Payload Too Large` | przekroczenie rozmiaru |
| `422 Unprocessable Entity` | poprawna składnia, niepoprawny semantycznie payload |
| `429 Too Many Requests` | rate limiting |

## Format błędów
```json
{
  "code": "MEDIA_LIMIT_EXCEEDED",
  "message": "Przekroczono limit przestrzeni dla wydarzenia.",
  "details": [
    { "field": "fileSize", "code": "MAX_SIZE_EXCEEDED" }
  ],
  "correlationId": "01JXYZ...",
  "timestamp": "2026-07-12T18:00:00Z"
}
```

## Operacje asynchroniczne
- `202 Accepted` zwraca identyfikator joba albo zasobu statusowego.
- Klient może odpytywać endpoint statusu lub otrzymać notyfikację po zakończeniu.

## Multipart i duże pliki
- Pierwsza wersja może użyć klasycznego multipart.
- Interfejs powinien być gotowy na przyszłe resumable upload.

## Powiązane dokumenty
- [API_ENDPOINTS.md](API_ENDPOINTS.md)
- [ERROR_HANDLING.md](ERROR_HANDLING.md)
- [../adr/0008-api-error-format.md](../adr/0008-api-error-format.md)

## Decyzje otwarte
- Czy galerie publiczne od początku użyją cursor pagination zamiast klasycznej paginacji stron.
