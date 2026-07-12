# Prompt: Release Preparation

## Cel dokumentu
Szablon promptu do przygotowania wydania lub wdrożenia.

## Status dokumentu
- Status: draft
- Zakres: prompt dla release preparation
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon opisuje planowanie releasu; nie oznacza, że pipeline releasowy już istnieje.

## Stan docelowy
- Przygotowanie wydania obejmuje build, testy, backup, rollout, rollback i smoke tests.

## Szablon promptu
```md
Przygotuj release dla: [wersja / zakres].

Przed pracą przeczytaj:
- AGENTS.md
- docs/operations/DEPLOYMENT.md
- docs/operations/CONFIGURATION.md
- docs/operations/BACKUP_AND_RESTORE.md
- docs/checklists/RELEASE_CHECKLIST.md
- docs/checklists/DEPLOYMENT_CHECKLIST.md

Zasady analizy:
- sprawdź build, testy, migracje, konfigurację, backup, rollback i monitoring,
- wskaż blokery do releasu,
- nie deklaruj sukcesu, jeśli coś nie zostało uruchomione lub sprawdzone.

Zakres:
- [release candidate]

Poza zakresem:
- implementacja nowych funkcji.

Wymagania testowe:
- build, lint, testy, smoke tests.

Wymagania bezpieczeństwa:
- sekrety, konfiguracja, backup, dostęp administracyjny, health checks.

Format końcowego podsumowania:
- gotowość do releasu,
- blokery,
- wykonane kontrole,
- plan rollbacku.
```

## Powiązane dokumenty
- [../checklists/RELEASE_CHECKLIST.md](../checklists/RELEASE_CHECKLIST.md)
- [../checklists/DEPLOYMENT_CHECKLIST.md](../checklists/DEPLOYMENT_CHECKLIST.md)

## Decyzje otwarte
- Czy dodać osobny prompt dla hotfix release.
