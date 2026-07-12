# Wytyczne Backendowe

## Cel dokumentu
Definiuje zasady implementacji backendu zgodne z docelową architekturą platformy.

## Status dokumentu
- Status: draft
- Zakres: standardy backendu dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Istnieje szkic aplikacji backendowej, ale nie odpowiada jeszcze docelowemu stackowi ani podziałowi modułowemu.

## Stan docelowy
- Backend w Java 21 i Spring Boot 3, zorganizowany jako modularny monolit.

## Docelowy stack
- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL Driver
- Flyway
- Spring Boot Actuator
- Springdoc OpenAPI
- Maven
- JUnit 5, Mockito, Testcontainers
- MapStruct tylko tam, gdzie ogranicza powtarzalne mapowania bez zaciemniania logiki

## Zasady kodu
- Pakiety organizujemy według modułów domenowych, nie według warstw globalnych.
- Use case powinien być podstawową jednostką orkiestracji biznesowej.
- Kontroler nie zawiera logiki domenowej.
- Serwisy nie powinny zwracać encji JPA bezpośrednio do API.
- Walidacja wejścia odbywa się na granicy API i w krytycznych miejscach logiki biznesowej.

## Transakcje
- Granica transakcji powinna być na poziomie use case.
- Długie operacje na plikach i ZIP nie powinny blokować transakcji biznesowej dłużej niż to konieczne.

## Ownership i autoryzacja
- Każdy use case musi jawnie sprawdzać kontekst użytkownika i zasobu.
- Nie wystarcza samo `@PreAuthorize`; potrzebne są reguły serwisowe.
- Operacje administracyjne muszą iść osobnym torem i być audytowane.

## Obsługa błędów
- Wspólny format błędów dla wszystkich endpointów.
- Kody domenowe i pola walidacyjne powinny być stabilne.
- Nie ujawniamy szczegółów infrastruktury w odpowiedzi HTTP.

## Integracje
- Storage, e-mail, QR i background jobs są używane przez interfejsy.
- Integracje muszą wspierać podstawianie implementacji testowych.

## Powiązane dokumenty
- [API_CONVENTIONS.md](API_CONVENTIONS.md)
- [DATABASE_CONVENTIONS.md](DATABASE_CONVENTIONS.md)
- [AUTHENTICATION_AND_AUTHORIZATION.md](AUTHENTICATION_AND_AUTHORIZATION.md)

## Decyzje otwarte
- Czy MapStruct będzie standardem globalnym, czy narzędziem dopuszczonym selektywnie.
