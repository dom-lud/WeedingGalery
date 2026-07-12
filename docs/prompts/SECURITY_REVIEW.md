# Prompt: Security Review

## Cel dokumentu
Szablon promptu do security review funkcji lub zmiany technicznej.

## Status dokumentu
- Status: draft
- Zakres: prompt dla przeglądu bezpieczeństwa
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon wspiera analizę bezpieczeństwa i odwołuje się do istniejących wymagań security.

## Stan docelowy
- Każda zmiana wrażliwa jest oceniana pod kątem realnych wektorów ataku.

## Szablon promptu
```md
Wykonaj security review dla: [zakres].

Przed pracą przeczytaj:
- AGENTS.md
- docs/security/SECURITY_REQUIREMENTS.md
- docs/security/FILE_UPLOAD_SECURITY.md
- docs/security/THREAT_MODEL.md
- docs/checklists/SECURITY_REVIEW_CHECKLIST.md
- [powiązane dokumenty domenowe]
- [powiązane ADR]

Zasady analizy:
- sprawdź authentication, authorization, ownership i IDOR,
- oceń CSRF, CORS, XSS, injection, upload security, rate limiting i logowanie,
- wskaż realne ryzyka, nie ogólniki.

Zakres:
- [moduły / endpointy / proces]

Poza zakresem:
- [obszary nieobjęte]

Wymagania testowe:
- [security test cases]

Wymagania bezpieczeństwa:
- pełny zakres tego zadania jest bezpieczeństwem.

Format końcowego podsumowania:
- krytyczne ryzyka,
- ryzyka średnie,
- rekomendacje,
- obszary nieweryfikowane.
```

## Powiązane dokumenty
- [../checklists/SECURITY_REVIEW_CHECKLIST.md](../checklists/SECURITY_REVIEW_CHECKLIST.md)
- [../operations/INCIDENT_RESPONSE.md](../operations/INCIDENT_RESPONSE.md)

## Decyzje otwarte
- Czy security review będzie zapisywane jako osobny artefakt per zadanie.
