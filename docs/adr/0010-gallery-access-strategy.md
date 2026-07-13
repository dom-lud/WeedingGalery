# ADR 0010: Strategia Dostępu do Galerii

## Status dokumentu
- Status: accepted
- Ostatnia aktualizacja: 2026-07-13

## Cel dokumentu
Opisuje model dostępu gościa do galerii.

## Stan obecny
- Publiczny dostęp jest wdrażany jako rozszerzenie Etapu 4 po `GALLERY-001`.

## Stan docelowy
- Dostęp oparty o slug, token, opcjonalny kod dostępu i ustawienia galerii.

## Kontekst
Gość nie ma konta, ale musi mieć prosty dostęp do właściwej galerii bez narażania prywatności innych zasobów.

## Decyzja
Galeria publiczna używa slugu wyłącznie jako czytelnego identyfikatora UI. Slug
nie jest credentialem. Dostęp zawsze wymaga aktywnego, losowego tokenu o entropii
co najmniej 256 bitów, a właściciel może dodatkowo włączyć kod dostępu na poziomie
konkretnej galerii.

Surowy token jest zwracany tylko raz podczas rotacji i jest przechowywany w bazie
wyłącznie jako SHA-256. Kod dostępu jest write-only i jest przechowywany jako hash
BCrypt. Link SPA przenosi token we fragmencie `#token=...`, aby sekret nie trafiał
do logów HTTP ani nagłówka `Referer`.

Publiczne `POST /access` wymienia token i opcjonalny kod na ograniczony grant w
sesji serwerowej. Grant jest przypisany do jednej galerii, ma TTL i przy każdym
użyciu jest ponownie sprawdzany względem aktualnego tokenu, statusu galerii,
ustawień publikacji i dat. Rotacja tokenu, wyłączenie publikacji, archiwizacja lub
usunięcie natychmiast unieważniają istniejące granty.

Mutacje ustawień i sekretów są dostępne tylko dla ownera wydarzenia. Aktywny
manager może odczytać ustawienia bez sekretów. Publiczne próby dostępu podlegają
aplikacyjnemu rate limitingowi; implementacja single-instance może używać limitera
w pamięci, ale skalowanie wymaga limitera współdzielonego albo edge.

## Konsekwencje
- Łatwiejsze udostępnianie linków i QR.
- Potrzeba ochrony przed enumeracją i nadużyciami publicznych endpointów.
- Tokenu nie da się odzyskać po rotacji; UI pokazuje go jednorazowo.
- Sesja publiczna jest capability o ograniczonym zakresie, a nie tożsamością konta.
- Błędny slug, token oraz niedostępna galeria mają nierozróżnialną odpowiedź.

## Alternatywy
- Wyłącznie niezgadnialny token bez slugu: mniej czytelne linki.
- Globalnie otwarte galerie bez dodatkowych zabezpieczeń: odrzucone.
- Jawne przechowywanie tokenu w bazie: odrzucone z powodu skutków wycieku bazy.
- Token w query string: odrzucony z powodu logów, historii i nagłówka `Referer`.

## Decyzje otwarte
- Przed skalowaniem horyzontalnym trzeba wybrać współdzielony limiter i magazyn sesji.
