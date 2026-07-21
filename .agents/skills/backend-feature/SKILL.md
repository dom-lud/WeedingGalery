---
name: backend-feature
description: Implementacja i audyt funkcji backendowych WeedingGallery w modularnym monolicie, w tym API, use case, walidacja, auth, ownership, transakcje oraz testy ryzyk i coverage.
---

# Backend Feature

## Obowiązkowy standard testów i coverage

- Przed testami zapisz Test Design Brief: wymagania, ryzyka, błędne implementacje, scenariusze pozytywne, negatywne i graniczne oraz najniższa wiarygodna warstwa.
- Jeśli dotyczą, sprawdź `N-1/N/N+1`, ownership dozwolony/zabroniony, idempotency pierwszy zapis/replay/konflikt i storage sukces/awaria/kompensacja/brak sierot.
- Dodaj testy jednostkowe oraz kompletny test integracyjny lub E2E krytycznego flow. Zachowania zależne od bazy testuj na rzeczywistym wspieranym silniku, gdy H2 nie jest równoważne.
- Przejrzyj JaCoCo globalnie, per zmieniona klasa oraz po niepokrytych gałęziach.
- Utrzymaj blokujące bramki `90%` instructions i `90%` branches. Pokryj granice `N-1/N/N+1`, null/puste/zły format, replay/konflikt, expiry, ownership dozwolony i zabroniony, błędy zależności oraz kompensacje storage; sam wynik coverage nie zamyka zadania.
- Nie obniżaj progów, nie wyłączaj sztucznie klas, nie wyłączaj security i nie dopisuj testów bez istotnych asercji.

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
2. Sprawdź granice modułów i zgodność z ADR.
3. Sprawdź SOLID bez tworzenia pustych warstw i zbędnych abstrakcji.
4. Sprawdź DTO, walidację i format błędów.
5. Określ auth, ownership i granice transakcji.
6. Oceń logowanie, correlation ID i brak danych wrażliwych w logach.
7. Oceń wydajność zapytań, paginację, fetchowanie i ryzyko N+1.
8. Dodaj minimalny zakres zmian.
9. Dodaj testy jednostkowe i integracyjne.
10. Zaktualizuj dokumentację API i domenową, jeśli dotyczy.

## Checklista
- [ ] Architektura domenowa została zachowana.
- [ ] SOLID został sprawdzony pragmatycznie, bez overengineeringu.
- [ ] Granice modułów zostały zachowane.
- [ ] DTO są jawne i nie ujawniają encji.
- [ ] Walidacja wejścia została dodana.
- [ ] Walidacja biznesowa jest w warstwie aplikacyjnej lub domenowej.
- [ ] Centralna obsługa błędów została zachowana.
- [ ] Ownership i autoryzacja zostały sprawdzone.
- [ ] Transakcje są poprawnie wyznaczone.
- [ ] Logowanie nie ujawnia danych wrażliwych i wspiera correlation ID.
- [ ] Wydajność zapytań, paginacja i ryzyko N+1 zostały sprawdzone.
- [ ] Istnieją testy jednostkowe i integracyjne.
- [ ] Dokumentacja API została zaktualizowana, jeśli zmienił się kontrakt.
- [ ] Dokumentacja domenowa lub ADR zostały zaktualizowane, jeśli zmieniła się decyzja lub zakres.

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
