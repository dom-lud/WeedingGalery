# Test Design Brief: Etap 11, wybrane elementy Etapu 13 i QR-001

## Zakres

- Statystyki wydarzenia i galerii.
- Powiadomienia uzytkownika oraz alerty domenowe administratora.
- Filtrowany podglad audytu i logow eventowych dla administratora.
- QR generowany z publicznego URL bez sekretow.
- Testy obciazeniowe jako osobny artefakt operacyjny.

## Macierz ryzyk

| Ryzyko | Bledna implementacja | Scenariusze | Najnizsza warstwa |
| --- | --- | --- | --- |
| IDOR statystyk | event obcego uzytkownika jest widoczny | owner/manager, outsider, USER bez relacji | service + integration |
| Zakres danych | statystyka obejmuje inna galerie lub daty | brak zakresu, `N-1/N/N+1`, `from > to`, obca galeria | repository + integration |
| Prywatnosc | odpowiedz ujawnia sesje/IP/token | payload pozytywny i redaction audytu | controller/integration |
| Alerty | USER odczytuje lub potwierdza alert admina | anonymous/USER/ADMIN, replay acknowledge | controller + security |
| Powiadomienia | odczyt cudzej notyfikacji | owner, outsider, read replay | service + integration |
| Audyt | filtr ignoruje zasob lub daty | eventType, actorType, resource, date range, pagination | repository + controller |
| QR | kod zawiera token lub dziala dla prywatnej galerii | decode PNG/SVG, public/private, owner/outsider | service + integration |
| QR input | rozmiar powoduje DoS lub zly format | 255/256/2048/2049, PNG/SVG/invalid | controller |
| Obciazenie | dashboard skanuje tabele i timeoutuje | baseline, N+1, rosnaca liczba eventow | load test |
