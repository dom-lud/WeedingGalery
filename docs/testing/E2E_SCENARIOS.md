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
  - Zapisanie eventu audytowego `USER_REGISTERED`.
- **Edge cases:**
  - Proba utworzenia konta na e-mail, ktory juz istnieje. Oczekiwany blad walidacji i komunikat.
  - Proba wywolania endpointu bez aktywnej sesji administratora. Oczekiwany `401 Unauthorized`.
  - Proba wywolania endpointu przez zwyklego uzytkownika. Oczekiwany `403 Forbidden`.
  - Proba wyslania formularza bez poprawnego tokenu CSRF. Oczekiwany brak utworzenia konta i odpowiedz odmowna.
  - Proba wyslania niepoprawnego payloadu, np. bledny e-mail albo zbyt krotkie haslo. Oczekiwany blad walidacji.

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
  - Zapisanie eventu audytowego `USER_LOGGED_IN`.
- **Warianty obowiazkowe:**
  - Uzytkownik wpisuje poprawne dane bardzo szybko, bez czekania na "ustabilizowanie" strony po otwarciu `/login`.
  - Uzytkownik loguje sie e-mailem zapisanym innym casingiem niz w bazie, ale semantycznie tym samym adresem.
  - Uzytkownik podaje bledne haslo i pozostaje na stronie logowania z czytelnym komunikatem bledu.
  - Test nie moze sztucznie oczekiwac na `/api/auth/csrf` jeszcze przed akcja uzytkownika, jesli celem jest odtworzenie realnego szybkiego kliku.

### 3. Brak dostepu dla niezalogowanych
**Krytycznosc:** Wysoka
- **Warunki poczatkowe:** Uzytkownik nie jest zalogowany.
- **Kroki:**
  1. Bezposrednie otwarcie strony chronionej, np. `/dashboard`.
- **Oczekiwany rezultat:**
  - Wykrycie braku tozsamosci w kontekscie bezpieczenstwa.
  - Natychmiastowe przekierowanie do strony logowania (`/login`).
- **Edge cases:**
  - Wymuszenie bezposrednich zapytan do API `/api/auth/me` powinno konczyc sie `401 Unauthorized`.
  - Wejscie na nieistniejaca publiczna sciezke rejestracji nie moze odslonic flow tworzenia konta i powinno skonczyc sie przekierowaniem do logowania albo widokiem niedostepnosci.

### 4. Wylogowanie i uniewaznienie sesji
**Krytycznosc:** Wysoka
- **Warunki poczatkowe:** Uzytkownik jest zalogowany i znajduje sie na stronie chronionej.
- **Kroki:**
  1. Klikniecie przycisku wylogowania.
  2. Oczekiwanie na odpowiedz `POST /api/auth/logout`.
  3. Ponowne wejscie na strone chroniona.
- **Oczekiwany rezultat:**
  - Backend zwraca `200 OK`.
  - Sesja zostaje uniewazniona.
  - Uzytkownik wraca do `/login`.
  - Ponowna proba wejscia na `/dashboard` konczy sie przekierowaniem do `/login`.

### 5. Stabilnosc scenariuszy auth
**Krytycznosc:** Wysoka
- **Zasada:** Testy E2E maja odtwarzac realne zachowanie uzytkownika, a nie idealny przebieg zsynchronizowany z implementacja.
- **Wymagania:**
  - Strona obiektu Page Object moze kapsulkowac selektory i ergonomie akcji, ale nie moze ukrywac bledow przez sztuczne oczekiwanie na odpowiedzi API, ktorych uzytkownik nie wyzwolil jeszcze swoim dzialaniem.
  - W scenariuszach logowania musza istniec warianty szybkie i mniej idealne, w tym szybki klik po otwarciu widoku oraz ponowna proba po bledzie.
  - Dla podstawowej identity warto utrzymywac tez przypadek blokady po wielu blednych logowaniach na poziomie API lub pelnego testu integracyjnego.
  - Testy maja znajdowac regresje w synchronizacji UI, a nie stabilizowac aplikacje samym oczekiwaniem testu.
