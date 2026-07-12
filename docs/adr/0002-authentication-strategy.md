# ADR 0002: Strategia Uwierzytelniania

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Porównuje JWT i sesję HTTP po stronie serwera.

## Stan obecny
- Strategia nie została jeszcze zatwierdzona.

## Stan docelowy
- Jedna spójna strategia dla panelu użytkownika i administratora oraz osobny model tokenów dla dostępu publicznego.

## Kontekst
System wymaga wygodnego unieważniania sesji, obsługi panelu administracyjnego, ochrony prywatnych danych i prostego modelu bezpieczeństwa.

## Alternatywy
### JWT
- Zalety: łatwa integracja między usługami, bezstanowość po stronie API.
- Wady: trudniejsze unieważnianie, większa złożoność rotacji i revocation.

### Sesja serwerowa
- Zalety: prostsze wylogowanie, centralna kontrola sesji, naturalna zgodność z panelem webowym.
- Wady: wymaga przechowywania sesji po stronie serwera i starannej obsługi skalowania.

## Rekomendacja
Rekomendacja robocza: sesja HTTP lub sesja tokenowa zarządzana serwerowo jako wybór początkowy.

## Konsekwencje
- Prostszy model bezpieczeństwa i administracji sesjami.
- Konieczność zabezpieczenia CSRF i bezpiecznych cookies.

## Decyzje otwarte
- Ostateczne zatwierdzenie po przeglądzie wymagań frontendu i operacji.
