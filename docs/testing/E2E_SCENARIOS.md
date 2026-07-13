# Scenariusze Testowe E2E

Ten dokument gromadzi i opisuje zaplanowane scenariusze testow End-to-End (E2E) dla platformy. Scenariusze sa utrzymywane niezaleznie od implementacji, aby testy bronily biznesu i kontraktu.

## Modul: Uwierzytelnianie (Tozsamosc)

### 1. Administracyjne utworzenie nowego uzytkownika
**Krytycznosc:** Wysoka
- **Warunki poczatkowe:** Administrator jest zalogowany i posiada uprawnienie do zarzadzania uzytkownikami.
- **Kroki:**
  1. Otwarcie administracyjnego ekranu tworzenia uzytkownika.
  2. Wypelnienie pola e-mail unikalnym adresem.
  3. Wypelnienie pola hasla haslem spelniajacym wymogi bezpieczenstwa.
  4. Zatwierdzenie formularza.
- **Oczekiwany rezultat:**
  - Wyslanie poprawnego zadania POST do `/api/auth/register` i odpowiedz `200 OK`.
  - Wyswietlenie komunikatu o sukcesie utworzenia konta.
- **Edge cases:**
  - Proba utworzenia konta na e-mail, ktory juz istnieje. Oczekiwany blad walidacji i komunikat.
  - Proba wywolania endpointu bez aktywnej sesji administratora. Oczekiwany `401 Unauthorized`.
  - Proba wywolania endpointu przez zwyklego uzytkownika. Oczekiwany `403 Forbidden`.

### 2. Logowanie poprawne i dostep do chronionych zasobow
**Krytycznosc:** Wysoka
- **Warunki poczatkowe:** Uzytkownik posiada konto utworzone wczesniej przez administratora.
- **Kroki:**
  1. Otwarcie strony logowania (`/login`).
  2. Wprowadzenie e-maila i hasla istniejacego konta.
  3. Zatwierdzenie formularza.
- **Oczekiwany rezultat:**
  - Zadanie POST `/api/auth/login` z poprawnym tokenem CSRF i statusem `200 OK`.
  - Ustawienie sesji (`JSESSIONID`) i zapisanie profilu uzytkownika na frontendzie.
  - Natychmiastowe przekierowanie na panel (`/dashboard`).
  - Wyswietlenie widoku panelu ze stanem zalogowanego uzytkownika.

### 3. Brak dostepu dla niezalogowanych
**Krytycznosc:** Wysoka
- **Warunki poczatkowe:** Uzytkownik nie jest zalogowany.
- **Kroki:**
  1. Bezposrednie otwarcie strony chronionej (np. `/dashboard`).
- **Oczekiwany rezultat:**
  - Wykrycie braku tozsamosci w kontekscie bezpieczenstwa.
  - Natychmiastowe przekierowanie do strony logowania (`/login`).
- **Edge cases:**
  - Wymuszenie bezposrednich zapytan do API `/api/auth/me` powinno konczyc sie `401 Unauthorized`.
