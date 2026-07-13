# API Contract - System Galeria Weselna

Ten dokument stanowi SSOT (Single Source of Truth) dla wszystkich kontraktow sieciowych miedzy Frontend (FE) i Backend (BE). Kazdy deweloper oraz agent AI musi aktualizowac ten dokument przed wdrozeniem zmian w endpointach.

## Autoryzacja i Tozsamosc (Identity)

System korzysta ze Spring Security (Session Cookie + CSRF Cookie).

### `POST /api/auth/register`
Utworzenie nowego uzytkownika przez zalogowanego administratora. Endpoint nie jest publicznym signupem.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "strongPassword123"
}
```

Uwagi:
- Email musi byc normalizowany do malych liter na backendzie.

**Responses:**
- `200 OK`: Rejestracja powiodla sie. Zwraca tekst `"User registered successfully"`.
- `400 Bad Request`: Email jest juz zajety lub dane nie przeszly walidacji.
- `401 Unauthorized`: Brak aktywnej sesji administratora.
- `403 Forbidden`: Zalogowany uzytkownik nie ma roli administratora.

### `POST /api/auth/login`
Logowanie. Zadanie wymaga poprawnego tokenu CSRF (`X-XSRF-TOKEN`).

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "strongPassword123"
}
```

**Responses:**
- `200 OK`: Pomyslne zalogowanie. Backend ustawia w cookies `JSESSIONID`.
  **Body:**
  ```json
  {
    "email": "user@example.com"
  }
  ```
- `401 Unauthorized`: Bledne dane logowania.

### `GET /api/auth/me`
Pobranie profilu aktualnie zalogowanego uzytkownika.

**Responses:**
- `200 OK`: Zwraca e-mail zalogowanego uzytkownika.
  **Body:**
  ```json
  {
    "email": "user@example.com"
  }
  ```
- `401 Unauthorized`: Uzytkownik nie posiada aktywnej sesji (`JSESSIONID`).

### `GET /api/auth/csrf`
Endpoint do recznego wyzwolenia wygenerowania tokenu CSRF. Uzywany przez aplikacje kliencka na poczatku ladowania. Backend ustawia ciasteczko `XSRF-TOKEN`.

**Responses:**
- `200 OK`: Sukces. Ciasteczko zostalo wyslane.
