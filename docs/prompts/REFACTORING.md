# Prompt: Refactoring

## Cel dokumentu
Szablon promptu do refaktoryzacji bez zmiany zakresu biznesowego.

## Status dokumentu
- Status: draft
- Zakres: prompt dla refaktoryzacji
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon ma ograniczać refaktoryzację do zmian uzasadnionych technicznie.

## Stan docelowy
- Refaktoryzacja jest mała, mierzalna i bez niekontrolowanych zmian funkcjonalnych.

## Szablon promptu
```md
Przeprowadź refaktoryzację: [obszar].

Przed pracą przeczytaj:
- AGENTS.md
- docs/development/WORKFLOW.md
- docs/DEFINITION_OF_DONE.md
- [powiązane dokumenty domenowe]
- [powiązane ADR]

Zasady analizy:
- wskaż problem techniczny uzasadniający refaktoryzację,
- nie rozszerzaj zakresu na nowe funkcje,
- oceń wpływ na API, auth, ownership, testy i dokumentację.

Zakres:
- [konkretny obszar]

Poza zakresem:
- nowe funkcje i przypadkowe zmiany poboczne.

Wymagania testowe:
- testy regresyjne dla zachowania przed i po.

Wymagania bezpieczeństwa:
- brak osłabienia auth, walidacji i logowania.

Format końcowego podsumowania:
- problem techniczny,
- zakres refaktoryzacji,
- wpływ funkcjonalny,
- testy,
- ryzyka.
```

## Powiązane dokumenty
- [../DEFINITION_OF_DONE.md](../DEFINITION_OF_DONE.md)
- [../checklists/CODE_REVIEW_CHECKLIST.md](../checklists/CODE_REVIEW_CHECKLIST.md)

## Decyzje otwarte
- Czy refaktoryzacje ponad określony rozmiar powinny mieć obowiązkowy ADR techniczny.
