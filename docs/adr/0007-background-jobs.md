# ADR 0007: Zadania w Tle

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Opisuje kierunek implementacji background jobs.

## Stan obecny
- Mechanizm nie został jeszcze wybrany.

## Stan docelowy
- Zadania przechowywane trwale i wykonywane przez worker z tej samej codebase.

## Kontekst
System wymaga asynchronicznego przetwarzania mediów, ZIP i cleanupów bez wprowadzania mikroserwisów.

## Decyzja
Na start stosujemy trwałe joby w bazie i worker uruchamiany jako część monolitu lub osobny proces tej samej aplikacji.

## Konsekwencje
- Niski koszt operacyjny.
- Potrzeba mechanizmów blokowania, retry i monitoringu jobów.

## Alternatywy
- Zewnętrzny broker i osobny system kolejkowy: odłożone.

## Decyzje otwarte
- Czy worker będzie osobnym procesem od pierwszego środowiska produkcyjnego.
