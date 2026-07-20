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

Uwagi:
- Backend normalizuje e-mail do malych liter przed uwierzytelnieniem.
- Backend zlicza nieudane proby logowania dla istniejacych kont i czasowo blokuje konto po przekroczeniu limitu.
- Odpowiedz `401 Unauthorized` pozostaje celowo generyczna rowniez dla kont czasowo zablokowanych, aby nie ulatwiac enumeracji kont.

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

### `POST /api/auth/logout`
Wylogowanie aktualnego uzytkownika. Zadanie wymaga aktywnej sesji oraz poprawnego tokenu CSRF (`X-XSRF-TOKEN`).

**Responses:**
- `200 OK`: Sesja zostala uniewazniona, a cookie `JSESSIONID` jest czyszczone.
- `401 Unauthorized`: Brak aktywnej sesji.
- `403 Forbidden`: Brak tokenu CSRF lub niepoprawny token.
## Wydarzenia (EVENT-001)

Wszystkie endpointy w tej sekcji wymagaja aktywnej sesji. Kazde zadanie
`POST`, `PUT` lub `DELETE` wymaga dodatkowo poprawnego tokenu CSRF
(`X-XSRF-TOKEN`). Administrator systemowy nie omija ownership wydarzenia;
osobny tor administracyjny pozostaje poza zakresem Etapu 3.

### Model uprawnien pierwszego zakresu

- `OWNER` jest wyliczany z `Event.owner_user_id` i nie jest duplikowany w
  `EventMembership`.
- Aktywne `EventMembership` w pierwszym zakresie ma role `MANAGER`.
- `OWNER` moze odczytywac, edytowac, archiwizowac i usuwac wydarzenie,
  zarzadzac managerami oraz transferowac ownership.
- `MANAGER` moze odczytywac i edytowac wydarzenie oraz odczytywac liste
  managerow. Nie moze archiwizowac ani usuwac wydarzenia, zarzadzac
  membership ani transferowac ownership.
- Uzytkownik bez ownership lub aktywnego membership nie ma dostepu. Dla
  prywatnych zasobow nieistniejacy i niedostepny `eventId` zwracaja taki sam
  `404 Not Found`, aby nie ujawniac istnienia zasobu.
- Usuniete membership natychmiast traci dostep. Ponowne dodanie tego samego
  uzytkownika reaktywuje historyczny rekord membership. Proba dodania juz
  aktywnego managera zwraca `409 Conflict`.

### Wspolny model odpowiedzi wydarzenia

```json
{
  "id": "5ab7b146-10b4-40c9-9517-b0678860ac52",
  "name": "Wesele Ani i Tomka",
  "type": "WEDDING",
  "eventDate": "2026-08-15",
  "description": "Nasze wesele",
  "status": "DRAFT",
  "privacyMode": "PRIVATE",
  "currentUserRole": "OWNER",
  "createdAt": "2026-07-13T12:00:00Z",
  "updatedAt": "2026-07-13T12:00:00Z"
}
```

Dozwolone wartosci:
- `type`: `WEDDING`, `BIRTHDAY`, `CORPORATE`, `OTHER`;
- `status`: `DRAFT`, `PUBLISHED`, `ARCHIVED` (`DELETED` nie jest zwracany);
- `privacyMode`: w Etapie 3 wylacznie `PRIVATE`;
- `currentUserRole`: `OWNER` albo `MANAGER`.

Walidacja danych zapisu:
- `name`: wymagane, po przycieciu 1-255 znakow;
- `description`: opcjonalne, maksymalnie 5000 znakow;
- `eventDate`: opcjonalna data ISO `YYYY-MM-DD`;
- `type` i `privacyMode`: wymagane wartosci z katalogu powyzej.

### `POST /api/events`

Tworzy wydarzenie. Owner zawsze pochodzi z zalogowanego principal i nie moze
zostac wskazany w payloadzie.

**Request Body:**
```json
{
  "name": "Wesele Ani i Tomka",
  "type": "WEDDING",
  "eventDate": "2026-08-15",
  "description": "Nasze wesele",
  "privacyMode": "PRIVATE"
}
```

**Responses:**
- `201 Created`: Zwraca utworzone wydarzenie i naglowek `Location`.
- `400 Bad Request`: Niepoprawny payload lub walidacja.
- `401 Unauthorized`: Brak aktywnej sesji.
- `403 Forbidden`: Brak albo niepoprawny token CSRF.

### `GET /api/events`

Zwraca nieusuniete wydarzenia, ktorych uzytkownik jest ownerem albo aktywnym
managerem. Wynik jest sortowany malejaco po `createdAt`.

**Responses:**
- `200 OK`: Tablica odpowiedzi wydarzenia.
- `401 Unauthorized`: Brak aktywnej sesji.

### `GET /api/events/{eventId}`

**Responses:**
- `200 OK`: Zwraca wydarzenie dla ownera albo aktywnego managera.
- `401 Unauthorized`: Brak aktywnej sesji.
- `404 Not Found`: Wydarzenie nie istnieje, jest usuniete albo uzytkownik nie
  ma do niego relacji.

### `PUT /api/events/{eventId}`

Pelna aktualizacja edytowalnych metadanych wydarzenia przez ownera albo
aktywnego managera. Owner, identyfikator i znaczniki czasu nie sa edytowalne.
Wydarzenia `ARCHIVED` nie mozna edytowac.

**Request Body:** taki jak dla `POST /api/events`.

**Responses:**
- `200 OK`: Zwraca zaktualizowane wydarzenie.
- `400 Bad Request`: Niepoprawny payload lub walidacja.
- `401 Unauthorized`: Brak aktywnej sesji.
- `403 Forbidden`: Brak albo niepoprawny token CSRF.
- `404 Not Found`: Brak widocznego wydarzenia.
- `409 Conflict`: Wydarzenie jest zarchiwizowane.

### `POST /api/events/{eventId}/archive`

Archiwizuje wydarzenie. Operacja jest dostepna tylko dla ownera i jest
idempotentna dla juz zarchiwizowanego wydarzenia.

**Responses:**
- `200 OK`: Zwraca wydarzenie ze statusem `ARCHIVED`.
- `401 Unauthorized`: Brak aktywnej sesji.
- `403 Forbidden`: Zalogowany manager nie ma uprawnienia ownera albo zadanie
  nie ma poprawnego CSRF.
- `404 Not Found`: Brak widocznego wydarzenia.

### `DELETE /api/events/{eventId}`

Logicznie usuwa wydarzenie. Operacja jest dostepna tylko dla ownera. Usuniete
wydarzenie znika z list i nie jest dostepne przez identyfikator.

**Responses:**
- `204 No Content`: Wydarzenie zostalo usuniete.
- `401 Unauthorized`: Brak aktywnej sesji.
- `403 Forbidden`: Zalogowany manager nie ma uprawnienia ownera albo zadanie
  nie ma poprawnego CSRF.
- `404 Not Found`: Brak widocznego wydarzenia.

## Czlonkostwo wydarzenia (MEMBER-001)

Pierwszy zakres nie tworzy zaproszen ani nie wysyla e-maili. Owner dodaje
istniejace konto bezposrednio po znormalizowanym adresie e-mail. Membership
przechowuje historie przez `removedAt`; nie wolno modyfikowac membership
z innego wydarzenia przez zagniezdzona sciezke.

### Wspolny model odpowiedzi membership

```json
{
  "id": "ba128baf-b0ed-4686-9238-a9fd08c10d51",
  "userId": "52c91337-00d7-450b-afab-a5a319593fb3",
  "email": "manager@example.com",
  "role": "MANAGER",
  "joinedAt": "2026-07-13T12:10:00Z"
}
```

### `GET /api/events/{eventId}/members`

Zwraca ownera jako pozycje z wyliczona rola `OWNER` oraz aktywnych managerow.
Endpoint jest dostepny dla ownera i aktywnego managera.

**Responses:**
- `200 OK`: Tablica odpowiedzi membership.
- `401 Unauthorized`: Brak aktywnej sesji.
- `404 Not Found`: Brak widocznego wydarzenia.

### `POST /api/events/{eventId}/members`

Dodaje istniejace konto jako managera. Dostepne tylko dla ownera.

**Request Body:**
```json
{
  "email": "manager@example.com",
  "role": "MANAGER"
}
```

**Responses:**
- `201 Created`: Zwraca utworzone albo reaktywowane membership.
- `400 Bad Request`: Niepoprawny e-mail, rola inna niz `MANAGER` albo proba
  dodania aktualnego ownera.
- `401 Unauthorized`: Brak aktywnej sesji.
- `403 Forbidden`: Manager nie ma uprawnienia albo brak poprawnego CSRF.
- `404 Not Found`: Brak widocznego wydarzenia lub konto docelowe nie istnieje.
- `409 Conflict`: Konto ma juz aktywne membership w wydarzeniu.

### `DELETE /api/events/{eventId}/members/{membershipId}`

Usuwa aktywne membership managera przez ustawienie `removedAt`. Dostepne tylko
dla ownera. Owner nie jest rekordem membership, wiec nie moze zostac usuniety
tym endpointem.

**Responses:**
- `204 No Content`: Membership zostalo usuniete.
- `401 Unauthorized`: Brak aktywnej sesji.
- `403 Forbidden`: Manager nie ma uprawnienia albo brak poprawnego CSRF.
- `404 Not Found`: Brak widocznego wydarzenia, aktywnego membership albo
  `membershipId` nalezy do innego wydarzenia.

### `POST /api/events/{eventId}/ownership-transfer`

Atomowo transferuje ownership do aktywnego managera. Docelowe membership jest
oznaczane jako usuniete, a poprzedni owner otrzymuje aktywne membership
`MANAGER`. Tylko aktualny owner moze wykonac operacje.

**Request Body:**
```json
{
  "targetMembershipId": "ba128baf-b0ed-4686-9238-a9fd08c10d51"
}
```

**Responses:**
- `200 OK`: Zwraca wydarzenie widziane przez poprzedniego ownera, obecnie z
  `currentUserRole: "MANAGER"`.
- `400 Bad Request`: Target nie moze zostac nowym ownerem.
- `401 Unauthorized`: Brak aktywnej sesji.
- `403 Forbidden`: Manager nie ma uprawnienia albo brak poprawnego CSRF.
- `404 Not Found`: Brak widocznego wydarzenia, aktywnego membership albo
  `targetMembershipId` nalezy do innego wydarzenia.

## Bledy EVENT-001 i MEMBER-001

Odpowiedzi domenowe i walidacyjne uzywaja wspolnego formatu:

```json
{
  "code": "EVENT_NOT_FOUND",
  "message": "Event was not found.",
  "details": [],
  "correlationId": "01JXYZ...",
  "timestamp": "2026-07-13T12:00:00Z"
}
```

Stabilne kody pierwszego zakresu:
- `EVENT_NOT_FOUND`, `EVENT_OWNER_REQUIRED`, `EVENT_ARCHIVED`;
- `MEMBERSHIP_NOT_FOUND`, `MEMBERSHIP_ALREADY_ACTIVE`,
  `INVALID_MEMBERSHIP_TARGET`, `USER_NOT_FOUND`;
- `VALIDATION_ERROR`, `INTERNAL_SERVER_ERROR`.

Udane mutacje zapisuja audyt biznesowy: `EVENT_CREATED`, `EVENT_UPDATED`,
`EVENT_ARCHIVED`, `EVENT_DELETED`, `EVENT_MANAGER_ADDED`,
`EVENT_MANAGER_REMOVED` oraz `EVENT_OWNERSHIP_TRANSFERRED`. Audyt zawiera
aktora, `eventId`, identyfikator celu (jesli dotyczy) i zmiane roli, ale nie
zawiera hasel, tokenow, danych CSRF ani innych sekretow. Operacja wrazliwa nie
moze zostac uznana za udana, jesli wymagany wpis audytowy nie zostal zapisany.

## Kontrakt GALLERY-001 - Etap 4

Status: `IMPLEMENTED`. Sekcja definiuje zaimplementowany pierwszy zakres Etapu 4
i nie definiuje publicznego dostepu.

Wszystkie endpointy wymagaja aktywnej sesji. Mutacje wymagaja CSRF. Galeria
zawsze dziedziczy ownership z wydarzenia; payload nie przyjmuje `eventId`,
`ownerId`, `slug`, statusu ani roli.

### Uprawnienia
- `OWNER` i aktywny `MANAGER`: list, get, create, update.
- tylko `OWNER`: archive i delete.
- rola systemowa `ADMIN` nie omija ownership w management API.
- obcy, nieistniejacy, soft-deleted i cross-event `galleryId` zwracaja ten sam
  `404 Not Found`.

### Model odpowiedzi galerii
```json
{
  "id": "c213ed1d-d10e-4a90-91ee-aa7416b8b2bb",
  "eventId": "5ab7b146-10b4-40c9-9517-b0678860ac52",
  "name": "Przygotowania",
  "slug": "przygotowania-7f3a2c",
  "description": "Zdjecia sprzed ceremonii",
  "sortOrder": 10,
  "status": "ACTIVE",
  "currentUserRole": "OWNER",
  "createdAt": "2026-07-13T12:00:00Z",
  "updatedAt": "2026-07-13T12:00:00Z"
}
```

Zasady:
- `name`: wymagane, po przycieciu 1-255 znakow;
- `description`: opcjonalne, maksymalnie 5000 znakow;
- `sortOrder`: liczba calkowita `0..100000`;
- `slug`: generowany przez serwer, globalnie unikalny, stabilny i
  nieedytowalny; nie jest mechanizmem autoryzacji i pozostaje zarezerwowany po
  soft delete;
- `status`: `ACTIVE` albo `ARCHIVED`; usunieta galeria nie jest zwracana.

### `GET /api/events/{eventId}/galleries`
Zwraca aktywne i zarchiwizowane galerie widocznego wydarzenia, sortowane po
`sortOrder ASC`, a nastepnie `createdAt ASC` i `id ASC`.

Responses: `200`, `401`, `404`.

### `POST /api/events/{eventId}/galleries`
Tworzy galerie ze statusem `ACTIVE`. `eventId` oraz aktor pochodza z kontekstu
zadania, a slug generuje serwer.

```json
{
  "name": "Przygotowania",
  "description": "Zdjecia sprzed ceremonii",
  "sortOrder": 10
}
```

Responses: `201` z `Location`, `400`, `401`, `403` dla CSRF, `404`, `409` dla
nieusuwalnego konfliktu slugu lub optimistic locking.

### `GET /api/events/{eventId}/galleries/{galleryId}`
Responses: `200`, `401`, `404`.

### `PUT /api/events/{eventId}/galleries/{galleryId}`
Pelna aktualizacja `name`, `description` i `sortOrder`. Nie zmienia slugu,
eventu ani lifecycle. Zarchiwizowanej galerii nie mozna edytowac.

Responses: `200`, `400`, `401`, `403` dla CSRF, `404`, `409`.

### `POST /api/events/{eventId}/galleries/{galleryId}/archive`
Idempotentnie archiwizuje galerie. Dostepne tylko dla ownera wydarzenia.

Responses: `200`, `401`, `403`, `404`, `409`.

### `DELETE /api/events/{eventId}/galleries/{galleryId}`
Soft delete dostepny tylko dla ownera. Natychmiast usuwa galerie z management
API. Slug pozostaje zarezerwowany.

Responses: `204`, `401`, `403`, `404`, `409`.

### Bledy i audyt GALLERY-001
Stabilne kody: `GALLERY_NOT_FOUND`, `GALLERY_OWNER_REQUIRED`,
`GALLERY_ARCHIVED`, `GALLERY_SLUG_CONFLICT`, `CONCURRENT_MODIFICATION`,
`VALIDATION_ERROR`.

Udane mutacje wymagaja fail-closed audit eventow: `GALLERY_CREATED`,
`GALLERY_UPDATED`, `GALLERY_ARCHIVED`, `GALLERY_DELETED`. Audyt zawiera aktora,
`eventId` i `galleryId`, ale nie zapisuje payloadu galerii ani przyszlych
sekretow dostepu.

### Jawnie poza kontraktem GALLERY-001
Publiczne API, `GALLERY-002`, tokeny, access code, QR, upload, download, media,
moderacja, cover i personalizacja. Ich dodanie wymaga osobnej aktualizacji tego
SSOT oraz - dla publicznego dostepu - zaakceptowanego ADR 0010.

## Kontrakt GALLERY-002 i PUBLIC-001 - Etap 4B

Status: `IMPLEMENTED`. QR, listowanie mediow i download
pozostaja poza tym kontraktem.

### Ustawienia i uprawnienia

- `publicViewEnabled` wymaga aktywnego tokenu.
- `uploadEnabled` moze byc wlaczone tylko razem z `publicViewEnabled`.
- `downloadEnabled` jest zapisywane dla kolejnego etapu; Etap 4B/5 nie udostepnia
  endpointu pobierania.
- `moderationMode`: `NONE` albo `REQUIRED`; media Etapu 5 nie sa publikowane.
- `publishedAt` i `expiresAt` sa opcjonalne; jesli oba istnieja, `expiresAt` musi
  byc pozniejsze.
- owner i aktywny manager odczytuja ustawienia; tylko owner je zmienia, rotuje
  token oraz ustawia lub usuwa kod.
- odpowiedzi nie zawieraja hasha ani kodu/tokena poza jednorazowa rotacja.

### `GET /api/events/{eventId}/galleries/{galleryId}/settings`

Sesja uzytkownika. Zwraca `200`:

```json
{
  "publicViewEnabled": false,
  "uploadEnabled": false,
  "downloadEnabled": false,
  "moderationMode": "REQUIRED",
  "accessTokenConfigured": false,
  "accessCodeConfigured": false,
  "publishedAt": null,
  "expiresAt": null,
  "version": 0
}
```

### `PUT /api/events/{eventId}/galleries/{galleryId}/settings`

Owner, sesja i CSRF. Body odpowiada polom ustawien oraz zawiera `version`.
Zwraca `200`. Wlaczenie widoku bez tokenu zwraca `409
GALLERY_ACCESS_TOKEN_REQUIRED`; bledne okno dat zwraca `400
GALLERY_INVALID_PUBLICATION_WINDOW`; konflikt wersji zwraca `409
CONCURRENT_MODIFICATION`.

### `POST /api/events/{eventId}/galleries/{galleryId}/access-token/rotate`

Owner, sesja i CSRF. Uniewaznia poprzedni token i zwraca `201` tylko raz:

```json
{
  "accessToken": "<base64url-256-bit>",
  "sharePath": "/g/example-slug#token=<base64url-256-bit>"
}
```

### `PUT|DELETE /api/events/{eventId}/galleries/{galleryId}/access-code`

Owner, sesja i CSRF. `PUT` przyjmuje `{ "accessCode": "6-64 ASCII" }`, nie
zwraca kodu i konczy sie `204`. `DELETE` usuwa wymaganie kodu i zwraca `204`.

### `POST /api/public/galleries/{slug}/access`

Endpoint anonimowy, ale POST wymaga cookie/header CSRF uzyskanego przez
`GET /api/auth/csrf`. Body: `{ "accessToken": "<token>", "accessCode":
"optional" }`.

Sukces `200` tworzy grant w HttpSession z TTL 30 minut i zwraca:

```json
{
  "slug": "example-slug",
  "name": "Wesele",
  "description": "Zdjecia gosci",
  "uploadEnabled": true,
  "downloadEnabled": false,
  "moderationMode": "REQUIRED",
  "publishedAt": null,
  "expiresAt": null
}
```

Brak kodu przy poprawnym tokenie: `401 GALLERY_ACCESS_CODE_REQUIRED`. Bledny kod:
`401 GALLERY_ACCESS_DENIED`. Nieistniejacy slug, bledny token, prywatna,
nieopublikowana, wygasla, zarchiwizowana lub usunieta galeria zwracaja identyczne
`404 PUBLIC_GALLERY_NOT_FOUND`. Rate limit zwraca `429 RATE_LIMIT_EXCEEDED` i
`Retry-After`.

### `GET /api/public/galleries/{slug}`

Wymaga waznego grantu dla tej galerii. Zwraca publiczny model albo generyczne
`404`. Grant jest ponownie walidowany, wiec rotacja tokenu i zmiana ustawien
dzialaja natychmiast.

## Kontrakt STORAGE-001, UPLOAD-001 i minimalny UPLOAD-002 - Etap 5

Status: `IMPLEMENTED`. Implementacja uzywa klasycznego
multipart po jednym pliku. Chunking i resumable parts sa poza zakresem, ale model
sesji nie blokuje przyszlego `/parts`.

### Limity i typy

- typy v1: JPEG (`.jpg/.jpeg`, `image/jpeg`), PNG, WebP i MP4,
- SVG, archiwa, pliki wykonywalne, pusty i niespojny plik sa odrzucane,
- obraz: maks. 25 MiB; wideo: maks. 500 MiB,
- sesja: maks. 50 plikow, 2 GiB deklarowanego rozmiaru, TTL 30 minut,
- maks. 3 aktywne sesje na publiczny grant,
- quota galerii: 5 GiB lacznie stored + reserved,
- walidowane sa rozszerzenie, deklarowany MIME, magic bytes i rozmiar.

### `POST /api/public/galleries/{slug}/upload-sessions`

Wymaga publicznego grantu z aktualnym `uploadEnabled`, CSRF i naglowka
`Idempotency-Key` (8-128 znakow). Body zawiera manifest:

```json
{
  "files": [
    {
      "clientFileId": "local-1",
      "fileName": "photo.jpg",
      "declaredContentType": "image/jpeg",
      "size": 12345
    }
  ]
}
```

Zwraca `201` z `id`, `status: OPEN`, `expiresAt` i lista plikow w
statusie `PENDING`. Powtorzenie tego samego klucza i manifestu zwraca ten sam
zasob (`200`); ten sam klucz z innym manifestem zwraca `409
IDEMPOTENCY_KEY_CONFLICT`.

### `GET /api/public/galleries/{slug}/upload-sessions/{sessionId}`

Zwraca serwerowy stan sesji i pozwala klientowi wznowic retry, jezeli nadal ma
lokalne obiekty `File`. Przegladarka nie moze automatycznie odtworzyc zawartosci
lokalnych plikow po pelnym odswiezeniu bez ponownego wyboru przez uzytkownika.
Grant musi byc tym samym grantem, ktory utworzyl sesje. Obca sesja zwraca `404
UPLOAD_SESSION_NOT_FOUND`, a wygasla `409 UPLOAD_SESSION_NOT_OPEN` i zwalnia
rezerwacje quota.

### `PUT /api/public/galleries/{slug}/upload-sessions/{sessionId}/files/{clientFileId}`

Wymaga tego samego grantu i CSRF. `multipart/form-data` zawiera jedno pole `file`
i musi miec `Content-Length`. Filtr przed parserem multipart sprawdza grant,
`sessionId`, `clientFileId` i czy rozmiar transportowy jest zgodny z manifestem.
Sukces zwraca `200` z wynikiem `STORED`, wykrytym MIME, size i checksum. Stan
serwerowy pliku moze przejsc przez `RECEIVING`, a awaria kompensacji przez
`CLEANUP_REQUIRED`. Replay po
sukcesie nie tworzy drugiego obiektu. Blad jednego pliku nie blokuje pozostalych.

Kody: `UPLOAD_FILE_TOO_LARGE` (`413`), `UPLOAD_TYPE_NOT_ALLOWED` (`415`),
`UPLOAD_CONTENT_MISMATCH` (`422`), `UPLOAD_SIZE_MISMATCH` (`422`),
`UPLOAD_FILE_NOT_FOUND` (`404`), `UPLOAD_SESSION_NOT_OPEN` (`409`),
`STORAGE_QUOTA_EXCEEDED` (`409`), `STORAGE_WRITE_FAILED` (`503`).
Gdy dwa pelne dekodery obrazu sa zajete, serwer zwraca przejsciowe `503
UPLOAD_VALIDATION_BUSY` zamiast ryzykowac przekroczenie budzetu pamieci.

### `POST /api/public/galleries/{slug}/upload-sessions/{sessionId}/cancel`

CSRF. Idempotentnie ustawia `CANCELLED`, zwalnia rezerwacje plikow bez statusu
`STORED` i zwraca `200`. Nie usuwa poprawnie zapisanych mediow.

### Semantyka storage i danych

- object key jest generowany przez serwer i nie zalezy od nazwy klienta,
- storage jest poza web rootem i nie ma publicznego static mappingu,
- zapis uzywa `.tmp`, `CREATE_NEW`, walidacji i atomowej publikacji przez hard link w jednym volume, bez nadpisywania istniejacego obiektu,
- fizyczne sciezki, hashe tokenow i kody nie wystepuja w API ani logach,
- media po zapisie maja status `STORED`, nie sa publicznie listowane ani pobierane,
- blad po zapisie uruchamia kompensacyjne usuniecie; nierozwiazany cleanup jest
  jawnie oznaczany stanem wymagajacym reconciliacji.
