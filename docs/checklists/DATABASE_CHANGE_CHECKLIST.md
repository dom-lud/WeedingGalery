# Checklist Zmiany Bazy Danych

## Cel dokumentu
Lista kontrolna dla zmian schematu, encji i migracji.

## Status dokumentu
- Status: draft
- Zakres: database change checklist
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Checklista wspiera manualną kontrolę jakości zmian bazy.

## Stan docelowy
- Każda zmiana bazy danych jest analizowana pod kątem bezpieczeństwa, kompatybilności i operacji.

## Lista kontrolna
- [ ] Dodano nową migrację schematu, jeśli zmiana tego wymaga i mechanizm migracji jest już częścią repozytorium.
- [ ] Nie zmodyfikowano zatwierdzonych migracji.
- [ ] Constraints zostały przeanalizowane.
- [ ] Indeksy zostały przeanalizowane.
- [ ] Nullability jest uzasadniona.
- [ ] Wpływ na istniejące dane został przeanalizowany.
- [ ] Zachowano kompatybilność lub opisano plan migracji.
- [ ] Soft delete i retencja zostały uwzględnione, jeśli dotyczy.
- [ ] Ownership i zakres dostępu do nowych danych są jasne.
- [ ] Przygotowano test migracji.
- [ ] Uwzględniono backup przed wdrożeniem.

## Powiązane dokumenty
- [../backend/DATABASE_CONVENTIONS.md](../backend/DATABASE_CONVENTIONS.md)
- [../prompts/DATABASE_CHANGE.md](../prompts/DATABASE_CHANGE.md)

## Decyzje otwarte
- Czy zmiany indeksów wymagają osobnej ścieżki review wydajnościowego.
