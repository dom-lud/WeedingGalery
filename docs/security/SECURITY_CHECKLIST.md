# Checklist Bezpieczeństwa

## Cel dokumentu
Służy jako operacyjna lista kontrolna przed wdrożeniem i przy większych zmianach.

## Status dokumentu
- Status: draft
- Zakres: checklisty bezpieczeństwa
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Lista kontrolna jest przygotowana dla przyszłych wdrożeń.

## Stan docelowy
- Każde wydanie produkcyjne przechodzi przez checklistę bezpieczeństwa.

## Checklist aplikacyjny
- [ ] Hashowanie haseł skonfigurowane zgodnie z polityką
- [ ] CSRF i CORS skonfigurowane
- [ ] Publiczne endpointy objęte rate limitingiem
- [ ] Upload waliduje typ, rozmiar i liczbę plików
- [ ] Błędy nie ujawniają szczegółów technicznych
- [ ] Audyt działań admina działa

## Checklist infrastrukturalny
- [ ] HTTPS wymuszone
- [ ] Sekrety poza repozytorium
- [ ] Backup skonfigurowany
- [ ] Rotacja logów aktywna
- [ ] Monitoring miejsca na dysku aktywny
- [ ] Kontenery nie działają jako root, jeśli nie jest to wymagane

## Checklist procesu
- [ ] Przegląd ADR i dokumentacji po zmianie bezpieczeństwa
- [ ] Aktualne zależności bez znanych krytycznych CVE
- [ ] Przetestowane scenariusze błędnych logowań i uploadów

## Powiązane dokumenty
- [SECURITY_REQUIREMENTS.md](SECURITY_REQUIREMENTS.md)
- [../testing/QUALITY_GATES.md](../testing/QUALITY_GATES.md)
- [../operations/DEPLOYMENT.md](../operations/DEPLOYMENT.md)

## Decyzje otwarte
- Czy przed produkcją dodać obowiązkowy półautomatyczny security review template do PR.
