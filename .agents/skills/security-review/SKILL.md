# Security Review

## Cel dokumentu
Skill wspierający bezpieczeństwo zmian funkcjonalnych i technicznych.

## Status dokumentu
- Status: draft
- Zakres: security review skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Skill służy do uporządkowanego przeglądu ryzyk bezpieczeństwa.

## Stan docelowy
- Agent używa go przy auth, uploadach, adminie, danych i publicznym API.

## Kiedy używać
- Przy zmianach w authentication, authorization, uploadzie, publicznych endpointach, panelu administratora i retencji danych.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/security/SECURITY_REQUIREMENTS.md`
- `docs/security/FILE_UPLOAD_SECURITY.md`
- `docs/security/THREAT_MODEL.md`
- `docs/checklists/SECURITY_REVIEW_CHECKLIST.md`

## Wymagane kroki
1. Sprawdź authentication i authorization.
2. Sprawdź ownership i IDOR.
3. Sprawdź upload security, CORS, CSRF, XSS i rate limiting.
4. Sprawdź sekrety, logowanie i działania administratora.
5. Wskaż ryzyka i luki testowe.

## Checklista
- [ ] Authentication jest poprawne.
- [ ] Authorization jest poprawne.
- [ ] Ownership i IDOR są sprawdzone.
- [ ] Upload security jest sprawdzone, jeśli dotyczy.
- [ ] CORS i CSRF są sprawdzone.
- [ ] XSS i injection są sprawdzone.
- [ ] Rate limiting jest sprawdzone.
- [ ] Sekrety i logowanie są sprawdzone.

## Zakazane działania
- Pomijanie ryzyk „bo to tylko MVP”.
- Deklarowanie bezpieczeństwa bez sprawdzenia ścieżek negatywnych.
- Brak wskazania nieweryfikowanych obszarów.

## Oczekiwany format wyniku
- krytyczne ryzyka,
- ryzyka średnie,
- rekomendacje,
- wymagane testy,
- nieweryfikowane obszary.

## Decyzje otwarte
- Czy security review będzie obowiązkowe dla wszystkich zmian admin API.
