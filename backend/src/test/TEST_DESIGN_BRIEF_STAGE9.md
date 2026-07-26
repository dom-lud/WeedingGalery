# Test Design Brief - Etap 9 personalizacji

## Wymagania i ryzyka

| Ryzyko | Bledna implementacja, ktora test wykrywa | Najnizsza wiarygodna warstwa | Scenariusze |
| --- | --- | --- | --- |
| Cross-event IDOR | Endpoint uzywa tylko `galleryId` i ignoruje `eventId` | Integracyjny HTTP | owner poprawnego eventu, obcy event, outsider |
| Manager zapisuje konfiguracje | GET i PUT maja wspolna autoryzacje | Integracyjny HTTP | owner GET/PUT, manager GET, manager PUT=403 |
| Niebezpieczny payload | Dowolne enumy, kolory, HTML/XSS lub tekst ponad limit trafia do bazy | Walidacja + integracyjny HTTP | enum spoza allowlisty, zly hex, `<script>`, 1000/1001 znakow |
| Obcy lub nieopublikowany cover | Akceptowany jest mediaId spoza galerii albo bez wymaganej publikacji/storage | Serwis + integracyjny HTTP | cover z tej galerii zatwierdzony i stored, obca galeria, pending, brak storage |
| Ujawnienie storage | Odpowiedz zawiera object key lub sciezke | Integracyjny HTTP | asercja dozwolonych pol i brak `storageKey`/`path` |
| Replay starej wersji | PUT nadpisuje nowsza konfiguracje | Serwis + integracyjny HTTP | version N, drugi PUT z N=409 |
| Granice wersji i defaults | Brak rekordu/defaulty albo bledny version powoduje niejawny zapis | Serwis + integracyjny HTTP | GET bez rekordu, PUT version 0, version stale |

## Pozytywne przypadki

- Owner odczytuje defaultowa konfiguracje i zapisuje komplet dozwolonych pol.
- Manager odczytuje konfiguracje bez prawa mutacji.
- Cover nalezacy do galerii z `APPROVED`, prawidlowym statusem technicznym i istniejacym storage jest zwracany jako samo `coverMediaId`.

## Negatywne i graniczne przypadki

- `welcomeText` o dlugosci 1000 jest akceptowany, 1001 odrzucany.
- Kolory akceptuja tylko `#RRGGBB`; wartosci CSS, URL, javascript i skrocone hex sa odrzucane.
- Tekst zawierajacy znacznik HTML/XSS nie jest zapisywany jako renderowalna tresc.
- Cover spoza galerii, niezatwierdzony, niesstored lub bez obiektu storage jest odrzucany.
- Obcy event i outsider nie otrzymuja informacji o istnieniu zasobu.

## Macierz kontraktu

- `GET /api/events/{eventId}/galleries/{galleryId}/customization`: owner/manager, `200`.
- `PUT /api/events/{eventId}/galleries/{galleryId}/customization`: owner, CSRF, aktualny `version`, `200`.
- Walidacja payloadu: `400 VALIDATION_ERROR`.
- Brak ownership/scopingu: `404` zgodnie z istniejacym event/gallery maskingiem.
- Manager mutacji: `403`.
- Konflikt optimistic locking: `409 CONCURRENT_MODIFICATION`.
