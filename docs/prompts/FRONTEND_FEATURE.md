# Prompt: Frontend Feature

## Cel dokumentu
Szablon promptu do realizacji funkcji frontendowej.

## Status dokumentu
- Status: draft
- Zakres: prompt dla frontend feature
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon opisuje proces pracy nad funkcją UI zgodnie z istniejącą architekturą frontendu.

## Stan docelowy
- Każda funkcja frontendowa uwzględnia loading, error, empty state, mobile-first i dostępność.

## Szablon promptu
```md
Zaimplementuj funkcję frontendową: [ID / tytuł].

Przed pracą przeczytaj:
- AGENTS.md
- docs/frontend/FRONTEND_ARCHITECTURE.md
- docs/frontend/UI_SYSTEM.md
- docs/frontend/STATE_MANAGEMENT.md
- docs/frontend/UPLOAD_UX.md
- docs/frontend/ACCESSIBILITY.md
- docs/checklists/FRONTEND_REVIEW_CHECKLIST.md
- [powiązane dokumenty domenowe]

Zasady analizy:
- wskaż trasy, widoki, komponenty i warstwę API,
- użyj Material UI jako domyślnego systemu UI,
- najpierw sprawdź komponenty MUI, potem props, `sx`, `styled()`, komponent domenowy i dopiero na końcu własny CSS,
- używaj theme zamiast hardcoded kolorów, spacingów i fontów,
- nie dodawaj drugiej pełnej biblioteki UI bez ADR,
- uwzględnij loading state, error state, empty state i responsywność,
- uwzględnij retry, forbidden i offline state, jeśli dotyczą przepływu,
- nie omijaj auth, ownership i guardów widoku.

Zakres:
- [frontend only]

Poza zakresem:
- [backend / infra / przypadkowy redesign]

Wymagania testowe:
- testy komponentów, hooków i warstwy API,
- testy dostępności lub przynajmniej krytycznych interakcji.

Wymagania bezpieczeństwa:
- bezpieczne renderowanie danych, brak dowolnego HTML, prawidłowa obsługa błędów auth.

Format końcowego podsumowania:
- widoki i komponenty,
- stany UI,
- testy,
- dokumentacja,
- ryzyka.
```

## Powiązane dokumenty
- [../frontend/FRONTEND_ARCHITECTURE.md](../frontend/FRONTEND_ARCHITECTURE.md)
- [../frontend/UI_SYSTEM.md](../frontend/UI_SYSTEM.md)
- [../frontend/ACCESSIBILITY.md](../frontend/ACCESSIBILITY.md)

## Decyzje otwarte
- Czy dodać wariant promptu dla samego publicznego widoku galerii.
