# Strategia Testow

## Cel dokumentu

Opisuje docelowa strategie testowania platformy na poziomie backendu, frontendu i end-to-end.

## Status dokumentu

- Status: draft
- Zakres: test strategy dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-26

## Stan obecny

- Etapy 7-10 maja test design brief, testy backendowe/frontendowe oraz
  Playwright + axe w repozytorium.
- Workflow PR i nightly uruchamiaja E2E na profilu developerskim z H2. Osobny
  test migracji Flyway sprawdza MySQL w runnerze z Dockerem; nie jest to
  substytut pelnego HTTP E2E na MySQL/Compose.
- Produkcyjne braki sa jawnie sledzone w [STAGES_7_10_PRODUCTION_CHECKLIST.md](../checklists/STAGES_7_10_PRODUCTION_CHECKLIST.md).

### Wynik ostatniej walidacji etapow 7-10

- Backend: `./mvnw -B verify` zakonczyl sie zielono; 179 testow przeszlo bez
  failures/errors/skipped, a coverage branches wyniosl `90.22%` przy progu 90%.
- Frontend: Vitest/V8, lint i build przeszly w CI przy zachowaniu progow coverage.
- E2E: Playwright uruchomil 21 scenariuszy, obejmujac public gallery upload,
  CSRF, ownership, moderacje, personalizacje, admin API/UI oraz axe,
  klawiature i responsive behavior.
- Regresje usuniete przed zamknieciem: status `STORED` nie konczy zbyt wczesnie
  pollingu, publiczny exchange wysyla CSRF, a wybor moderacji korzysta z
  semantycznego combobox/option Material UI.

## Stan docelowy

- Testy pokrywaja krytyczne sciezki biznesowe, bezpieczenstwo, ownership, storage i zadania asynchroniczne.

## Piramida testow

- Testy jednostkowe dla logiki domenowej i UI
- Testy integracyjne dla API, repozytoriow, storage i security
- Testy E2E dla glownych przeplywow uzytkownika

## Priorytety

- Ownership i autoryzacja
- Upload i przetwarzanie mediow
- Retencja i usuwanie danych
- Operacje administracyjne
- Stabilnosc kontraktow API

## Zasady jakosci testow

- Przed napisaniem testow przygotuj krotki test design brief: wymaganie/ryzyko,
  poprawne zachowanie, bledna implementacja do wykrycia, wariant pozytywny,
  negatywny i graniczny oraz wlasciwa warstwa testu.
- Utrzymuj jawna macierz wymaganie -> test dla etapow o wysokim ryzyku. Dla
  Etapow 4/4B/5 zrodlem sa aktualne testy integracyjne i kontraktowe opisane w tym dokumencie.
- Coverage jest zapadka regresyjna, a nie dowodem kompletnosci. Progu nie wolno
  obnizac tylko po to, aby zmiana przeszla CI.
- Kazdy przebieg coverage konczy sie analiza na trzech poziomach: wynik globalny,
  wynik per plik/klasa oraz niepokryte branche krytycznych regul. Najpierw
  uzupelnij scenariusze wynikajace z ryzyka, dopiero potem podnos zapadke.
- Zabronione jest poprawianie wyniku przez testy bez istotnych asercji, masowe
  wykluczenia kodu produkcyjnego albo wykonywanie metod bez weryfikacji skutku.
- Blokujaca zapadka wynosi 90% dla kazdej raportowanej metryki backendu i
  frontendu. Security, ownership, limity, storage i idempotency wymagaja
  wszystkich scenariuszy z macierzy nawet wtedy, gdy procent jest wyzszy.
- Dla kazdego wejscia dobierz klasy rownowaznosci: poprawne, brakujace, puste,
  minimalne, maksymalne, `N-1/N/N+1`, zly format/typ, replay/konflikt,
  anulowanie/powtorzenie, timeout/expiry, offline/blad zaleznosci oraz zmiane
  kolejnosci akcji. Nie obiecuj enumeracji nieskonczonej przestrzeni wejsc;
  uzasadnij, dlaczego klasy i interakcje pokrywaja realne zachowania uzytkownika.
- Dla krytycznych ekranow uruchamiaj automatyczny audit WCAG A/AA oraz test
  klawiatury, focusu i braku poziomego overflow.
- Test ma bronic wymagania, kontraktu albo regresji, nie aktualnej struktury kodu.
- Zanim napiszesz kod testu, zawsze wykonaj analize zmian i zaplanuj scenariusze testowe, uwzgledniajac sciezki negatywne i edge case'y.
- Test powinien umiec obalic bledna implementacje, a nie tylko potwierdzic szczesliwa sciezke.
- Dla krytycznych zmian preferuj testy negatywne, graniczne i regresyjne obok testow pozytywnych.
- Nie pisz testu tylko dlatego, ze latwo go dopasowac do obecnego kodu.
- Test musi byc tworzony niezaleznie od implementacji, na podstawie potrzeb biznesowych, kontraktu i ryzyk, tak aby realnie mial szanse znalezc buga.
- Sam fakt, ze test przechodzi, nie oznacza jeszcze dobrej jakosci. Dobry test powinien byc w stanie zawiesc, gdy zachowanie produktu odchyla sie od wymagan.
- Przy krytycznych flow nalezy dodawac warianty szybkie, nieidealne i uzytkownikocentryczne, a nie wylacznie scenariusz "strona sie ustabilizowala i wszystko poszlo idealnie".
- Jesli test jest zbyt mocno sprzezony z detalem implementacyjnym, trzeba to uzasadnic.
- Nie mieszaj testow API z testami E2E UI. Nalezy je utrzymywac oddzielnie, uzywajac innych narzedzi, np. Playwright do E2E, a Spring Boot Test lub REST Assured do API.
- W testach Playwright preferowany jest page object pattern, aby selektory i techniczne kroki byly utrzymywane centralnie, a specy pozostawaly opisem zachowania biznesowego.
- Wyniki Playwright w CI powinny byc widoczne zarowno w artefakcie HTML, jak i w komentarzu PR z podsumowaniem przebiegu, aby reviewer od razu widzial skale problemu bez przeklikiwania calego workflow.
- Allure Report jest dodatkowa warstwa prezentacji E2E dla czlowieka:
  pokazuje testy, kroki, retry/flaky i metadane runu. Z raportu Allure musi
  byc widoczny link do Quality Dashboardu oraz runu GitHub Actions, zeby
  reviewer mogl przejsc do trendow, coverage gates i `quality-report.json`.
- Kazdy istotny feature powinien zostawic dwa slady: macierz ryzyk/test design
  brief w dokumentacji lub artefakcie pracy oraz raport CI z `quality-report.json`.
  Raport historyczny sluzy do trendow, wykrywania regresji i diagnostyki runow,
  ale nie uzasadnia pomijania scenariuszy negatywnych, granicznych albo
  security.
- Nightly quality jest miejscem na pelniejszy, drozszy przebieg regresji:
  backend verify, frontend coverage/build, Docker/Compose, Playwright E2E,
  accessibility i publikacje dashboardu Pages.
- Lokalna weryfikacja krytycznych flow E2E i integracyjnych powinna byc odpalana na komponentach dockerowych po ich przebudowaniu: najpierw `backend\mvnw.cmd -DskipTests package`, potem `docker compose up --build`, a dopiero potem testy.

## Powiazane dokumenty

- [BACKEND_TESTING.md](BACKEND_TESTING.md)
- [FRONTEND_TESTING.md](FRONTEND_TESTING.md)
- [E2E_SCENARIOS.md](E2E_SCENARIOS.md)

## Decyzje otwarte

- Zakres automatycznych testow wydajnosciowych przed pierwsza produkcja.
