# Release Preparation

## Cel dokumentu
Skill wspierający przygotowanie wydania i wdrożenia.

## Status dokumentu
- Status: draft
- Zakres: release preparation skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Skill wspiera planowanie releasu, ale nie oznacza istnienia kompletnej automatyzacji release.

## Stan docelowy
- Agent używa go przy przygotowaniu wydania, rolloutu i smoke testów.

## Kiedy używać
- Przy przygotowaniu release candidate, wdrożenia, rollback planu lub checklist releasowych.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/operations/DEPLOYMENT.md`
- `docs/operations/CONFIGURATION.md`
- `docs/operations/BACKUP_AND_RESTORE.md`
- `docs/checklists/RELEASE_CHECKLIST.md`
- `docs/checklists/DEPLOYMENT_CHECKLIST.md`

## Wymagane kroki
1. Sprawdź build, testy i lint.
2. Sprawdź migracje i wpływ na dane.
3. Sprawdź backup i rollback.
4. Sprawdź konfigurację i sekrety.
5. Sprawdź health checks, monitoring i smoke tests.

## Checklista
- [ ] Build jest gotowy.
- [ ] Testy są gotowe.
- [ ] Migracje są zweryfikowane.
- [ ] Backup został potwierdzony.
- [ ] Konfiguracja i env są gotowe.
- [ ] Smoke tests są gotowe.
- [ ] Rollback jest gotowy.
- [ ] Monitoring jest gotowy.

## Zakazane działania
- Deklarowanie gotowości releasu bez sprawdzenia backupu lub rollbacku.
- Pomijanie wpływu migracji na dane.
- Ukrywanie nieweryfikowanych obszarów.

## Oczekiwany format wyniku
- status gotowości,
- blokery,
- wykonane kontrole,
- plan rollbacku,
- ryzyka operacyjne.

## Decyzje otwarte
- Czy dodać osobny skill dla hotfixów i incydentowych wdrożeń.
