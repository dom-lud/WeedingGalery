# Public Gallery Upload Regressions

## Status dokumentu
- Status: draft
- Zakres: wymagania UX dla publicznej kolejki uploadu i podgladu dodanych mediow
- Ostatnia aktualizacja: 2026-07-21

## Stan obecny
- Publiczna galeria `/g/:slug` pokazuje juz dodane media jako atrakcyjna siatke zdjec i wideo, a klikniecie elementu otwiera powiekszony podglad.
- Wybrane lokalnie pliki sa widoczne od razu jako podglad; w czasie wysylania obraz jest rozmyty i przykryty spinnerem, a po udanym uploadzie galeria odswieza sie do normalnego podgladu z backendu.
- Plik wybrany lokalnie mozna usunac z kolejki przed utworzeniem sesji uploadu.
- Retry jest dostepne tylko dla bledow ponawialnego uploadu. Lokalnie niewalidowalne pliki, np. za duze, pozostaja w stanie bledu i wymagaja usuniecia albo ponownego wyboru poprawnego pliku.
- Statusy techniczne backendu, takie jak `READY`, `PROCESSING` albo `STORED`, nie sa eksponowane jako glowny jezyk interfejsu publicznego.
- Owner i manager maja podglad mediow galerii w panelu wydarzenia. Owner ma dodatkowo link pobrania ZIP-a calej galerii.

## Zasady UX
- Nie oznaczaj lokalnie niewalidowalnego pliku jako gotowego po retry.
- Nie pokazuj `Retry` dla bledow walidacji lokalnej.
- Nie tworz sesji uploadu, jesli wszystkie pliki w kolejce sa lokalnie niepoprawne.
- Nie ujawniaj `storageKey`, checksumow ani fizycznych sciezek w podgladzie publicznym.
- Nie zastepuj mediow kafelkami technicznych metadanych, jesli dostepny jest `thumbnailUrl` albo `contentUrl`.
- Nie pokazuj gosciom ani ownerowi surowych statusow przetwarzania jako glownego komunikatu sukcesu uploadu.
- Podglad i pobieranie maja isc przez kontrolowane endpointy API, nie przez publiczne mapowanie katalogu storage.
