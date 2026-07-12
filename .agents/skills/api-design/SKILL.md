# API Design

## Cel dokumentu
Skill wspierający projektowanie i przegląd REST API.

## Status dokumentu
- Status: draft
- Zakres: api design skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Skill opiera się na istniejących konwencjach backendowych i dokumentach API.

## Stan docelowy
- Agent korzysta z niego przy nowych endpointach i zmianach kontraktów.

## Kiedy używać
- Przy projektowaniu endpointów, payloadów, błędów, paginacji i asynchronicznych operacji.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/backend/API_CONVENTIONS.md`
- `docs/backend/API_ENDPOINTS.md`
- `docs/backend/ERROR_HANDLING.md`
- `docs/checklists/API_REVIEW_CHECKLIST.md`

## Wymagane kroki
1. Określ zasób i odpowiedzialność endpointu.
2. Dobierz statusy HTTP i kontrakty błędów.
3. Opisz paginację, filtrowanie i walidację.
4. Opisz auth, ownership i idempotency.
5. Zaktualizuj dokumentację API.

## Checklista
- [ ] REST i nazewnictwo są spójne.
- [ ] Statusy HTTP są poprawne.
- [ ] Paginacja i filtrowanie są opisane.
- [ ] Walidacja jest opisana.
- [ ] Format błędów jest zgodny z projektem.
- [ ] Idempotency jest rozważone.
- [ ] Autoryzacja i ownership są opisane.

## Zakazane działania
- Projektowanie endpointów bez określenia auth i ownership.
- Duplikowanie istniejących wzorców z nowym nazewnictwem bez potrzeby.
- Pomijanie formatu błędów.

## Oczekiwany format wyniku
- lista endpointów,
- auth i ownership,
- walidacja i błędy,
- otwarte decyzje,
- dokumenty do aktualizacji.

## Decyzje otwarte
- Czy przygotować osobny skill dla publicznego API galerii.
