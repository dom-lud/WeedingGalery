# Quality Gates

## Cel dokumentu
Opisuje warunki, ktore musza byc spelnione przed scaleniem zmian i przed wdrozeniem.

## Status dokumentu
- Status: draft
- Zakres: quality gates dla rozwoju i releasow
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Quality gates sa zdefiniowane docelowo, a aktualne workflowy GitHub Actions egzekwuja backend testy, frontend lint, frontend testy, frontend build, `docker compose config` oraz budowe obrazow backendu i frontendu.
- Nie wszystkie docelowe quality gates sa jeszcze automatycznie egzekwowane.

## Stan docelowy
- Kazdy relewantny change przechodzi przez spojny zestaw kontroli jakosci.
- Quality gates sa zintegrowane z docelowym GitHub Actions flow opisanym w [../operations/CI_CD_STRATEGY.md](../operations/CI_CD_STRATEGY.md).

## Gates dla kodu
- Build przechodzi
- Lint przechodzi
- Testy wlasciwe dla zakresu przechodza
- Brak nowych krytycznych ostrzezen bezpieczenstwa
- Dokumentacja zaktualizowana

## Gates CI dla pull requestow
- Zmiany sa automatycznie formatowane i walidowane przez formatery (Spotless dla backendu, Prettier dla frontendu).
- Backend Maven build przechodzi.
- Frontend TypeScript build przechodzi.
- Frontend lint przechodzi.
- Testy jednostkowe i integracyjne wymagane zakresem zmiany przechodza.
- `docker compose config` przechodzi.
- Obrazy Docker buduja sie dla zmian infrastrukturalnych lub release.
- Brak sekretow w repozytorium.
- Dokumentacja i ADR sa zaktualizowane, jesli zmiana wplywa na decyzje, API, dane, security, UX lub operacje.

## Gates dla funkcji biznesowych
- Ownership sprawdzony
- Autoryzacja sprawdzona
- Obsluga bledow sprawdzona
- Limity i storage przeanalizowane
- Audyt dodany tam, gdzie wymagany
- Testy sa oparte na wymaganiach biznesowych, kontrakcie i ryzykach, a nie na dopasowaniu do aktualnej implementacji
- Dla krytycznych zmian istnieje przynajmniej jeden test, ktory odtwarza realistyczny wariant awarii, szybkiej interakcji albo scenariusz brzegowy

## Gates przed wdrozeniem
- Migracje zweryfikowane
- Backup gotowy
- Plan rollbacku gotowy
- Monitoring i alerty zaktualizowane, jesli dotyczy

## Powiazane dokumenty
- [TEST_STRATEGY.md](TEST_STRATEGY.md)
- [../DEFINITION_OF_DONE.md](../DEFINITION_OF_DONE.md)
- [../security/SECURITY_CHECKLIST.md](../security/SECURITY_CHECKLIST.md)
- [../operations/CI_CD_STRATEGY.md](../operations/CI_CD_STRATEGY.md)

## Decyzje otwarte
- Ktore quality gates beda blokujace w pierwszym CI, a ktore ostrzegawcze.
