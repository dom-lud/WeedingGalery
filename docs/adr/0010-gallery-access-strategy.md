# ADR 0010: Strategia Dostępu do Galerii

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Opisuje model dostępu gościa do galerii.

## Stan obecny
- Model dostępu nie został jeszcze zatwierdzony.

## Stan docelowy
- Dostęp oparty o slug, token, opcjonalny kod dostępu i ustawienia galerii.

## Kontekst
Gość nie ma konta, ale musi mieć prosty dostęp do właściwej galerii bez narażania prywatności innych zasobów.

## Decyzja
Galeria publiczna używa publicznego slugu jako identyfikatora UI, a realny dostęp może wymagać dodatkowego tokenu i kodu dostępu zależnie od ustawień.

## Konsekwencje
- Łatwiejsze udostępnianie linków i QR.
- Potrzeba ochrony przed enumeracją i nadużyciami publicznych endpointów.

## Alternatywy
- Wyłącznie niezgadnialny token bez slugu: mniej czytelne linki.
- Globalnie otwarte galerie bez dodatkowych zabezpieczeń: odrzucone.

## Decyzje otwarte
- Czy kod dostępu ma być wspierany na poziomie wydarzenia, galerii czy obu tych poziomów.
