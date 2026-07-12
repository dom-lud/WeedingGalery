# Prompt: Backend Feature

## Cel dokumentu
Szablon promptu do realizacji funkcji backendowej.

## Status dokumentu
- Status: draft
- Zakres: prompt dla backend feature
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon jest przewodnikiem roboczym dla zmian backendowych.

## Stan docelowy
- Funkcje backendowe powstają zgodnie z architekturą modułową i wymaganiami bezpieczeństwa.

## Szablon promptu
```md
Zaimplementuj funkcję backendową: [ID / tytuł].

Przed pracą przeczytaj:
- AGENTS.md
- docs/backend/BACKEND_GUIDELINES.md
- docs/backend/API_CONVENTIONS.md
- docs/backend/ERROR_HANDLING.md
- docs/development/WORKFLOW.md
- [powiązane dokumenty domenowe]
- [powiązane ADR]

Zasady analizy:
- wskaż moduł domenowy i use case,
- oceń DTO, walidację, transakcje, auth, ownership, audyt i błędy,
- ogranicz zmianę do minimalnego spójnego zakresu.

Zakres:
- [backend only]

Poza zakresem:
- [frontend / infra / niezwiązany refaktor]

Wymagania testowe:
- testy jednostkowe i integracyjne,
- testy auth i ownership, jeśli dotyczy.

Wymagania bezpieczeństwa:
- auth, ownership, logowanie i brak wycieków danych.

Format końcowego podsumowania:
- moduły i pliki,
- logika biznesowa,
- testy,
- dokumentacja,
- ryzyka.
```

## Powiązane dokumenty
- [../backend/BACKEND_GUIDELINES.md](../backend/BACKEND_GUIDELINES.md)
- [../checklists/FEATURE_CHECKLIST.md](../checklists/FEATURE_CHECKLIST.md)

## Decyzje otwarte
- Czy dodać wariant backendowego promptu dla wyłącznie administracyjnych use case.
