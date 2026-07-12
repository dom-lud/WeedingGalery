# Checklist Release

## Cel dokumentu
Lista kontrolna przed wydaniem lub oznaczeniem zmiany jako gotowej do releasu.

## Status dokumentu
- Status: draft
- Zakres: release checklist
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Checklista jest przygotowana dla przyszłych wydań.

## Stan docelowy
- Każdy release candidate przechodzi przez ten sam zestaw kontroli jakości i operacji.

## Lista kontrolna
- [ ] Build przechodzi.
- [ ] Testy przechodzą.
- [ ] Lint przechodzi.
- [ ] Migracje są przygotowane i zweryfikowane.
- [ ] Konfiguracja została sprawdzona.
- [ ] Zmienne środowiskowe zostały zweryfikowane.
- [ ] Backup został wykonany lub potwierdzony zgodnie z procedurą.
- [ ] Changelog lub opis zakresu releasu jest gotowy.
- [ ] Plan rollbacku jest gotowy.
- [ ] Health check po wdrożeniu jest zaplanowany.
- [ ] Monitoring i alerty są gotowe.
- [ ] Smoke tests są zdefiniowane.

## Powiązane dokumenty
- [../operations/DEPLOYMENT.md](../operations/DEPLOYMENT.md)
- [../prompts/RELEASE_PREPARATION.md](../prompts/RELEASE_PREPARATION.md)

## Decyzje otwarte
- Czy release checklist ma być rozdzielona na staging i production.
