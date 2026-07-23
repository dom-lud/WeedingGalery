# Scenariusze Testowe E2E

Ten dokument gromadzi i opisuje zaplanowane scenariusze testow End-to-End (E2E) dla platformy. Scenariusze sa utrzymywane niezaleznie od implementacji, aby testy bronily biznesu i kontraktu.

## Standard uruchomienia lokalnego
- Dla lokalnej weryfikacji krytycznych flow E2E nie polegaj wylacznie na procesach developerskich odpalonych recznie.
- Najpierw przebuduj backend komenda `backend\mvnw.cmd -DskipTests package`.
- Nastepnie uruchom lub odswiez srodowisko komenda `docker compose up --build`.
- Dopiero na takim srodowisku uruchamiaj testy E2E, aby weryfikacja odpowiadala rzeczywistemu flow aplikacji.

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

## Modul: Wydarzenia i membership (Etap 3)

### 1. Owner tworzy wydarzenie i dodaje managera
**Krytycznosc:** Wysoka
- Owner loguje sie, tworzy prywatne wydarzenie przez UI i widzi je po odswiezeniu listy.
- Owner dodaje istniejace konto po e-mailu bez flow zaproszenia.
- Duplikat aktywnego membership zwraca `409` i nie tworzy drugiego rekordu.
- Manager po zalogowaniu widzi wydarzenie i moze edytowac metadane.

### 2. Ograniczenia managera i IDOR
**Krytycznosc:** Krytyczna
- Manager nie widzi kontrolek ani nie moze wywolac lifecycle, membership i transferu ownership.
- Uzytkownik bez relacji oraz losowy `eventId` zwracaja ten sam `404`.
- Membership z innego wydarzenia nie moze zostac usuniete przez zagniezdzona sciezke.
- Brak CSRF na mutacji zwraca `403` i nie zmienia danych ani audytu.

### 3. Usuniecie membership i transfer ownership
**Krytycznosc:** Krytyczna
- Po usunieciu membership manager natychmiast traci dostep.
- Ponowne dodanie reaktywuje historyczny rekord bez naruszenia unique constraint.
- Transfer zmienia ownera atomowo: nowy owner otrzymuje pelne prawa, a poprzedni owner zostaje managerem.
- Stan utrzymuje sie po wylogowaniu, ponownym logowaniu i odswiezeniu UI.

### Stan automatyzacji
- `backend`: unit i integracyjny flow z realna sesja oraz CSRF.
- `frontend/tests/e2e/events-memberships.spec.ts`: flow UI create -> add manager -> ograniczenia -> transfer.
- Lokalny E2E domyslnie korzysta z `http://localhost`, aby przejsc przez glowny Nginx proxy FE-BE; zmienna `PLAYWRIGHT_BASE_URL` sluzy do jawnego nadpisania srodowiska.

## Modul: Galerie (Etap 4 - GALLERY-001)

### 1. Wiele galerii i kolejnosc
**Krytycznosc:** Wysoka
- Owner tworzy co najmniej dwie galerie w jednym wydarzeniu i po odswiezeniu widzi je wedlug `sortOrder`.
- Slug powstaje po stronie serwera, nie zmienia sie przy edycji nazwy i nie jest polem formularza.
- Pusty stan, loading, blad oraz retry sa widoczne i dostepne w UI.

### 2. Uprawnienia managera i lifecycle ownera
**Krytycznosc:** Krytyczna
- Aktywny manager moze utworzyc galerie i edytowac jej nazwe, opis oraz kolejnosc.
- Manager nie widzi akcji archive/delete, a bezposrednie wywolanie API zwraca `403`.
- Owner moze zarchiwizowac galerie, po czym metadane nie sa juz edytowalne, oraz wykonac soft delete.

### 3. Izolacja i negatywne sciezki
**Krytycznosc:** Krytyczna
- Obcy uzytkownik nie moze listowac ani odczytywac galerii wydarzenia.
- `galleryId` nalezacy do innego `eventId` zwraca `404`.
- Brak CSRF na mutacji zwraca `403`, a niepoprawna nazwa lub `sortOrder` zwraca `400` bez zapisu.
- Po usunieciu membership manager natychmiast traci dostep do galerii.

### Stan automatyzacji
- `backend`: integracyjny flow z realna sesja, CSRF, ownerem, managerem, outsiderem, IDOR, walidacja, lifecycle, soft delete i audytem.
- `frontend`: testy komponentu dla empty/create, ograniczen managera oraz error/retry.
- `frontend/tests/e2e/galleries.spec.ts`: flow UI owner -> manager -> edit -> archive -> delete na Docker Compose.

## Modul: Publiczna galeria i upload (Etapy 4B/5)

### 1. Publikacja i wejscie goscia
**Krytycznosc:** Krytyczna
- Owner rotuje jednorazowy token, ustawia opcjonalny kod i wlacza public view oraz upload.
- Token pozostaje we fragmencie URL i jest usuwany z paska po zapisaniu w `sessionStorage`.
- Bledny token, kod, wygasle okno, archiwizacja i rotacja tokenu nie ujawniaja galerii.

### 2. Upload wieloplikowy i izolacja
**Krytycznosc:** Krytyczna
- Guest tworzy manifest z idempotency key, wysyla plik i widzi status per plik.
- Inny grant tego samego tokenu ma osobny limit sesji i przestrzen idempotency.
- Obcy grant/sessionId, brak CSRF, niespojny MIME/magic bytes/rozmiar oraz przekroczenie quota sa odrzucane.
- Wygasla albo uniewazniona sesja zwalnia zarezerwowane bajty.

### 3. Storage i odpornosc
**Krytycznosc:** Wysoka
- Zapisany plik przetrwa restart backendu w volume `media_data`.
- Klient nie otrzymuje object key ani sciezki systemowej.
- Przerwany zapis i stare `.tmp` podlegaja reconciliacji/cleanupowi.

### Stan automatyzacji
- `backend`: integracyjny public access + upload z realna sesja, CSRF, MySQL-compatible Flyway, grant isolation, expiry cleanup i negatywne formaty.
- `frontend`: testy komponentu publicznego entrypointu, kodu, kolejki, retry i uploadu.
- `frontend/tests/e2e/public-gallery-upload.spec.ts`: owner publikuje galerie, a guest w nowym kontekscie wgrywa poprawny PNG przez Nginx.

## Modul: Przetwarzanie mediow (Etap 6)

### 1. Upload obrazu i status processingu
**Krytycznosc:** Krytyczna
- Owner publikuje galerie z uploadem, a guest uzyskuje grant publiczny.
- Guest uploaduje poprawny PNG przez standardowy flow public upload.
- Backend zapisuje oryginal i tworzy job processingu poza watkiem requestu.
- UI pokazuje status `PROCESSING`, a nastepnie `PROCESSED` albo kontrolowany stan oczekiwania, bez ujawniania sciezek storage.
- Replay uploadu po sukcesie nie tworzy drugiego pliku ani drugiego joba.

### 2. Blad processingu bez utraty oryginalu
**Krytycznosc:** Wysoka
- Symulowany blad generowania miniatury konczy plik statusem `PROCESSING_FAILED`.
- Oryginal pozostaje zapisany, `MEDIA_STORED` pozostaje prawdziwe, a komunikat bledu nie zawiera sciezki storage.
- Pozostale pliki w tej samej sesji nie sa blokowane przez blad jednego pliku.

### Stan automatyzacji
- Stan obecny: statusy processingu sa pokryte testem komponentu frontendu, a upload -> job jest pokryty integracyjnie w backendzie.
- Do dodania przed Etapem 7: rozszerzenie `frontend/tests/e2e/public-gallery-upload.spec.ts` albo osobny spec processingu uruchamiany na Docker Compose po przebudowaniu backendu.
- Wymagane jest oddzielne pokrycie integracyjne workerow i migracji; E2E nie zastapi testow retry, lockow i idempotencji jobow.
