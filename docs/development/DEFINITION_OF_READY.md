# Definition of Ready

## Cel dokumentu
Definiuje warunki, które zadanie musi spełnić przed rozpoczęciem realizacji.

## Status dokumentu
- Status: draft
- Zakres: DoR dla zadań projektowych i implementacyjnych
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Kryteria gotowości nie są jeszcze egzekwowane procesowo, ale powinny być stosowane ręcznie.

## Stan docelowy
- Zadanie trafia do realizacji dopiero po zamknięciu krytycznych niejasności.

## Kryteria gotowości
- [ ] Cel zadania jest jasno określony.
- [ ] Zakres i poza zakresem są opisane.
- [ ] Kryteria akceptacji są zdefiniowane.
- [ ] Zależności są znane.
- [ ] Wpływ na API został ustalony albo jawnie oznaczony jako brak.
- [ ] Wpływ na bazę danych został ustalony albo jawnie oznaczony jako brak.
- [ ] Wymagania bezpieczeństwa są określone.
- [ ] Autoryzacja i ownership są opisane.
- [ ] Wymagane testy są wskazane.
- [ ] Potrzebne ADR-y zostały zidentyfikowane.
- [ ] Nie ma nierozwiązanych krytycznych decyzji blokujących implementację.

## Czego nie uznajemy za READY
- Opisu typu „zrób backend i frontend”.
- Zakresu bez kryteriów akceptacji.
- Zadania wymagającego zmian w danych bez analizy migracji.
- Zadania naruszającego istniejące ADR lub wymagającego nowej decyzji bez przygotowanego ADR.

## Powiązane dokumenty
- [WORKFLOW.md](WORKFLOW.md)
- [AI_TASK_PLANNING.md](AI_TASK_PLANNING.md)
- [../templates/TASK_TEMPLATE.md](../templates/TASK_TEMPLATE.md)

## Decyzje otwarte
- Czy DoR będzie obowiązkowo odhaczany w każdym zadaniu backlogu czy tylko dla zadań realizacyjnych.
