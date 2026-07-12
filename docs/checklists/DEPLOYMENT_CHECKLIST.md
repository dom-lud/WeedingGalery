# Checklist Deployment

## Cel dokumentu
Lista kontrolna dla samego procesu wdrożenia.

## Status dokumentu
- Status: draft
- Zakres: deployment checklist
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Checklista wspiera manualny rollout.

## Stan docelowy
- Wdrożenie jest przewidywalne i odtwarzalne operacyjnie.

## Lista kontrolna
- [ ] Wersja do wdrożenia jest jednoznacznie wskazana.
- [ ] Backup przed wdrożeniem jest potwierdzony.
- [ ] Migracje są gotowe do uruchomienia.
- [ ] Sekrety i konfiguracja środowiska są poprawne.
- [ ] Health checks i readiness są dostępne.
- [ ] Monitoring działa przed rolloutem.
- [ ] Plan rollbacku jest przygotowany.
- [ ] Smoke test po wdrożeniu jest uruchamiany.
- [ ] Logi i alerty są obserwowane po rolloutcie.
- [ ] Wynik wdrożenia jest zapisany w podsumowaniu operacyjnym.

## Powiązane dokumenty
- [../operations/DEPLOYMENT.md](../operations/DEPLOYMENT.md)
- [../operations/ENVIRONMENTS.md](../operations/ENVIRONMENTS.md)

## Decyzje otwarte
- Czy deployment checklist ma obejmować osobny etap akceptacji produktu po smoke testach.
