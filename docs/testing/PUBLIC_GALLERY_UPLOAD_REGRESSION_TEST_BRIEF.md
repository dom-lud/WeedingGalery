# Public Gallery Upload Regression Test Brief

## Status dokumentu
- Status: draft
- Zakres: regresje publicznej galerii, kolejki uploadu i podgladu dodanych mediow
- Ostatnia aktualizacja: 2026-07-21

## Wymaganie
- Uzytkownik publiczny moze usunac lokalnie wybrany plik z kolejki przed uploadem szybkim przyciskiem `X`.
- Kazda rola z dostepem do publicznej galerii, w tym osoba dodajaca, widzi juz dodane media.
- Retry nie moze zmieniac lokalnie niewalidowalnego pliku, np. zbyt duzego, na stan gotowy do uploadu.
- Po udanym uploadzie lokalne kafelki nie moga wisiec ze spinnerem; flow ma resetowac kolejke i pozwalac natychmiast wybrac kolejne zdjecia.
- Publiczny upload ma minimalizowac kroki: wybor poprawnych plikow powinien uruchamiac dodawanie bez dodatkowego, technicznego posredniego kroku.

## Macierz ryzyk

| Ryzyko | Bledna implementacja, ktora test ma wykryc | Scenariusze | Najnizsza wiarygodna warstwa |
| --- | --- | --- | --- |
| Brak usuwania z kolejki | Plik po wyborze zostaje bez mozliwosci odznaczenia szybkim `X` | przed uploadem, kilka plikow | komponent frontend |
| Retry omija walidacje lokalna | Za duzy plik po retry dostaje `PENDING`/Ready i idzie do manifestu | oversized image, klik retry, start upload | komponent frontend |
| Brak publicznego podgladu mediow | `GET /api/public/galleries/{slug}` nie zwraca listy zapisanych mediow | po zapisanym uploadzie, nowy odczyt galerii | integracyjny backend |
| Wyciek danych storage | Publiczny model zwraca `storageKey` albo checksum | media response | integracyjny backend |
| UI nie pokazuje dodanych mediow | Dane z API sa ignorowane albo widoczne tylko dla ownera | osoba dodajaca i gosc z grantem | komponent frontend |
| Spinner zostaje po sukcesie | Plik ma status `PROCESSING`, ale zostaje w lokalnej kolejce jako wiecznie dodawany | upload zakonczony, public gallery refresh, kolejka resetowana | komponent frontend |
| Za duzo krokow dla goscia | Po wyborze plikow trzeba jeszcze czytac i klikac techniczny przycisk uploadu | wybor poprawnego pliku startuje upload automatycznie | komponent frontend |

## Weryfikacja
- Frontend component tests dla usuwania pliku przez `X`, auto-startu uploadu, resetu kolejki po sukcesie, retry oversized i renderowania publicznej listy mediow.
- Backend integration test dla publicznego odczytu galerii po uploadzie, bez `storageKey`.
- Aktualizacja API contract i dokumentacji UX.
