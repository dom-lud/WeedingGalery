# Zasady Rozwoju

## Cel dokumentu
Opisuje zasady prowadzenia prac rozwojowych zgodnie z przyjętą architekturą i dokumentacją.

## Status dokumentu
- Status: draft
- Zakres: reguły pracy nad projektem
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Zasady są ustanawiane równolegle z dokumentacją.

## Stan docelowy
- Spójny proces zmian technicznych bez dryfu architektonicznego.

## Zasady
- Nie zmieniaj stacku bez zgody i aktualizacji ADR.
- Nie dodawaj mikroserwisów bez wyraźnej decyzji architektonicznej.
- Nie dodawaj przypadkowych bibliotek bez uzasadnienia.
- Nie omijaj autoryzacji, ownership i audytu.
- Nie modyfikuj zatwierdzonych migracji Flyway.
- Nie twierdź, że testy przeszły, jeśli nie zostały uruchomione.
- Dokumentacja musi być aktualizowana razem ze zmianą.

## Powiązane dokumenty
- [DEFINITION_OF_DONE.md](DEFINITION_OF_DONE.md)
- [CONTRIBUTING.md](CONTRIBUTING.md)
- [adr/README.md](adr/README.md)

## Decyzje otwarte
- Czy każda zmiana w obszarze security ma wymagać obowiązkowego review przez wyznaczoną osobę.
