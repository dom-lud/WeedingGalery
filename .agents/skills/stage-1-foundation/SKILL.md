---
name: stage-1-foundation
description: Skill prowadzący przez Etap 1 – Fundament techniczny (FND-002). Obejmuje dokończenie nazewnictwa backendu, mechanizm migracji schematu, konfiguracje środowisk, health/observability baseline, rozszerzenie testów i CI readiness.
---

# Stage 1 Foundation

## Cel dokumentu
Skill prowadzący przez **Etap 1 – Fundament techniczny** (FND-002 i powiązane zadania operacyjne).
Etap 1 domyka techniczny szkielet repozytorium bez wchodzenia w funkcje biznesowe (auth, galerie, upload).

## Status dokumentu
- Status: active
- Zakres: stage-1-foundation skill (FND-002)
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Etap 0 (FND-001) jest zamknięty: dokumentacja i canonical package `pl.backend.weddinggallery` istnieją.
- Stary namespace `pl.backend.weedinggalery` (literówka) został usunięty.
- Backend buduje się z H2 na testach, ale brakuje: mechanizmu migracji schematu, konfiguracji środowisk (dev/test/prod), health endpointów i wyodrębnionych contract tests.
- Frontend ma szkielet Vite + MUI + TypeScript, ale wymaga przeglądu konfiguracji środowisk.

## Stan docelowy
- Backend ma spójny, działający szkielet zgodny z dokumentacją i ADR.
- Mechanizm migracji schematu jest skonfigurowany (Flyway) z baseline migration.
- Konfiguracje środowisk (dev, test, prod) są wyraźnie rozdzielone.
- Health endpoint `/actuator/health` odpowiada i jest testowany.
- Foundation contract tests chronią kluczowe invarianty repozytorium.
- Repo jest gotowe do wejścia w Etap 2 (Identity).

## Kiedy używać
- Gdy użytkownik pisze: `zacznij etap 1`, `zrób FND-002`, `przygotuj fundament techniczny`.
- Gdy chcesz sprawdzić stan gotowości repozytorium do Etapu 2 (Identity).
- Po dodaniu nowych zależności lub zmianie stack podstawy backendu.
- Nie używaj tego skilla do implementacji auth, galerii, uploadu ani panelu admina.

## Tryby pracy
- `audit`: sprawdź stan i przygotuj listę braków bez zmian.
- `plan`: przygotuj kolejność zmian bez implementacji.
- `implement`: wykonaj minimalny bezpieczny zakres Etapu 1.
- `verify`: uruchom kontrole i podsumuj blokery.

Jeśli użytkownik nie poda trybu, przyjmij `audit + plan`.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/product/BACKLOG.md` (FND-002 i powiązane zadania)
- `docs/product/FEATURE_ROADMAP.md` (Etap 1)
- `docs/architecture/REPOSITORY_STRUCTURE.md`
- `docs/architecture/SYSTEM_ARCHITECTURE.md`
- `docs/backend/BACKEND_GUIDELINES.md`
- `docs/backend/DATABASE_CONVENTIONS.md`
- `docs/operations/ENVIRONMENTS.md`
- `docs/operations/CONFIGURATION.md`
- `docs/operations/MONITORING.md`
- `docs/testing/BACKEND_TESTING.md`
- `docs/testing/QUALITY_GATES.md`
- `docs/operations/CI_CD_STRATEGY.md`
- `docs/adr/0001-modular-monolith.md`
- ADR dot. migracji schematu (gdy będzie)

## Zakres Etapu 1
Etap 1 może obejmować:
- weryfikację canonical package `pl.backend.weddinggallery`,
- wybór i konfigurację mechanizmu migracji schematu (Flyway – wymaga ADR lub jawnej decyzji),
- stworzenie baseline migration (schemat zgodny z modelem danych Etapu 1),
- konfigurację `application-dev.yml`, `application-test.yml` i `application-prod.yml`,
- dodanie Spring Boot Actuator z endpointem `/actuator/health`,
- zabezpieczenie health endpointów (brak ujawniania wrażliwych danych),
- rozszerzenie testów: foundation contract tests (nazewnictwo, konfiguracja, health),
- weryfikację konfiguracji Docker Compose pod kątem środowisk,
- weryfikację `.env.example` pod kątem nowych zmiennych środowiskowych,
- przegląd frontendu: konfiguracja środowisk w Vite (`VITE_API_URL`),
- dokumentację wymagań Etapu 1 (Definition of Done check).

## Poza zakresem Etapu 1
- Implementacja auth (JWT, sesje, logowanie) – to Etap 2.
- Model domenowy użytkowników, wydarzeń, galerii – to Etapy 3–4.
- Upload i przetwarzanie mediów – to Etapy 5–6.
- Panel admina – Etap 10.
- Mikroserwisy, Kubernetes bez ADR.
- Pełny deployment produkcyjny bez weryfikacji security baseline.

## Wymagane kroki
1. Przeczytaj wymagane dokumenty i ADR.
2. Sprawdź aktualną strukturę repo i backlog FND-002.
3. Zweryfikuj canonical package `pl.backend.weddinggallery` (brak duplikatów, brak literówek).
4. Sprawdź, czy mechanizm migracji schematu jest skonfigurowany (Flyway).
5. Sprawdź konfiguracje środowisk (dev, test, prod) – brak hardkodowanych sekretów.
6. Sprawdź, czy Actuator health jest dostępny i skonfigurowany.
7. Sprawdź testy: czy contract tests chronią kluczowe invarianty.
8. Sprawdź Docker Compose i `.env.example`.
9. Sprawdź frontend: konfiguracja `VITE_API_URL` i środowiska.
10. Wskaż braki blokujące Etap 2.
11. Jawnie zdecyduj, czy Etap 1 wymaga subagentów; domyślnie nie.
12. Jeśli tryb to `implement`, wykonaj minimalne poprawki.
13. Dodaj lub zaktualizuj testy broniące foundation contracts.
14. Uruchom kontrole końcowe.
15. Wykonaj self-review.
16. Jeśli wykryto problemy, wróć do implementacji i powtarzaj pętlę.
17. Podsumuj, co jest gotowe i co zostaje poza zakresem.

## Decyzja techniczna: migracje schematu

> **Uwaga**: przed implementacją migracji schematu wymagana jest jawna decyzja architektoniczna.
> Domyślna rekomendacja to **Flyway** (prostszy, standardowy w Spring Boot, dobrze wspierany przez `spring-boot-starter-data-jpa`).
> Jeśli projekt wymaga większej elastyczności (XML migrations, rollback), rozważ Liquibase.
> Decyzja powinna mieć ADR. Bez ADR nie implementuj migracji schematu domenowego.

## Standardowe komendy weryfikacyjne
Dobierz komendy do stanu repo, ale preferuj:

```bash
# Backend
cd backend && .\mvnw.cmd test
cd backend && .\mvnw.cmd spring-boot:run

# Frontend
cd frontend && npm run lint
cd frontend && npm run build

# Docker
docker compose config --quiet
```

Na Windows użyj `.\\mvnw.cmd` zamiast `./mvnw`.

## Checklista Etapu 1
- [ ] Canonical package `pl.backend.weddinggallery` jest jedynym namespace backendu.
- [ ] Brak starych/duplikatowych pakietów (np. `weedinggalery`).
- [ ] Mechanizm migracji schematu (Flyway) jest skonfigurowany z ADR lub jawną decyzją.
- [ ] Baseline migration istnieje i jest pusta lub zawiera tylko techniczną strukturę.
- [ ] `application-dev.yml` konfiguruje H2 lub MySQL dev bez hardkodowanych sekretów.
- [ ] `application-test.yml` konfiguruje H2 in-memory dla testów.
- [ ] `application-prod.yml` używa wyłącznie zmiennych środowiskowych.
- [ ] Spring Boot Actuator jest skonfigurowany z `/actuator/health`.
- [ ] Health endpoint nie ujawnia wrażliwych szczegółów publicznie.
- [ ] Foundation contract tests chronią: canonical package, konfigurację profili, health endpoint.
- [ ] Backend testy przechodzą (`mvnw.cmd test`).
- [ ] Frontend lint i build przechodzą.
- [ ] Docker Compose config jest spójny z nową konfiguracją środowisk.
- [ ] `.env.example` zawiera wszystkie wymagane zmienne środowiskowe Etapu 1.
- [ ] Brak sekretów w plikach konfiguracyjnych.
- [ ] Wykonano self-review.
- [ ] Nie zaimplementowano funkcji biznesowych (auth, galerie, upload).
- [ ] Lista kolejnych kroków prowadzi do Etapu 2 (Identity/AUTH-001).

## Powiązane zadania backlogowe
- FND-002 – Szkielet techniczny monorepo (główne zadanie)
- OPS-001 – Konfiguracja środowisk i operacyjna gotowość wdrożenia (fragment)
- MON-001 – Monitoring i observability (wymaganie wstępne: Actuator health)

## Kiedy użyć subagentów
Etap 1 zwykle robi jeden agent. Użyj `task-orchestration` tylko gdy:
- konfiguracja środowisk i migracje wymagają niezależnej analizy security,
- stack, ADR lub Docker konfiguracja są sprzeczne,
- zmiany dotykają jednocześnie backendu, frontendu i infrastruktury.

## Oczekiwany format wyniku
- status Etapu 1,
- wykonane zmiany lub lista braków,
- wyniki komend,
- blokery,
- ryzyka,
- rekomendowany następny etap (Etap 2 – Identity),
- czego nie zweryfikowano.

## Powiązane skille
- Poprzedni etap: [project-foundation](./../project-foundation/SKILL.md)
- Migracje: [database-migration](./../database-migration/SKILL.md)
- Bezpieczeństwo: [security-review](./../security-review/SKILL.md)
- Testy: [testing](./../testing/SKILL.md)

## Decyzje otwarte
- Flyway vs Liquibase – wymaga ADR przed implementacją migracji schematu.
- Zakres health endpointów: czy ujawniać szczegóły DB/storage w środowisku dev.
- Czy Etap 1 powinien zawierać pierwszy GitHub Actions workflow CI, czy tylko lokalną weryfikację.
