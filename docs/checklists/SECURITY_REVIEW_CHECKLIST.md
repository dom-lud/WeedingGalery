# Checklist Security Review

## Cel dokumentu
Lista kontrolna do przeglądu bezpieczeństwa zmian funkcjonalnych i architektonicznych.

## Status dokumentu
- Status: draft
- Zakres: security review checklist
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Checklista ma charakter manualny i wspiera analizę bezpieczeństwa.

## Stan docelowy
- Wrażliwe zmiany przechodzą przez uporządkowany przegląd bezpieczeństwa.

## Lista kontrolna
- [ ] Authentication zostało sprawdzone.
- [ ] Authorization zostało sprawdzone.
- [ ] Ownership i IDOR zostały sprawdzone.
- [ ] CSRF zostało uwzględnione.
- [ ] CORS zostało uwzględnione.
- [ ] XSS zostało uwzględnione.
- [ ] SQL injection i inne injection zostały uwzględnione.
- [ ] Path traversal zostało sprawdzone.
- [ ] Uploady i typy MIME zostały sprawdzone, jeśli dotyczy.
- [ ] Limity i rate limiting zostały sprawdzone.
- [ ] Sekrety i konfiguracja zostały sprawdzone.
- [ ] Dane osobowe i logi zostały przeanalizowane.
- [ ] Działania administratora są autoryzowane i audytowane.

## Powiązane dokumenty
- [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md)
- [../prompts/SECURITY_REVIEW.md](../prompts/SECURITY_REVIEW.md)

## Decyzje otwarte
- Czy część punktów powinna mieć obowiązkowe artefakty dowodowe.
