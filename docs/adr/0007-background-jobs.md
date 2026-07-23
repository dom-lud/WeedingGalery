# ADR 0007: Zadania w Tle

## Status dokumentu
- Status: accepted
- Ostatnia aktualizacja: 2026-07-21

## Cel dokumentu
Opisuje kierunek implementacji background jobs.

## Stan obecny
- Mechanizm nie zostal jeszcze zaimplementowany.
- Etap 5 ma juz scheduler cleanupu uploadu, ale nie ma ogolnego modelu trwalych jobow.

## Stan docelowy
- Zadania przechowywane trwale i wykonywane przez worker z tej samej codebase.

## Kontekst
System wymaga asynchronicznego przetwarzania mediow, ZIP i cleanupow bez wprowadzania mikroserwisow.

## Decyzja
Na start stosujemy trwale joby w relacyjnej bazie danych i worker uruchamiany in-process w backendzie jako scheduler z kontrolowana wspolbieznoscia.

Zasady pierwszej iteracji:
- API/use case zapisuje rekord joba w tej samej transakcji, w ktorej uznaje operacje za zakonczona biznesowo.
- Worker pobiera tylko joby `PENDING` albo `RETRY_SCHEDULED`, ktorych `scheduled_at` juz nadszedl.
- Pobranie joba musi byc atomowe i odporne na rownolegly worker; preferowany jest status `RUNNING`, licznik prob, `locked_at` i `locked_by`.
- Retry jest automatyczne tylko dla bledow przejsciowych, z limitem prob i opoznieniem backoff.
- Po przekroczeniu limitu prob job przechodzi do `MANUAL_REVIEW` albo `FAILED` zgodnie z typem bledu.
- Osobny proces workera w Compose/produkcji jest odlozony; model DB nie moze go blokowac.

## Konsekwencje
- Niski koszt operacyjny i brak zewnetrznego brokera.
- Potrzeba mechanizmow blokowania, retry, idempotencji i monitoringu jobow.
- Migracja Etapu 6 musi dodac indeksy pod pobieranie jobow po `status`, `scheduled_at` i locku.
- Testy migracji i jobow musza dzialac na MySQL lub zgodnym kontenerowym silniku, gdy zachowanie lockow jest istotne.

## Alternatywy
- Zewnetrzny broker i osobny system kolejkowy: odlozone, bo pierwszy zakres nie uzasadnia dodatkowej infrastruktury.
- Synchroniczne wykonywanie w request thread: odrzucone jako ryzykowne dla uploadu i przyszlego downloadu ZIP.
- Osobny proces worker od pierwszego wdrozenia: odlozone, ale schemat i konfiguracja maja pozostac z nim zgodne.

## Decyzje otwarte
- Konkretna strategia blokowania MySQL dla pobierania jobow (`FOR UPDATE SKIP LOCKED`, optimistic claim lub dedykowane update-claim) zostanie potwierdzona w implementacji Etapu 6 testem integracyjnym na realnym silniku.
