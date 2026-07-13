# Git Hooks

## Cel dokumentu
Opisuje lokalne hooki Git utrzymywane w repozytorium oraz sposob ich aktywacji.

## Status dokumentu
- Status: draft
- Zakres: lokalne hooki developerskie
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Repozytorium utrzymuje hook `pre-commit` w katalogu `.githooks/`.
- Hook przed commitem uruchamia `npm run format:check` w `frontend` oraz `spotless:check` w `backend`.

## Aktywacja
W repozytorium ustaw lokalna sciezke hookow:

```powershell
git config core.hooksPath .githooks
```

Od tego momentu `git commit` uruchomi:
- frontendowy check formatowania,
- backendowy Spotless check.

## Zasady
- Hook ma blokowac commit, jesli formatowanie frontendu albo backendu nie przechodzi.
- Hook nie zastepuje CI; ma szybciej wychwycic problem lokalnie.
- Jesli zmienisz standardy formatowania albo narzedzia quality gates, zaktualizuj hook razem z dokumentacja.

## Powiazane dokumenty
- [WORKFLOW.md](WORKFLOW.md)
- [../testing/TEST_STRATEGY.md](../testing/TEST_STRATEGY.md)
- [../operations/CI_CD_STRATEGY.md](../operations/CI_CD_STRATEGY.md)
