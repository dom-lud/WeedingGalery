---
name: testing
description: Projektowanie, implementacja i audyt testów oraz coverage w WeedingGallery. Użyj przy każdej zmianie logiki, API, UI, security, danych, storage lub progów jakości.
---

# Testing

## Obowiązkowy standard coverage

1. Przed napisaniem testów utwórz Test Design Brief: wymaganie, ryzyko, błędna implementacja wykrywana przez test, scenariusze pozytywne, negatywne i graniczne oraz najniższa wiarygodna warstwa testu.
2. Uruchom bazowy raport i uporządkuj luki per plik/klasa oraz niepokryta gałąź. Najpierw wybieraj krytyczną lub zmienioną logikę, nie najłatwiejsze linie.
3. Dodaj testy zachowania z istotnymi asercjami, a potem uruchom testy celowane, pełne zestawy oraz E2E albo kompletny test integracyjny krytycznego flow.
4. Oceń razem macierz ryzyk, wynik globalny, wynik zmienionych plików/klas i niepokryte gałęzie.
5. Podnoś progi z bezpiecznym marginesem i synchronizuj konfigurację, CI oraz dokumentację.
6. Wykonaj self-review jako reviewer i powtarzaj testy oraz review do skutku.

Aktualne blokujące bramki globalne wynoszą `90%` dla każdej raportowanej metryki: backend JaCoCo instructions i branches oraz frontend Vitest statements, branches, functions i lines. Nie traktuj 90% jako celu testu: macierz ryzyk i istotne asercje są wymagane niezależnie od wyniku.

Obowiązkowe klasy ryzyka, jeśli dotyczą zmiany: poprawne/brakujące/puste/zły format, limit `N-1/N/N+1`, ownership dozwolony/zabroniony, idempotency pierwszy zapis/replay/konflikt, auth i CSRF, expiry/timeout, anulowanie i powtórzenie akcji, storage sukces/awaria/kompensacja/brak sierot oraz UI loading/success/empty/error/retry/forbidden/offline.

Nie wolno obniżać progów, sztucznie wyłączać plików, dopisywać pustych asercji, mockować testowanego zachowania ani uznawać wysokiego coverage za dowód kompletności scenariuszy.

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
