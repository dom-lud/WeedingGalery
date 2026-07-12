# Prompt: Bug Fix

## Cel dokumentu
Szablon promptu do naprawy błędu bez wychodzenia poza wymagany zakres.

## Status dokumentu
- Status: draft
- Zakres: prompt dla naprawy błędów
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon wspiera analizę i naprawę błędu, ale nie zastępuje dokumentacji domenowej.

## Stan docelowy
- Agent diagnozuje przyczynę, naprawia błąd i ogranicza ryzyko regresji.

## Szablon promptu
```md
Napraw błąd: [ID / tytuł / objaw].

Opis problemu:
- [objawy]
- [kroki reprodukcji]
- [oczekiwane zachowanie]

Przed pracą przeczytaj:
- AGENTS.md
- docs/development/WORKFLOW.md
- docs/DEFINITION_OF_DONE.md
- [powiązane dokumenty domenowe]
- [powiązane ADR]

Zasady analizy:
- odtwórz problem na podstawie kodu, testów i logiki,
- znajdź przyczynę źródłową, nie tylko objaw,
- oceń wpływ na auth, ownership, bezpieczeństwo, API i dane.

Zakres:
- naprawa przyczyny błędu,
- minimalny potrzebny refaktor,
- test regresyjny.

Poza zakresem:
- szersze porządki niezwiązane z błędem.

Wymagania testowe:
- test reprodukujący błąd,
- test potwierdzający poprawkę.

Wymagania bezpieczeństwa:
- nie pogarszaj autoryzacji, ownership ani logowania błędów.

Format końcowego podsumowania:
- przyczyna błędu,
- zakres poprawki,
- testy,
- ryzyka pozostałe.
```

## Powiązane dokumenty
- [../checklists/FEATURE_CHECKLIST.md](../checklists/FEATURE_CHECKLIST.md)
- [../checklists/CODE_REVIEW_CHECKLIST.md](../checklists/CODE_REVIEW_CHECKLIST.md)

## Decyzje otwarte
- Czy utworzyć osobny wariant dla incydentów produkcyjnych.
