# Test Design Brief: Etapy 7-10

## Etap 7A

| Ryzyko | Bledna implementacja | Scenariusze | Warstwa |
| --- | --- | --- | --- |
| Publiczne media | Lista pokazuje media bez prawidlowego grantu albo media niedostepne | poprawny grant; brak grantu; wygasniecie; obcy slug/media; processing failure | backend integration |
| Download | Pobranie ignoruje ustawienie galerii albo ownership | wlaczone; wylaczone; owner; manager; guest; obcy zasob | backend service/integration |
| Duza galeria | Odczyt zwraca nieograniczona liczbe rekordow | 0; 1; limit-1; limit; limit+1 | backend service/API |
| Publiczny lightbox | Oryginal jest pobierany przed otwarciem albo focus ucieka z dialogu | grid; open; next/previous; Escape; focus return; retry | component/E2E |

## Etap 8

| Ryzyko | Bledna implementacja | Scenariusze | Warstwa |
| --- | --- | --- | --- |
| Widocznosc | Status processingu jest traktowany jako status publikacji | NONE; REQUIRED; pending; approved; hidden; rejected | domain/integration |
| Ownership | Obcy event/gallery/media moze byc moderowany | owner; manager; outsider; admin przez zwykle API | security integration |
| Bulk | Replay albo konflikt wykonuje akcje wielokrotnie | 0; 1; N-1; N; N+1; replay; conflict | service/integration |
| Audyt | Udana decyzja nie ma fail-closed audytu | sukces; odmowa; blad; brak zapisu audytu | service/integration |

## Etap 9

| Ryzyko | Bledna implementacja | Scenariusze | Warstwa |
| --- | --- | --- | --- |
| Walidacja | Uzytkownik zapisuje dowolny HTML/CSS/JS | allowlist; pusty tekst; limit-1; limit; limit+1; nieznany enum | unit/API |
| XSS | Personalizacja wykonuje kod w publicznym widoku | markup; event handler; javascript URL; znaki specjalne | security/E2E |
| Ownership | Obca galeria albo obcy cover zostaje przypisany | owner; manager; outsider; cover z innej galerii | integration |
| Responsive | Dlugie wartosci lamia layout | 360px; 390px; 768px; 1024px; brak covera | component/axe/E2E |

## Etap 10

| Ryzyko | Bledna implementacja | Scenariusze | Warstwa |
| --- | --- | --- | --- |
| Separacja admina | ADMIN omija ownership zwyklego API | user; manager; admin; endpoint zwykly; endpoint admin | security integration |
| RBAC | Zwykly user wykonuje akcje operatorska | brak sesji; USER; ADMIN; brak powodu; replay | API integration |
| Paginacja | Wyniki sa niekompletne albo niestabilne | pusta lista; 1; N-1; N; N+1; filtry; sortowanie | repository/API |
| Audyt | Akcja nie ma aktora, powodu albo wyniku | sukces; odmowa; konflikt; blad zaleznosci; redakcja sekretow | integration |

## Bramka kazdego etapu

Po implementacji wymagane sa testy jednostkowe, integracyjne na MySQL dla zmian danych/API, testy frontendowe oraz Playwright E2E krytycznego flow. Nalezy sprawdzic coverage 90% bez obnizania progow, axe WCAG, klawiature, focus, responsive oraz regresje Etapow 0-6.
