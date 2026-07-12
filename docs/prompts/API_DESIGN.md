# Prompt: API Design

## Cel dokumentu
Szablon promptu do projektowania lub przeglądu API REST.

## Status dokumentu
- Status: draft
- Zakres: prompt dla API design
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon wspiera projektowanie API zgodnie z istniejącymi konwencjami backendu.

## Stan docelowy
- Projekt API uwzględnia auth, ownership, błędy i długofalową spójność kontraktów.

## Szablon promptu
```md
Zaprojektuj API dla: [funkcja / moduł].

Przed pracą przeczytaj:
- AGENTS.md
- docs/backend/API_CONVENTIONS.md
- docs/backend/API_ENDPOINTS.md
- docs/backend/ERROR_HANDLING.md
- docs/checklists/API_REVIEW_CHECKLIST.md
- [powiązane dokumenty domenowe]

Zasady analizy:
- użyj REST i istniejących prefiksów,
- opisz endpointy, statusy HTTP, walidację, paginację, filtrowanie, błędy, idempotency,
- wskaż auth i ownership dla każdego kluczowego endpointu.

Zakres:
- [endpointy / kontrakty]

Poza zakresem:
- [implementacja kodu]

Wymagania testowe:
- testy API, walidacji i autoryzacji.

Wymagania bezpieczeństwa:
- auth, ownership, rate limiting, brak ujawniania zasobów.

Format końcowego podsumowania:
- proponowane endpointy,
- model auth,
- błędy i statusy,
- otwarte decyzje.
```

## Powiązane dokumenty
- [../backend/API_CONVENTIONS.md](../backend/API_CONVENTIONS.md)
- [../backend/ERROR_HANDLING.md](../backend/ERROR_HANDLING.md)

## Decyzje otwarte
- Czy dodać osobny prompt dla publicznego API galerii.
