# Testowanie Backendu

## Cel dokumentu
Opisuje zakres i standardy testów backendowych.

## Status dokumentu
- Status: draft
- Zakres: testy backendowe dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Backend nie posiada jeszcze docelowego zestawu testów.
- Obecnie istnieją testy foundation dla startu aplikacji i konfiguracji produkcyjnej.

## Stan docelowy
- Testy jednostkowe, integracyjne i kontraktowe chroniące krytyczne reguły biznesowe.

## Zakres
- Testy jednostkowe use case i polityk
- Testy repository na silniku zgodnym z aktualnym runtime backendu, docelowo uruchamiane kontenerowo
- Testy security i ownership
- Testy API
- Testy uploadu i storage
- Testy przetwarzania mediów i background jobs
- Testy migracji schematu po wprowadzeniu mechanizmu migracji do repozytorium

## Wymagania
- Każda nowa reguła ownership wymaga testu pozytywnego i negatywnego.
- Każda operacja administracyjna wymaga testu audytu.
- Błędy integracyjne powinny mieć testy retry lub degradacji.

## Powiązane dokumenty
- [TEST_STRATEGY.md](TEST_STRATEGY.md)
- [QUALITY_GATES.md](QUALITY_GATES.md)
- [../backend/BACKEND_GUIDELINES.md](../backend/BACKEND_GUIDELINES.md)

## Decyzje otwarte
- Czy w pierwszej fazie dodać testy kontraktowe OpenAPI jako osobną warstwę.

## Uwagi implementacyjne

### Spring Boot 4.1.0 – zmiany w API testowym
Spring Boot 4.1.0 usunął następujące klasy/adnotacje:
- `@AutoConfigureMockMvc` – niedostępny w `spring-boot-test-autoconfigure`
- `TestRestTemplate` – niedostępny w `spring-boot-test`

Aktualnie obsługiwane podejście do testów HTTP integracyjnych:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ExampleTest {
    @LocalServerPort
    private int port;
    
    private final RestTemplate restTemplate = new RestTemplate();
    // ...
}
```
