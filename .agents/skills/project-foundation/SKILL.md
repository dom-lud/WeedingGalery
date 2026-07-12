# Project Foundation

## Cel dokumentu
Skill prowadzący przez Etap 0, czyli techniczne fundamenty repozytorium przed implementacją funkcji biznesowych.

## Status dokumentu
- Status: done
- Zakres: project foundation / etap 0 skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Etap 0 (FND-001) jest zamknięty: dokumentacja, workflow, checklisty, mapa dokumentacji i prompty istnieją.
- Repozytorium ma spójny, minimalny szkielet z canonical package `pl.backend.weddinggallery`.

## Stan docelowy
- Skill jest archiwizowany jako wzorzec dla kolejnych edycji fundamentów.
- Dalszy rozwój fundamentu technicznego odbywa się przez skill `stage-1-foundation`.

## Kiedy używać
- Gdy użytkownik pisze: `zacznij etap 0` lub `co zawierał etap 0`.
- Gdy potrzebujesz zrozumieć zakres lub wyniki Etapu 0.
- Gdy chcesz powtórzyć audit fundamentów po dużej zmianie stacku.
- Etap 0 jest zamknięty (FND-001 DONE). Dla Etapu 1 użyj skilla `stage-1-foundation`.

## Tryby pracy
- `audit`: sprawdź stan i przygotuj listę braków bez zmian.
- `plan`: przygotuj kolejność zmian Etapu 0 bez implementacji.
- `implement`: wykonaj minimalny bezpieczny zakres Etapu 0.
- `verify`: uruchom kontrole i podsumuj blokery.

Jeśli użytkownik nie poda trybu, przyjmij `audit + plan`, a implementuj dopiero gdy prosi o wykonanie.

## Wymagane dokumenty
- `AGENTS.md`
- `README.md`
- `docs/DOCUMENTATION_MAP.md`
- `docs/development/WORKFLOW.md`
- `docs/operations/CI_CD_STRATEGY.md`
- `docs/testing/QUALITY_GATES.md`
- `docs/backend/BACKEND_GUIDELINES.md`
- `docs/frontend/FRONTEND_ARCHITECTURE.md`
- `docs/frontend/UI_SYSTEM.md`
- `docs/architecture/REPOSITORY_STRUCTURE.md`
- odpowiednie ADR

## Zakres Etapu 0
Etap 0 może obejmować:
- spójność README, AGENTS, mapy dokumentacji i realnej struktury repo,
- spójność wersji stacku między docs i kodem,
- build backendu,
- testy backendu,
- build frontendu,
- lint frontendu,
- Docker Compose config,
- Docker build,
- `.env.example`,
- `.gitignore`,
- `.dockerignore`,
- podstawowe health/config readiness,
- przygotowanie Flyway i pustych migracji tylko wtedy, gdy nie wprowadza to jeszcze modelu domenowego,
- minimalny CI/CD plan lub workflow dopiero gdy użytkownik wyraźnie poprosi o implementację pipeline.

## Poza zakresem Etapu 0
- Implementacja auth.
- Implementacja eventów, galerii, uploadu, panelu admina lub publicznej galerii.
- Docelowy model danych poza technicznym przygotowaniem migracji.
- Pełny deployment produkcyjny.
- Mikroserwisy, Kubernetes lub zmiana stacku bez ADR.

## Wymagane kroki
1. Przeczytaj wymagane dokumenty.
2. Sprawdź aktualną strukturę repo.
3. Sprawdź zgodność stacku między docs i kodem.
4. Sprawdź backend: build, testy, konfiguracja, Dockerfile.
5. Sprawdź frontend: zależności, MUI, theme, lint, build, Dockerfile.
6. Sprawdź Docker Compose i `.env.example`.
7. Sprawdź `.gitignore` i `.dockerignore`.
8. Sprawdź, czy istnieją podstawowe quality gates.
9. Wskaż braki blokujące pierwszy vertical slice.
10. Jawnie zdecyduj, czy Etap 0 wymaga subagentów; domyślnie nie.
11. Jeśli tryb to `implement`, wykonaj minimalne poprawki.
12. Dodaj lub zaktualizuj testy tak, by broniły foundation contracts, a nie tylko aktualnego kodu.
13. Uruchom kontrole końcowe.
14. Wykonaj self-review w świeżym kontekście.
15. Jeśli wykryto problemy, wróć do implementacji i powtarzaj pętlę.
16. Podsumuj, co jest gotowe i co zostaje poza zakresem.

## Standardowe komendy weryfikacyjne
Dobierz komendy do stanu repo, ale preferuj:

```bash
cd backend && ./mvnw test
cd frontend && npm run lint
cd frontend && npm run build
docker compose config --quiet
docker compose build
```

Na Windows użyj odpowiedników, np. `.\mvnw.cmd test`.

## Checklista
- [ ] Dokumentacja wejściowa jest spójna.
- [ ] Stack w docs zgadza się z kodem.
- [ ] Backend testy przechodzą.
- [ ] Frontend lint przechodzi.
- [ ] Frontend build przechodzi.
- [ ] Docker Compose config przechodzi.
- [ ] Docker build przechodzi albo blocker jest jasno opisany.
- [ ] `.env.example` nie zawiera sekretów.
- [ ] `.gitignore` obejmuje artefakty lokalne.
- [ ] `.dockerignore` ogranicza kontekst builda.
- [ ] Testy foundation nie są napisane wyłącznie pod aktualną implementację.
- [ ] Wykonano self-review i poprawiono wykryte problemy.
- [ ] Nie zaimplementowano funkcji biznesowych poza zakresem.
- [ ] Lista następnych kroków prowadzi do pierwszego vertical slice.

## Kiedy użyć subagentów
Etap 0 zwykle robi jeden agent. Użyj `task-orchestration` tylko gdy:
- Docker/CI/security wymaga niezależnej analizy,
- stack lub ADR są sprzeczne,
- planujesz realną implementację pipeline,
- zmiany dotykają wielu obszarów naraz.

## Oczekiwany format wyniku
- status Etapu 0,
- wykonane zmiany albo lista braków,
- wyniki komend,
- blokery,
- ryzyka,
- rekomendowany następny etap,
- czego nie zweryfikowano.

## Przykłady krótkich zleceń
```text
Użyj project-foundation i zrób audit Etapu 0.
```

```text
Użyj project-foundation i zaimplementuj minimalny Etap 0.
```

```text
Użyj project-foundation, sprawdź Docker/CI readiness i nic nie zmieniaj.
```

## Powiązany skill
- Następny etap: [stage-1-foundation](./../stage-1-foundation/SKILL.md)

## Decyzje otwarte
- Czy skill project-foundation powinien być rozszerzony o kolejną rundę fundamentów po dużej zmianie stacku.
