# AGENTS

## Cel dokumentu
Krótki punkt wejścia dla agentów AI i nowych osób pracujących w repozytorium.

## Status dokumentu
- Status: draft
- Zakres: zasady pracy z repozytorium i kolejność czytania dokumentacji
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Repozytorium zawiera wstępny szkielet techniczny.
- Docelowy system opisuje dokumentacja; nie należy zakładać, że planowane funkcje są już zaimplementowane.

## Stan docelowy
- Repozytorium będzie rozwijane jako monorepo dla pełnej platformy wieloużytkownikowej do obsługi wydarzeń i galerii.

## Opis projektu
Platforma webowa do zbierania, organizowania i udostępniania zdjęć oraz filmów z wesel i innych prywatnych wydarzeń, projektowana od początku jako system dla wielu użytkowników, wielu wydarzeń i wielu galerii.

## Kolejność czytania
1. `AGENTS.md`
2. [README.md](README.md)
3. Struktura repozytorium i aktualne pliki
4. Dokumenty domenowe związane z zadaniem
5. [docs/adr/README.md](docs/adr/README.md) i odpowiednie ADR
6. Istniejący kod
7. Istniejące testy

## Mapa dokumentacji
- Produkt: [docs/product/PRODUCT_VISION.md](docs/product/PRODUCT_VISION.md)
- Role i uprawnienia: [docs/product/USER_ROLES.md](docs/product/USER_ROLES.md), [docs/product/PERMISSIONS_MATRIX.md](docs/product/PERMISSIONS_MATRIX.md)
- Architektura: [docs/architecture/SYSTEM_ARCHITECTURE.md](docs/architecture/SYSTEM_ARCHITECTURE.md)
- API: [docs/backend/API_ENDPOINTS.md](docs/backend/API_ENDPOINTS.md)
- Bezpieczeństwo: [docs/security/SECURITY_REQUIREMENTS.md](docs/security/SECURITY_REQUIREMENTS.md)
- Testy: [docs/testing/TEST_STRATEGY.md](docs/testing/TEST_STRATEGY.md)
- Operacje: [docs/operations/DEPLOYMENT.md](docs/operations/DEPLOYMENT.md)
- Zasady rozwoju: [docs/DEVELOPMENT_RULES.md](docs/DEVELOPMENT_RULES.md), [docs/DEFINITION_OF_DONE.md](docs/DEFINITION_OF_DONE.md)

## Zasady pracy
- Traktuj dokumentację jako źródło prawdy dla stanu docelowego.
- Nie zmieniaj stacku bez świadomej decyzji architektonicznej.
- Nie dodawaj mikroserwisów ani Kubernetes bez ADR i zgody.
- Nie omijaj ownership zasobów, autoryzacji ani audytu.
- Nie zapisuj sekretów w repozytorium.
- Nie traktuj planowanych funkcji jako istniejących.
- Każda istotna decyzja techniczna powinna mieć odzwierciedlenie w dokumentacji lub ADR.

## Minimalny workflow agenta
1. Przeczytaj dokumenty wejściowe.
2. Sprawdź aktualny stan kodu i testów.
3. Przygotuj krótki plan.
4. Wykonaj minimalny spójny zakres.
5. Dodaj lub zaktualizuj testy.
6. Uruchom build, lint i testy, jeśli zmieniasz kod.
7. Zaktualizuj dokumentację.
8. Podsumuj wynik i ograniczenia.

## Definition of Done
Skrócona definicja:
- funkcjonalność zgodna z zakresem,
- autoryzacja i ownership zachowane,
- testy dodane i uruchomione, jeśli dotyczy,
- dokumentacja zaktualizowana,
- brak sekretów i obejść bezpieczeństwa,
- zgodność z ADR.

Pełna definicja: [docs/DEFINITION_OF_DONE.md](docs/DEFINITION_OF_DONE.md)

## Zasady aktualizacji dokumentacji
- Każdy dokument musi rozróżniać stan obecny i docelowy.
- Linki między dokumentami muszą być względne.
- Nie kopiuj całych sekcji między plikami; linkuj do źródła.
- Zmiana architektoniczna bez aktualizacji dokumentacji jest niekompletna.

## Zasady ADR
- ADR tworzymy dla decyzji wpływających na architekturę, bezpieczeństwo, dane lub operacje.
- Statusy: `proposed`, `accepted`, `rejected`, `superseded`.
- Każdy ADR musi opisywać kontekst, decyzję, konsekwencje i alternatywy.

## Decyzje otwarte
- Strategia uwierzytelniania.
- Docelowy silnik background jobs.
- Finalny zakres pierwszego wdrożenia produkcyjnego.
