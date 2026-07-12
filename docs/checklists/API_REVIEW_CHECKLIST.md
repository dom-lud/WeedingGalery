# Checklist API Review

## Cel dokumentu
Lista kontrolna dla projektowania i przeglądu endpointów API.

## Status dokumentu
- Status: draft
- Zakres: API review checklist
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Checklista wspiera ręczny przegląd kontraktów API.

## Stan docelowy
- API jest spójne, bezpieczne i przewidywalne dla klientów.

## Lista kontrolna
- [ ] Endpointy są zgodne z konwencjami REST.
- [ ] Prefiksy i nazwy zasobów są spójne.
- [ ] Statusy HTTP są dobrane poprawnie.
- [ ] Walidacja wejścia jest zdefiniowana.
- [ ] Paginacja, sortowanie i filtrowanie są określone, jeśli potrzebne.
- [ ] Format błędów jest zgodny z konwencją projektu.
- [ ] Idempotency zostało ocenione.
- [ ] Autoryzacja jest zdefiniowana.
- [ ] Ownership zasobów jest zdefiniowany.
- [ ] Odpowiedzi asynchroniczne są opisane, jeśli dotyczy.

## Powiązane dokumenty
- [../backend/API_CONVENTIONS.md](../backend/API_CONVENTIONS.md)
- [../prompts/API_DESIGN.md](../prompts/API_DESIGN.md)

## Decyzje otwarte
- Czy do review API dodać obowiązkowe przykłady request/response dla nowych endpointów.
