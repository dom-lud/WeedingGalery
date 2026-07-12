# Scenariusze Testowe E2E

Ten dokument gromadzi i opisuje wszystkie zaplanowane scenariusze testów End-to-End (E2E) dla platformy. Utrzymujemy je w oderwaniu od kodu, aby testy weryfikowały biznes i kontrakt, a nie by były pisane stricte "pod implementację".

## Moduł: Uwierzytelnianie (Tożsamość)

### 1. Rejestracja nowego użytkownika
**Krytyczność:** Wysoka
- **Warunki początkowe:** Użytkownik nie posiada konta.
- **Kroki:**
  1. Otwarcie strony rejestracji (`/register`).
  2. Wypełnienie pola e-mail unikalnym adresem (np. `nowy@example.com`).
  3. Wypełnienie pola hasła hasłem spełniającym wymogi bezpieczeństwa (min. 8 znaków).
  4. Zatwierdzenie formularza.
- **Oczekiwany rezultat:**
  - Wysłanie poprawnego żądania POST do `/api/auth/register` (zwraca 200).
  - Wyświetlenie komunikatu o sukcesie rejestracji.
  - Automatyczne przekierowanie na stronę logowania po opóźnieniu (ok. 2 sekund).
- **Edge cases (negatywne):**
  - Próba rejestracji na e-mail, który już istnieje. Oczekiwany błąd walidacji i komunikat.
  - Zbyt krótkie hasło (walidacja frontendowa HTML5).

### 2. Logowanie poprawne i dostęp do chronionych zasobów
**Krytyczność:** Wysoka
- **Warunki początkowe:** Użytkownik jest pomyślnie zarejestrowany.
- **Kroki:**
  1. Otwarcie strony logowania (`/login`).
  2. Wprowadzenie e-maila i hasła z istniejącego konta.
  3. Zatwierdzenie formularza.
- **Oczekiwany rezultat:**
  - Żądanie POST `/api/auth/login` z poprawnym tokenem CSRF i poprawnym statusem 200.
  - Ustawienie sesji (`JSESSIONID`) i zapisanie profilu użytkownika na frontendzie.
  - Natychmiastowe przekierowanie na panel (`/dashboard`).
  - Wyświetlenie widoku panelu ze stanem zalogowanego usera.

### 3. Brak dostępu dla niezalogowanych
**Krytyczność:** Wysoka
- **Warunki początkowe:** Użytkownik nie jest zalogowany (brak aktywnych ciasteczek).
- **Kroki:**
  1. Bezpośrednie otwarcie strony chronionej (np. `/dashboard`).
- **Oczekiwany rezultat:**
  - Wykrycie braku tożsamości w kontekście bezpieczeństwa.
  - Natychmiastowe przekierowanie do strony logowania (`/login`).
- **Edge cases:**
  - Wymuszenie bezpośrednich zapytań (np. via curl) do API `/api/auth/me` powinno kończyć się błędem 401 Unauthorized.
