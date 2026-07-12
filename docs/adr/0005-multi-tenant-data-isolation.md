# ADR 0005: Izolacja Danych Wieloużytkownikowych

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Opisuje sposób logicznej separacji danych wielu użytkowników w jednej aplikacji i bazie.

## Stan obecny
- Model nie został jeszcze wdrożony.

## Stan docelowy
- Logic isolation oparta o ownership, `event_id` i membership.

## Kontekst
System obsługuje wiele niezależnych kont i wydarzeń. Prywatność danych jest kluczowa.

## Decyzja
Stosujemy pojedynczą bazę z logiczną separacją danych oraz obowiązkową kontrolą ownership na poziomie use case.

## Konsekwencje
- Prostsze utrzymanie niż osobne schematy lub bazy.
- Wysoki nacisk na testy autoryzacji i filtrowania.

## Alternatywy
- Oddzielne schematy per tenant: odłożone.
- Oddzielne bazy per tenant: odrzucone na ten etap.

## Decyzje otwarte
- Czy część danych analitycznych będzie utrzymywana w agregatach per tenant.
