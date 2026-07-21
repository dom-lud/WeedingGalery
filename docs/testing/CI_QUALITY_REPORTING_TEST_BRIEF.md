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

| Ryzyko | Bledna implementacja, ktora ma zostac wykryta | Weryfikacja | Warstwa |
| --- | --- | --- | --- |
| Raport PR pokazuje niepelne dane po awarii joba | Skrypt zaklada, ze kazdy raport istnieje i przerywa generowanie | uruchomienie agregacji z brakujacymi plikami czesciowymi | skrypt CI |
| Coverage gate w raporcie nie zgadza sie z konfiguracja | Progi w komentarzu sa nizsze niz `pom.xml` albo `vitest.config.ts` | porownanie dokumentacji i workflow, self-review progow 90% | review + config |
| Historia dashboardu jest nadpisywana | Renderer zapisuje tylko ostatni run | lokalne uruchomienie rendera z istniejacym `history.json` | skrypt dashboardu |
| Dashboard miesza PR, main i nightly | Wpisy nie maja eventu, brancha i SHA | walidacja `run.event`, `run.branch`, `run.sha` w `latest.json` | skrypt dashboardu |
| Pages publikuje dane z niezaufanego PR | Workflow deployuje dla `pull_request` lub forkow | review triggerow `workflow_run` i permissions Pages | workflow |
| Dane testow moga wstrzyknac HTML | Renderer wstawia tytuly testow bez escapowania | review funkcji escape i renderowania listy awarii | skrypt dashboardu |
| Artefakty diagnostyczne znikaja zbyt szybko | Retencja zostaje na 7 dni | sprawdzenie `retention-days` w uploadach | workflow |
| Failujacy job gubi raporty | Upload artefaktow nie ma `if: always()` | review krokow uploadu i agregacji | workflow |

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
- [ ] Skrypty raportujace zaimplementowane.
- [ ] Workflow PR/main/nightly/Pages zaktualizowane.
- [ ] Dokumentacja zaktualizowana.
- [ ] Lokalna walidacja i self-review wykonane.
