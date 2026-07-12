# Checklist Bezpieczeństwa Uploadu

## Cel dokumentu
Lista kontrolna dla zmian dotyczących uploadu i pobierania plików.

## Status dokumentu
- Status: draft
- Zakres: upload security checklist
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Checklista jest stosowana manualnie przy zmianach publicznego uploadu.

## Stan docelowy
- Każda zmiana uploadu przechodzi przez dedykowaną kontrolę bezpieczeństwa.

## Lista kontrolna
- [ ] Dozwolone typy plików są jawnie określone.
- [ ] MIME type i magic bytes są sprawdzane.
- [ ] Limity rozmiaru i liczby plików są egzekwowane.
- [ ] Upload session ma poprawny ownership i autoryzację.
- [ ] Path traversal jest zablokowane.
- [ ] SVG i pliki wykonywalne są obsłużone bezpiecznie.
- [ ] Zip bombs i inne nadużycia są uwzględnione.
- [ ] Retry i częściowe błędy nie omijają walidacji.
- [ ] Pobieranie plików nie ujawnia fizycznych ścieżek.
- [ ] Logi i monitoring uploadu są adekwatne.

## Powiązane dokumenty
- [../security/FILE_UPLOAD_SECURITY.md](../security/FILE_UPLOAD_SECURITY.md)
- [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md)

## Decyzje otwarte
- Czy w pierwszej produkcji dodać dodatkowe skanowanie antywirusowe poza podstawową walidacją typów.
