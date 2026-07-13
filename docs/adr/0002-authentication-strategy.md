# ADR 0002: Strategia Uwierzytelniania

## Status dokumentu
- Status: accepted
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Porównuje JWT i sesję HTTP po stronie serwera.

## Stan docelowy
- Jedna spójna strategia dla panelu użytkownika i administratora (Sesja Serwerowa) oraz osobny model tokenów dla dostępu publicznego do galerii (w przyszłości).

## Kontekst
System wymaga wygodnego unieważniania sesji, obsługi panelu administracyjnego, ochrony prywatnych danych i prostego modelu bezpieczeństwa. SPA i backend hostowane w tej samej domenie upraszczają zabezpieczenia HTTPOnly cookies.

## Alternatywy
### JWT (Odrzucone dla SPA auth)
- Zalety: łatwa integracja między usługami, bezstanowość po stronie API.
- Wady: trudniejsze unieważnianie, większa złożoność rotacji i revocation, konieczność bezpiecznego przechowywania tokenów po stronie SPA.

### Sesja serwerowa (Zaakceptowane)
- Zalety: prostsze wylogowanie, centralna kontrola sesji, naturalna zgodność z panelem webowym, ochrona przed XSS przez HTTPOnly cookie.
- Wady: wymaga przechowywania sesji po stronie serwera, konieczność ochrony przed CSRF.

## Rekomendacja
Sesja HTTP wbudowana w Spring Security zarządzana serwerowo, połączona z CSRF Token Repository obsługującym aplikację SPA (wystawienie ciastka XSRF-TOKEN).

## Konsekwencje
- Prostszy model bezpieczeństwa i administracji sesjami.
- Ochrona przed CSRF jest obowiązkowa dla wszystkich mutujących żądań API.
- Brak JWT dla głównego logowania użytkowników do zarządzania (chroni przed problemem odświeżania i wycieków XSS).
