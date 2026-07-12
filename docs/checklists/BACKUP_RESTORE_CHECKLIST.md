# Checklist Backup i Restore

## Cel dokumentu
Lista kontrolna dla backupu i odtwarzania platformy.

## Status dokumentu
- Status: draft
- Zakres: backup/restore checklist
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Checklista wspiera planowanie i weryfikację procedur backupowych.

## Stan docelowy
- Backup i restore są regularnie sprawdzane i mierzalne.

## Lista kontrolna
- [ ] Backup PostgreSQL jest wykonywany.
- [ ] Backup storage jest wykonywany.
- [ ] Backup konfiguracji jest wykonywany.
- [ ] Kopie są przechowywane poza VPS.
- [ ] Retencja kopii jest określona.
- [ ] Backup jest szyfrowany.
- [ ] Test odtworzenia został wykonany.
- [ ] RPO i RTO są udokumentowane.
- [ ] Sprawdzono spójność danych po odtworzeniu.
- [ ] Wynik testu restore został zapisany.

## Powiązane dokumenty
- [../operations/BACKUP_AND_RESTORE.md](../operations/BACKUP_AND_RESTORE.md)
- [../operations/MAINTENANCE.md](../operations/MAINTENANCE.md)

## Decyzje otwarte
- Jak często wykonywać obowiązkowy test pełnego odtworzenia środowiska.
