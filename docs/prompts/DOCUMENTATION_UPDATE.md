# Prompt: Documentation Update

## Cel dokumentu
Szablon promptu do aktualizacji dokumentacji projektu.

## Status dokumentu
- Status: draft
- Zakres: prompt dla zmian dokumentacyjnych
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon wspiera aktualizację dokumentacji bez tworzenia duplikatów.

## Stan docelowy
- Dokumentacja jest utrzymywana równolegle ze zmianami technicznymi i backlogiem.

## Szablon promptu
```md
Zaktualizuj dokumentację dla: [zadanie / obszar].

Przed pracą przeczytaj:
- AGENTS.md
- docs/DOCUMENTATION_MAP.md
- docs/development/WORKFLOW.md
- docs/DEFINITION_OF_DONE.md
- [powiązane dokumenty domenowe]
- [powiązane ADR]

Zasady analizy:
- nie duplikuj treści, linkuj do źródła,
- rozróżnij stan obecny i docelowy,
- nie twierdź, że funkcja istnieje, jeśli nie została wdrożona.

Zakres:
- [lista dokumentów]

Poza zakresem:
- zmiany w niepowiązanych sekcjach.

Wymagania testowe:
- sprawdzenie linków względnych i spójności nazw.

Wymagania bezpieczeństwa:
- brak sekretów, brak fałszywych stwierdzeń o wdrożonych mechanizmach.

Format końcowego podsumowania:
- utworzone lub zaktualizowane pliki,
- powiązane dokumenty,
- otwarte decyzje.
```

## Powiązane dokumenty
- [../DOCUMENTATION_MAP.md](../DOCUMENTATION_MAP.md)
- [../checklists/FEATURE_CHECKLIST.md](../checklists/FEATURE_CHECKLIST.md)

## Decyzje otwarte
- Czy dodać automatyczny przegląd spójności dokumentacji w CI.
