# Database Migration

## Cel dokumentu
Skill wspierający bezpieczne zmiany schematu danych i migracji Flyway.

## Status dokumentu
- Status: draft
- Zakres: database migration skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Skill wspiera projektowanie i review zmian bazodanowych.

## Stan docelowy
- Agent używa go przy każdej zmianie modelu danych i migracji.

## Kiedy używać
- Przy nowych tabelach, kolumnach, indeksach, constraintach i zmianach retencji.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/architecture/DATA_MODEL.md`
- `docs/backend/DATABASE_CONVENTIONS.md`
- `docs/checklists/DATABASE_CHANGE_CHECKLIST.md`
- odpowiednie ADR

## Wymagane kroki
1. Zidentyfikuj wpływ na model danych.
2. Dodaj nową migrację Flyway.
3. Przeanalizuj indeksy, constraints i kompatybilność danych.
4. Sprawdź soft delete, ownership i retencję.
5. Dodaj test migracji i testy integracyjne.

## Checklista
- [ ] Powstała nowa migracja.
- [ ] Nie zmieniono zatwierdzonych migracji.
- [ ] Indeksy są uzasadnione.
- [ ] Constraints są uzasadnione.
- [ ] Dane istniejące zostały przeanalizowane.
- [ ] Testy migracji istnieją.
- [ ] Backup przed wdrożeniem został uwzględniony.

## Zakazane działania
- Edycja zatwierdzonych migracji.
- Zmiana schematu bez analizy danych istniejących.
- Pominięcie ownership i retencji w nowych tabelach.

## Oczekiwany format wyniku
- zmiany schematu,
- wpływ na dane,
- testy,
- ryzyka wdrożeniowe,
- dokumentacja.

## Decyzje otwarte
- Czy część zmian wydajnościowych będzie przechodziła przez osobny proces review.
