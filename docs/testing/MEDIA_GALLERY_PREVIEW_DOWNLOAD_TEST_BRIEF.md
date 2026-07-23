# Media Gallery Preview And Download Test Design Brief

## Status dokumentu
- Status: draft
- Zakres: publiczny i zalogowany podglad mediow, lightbox, streaming oraz pobranie galerii
- Ostatnia aktualizacja: 2026-07-23

## Wymaganie
- Publiczny uzytkownik z grantem ma widziec dodane zdjecia jako galerie, a nie techniczne kafelki ze statusem pliku.
- Klikniecie w media otwiera powiekszony podglad.
- Owner/manager wydarzenia ma podglad mediow w panelu galerii.
- Owner moze pobrac galerie jako archiwum ZIP.
- Upload ma wygladac live: lokalnie wybrane zdjecie pojawia sie od razu jako rozmyty podglad ze spinnerem, a po zapisie staje sie normalnym elementem galerii.

## Macierz ryzyk

| Ryzyko | Bledna implementacja, ktora test ma wykryc | Scenariusze | Najnizsza wiarygodna warstwa |
| --- | --- | --- | --- |
| IDOR publicznych mediow | Endpoint zwraca plik bez grantu sesji albo dla zlego sluga | grant poprawny, brak grantu, zly slug/media | integracyjny test API |
| IDOR zalogowanych mediow | Uzytkownik spoza eventu pobiera plik po znanym mediaId | owner/manager dozwolony, obcy zabroniony | test serwisu lub API |
| Wyciek storage | API ujawnia `storageKey`, checksum albo sciezke | public/gallery response i owner media list | test kontraktu/API |
| Pobranie galerii bez uprawnien | Manager/gosc moze pobrac ZIP mimo wymagania ownera | owner dozwolony, manager zabroniony | test API |
| UX wraca do technicznych statusow | Publiczna galeria renderuje statusy zamiast obrazow i lightboxa | media obecne, empty, click enlarge | test komponentu |
| Upload nie jest live | Plik pojawia sie dopiero po zakonczeniu uploadu albo bez spinnera | lokalny selection, upload pending/success | test komponentu |
| Niedostepne miniatury blokują podglad | Przetwarzanie jeszcze trwa i thumbnail nie istnieje | fallback do original, spinner/blur | test komponentu/API kontrakt |

## Weryfikacja
- Backend: testy streamingu publicznego i ownerowego, ZIP dla ownera, odmowy dostepu.
- Frontend: testy publicznej galerii, lightboxa, live upload preview i owner preview/download controls.
- Kontrakt: aktualizacja `api-contract/API_CONTRACT.md` przed finalizacja implementacji.
