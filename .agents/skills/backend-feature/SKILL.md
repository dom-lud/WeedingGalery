# Backend Feature

## Cel dokumentu
Skill wspierający implementację funkcji backendowej w modularnym monolicie.

## Status dokumentu
- Status: draft
- Zakres: backend feature skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Skill jest przewodnikiem roboczym i nie zastępuje dokumentacji domenowej.

## Stan docelowy
- Agent korzysta ze skill przy zadaniach backendowych obejmujących use case, API, auth i testy.

## Kiedy używać
- Przy implementacji lub zmianie funkcji backendowej.
- Przy dodawaniu endpointu, use case, walidacji, auth, ownership albo transakcji.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/development/WORKFLOW.md`
- `docs/backend/BACKEND_GUIDELINES.md`
- `docs/backend/API_CONVENTIONS.md`
- `docs/backend/ERROR_HANDLING.md`
- odpowiednie dokumenty domenowe i ADR

## Wymagane kroki
1. Zidentyfikuj moduł domenowy i use case.
2. Sprawdź DTO, walidację i format błędów.
3. Określ auth, ownership i granice transakcji.
4. Dodaj minimalny zakres zmian.
5. Dodaj testy jednostkowe i integracyjne.
6. Zaktualizuj dokumentację.

## Checklista
- [ ] Architektura domenowa została zachowana.
- [ ] DTO są jawne i nie ujawniają encji.
- [ ] Walidacja wejścia została dodana.
- [ ] Centralna obsługa błędów została zachowana.
- [ ] Ownership i autoryzacja zostały sprawdzone.
- [ ] Transakcje są poprawnie wyznaczone.
- [ ] Istnieją testy jednostkowe i integracyjne.

## Zakazane działania
- Omijanie use case przez kontroler.
- Zwracanie encji JPA bezpośrednio do API.
- Dodawanie logiki auth wyłącznie w kontrolerze bez kontroli serwisowej.
- Refaktoryzacja niezwiązana z zadaniem.

## Oczekiwany format wyniku
- zakres zmian backendowych,
- moduły i pliki,
- auth i ownership,
- testy,
- dokumentacja,
- ryzyka i nieweryfikowane obszary.

## Decyzje otwarte
- Czy dodać wariant skill dla zmian administracyjnych i audit-heavy.
