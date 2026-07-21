# Test Design Brief - coverage 90 i testy pancerne

Status: completed (2026-07-21)

## Cel

Osiagnac i utrzymac minimum 90% dla kazdej raportowanej metryki backendu i
frontendu przez testy zachowania, ktore wykrywaja realne regresje dostepne z API,
UI, danych, storage oraz granic wejscia.

## Zasada zakresu

Nie istnieje skonczony zestaw wszystkich mozliwych zachowan uzytkownika. Zakres
obejmuje kazda osiagalna sciezke obecnego produktu oraz reprezentantow klas
rownowaznosci: poprawne dane, brak danych, minimum, maksimum, `N-1/N/N+1`, zly
format, powtorzenie, konflikt, przerwanie, blad zaleznosci, brak uprawnien i
nieoczekiwana kolejnosc akcji.

## Stan bazowy

| Obszar | Wynik bazowy | Cel |
| --- | ---: | ---: |
| Backend instructions | 88.53% | >= 90% |
| Backend branches | 68.57% | >= 90% |
| Frontend statements / lines | 89.33% | >= 90% |
| Frontend branches | 81.37% | >= 90% |
| Frontend functions | 72.41% | >= 90% |

## Macierz ryzyk

| Obszar | Bledna implementacja do wykrycia | Scenariusze pozytywne, negatywne i graniczne | Warstwa |
| --- | --- | --- | --- |
| Event UI | utrata danych, akcje ownera dostepne managerowi, zly lifecycle | create/update z pustymi i pelnymi opcjami, anulowanie, blad zapisu, archive/delete confirm/cancel, zmiana wyboru, pusta lista | Vitest component + E2E |
| Membership UI | IDOR lub usuniecie/transfer niewlasciwej osoby | add success/error, duplikat, cancel, remove self/other, transfer success/error/cancel, owner i manager | Vitest component + E2E |
| Gallery UI | niewlasciwe akcje roli, utrata focusu, zly status | create/edit success/error/cancel, publish/archive/delete success/error/cancel, link rotation, access modes, loading/empty/retry | Vitest component + E2E |
| Public gallery | token/code omijany, upload pozostaje w zlym stanie | brak slug, token URL/storage, wymagany/zly kod, odmowa, rate limit, pusta/duza partia, duplikaty nazw, partial failure, retry, cancel i blad cancel | Vitest component + integration/E2E |
| Auth i routing | fail-open, podwojny submit, bledny redirect | loading, anonymous, authenticated, blad CSRF/me/login/logout, szybki klik, Enter, toggle hasla | Vitest + E2E |
| API errors | utrata kodu/detail/correlation ID | odpowiedz z i bez body, network error, 401/403/404/409/413/429/5xx | Vitest contract + Spring integration |
| Upload declaration | spoofing rozszerzenia/MIME/nazwy | null/empty, case, NUL/path traversal, brak extension, wielokropki, `N-1/N/N+1` rozmiaru i nazwy | JUnit unit/integration |
| Media signatures | parser akceptuje uszkodzony lub poliglotyczny plik | JPEG/PNG/WebP warianty i truncation, MP4 box size 0/1/overflow, brak ftyp/moov/mdat, zla marka | JUnit unit |
| Upload lifecycle | replay tworzy dane, konflikt nadpisuje, awaria zostawia sieroty | first/replay/conflict, expired/cancelled/completed, quota/session limits `N-1/N/N+1`, storage save/delete failure, kompensacja | JUnit service + Spring integration |
| Public access | kod/token lub rate limit mozna ominac | public/code/private, brak/zly/poprawny sekret, wygasly token, archived gallery, IP normalization, limit `N-1/N/N+1`, okno czasowe | JUnit service/integration |
| Storage | traversal, overwrite, partial write lub zle sprzatanie | poprawny klucz, absolute/`..`/separator/NUL, pusty stream, kolizja, IO failure, delete missing, root boundary | JUnit unit |
| Ownership | sam identyfikator daje dostep do obcego zasobu | owner/manager/obcy/anonymous dla read/write/lifecycle i membership | JUnit service/integration + E2E |
| Error mapping | wyjatek zwraca zly status lub ujawnia szczegoly | wszystkie kody domenowe, validation single/multi, malformed body, unknown exception, rate limit headers | Spring MVC integration |

## Warunki zakonczenia

- kazda metryka globalna backendu i frontendu wynosi co najmniej 90%,
- zmienione klasy i komponenty maja przeanalizowane niepokryte branche,
- testy maja asercje efektu, kontraktu albo braku niedozwolonego efektu,
- nie dodano wykluczen coverage ani testow wykonywanych tylko dla licznika,
- backend `clean verify`, frontend coverage/lint/format/build i pelne E2E przechodza,
- bramki w konfiguracji, CI, dokumentacji i skills sa zsynchronizowane na 90%,
- self-review nie pozostawia findingow P0/P1.

## Wynik koncowy

| Obszar | Wynik | Bramka |
| --- | ---: | ---: |
| Backend instructions | 94.41% | 90% |
| Backend branches | 90.00% | 90% |
| Frontend statements / lines | 97.36% | 90% |
| Frontend branches | 90.03% | 90% |
| Frontend functions | 92.04% | 90% |

- Backend: 90 testow uruchomionych, 0 failures/errors, 1 lokalny kontrakt
  Testcontainers pominiety; `verify` i bramka JaCoCo 90/90 przechodza.
- Frontend: 52/52 Vitest, lint, Prettier i build przechodza.
- E2E: 15/15 Playwright na zdrowym stacku, w tym auth, ownership, galerie,
  public upload, WCAG A/AA, klawiatura/focus i responsywnosc.
- Nie dodano wykluczen coverage ani zmian w kodzie produkcyjnym/API.
