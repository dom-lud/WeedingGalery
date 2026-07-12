# Prompt: Feature Implementation

## Cel dokumentu
Gotowy szablon promptu do implementacji funkcji.

## Status dokumentu
- Status: draft
- Zakres: prompt dla implementacji funkcji
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon jest materiałem pomocniczym i nie wykonuje żadnych zmian samodzielnie.

## Stan docelowy
- Krótki prompt prowadzący agenta przez analizę, implementację i domknięcie zadania.

## Szablon promptu
```md
Zrealizuj zadanie: [ID i tytuł].

Cel zadania:
- [opis]

Przed pracą przeczytaj:
- AGENTS.md
- docs/DOCUMENTATION_MAP.md
- docs/development/WORKFLOW.md
- docs/development/DEFINITION_OF_READY.md
- docs/DEFINITION_OF_DONE.md
- [powiązane dokumenty domenowe]
- [powiązane ADR]

Zasady analizy:
- sprawdź aktualny kod i testy,
- wskaż moduły i pliki objęte zmianą,
- oceń wpływ na API, bazę, frontend, auth, ownership, bezpieczeństwo i dokumentację,
- jawnie zdecyduj, czy potrzebni są subagenci,
- jeśli potrzebna jest decyzja architektoniczna, nie implementuj jej bez ADR.

Zakres do wykonania:
- [konkretne elementy]

Poza zakresem:
- [czego nie robić]

Wymagania testowe:
- [testy jednostkowe, integracyjne, E2E lub brak]
- testy mają bronić wymagań, kontraktu i regresji,
- uwzględnij scenariusze negatywne i graniczne tam, gdzie niosą ryzyko.

Wymagania bezpieczeństwa:
- [auth, ownership, upload, dane osobowe, limity]

Nie wychodź poza zakres zadania.
Po implementacji wykonaj self-review i powtarzaj pętlę poprawki-testy-review aż wynik będzie akceptowalny albo blocker będzie jawnie opisany.

Format końcowego podsumowania:
- wykonane zmiany,
- uruchomione testy,
- wynik self-review,
- zaktualizowane dokumenty,
- ryzyka i nieweryfikowane obszary.
```

## Powiązane dokumenty
- [../development/WORKFLOW.md](../development/WORKFLOW.md)
- [../templates/TASK_TEMPLATE.md](../templates/TASK_TEMPLATE.md)

## Decyzje otwarte
- Czy dodać osobny wariant promptu dla zadań stricte domenowych bez zmian UI.
