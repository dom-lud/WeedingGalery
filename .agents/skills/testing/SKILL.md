# Testing

## Cel dokumentu
Skill wspierający dobór i wykonanie właściwych testów dla zmiany.

## Status dokumentu
- Status: draft
- Zakres: testing skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Skill pomaga dobrać poziomy testów zgodnie ze strategią projektu.

## Stan docelowy
- Każda zmiana ma adekwatny zestaw testów jednostkowych, integracyjnych, frontendowych lub E2E.

## Kiedy używać
- Przy każdej zmianie, która wpływa na logikę biznesową, API, UI, bezpieczeństwo lub operacje.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/testing/TEST_STRATEGY.md`
- `docs/testing/BACKEND_TESTING.md`
- `docs/testing/FRONTEND_TESTING.md`
- `docs/testing/E2E_SCENARIOS.md`

## Wymagane kroki
1. Określ poziomy testów potrzebne dla zmiany.
2. Dodaj scenariusze pozytywne, negatywne i regresyjne.
3. Uwzględnij Testcontainers dla zależności backendowych, jeśli dotyczy.
4. Uwzględnij testy auth, ownership i błędów.
5. Opisz czego nie udało się zweryfikować.

## Checklista
- [ ] Są testy jednostkowe, jeśli logika tego wymaga.
- [ ] Są testy integracyjne, jeśli zmiana dotyka API, bazy lub security.
- [ ] Są testy frontendowe, jeśli zmiana dotyka UI.
- [ ] Scenariusze błędów zostały pokryte.
- [ ] Ryzyko regresji zostało ocenione.
- [ ] Wskazano nieweryfikowane obszary.

## Zakazane działania
- Twierdzenie, że testy przeszły bez ich uruchomienia.
- Ograniczanie testów wyłącznie do ścieżki pozytywnej.
- Pomijanie auth i ownership w testach zmian dostępowych.

## Oczekiwany format wyniku
- jakie testy dodano lub uruchomiono,
- co zostało zweryfikowane,
- co nie zostało zweryfikowane,
- ryzyko regresji.

## Decyzje otwarte
- Czy stworzyć osobny skill dla testów wydajnościowych i smoke testów.
