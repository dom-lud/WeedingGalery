# ADR 0012: Flyway jako mechanizm migracji schematu

## Status dokumentu
- Status: accepted
- Ostatnia aktualizacja: 2026-07-13

## Cel dokumentu
Opisuje decyzję o wyborze Flyway jako narzędzia migracji schematu relacyjnej bazy danych.

## Stan obecny
- Flyway jest aktywny w profilach dev i prod.
- Projekt nie byl wdrozony produkcyjnie, dlatego przed Etapem 4 historia lokalnych migracji zostala swiadomie skonsolidowana.
- Pojedyncza migracja `V1__baseline.sql` tworzy kompletny, faktycznie zaimplementowany schemat Etapow 1-3 oraz lokalne konto startowe.
- `baseline-on-migrate` jest wylaczone; nowa baza zawsze wykonuje V1 od zera.

## Stan docelowy
- Flyway zarządza schematem bazy przez wersjonowane migracje SQL.
- Kolejne wersje rozwijaja schemat wyłącznie przez migracje do przodu; zatwierdzonych migracji nie modyfikujemy.

## Kontekst
Projekt potrzebuje audytowalnego, powtarzalnego mechanizmu migracji schematu:
- Wymagania z `DATABASE_CONVENTIONS.md`: tylko migracje do przodu, brak modyfikacji zatwierdzonych migracji.
- Stack: Spring Boot 4 + JPA + MySQL.
- Środowiska: dev (H2), prod (MySQL 8).
- Flyway jest wbudowany w Spring Boot auto-configuration (`spring.flyway.*`).

## Decyzja
Używamy **Flyway** jako mechanizmu migracji schematu.

Zależności dodane do `pom.xml`:
- `org.flywaydb:flyway-core` – core engine migracji
- `org.flywaydb:flyway-mysql` – wsparcie dla MySQL 8+ (np. `SKIP_LOCKED`, `CREATE TABLE ... IF NOT EXISTS`)

Konfiguracja:
- **dev** (H2): Flyway enabled, `spring.jpa.hibernate.ddl-auto=none`
- **test** (JUnit): Flyway disabled, `spring.jpa.hibernate.ddl-auto=create-drop`
- **prod** (MySQL): Flyway enabled, `spring.jpa.hibernate.ddl-auto=validate`
- **prod** (MySQL): `baseline-on-migrate=false`; wymagany jest pusty schemat albo historia zgodna z repozytorium.

Jedyna lokalizacja to `db/migration`. Baseline V1 jest zgodny z MySQL 8.4 oraz H2 uruchamianym w trybie MySQL. Od tego punktu kolejne zmiany powstaja jako V2, V3 itd.; V1 nie wolno juz modyfikowac bez kolejnej jawnej decyzji o resecie przed pierwszym wdrozeniem produkcyjnym.

## Konsekwencje

### Pozytywne
- Audytowalny, deterministyczny schemat bazy w prod.
- Automatyczne migracje przy starcie aplikacji.
- Narzędzie rozumiane przez Spring Boot – zero dodatkowej konfiguracji bootstrapu.
- Historia zmian w postaci plików SQL w repozytorium.

### Do rozważenia
- Migracje pisane w SQL muszą być kompatybilne z MySQL w produkcji.
- Testy JUnit używają H2 + Hibernate DDL (bez Flyway) – ryzyko rozbieżności schematu; adresować przez Testcontainers w przyszłości (patrz `BACKEND_TESTING.md`).
- Migracje zatwierdzone nie mogą być modyfikowane (zasada z `DATABASE_CONVENTIONS.md`).

## Alternatywy odrzucone

### Liquibase
- Odrzucony: większa złożoność (XML/YAML changeset format), więcej konfiguracji.
- Zalety (XML changesets, rollback support) nie są potrzebne na obecnym etapie.
- Można rozważyć ponownie przy dużych destrukcyjnych migracjach wymagających rollbacku.

### Hibernate `ddl-auto: update` lub `create-drop`
- Odrzucony dla produkcji: niedeterministyczny, ryzyko utraty danych, brak audytu.
- Pozostawiony wyłącznie w testach JUnit dla uproszczenia.

## Powiązane dokumenty
- [../backend/DATABASE_CONVENTIONS.md](../backend/DATABASE_CONVENTIONS.md)
- [../testing/BACKEND_TESTING.md](../testing/BACKEND_TESTING.md)
- [../operations/ENVIRONMENTS.md](../operations/ENVIRONMENTS.md)
