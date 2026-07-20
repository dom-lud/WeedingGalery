# Test Design Brief - domkniecie Etapow 0-5

## Cel
Zweryfikowac, ze techniczne i bezpieczenstwa zaleglosci wykryte po Etapie 5
zostaly usuniete bez regresji istniejacych flow identity, ownership, galerii i
uploadu.

## Zakres
- bezpieczny, jawnie wlaczany bootstrap pierwszego administratora,
- brak kont testowych i sekretow w migracjach produkcyjnych,
- fail-fast konfiguracji produkcyjnej bez wymaganych zmiennych,
- bezpieczne cookies sesji i CSRF oraz konfigurowalny CORS,
- powtarzalne formatowanie na Windows i Linux,
- zgodnosc migracji, Compose, backendu, frontendu i E2E Etapow 0-5.

## Poza zakresem
- `AUTH-002`, zaproszenia i reset hasla,
- pipeline przetwarzania mediow z Etapu 6,
- HTTPS rollout, backup/restore, alerty i load tests z Etapu 13,
- publiczne listowanie oraz download mediow.

## Macierz wymagan i ryzyk

| Wymaganie / ryzyko | Bledna implementacja, ktora test ma wykryc | Scenariusze | Najnizsza wiarygodna warstwa |
| --- | --- | --- | --- |
| Czysta migracja nie tworzy znanego admina | V1 nadal zapisuje `admin@example.com` lub hash znanego hasla | migracja na pusta H2 i MySQL; liczba uzytkownikow = 0 | kontrakt migracji H2 + Testcontainers MySQL |
| Bootstrap jest jawny i idempotentny | admin powstaje bez flagi, haslo jest resetowane przy restarcie albo bootstrap nadaje role istniejacemu USER | disabled; create; replay; konflikt istniejacego USER; konflikt innego ADMIN; normalizacja e-mail | unit/service + context smoke |
| Bootstrap nie przyjmuje slabych danych | puste/niepoprawne dane tworza uprzywilejowane konto | zly e-mail, haslo ponizej minimum | unit/service |
| Produkcja nie ma domyslnych sekretow | aplikacja laczy sie domyslnym haslem lub przypadkowa lokalna baza | kontrakt placeholderow `DB_*`, storage i bootstrap disabled | configuration contract |
| Produkcyjne cookies sa bezpieczne | `JSESSIONID` lub `XSRF-TOKEN` nie ma `Secure`/`SameSite`, a session cookie nie ma `HttpOnly` | bootstrap CSRF oraz login przy wlaczonym secure-cookies | integration/security |
| CORS nie jest na stale lokalny w prod | produkcja dopuszcza `localhost` bez jawnej konfiguracji | kontrakt konfiguracji prod i test z dozwolonym/niedozwolonym origin | configuration + integration/security |
| Flow 0-5 nie ma regresji | hardening psuje login, CSRF, ownership, upload albo UI | pelny backend verify, frontend coverage/lint/build, Playwright | unit + integration + E2E |
| Lokalne quality gates sa powtarzalne | CRLF powoduje czerwony Prettier tylko na Windows | `format:check` po normalizacji EOL | tooling contract |

## Krytyczne scenariusze negatywne i graniczne
- bootstrap wylaczony nie wykonuje zadnego zapisu,
- replay tych samych danych nie zmienia hasha hasla ani audytu,
- istniejacy USER o adresie bootstrapu nie zostaje podniesiony do ADMIN,
- istniejacy inny ADMIN blokuje utworzenie kolejnego przez bootstrap,
- brak wymaganej zmiennej produkcyjnej powoduje blad konfiguracji zamiast
  uzycia wartosci domyslnej,
- request z obcego originu nie otrzymuje naglowkow CORS bez jawnego allowlist,
- migracje V1-V4 przechodza na rzeczywistym MySQL 8.4.

## Warunek zakonczenia
- wszystkie nowe scenariusze przechodza,
- backend `verify` i Spotless przechodza,
- frontend coverage, lint, format i build przechodza,
- `docker compose config` i build obrazow przechodza,
- Playwright Etapow 0-5 przechodzi bez pominietych scenariuszy,
- self-review nie pozostawia findingow P0/P1 w zakresie closure sprintu.

