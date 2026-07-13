# Zasady Rozwoju

## Cel dokumentu
Opisuje zasady prowadzenia prac rozwojowych zgodnie z przyjeta architektura i dokumentacja.

## Status dokumentu
- Status: draft
- Zakres: reguly pracy nad projektem
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Zasady sa ustanawiane rownolegle z dokumentacja i stopniowo uszczelniane wraz z kolejnymi etapami.

## Stan docelowy
- Spojny proces zmian technicznych bez dryfu architektonicznego.

## Zasady
- Nie zmieniaj stacku bez zgody i aktualizacji ADR.
- Nie dodawaj mikroserwisow bez wyraznej decyzji architektonicznej.
- Nie dodawaj przypadkowych bibliotek bez uzasadnienia.
- Nie dodawaj drugiej pelnej biblioteki UI bez zaakceptowanego ADR; frontend stosuje Material UI jako glowny system UI.
- Nie omijaj autoryzacji, ownership i audytu.
- Nie modyfikuj zatwierdzonych migracji schematu, gdy mechanizm migracji zostanie juz wprowadzony do repozytorium.
- Nie twierdz, ze testy przeszly, jesli nie zostaly uruchomione.
- Dokumentacja musi byc aktualizowana razem ze zmiana.
- **Logika bledow i wyjatkow**: Bledy biznesowe nalezy zglaszac poprzez rzucanie wyjatkow dziedziczacych po `AppException` bezposrednio w serwisach, a mapowanie do REST API spoczywa na `GlobalExceptionHandler`.
- **Logowanie i audyt**: Wazne operacje w systemie musza byc utrwalone w tabeli audytu przez `AuditService`, natomiast przebieg procesow nalezy logowac technicznie przez `@Slf4j`.
- **Rozszerzanie audit eventow**: Kazda nowa wrazliwa akcja, zwlaszcza w auth, security i administracji, wymaga jawnej decyzji, czy trzeba dodac lub rozszerzyc `EventType`, szczegoly wpisu i testy audytu.
- **Aktualizacja dokumentacji audytu**: Jezeli zmienia sie katalog zdarzen audytowych albo miejsce wywolan `AuditService`, trzeba zaktualizowac dokumentacje auth, logowania operacyjnego, backlog lub roadmape, jesli dana zmiana wplywa na etap.
- **Ochrona danych w audycie**: Do audytu nie wolno zapisywac hasel, tokenow, danych sesyjnych, sekretow ani payloadow zawierajacych dane wrazliwe.

## Powiazane dokumenty
- [DEFINITION_OF_DONE.md](DEFINITION_OF_DONE.md)
- [CONTRIBUTING.md](CONTRIBUTING.md)
- [backend/AUTHENTICATION_AND_AUTHORIZATION.md](backend/AUTHENTICATION_AND_AUTHORIZATION.md)
- [operations/LOGGING.md](operations/LOGGING.md)
- [adr/README.md](adr/README.md)
- [frontend/UI_SYSTEM.md](frontend/UI_SYSTEM.md)

## Decyzje otwarte
- Czy kazda zmiana w obszarze security ma wymagac obowiazkowego review przez wyznaczona osobe.
