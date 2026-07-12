# API Contract - System Galeria Weselna

Ten dokument stanowi SSOT (Single Source of Truth) dla wszystkich kontraktów sieciowych między Frontend (FE) i Backend (BE). Każdy deweloper oraz agent AI musi aktualizować ten dokument ZANIM zaimplementuje zmiany w endpointach.

## Autoryzacja i Tożsamość (Identity)

Zarządzanie bezpieczeństwem, logowaniem i rejestracją. System korzysta ze Spring Security (Session Cookie + CSRF Cookie).

### `POST /api/auth/register`
Rejestracja nowego użytkownika.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "strongPassword123"
}
```
*Uwagi: Email musi być znormalizowany do małych liter na backendzie.*

**Responses:**
- `200 OK`: Rejestracja powiodła się. Zwraca tekst `"User registered successfully"`.
- `400 Bad Request`: Email jest już zajęty lub dane nie przeszły walidacji (np. hasło za krótkie, zły format maila).

### `POST /api/auth/login`
Logowanie. Żąda podania prawidłowego tokenu CSRF (`X-XSRF-TOKEN`).

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "strongPassword123"
}
```

**Responses:**
- `200 OK`: Pomyślne zalogowanie. Backend ustawia w cookies `JSESSIONID`.
  **Body:**
  ```json
  {
    "email": "user@example.com"
  }
  ```
- `401 Unauthorized`: Błędne dane logowania.

### `GET /api/auth/me`
Pobranie profilu aktualnie zalogowanego użytkownika. Zwraca błąd jeśli użytkownik jest niezalogowany.

**Responses:**
- `200 OK`: Zwraca e-mail zalogowanego użytkownika.
  **Body:**
  ```json
  {
    "email": "user@example.com"
  }
  ```
- `401 Unauthorized`: Użytkownik nie posiada aktywnej sesji (`JSESSIONID`).

### `GET /api/auth/csrf`
Endpoint do ręcznego wyzwolenia wygenerowania tokenu CSRF. Używany przez aplikację kliencką (SPA/Vite) na samym początku jej ładowania. Oczekuje brak autoryzacji. Backend ustawia ciasteczko `XSRF-TOKEN`.

**Responses:**
- `200 OK`: Sukces. Ciasteczko zostało wysłane.
