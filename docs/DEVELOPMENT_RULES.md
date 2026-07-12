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
- Nie dodawaj drugiej pełnej biblioteki UI bez zaakceptowanego ADR; frontend stosuje Material UI jako główny system UI.
- Nie omijaj autoryzacji, ownership i audytu.
- Nie modyfikuj zatwierdzonych migracji schematu, gdy mechanizm migracji zostanie już wprowadzony do repozytorium.
- Nie twierdź, że testy przeszły, jeśli nie zostały uruchomione.
- Dokumentacja musi być aktualizowana razem ze zmianą.
- **Logika błędów i wyjątków**: Błędy biznesowe należy zgłaszać poprzez rzucanie wyjątków dziedziczących po `AppException` bezpośrednio w serwisach (nigdy przez zwracanie flag boolean i if-ologię w kontrolerach). Ostateczne mapowanie do REST API spoczywa na `GlobalExceptionHandler`.
- **Logowanie i Audyt**: Ważne operacje w systemie (logowania, rejestracje, istotne zmiany) muszą być utrwalone w tabeli audytu (bez danych wrażliwych) używając `AuditService`, natomiast przebieg procesów należy logować technicznie używając `@Slf4j`.

## Powiązane dokumenty
- [DEFINITION_OF_DONE.md](DEFINITION_OF_DONE.md)
- [CONTRIBUTING.md](CONTRIBUTING.md)
- [adr/README.md](adr/README.md)
- [frontend/UI_SYSTEM.md](frontend/UI_SYSTEM.md)

## Decyzje otwarte
- Czy każda zmiana w obszarze security ma wymagać obowiązkowego review przez wyznaczoną osobę.
