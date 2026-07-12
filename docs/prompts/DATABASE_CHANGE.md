# Prompt: Database Change

## Cel dokumentu
Szablon promptu do zmian modelu danych i migracji.

## Status dokumentu
- Status: draft
- Zakres: prompt dla zmian bazy danych
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon wspiera analizę zmian bazy i migracji bez naruszania istniejących zasad Flyway.

## Stan docelowy
- Każda zmiana danych jest oceniona pod kątem kompatybilności, ownership i retencji.

## Szablon promptu
```md
Przygotuj zmianę bazy danych dla: [zadanie].

Przed pracą przeczytaj:
- AGENTS.md
- docs/architecture/DATA_MODEL.md
- docs/backend/DATABASE_CONVENTIONS.md
- docs/checklists/DATABASE_CHANGE_CHECKLIST.md
- docs/development/WORKFLOW.md
- [powiązane ADR]

Zasady analizy:
- nie modyfikuj zatwierdzonych migracji Flyway,
- oceń nullability, constraints, indeksy, soft delete, ownership i kompatybilność danych,
- wskaż wpływ na API, backend, frontend i operacje.

Zakres:
- [encje / migracje / indeksy]

Poza zakresem:
- [brak refaktoru niezwiązanego]

Wymagania testowe:
- test migracji,
- test repozytorium lub integracyjny,
- test danych istniejących, jeśli dotyczy.

Wymagania bezpieczeństwa:
- ownership, retencja, backup przed wdrożeniem.

Format końcowego podsumowania:
- zmiany schematu,
- wpływ na dane,
- testy,
- ryzyka migracyjne.
```

## Powiązane dokumenty
- [../checklists/DATABASE_CHANGE_CHECKLIST.md](../checklists/DATABASE_CHANGE_CHECKLIST.md)
- [../adr/0006-soft-delete-and-retention.md](../adr/0006-soft-delete-and-retention.md)

## Decyzje otwarte
- Czy dodać wariant dla zmian wyłącznie indeksowych i wydajnościowych.
