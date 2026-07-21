# Test Design Brief - CI quality reporting

## Cel

Zbudowac GitHub-native reporting dla testow i coverage: jeden maszynowy raport
JSON, czytelny raport PR/run summary, artefakty diagnostyczne, nightly quality
oraz dashboard historii na GitHub Pages.

## Decyzja o subagentach

Nie uruchamiam subagentow. `docs/development/SUBAGENT_ORCHESTRATION.md`
wymaga jawnej decyzji, ale aktualne instrukcje sesji dopuszczaja subagentow
tylko przy wyraznej prosbie. Zakres jest spojny i obejmuje CI, skrypty oraz
dokumentacje, wiec jeden agent utrzymuje najnizsze ryzyko konfliktow plikow.

## Macierz ryzyk

| Ryzyko                                                 | Bledna implementacja, ktora ma zostac wykryta                      | Weryfikacja                                                      | Warstwa           |
| ------------------------------------------------------ | ------------------------------------------------------------------ | ---------------------------------------------------------------- | ----------------- |
| Raport PR pokazuje niepelne dane po awarii joba        | Skrypt zaklada, ze kazdy raport istnieje i przerywa generowanie    | uruchomienie agregacji z brakujacymi plikami czesciowymi         | skrypt CI         |
| Coverage gate w raporcie nie zgadza sie z konfiguracja | Progi w komentarzu sa nizsze niz `pom.xml` albo `vitest.config.ts` | porownanie dokumentacji i workflow, self-review progow 90%       | review + config   |
| Historia dashboardu jest nadpisywana                   | Renderer zapisuje tylko ostatni run                                | lokalne uruchomienie rendera z istniejacym `history.json`        | skrypt dashboardu |
| Dashboard miesza PR, main i nightly                    | Wpisy nie maja eventu, brancha i SHA                               | walidacja `run.event`, `run.branch`, `run.sha` w `latest.json`   | skrypt dashboardu |
| Pages publikuje dane z niezaufanego PR                 | Workflow deployuje dla `pull_request` lub forkow                   | review triggerow `workflow_run` i permissions Pages              | workflow          |
| Dashboard pada na runie bez raportu jakosci            | `quality-dashboard.yml` pobiera `quality-report-json` bez guardow  | najpierw sprawdzic artefakt przez API, deploy tylko gdy istnieje | workflow          |
| Workflow emituje ostrzezenia Node 20                   | Akcje artifact/github-script zostaja na runtime Node 20            | podniesc artifact actions i github-script do wersji Node 24      | workflow          |
| Dane testow moga wstrzyknac HTML                       | Renderer wstawia tytuly testow bez escapowania                     | review funkcji escape i renderowania listy awarii                | skrypt dashboardu |
| Artefakty diagnostyczne znikaja zbyt szybko            | Retencja zostaje na 7 dni                                          | sprawdzenie `retention-days` w uploadach                         | workflow          |
| Failujacy job gubi raporty                             | Upload artefaktow nie ma `if: always()`                            | review krokow uploadu i agregacji                                | workflow          |

## Scenariusze

- pozytywny: backend, frontend i E2E maja raporty; agregacja tworzy
  `quality-report.json`, a dashboard dopisuje wpis do historii,
- negatywny: brak jednego raportu czesciowego nie blokuje JSON-a zbiorczego,
  tylko oznacza sekcje jako `failure` albo `skipped`,
- graniczny: historia ma wiecej niz limit wpisow i zostaje przycieta do
  najnowszych pozycji,
- security: Pages deploy ma minimalne permissions i nie uruchamia sie dla PR.

## Status

- [x] Brief przygotowany przed edycja skryptow i workflow.
- [x] Skrypty raportujace zaimplementowane.
- [x] Workflow PR/main/nightly/Pages zaktualizowane.
- [x] Dokumentacja zaktualizowana.
- [x] Lokalna walidacja i self-review wykonane.

## Wyniki walidacji

- `node .github/scripts/ci-metrics.mjs backend backend` parsuje lokalny backend
  JaCoCo/Surefire: 90 testow, 89 passed, 1 skipped, coverage 94.41%
  instructions i 90.00% branches.
- `node .github/scripts/ci-metrics.mjs frontend frontend` parsuje lokalny
  frontend coverage: statements 97.36%, branches 90.03%, functions 92.04%,
  lines 97.36%. Lokalny `vitest-results.json` nie byl obecny, wiec licznik
  testow w tym szybkim smoke tescie wyniosl 0; CI generuje go jawnie.
- `node .github/scripts/ci-metrics.mjs aggregate .` tworzy `quality-report.json`
  i `quality-report.md` z brakujacym E2E oznaczonym jako `skipped`, gdy
  `E2E_REQUIRED=false`.
- `node .github/scripts/render-quality-dashboard.mjs` generuje
  `quality-dashboard/index.html`, `history.json`, `latest.json` i
  `runs/<run-id>-<attempt>.json`.
- Drugi render z innym `GITHUB_RUN_ID` zachowal 2 wpisy w historii, wiec
  renderer nie nadpisuje trendow jednym ostatnim runem.
- `node --check` przechodzi dla obu skryptow Node.
- `frontend/node_modules/.bin/prettier.cmd --check ...` przechodzi dla
  zmienionych plikow JS/YAML/MD.
- `quality-dashboard.yml` sprawdza istnienie `quality-report-json` przed
  pobraniem artefaktu; stare albo przerwane runy bez raportu sa pomijane z
  warningiem, bez czerwonego deploy workflow.
- Artifact actions zostaly podniesione do `actions/upload-artifact@v7` i
  `actions/download-artifact@v7`, a `actions/github-script` do `@v8`, zeby
  korzystac z runtime Node 24.
- `git diff --check` przechodzi po jednorazowym `safe.directory`; lokalny Git
  wymaga tej opcji przez roznice wlasciciela repo i uzytkownika sandboxa.
- `actionlint` nie jest dostepny lokalnie, wiec nie wykonano dedykowanej
  walidacji skladni GitHub Actions poza review i smoke testami skryptow.
