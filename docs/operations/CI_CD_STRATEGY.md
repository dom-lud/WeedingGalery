# Strategia CI/CD

## Cel dokumentu
Opisuje docelowa strategie CI/CD dla GitHub Actions oraz aktualne repo truth dla workflow walidacyjnych.

## Status dokumentu
- Status: draft
- Zakres: pull request flow, quality gates, pipeline build/test/deploy, artefakty i rollback
- Ostatnia aktualizacja: 2026-07-20

## Stan obecny
- Repozytorium ma workflow PR validation oraz workflow buildowy dla `main`.
- Backend `verify` z JaCoCo, kontrakt migracji na MySQL, frontend lint/test/coverage/build, walidacja `docker compose config` i budowa obrazow sa egzekwowane automatycznie w GitHub Actions.
- Workflow PR validation uruchamia tez Playwright E2E z audytem accessibility, publikuje raporty Playwright i coverage oraz aktualizuje rzeczowy komentarz PR z tabelami testow, wykresami coverage, lista awarii i bezposrednimi linkami do artefaktow.
- Cache przegladarki Playwright jest kluczowany systemem, projektem Chromium i wersja `@playwright/test`, dlatego zmiany pozostalych zaleznosci nie wymuszaja ponownego pobrania browsera.
- Quality gates sa czesciowo zautomatyzowane; obszary biznesowe, security review i dalsza rozbudowa zakresu E2E nadal wymagaja pracy.

## Stan docelowy
- Kazda zmiana przechodzi przez pull request, automatyczne quality gates i review.
- `main` jest zawsze w stanie mozliwym do zbudowania.
- Deployment wykonuje sie z wersjonowanych artefaktow po przejsciu kontroli jakosci.

## Pull request flow
Kazda zmiana powinna przechodzic przez pull request.

Zakladany flow:

```text
feature branch
-> pull request
-> automatyczne quality gates
-> code review
-> opcjonalny Codex review
-> merge do main
-> build artefaktow
-> deployment na srodowisko docelowe
-> smoke tests
```

## Workflow

### PR validation
Uruchamiany dla pull requestow do `main`.

Zakres:
- checkout,
- setup Java i Node,
- cache Maven i npm,
- backend format (Spotless check) i testy,
- frontend format (Prettier check), lint, testy i build,
- walidacja `docker compose config`,
- budowa obrazu backendu,
- budowa obrazu frontendu,
- start backendu i frontendu dla Playwright E2E,
- publikacja artefaktu `playwright-report`,
- komentarz w PR z podsumowaniem jobow i wyniku Playwright.

### Main build
Uruchamiany po merge do `main`.

Zakres:
- backend format (Spotless check), testy i budowa JAR,
- frontend format (Prettier check), lint, testy i build,
- testy backendowe,
- testy frontendowe,
- budowa obrazow Docker,
- publikacja artefaktow CI jako GitHub Actions artifacts,
- przygotowanie pod pozniejsza publikacje do registry, jesli registry zostanie wybrane.

### Deployment
Uruchamiany recznie albo po zatwierdzeniu srodowiska.

Zakres:
- pobranie wersjonowanych artefaktow,
- weryfikacja konfiguracji i sekretow,
- backup przed migracja, jesli zmiana dotyczy danych,
- migracje,
- aktualizacja kontenerow,
- health checks,
- smoke tests,
- monitoring po wdrozeniu,
- mozliwosc rollbacku.

## Obowiazkowe quality gates

### Backend
- Maven build przechodzi.
- Testy jednostkowe przechodza.
- Testy integracyjne przechodza, jesli zmiana dotyczy API, bazy, security, storage lub background jobs.
- Testy security i ownership przechodza, jesli zmiana dotyczy autoryzacji lub danych uzytkownikow.
- Migracje schematu sa walidowane, jesli zmiana dotyczy danych i repozytorium zawiera juz aktywny mechanizm migracji.
- Brak znanych krytycznych podatnosci w zaleznosciach.

### Frontend
- TypeScript build przechodzi.
- Lint przechodzi.
- Testy frontendu przechodza.
- Testy komponentow, hookow lub widokow przechodza, jesli zmiana dotyczy UI.
- Kluczowe stany UI sa pokryte: loading, empty, error, retry, forbidden i offline, jesli dotycza przeplywu.
- Mobile-first i accessibility sa sprawdzone zgodnie z dokumentacja.
- Frontend uzywa MUI zgodnie z [../frontend/UI_SYSTEM.md](../frontend/UI_SYSTEM.md).

### Docker i operacje
- `docker compose config` przechodzi.
- Obrazy backendu i frontendu buduja sie.
- Health checks sa zdefiniowane dla uslug runtime, gdy zostana wdrozone.
- Zmiany deploymentu maja plan rollbacku.
- Zmiany wymagajace sekretow nie zapisuja sekretow w repozytorium.

### Dokumentacja
- Dokumentacja jest zaktualizowana, jesli zmiana wplywa na architekture, API, model danych, security, operacje, workflow lub UX.
- ADR jest dodany lub zaktualizowany, jesli decyzja wplywa na architekture, bezpieczenstwo, dane albo operacje.
- Linki wzgledne w nowych dokumentach sa poprawne.

## Gates blokujace i ostrzegawcze
Docelowo blokujace:
- backend build,
- frontend build,
- lint,
- testy wymagane zakresem zmiany,
- Docker build dla zmian infrastrukturalnych lub release,
- brak sekretow w repo,
- brak krytycznych podatnosci.

Poczatkowo ostrzegawcze moga byc:
- pelne E2E,
- skanowanie licencji,
- rozbudowane testy wydajnosciowe,
- automatyczne sprawdzanie linkow dokumentacji.

Zmiana gate z ostrzegawczego na blokujacy powinna zostac odnotowana w dokumentacji.

## Cache i artefakty
- Cache Maven powinien byc oparty o `backend/pom.xml`.
- Cache npm powinien byc oparty o `frontend/package-lock.json`.
- Cache przegladarek Playwright powinien byc utrzymywany oddzielnie od cache npm.
- Artefakty builda musza byc powiazane z commit SHA.
- Artefakt `playwright-report` powinien byc publikowany dla kazdego przebiegu E2E, tak aby review mialo dostep do HTML reportu i trace.
- Artefakty backendu i frontendu na `main` powinny byc dostepne do pobrania z workflow jako punkt odniesienia dla dalszych etapow delivery.
- Obrazy Docker powinny byc tagowane co najmniej przez commit SHA; tag `latest` nie moze byc jedynym identyfikatorem produkcyjnym.

## Sekrety CI
- Sekrety przechowujemy w GitHub Actions secrets albo docelowym mechanizmie secret management.
- Sekrety nie moga trafiac do logow.
- Workflow nie moze wypisywac pelnych wartosci env.
- Srodowiska produkcyjne powinny wymagac recznego zatwierdzenia albo environment protection rules.

## Migracje i dane
- Migracje schematu uruchamiamy przed startem nowej wersji aplikacji albo jako jawny etap deploymentu, gdy mechanizm migracji zostanie wdrozony.
- Destrukcyjne migracje wymagaja planu danych, backupu i rollbacku.
- Zmiany schematu musza byc kompatybilne z procesem rollbacku albo miec osobny plan operacyjny.

## Deployment i rollback
- Deployment powinien uzywac wersjonowanych obrazow lub artefaktow.
- Przed deploymentem zmian danych wymagany jest backup.
- Rollback musi wskazywac wersje aplikacji, migracje, konfiguracje i wplyw na dane.
- Po deploymentcie wymagane sa smoke tests dla API, frontendu i krytycznych sciezek.

## Smoke tests
Minimalne smoke tests po deploymentcie:
- frontend odpowiada przez Nginx,
- backend health endpoint odpowiada,
- polaczenie z baza dziala,
- endpoint publiczny galerii zwraca przewidywalny status,
- logowanie lub endpoint auth zwraca przewidywalna odpowiedz,
- upload testowy jest wykonywany na srodowisku testowym, gdy jest dostepny.

## Powiazane dokumenty
- [DEPLOYMENT.md](DEPLOYMENT.md)
- [CONFIGURATION.md](CONFIGURATION.md)
- [ENVIRONMENTS.md](ENVIRONMENTS.md)
- [BACKUP_AND_RESTORE.md](BACKUP_AND_RESTORE.md)
- [../testing/QUALITY_GATES.md](../testing/QUALITY_GATES.md)
- [../development/WORKFLOW.md](../development/WORKFLOW.md)

## Decyzje otwarte
- Docelowy registry obrazow Docker.
- Czy staging bedzie obowiazkowy przed pierwszym produkcyjnym wdrozeniem.
- Kiedy E2E stana sie gate blokujacym.
