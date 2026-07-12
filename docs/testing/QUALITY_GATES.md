# Quality Gates

## Cel dokumentu
Opisuje warunki, które muszą być spełnione przed scaleniem zmian i przed wdrożeniem.

## Status dokumentu
- Status: draft
- Zakres: quality gates dla rozwoju i releasów
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Quality gates są zdefiniowane docelowo, ale nie wszystkie są jeszcze automatycznie egzekwowane.

## Stan docelowy
- Każdy relewantny change przechodzi przez spójny zestaw kontroli jakości.

## Gates dla kodu
- Build przechodzi
- Lint przechodzi
- Testy właściwe dla zakresu przechodzą
- Brak nowych krytycznych ostrzeżeń bezpieczeństwa
- Dokumentacja zaktualizowana

## Gates dla funkcji biznesowych
- Ownership sprawdzony
- Autoryzacja sprawdzona
- Obsługa błędów sprawdzona
- Limity i storage przeanalizowane
- Audyt dodany tam, gdzie wymagany

## Gates przed wdrożeniem
- Migracje zweryfikowane
- Backup gotowy
- Plan rollbacku gotowy
- Monitoring i alerty zaktualizowane, jeśli dotyczy

## Powiązane dokumenty
- [TEST_STRATEGY.md](TEST_STRATEGY.md)
- [../DEFINITION_OF_DONE.md](../DEFINITION_OF_DONE.md)
- [../security/SECURITY_CHECKLIST.md](../security/SECURITY_CHECKLIST.md)

## Decyzje otwarte
- Które quality gates będą blokujące w pierwszym CI, a które ostrzegawcze.
