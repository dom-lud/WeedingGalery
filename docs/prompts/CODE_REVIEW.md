# Prompt: Code Review

## Cel dokumentu
Szablon promptu do review zmian z naciskiem na błędy, ryzyka i regresje.

## Status dokumentu
- Status: draft
- Zakres: prompt dla code review
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon wspiera review, ale nie zastępuje checklist review i zasad projektu.

## Stan docelowy
- Review skupia się na poprawności, bezpieczeństwie i zgodności z architekturą.

## Szablon promptu
```md
Przeprowadź code review dla: [zakres zmian].

Przed pracą przeczytaj:
- AGENTS.md
- docs/DEFINITION_OF_DONE.md
- docs/checklists/CODE_REVIEW_CHECKLIST.md
- [powiązane dokumenty domenowe]
- [powiązane ADR]

Zasady analizy:
- skup się na bugach, ryzykach, regresjach i brakujących testach,
- sprawdź auth, ownership, walidację, błędy i dokumentację,
- nie proponuj zmian stylistycznych bez znaczenia technicznego.

Zakres review:
- [pliki / moduły]

Poza zakresem:
- [elementy nieobjęte review]

Wymagania testowe:
- oceń czy istniejące i nowe testy są wystarczające.

Wymagania bezpieczeństwa:
- oceń auth, ownership, dane wrażliwe, logowanie i upload, jeśli dotyczy.

Format końcowego podsumowania:
- findings uporządkowane od najważniejszych,
- pytania otwarte,
- krótka ocena ryzyka.
```

## Powiązane dokumenty
- [../checklists/CODE_REVIEW_CHECKLIST.md](../checklists/CODE_REVIEW_CHECKLIST.md)
- [../checklists/SECURITY_REVIEW_CHECKLIST.md](../checklists/SECURITY_REVIEW_CHECKLIST.md)

## Decyzje otwarte
- Czy dodać wariant review dla zmian dokumentacyjnych bez kodu.
