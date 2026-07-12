# Wytyczne Backendowe

## Cel dokumentu
Definiuje szczegółowe zasady implementacji backendu zgodne z docelową architekturą platformy.

## Status dokumentu
- Status: draft
- Zakres: standardy backendu, warstwy, SOLID, bezpieczeństwo, dane i testy
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Istnieje szkic aplikacji backendowej, ale nie odpowiada jeszcze docelowemu podziałowi modułowemu.
- Część dokumentacji historycznie wskazywała Java 21 i Spring Boot 3; aktualny standard implementacyjny dla tego repozytorium to Java 25 i Spring Boot 4.

## Stan docelowy
- Backend w Java 25 i Spring Boot 4, zorganizowany jako modularny monolit.
- Pakiety i use case są organizowane według domen biznesowych, nie według globalnych warstw technicznych.
- Aktualny kod backendu korzysta z MySQL; ewentualna zmiana silnika bazy wymaga osobnej decyzji oraz migracji implementacji i konfiguracji.

## Docelowy stack
- Java 25
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- MySQL Driver
- Narzędzie migracji schematu: do ustalenia; po wdrożeniu staje się obowiązkowym elementem procesu zmian danych
- Spring Boot Actuator
- Springdoc OpenAPI, jeśli będzie zgodny z docelową wersją Spring Boot
- Maven
- JUnit 5, Mockito, Testcontainers
- MapStruct tylko tam, gdzie ogranicza powtarzalne mapowania bez zaciemniania logiki

## Ogólne zasady
- Modularny monolit jest domyślnym modelem architektonicznym.
- Organizuj kod według domen biznesowych.
- Czytelność jest ważniejsza niż sprytne rozwiązania.
- Preferuj małe, spójne zmiany.
- Unikaj overengineeringu.
- Nie dodawaj abstrakcji bez realnej potrzeby.
- Stosuj composition over inheritance.
- Stosuj dependency inversion na granicach infrastruktury.
- Wstrzykuj zależności jawnie przez konstruktor.
- Field injection jest zakazany.
- Nie twórz pustych warstw tylko po to, żeby wyglądały architektonicznie.
- Nie dodawaj mikroserwisów bez ADR i zgody.

## SOLID w praktyce
SOLID ma pomagać w utrzymaniu kodu, a nie prowadzić do interfejsu dla każdej klasy, pustych warstw lub nadmiernego rozdrobnienia.

### Single Responsibility Principle
- Klasa powinna mieć jeden główny powód do zmiany.
- Kontroler nie zawiera logiki biznesowej.
- Repozytorium nie odpowiada za autoryzację.
- Serwis nie powinien jednocześnie obsługiwać storage, e-maili i mapowania DTO.
- Use case powinien być czytelną jednostką orkiestracji jednej operacji biznesowej.

### Open/Closed Principle
- Rozszerzaj implementacje storage przez interfejs, jeśli istnieją realne warianty implementacyjne.
- Unikaj dużych bloków `if` zależnych od dostawcy infrastruktury.
- Nie twórz abstrakcji tylko z powodu hipotetycznej przyszłości.
- Preferuj prostą implementację, dopóki nie pojawia się realny drugi wariant.

### Liskov Substitution Principle
- Implementacje interfejsów muszą zachowywać kontrakt.
- Wyjątki i wartości zwracane muszą być spójne między implementacjami.
- Implementacja testowa nie może maskować zachowania, które produkcyjna implementacja traktuje jako błąd.

### Interface Segregation Principle
- Projektuj małe interfejsy związane z konkretną odpowiedzialnością.
- Unikaj ogromnych interfejsów typu `ApplicationService`.
- Port infrastrukturalny powinien opisywać realną potrzebę domeny lub use case, nie pełne API biblioteki.

### Dependency Inversion Principle
- Domena nie powinna zależeć bezpośrednio od lokalnego filesystemu.
- Storage, e-mail, czas i generowanie tokenów powinny być dostępne przez odpowiednie porty tam, gdzie jest to uzasadnione.
- Nie odwracaj zależności mechanicznie dla każdej klasy; stosuj porty na granicach infrastruktury i integracji.

## Warstwy i odpowiedzialności

### Controller
Odpowiada za:
- transport HTTP,
- przyjęcie requestu,
- podstawową walidację wejścia przez Bean Validation,
- mapowanie request/response,
- dobór statusów HTTP,
- przekazanie principal/context do use case.

Nie powinien zawierać:
- logiki biznesowej,
- decyzji ownership,
- bezpośrednich zapytań do repozytoriów,
- logiki storage,
- ręcznego formatowania błędów poza wspólnym mechanizmem.

### Application / Service / Use Case
Odpowiada za:
- przypadki użycia,
- transakcje,
- orkiestrację domeny,
- autoryzację biznesową,
- ownership,
- limity,
- wywołanie portów infrastrukturalnych,
- publikowanie zadań asynchronicznych, jeśli dotyczy.

Granica transakcji powinna zwykle znajdować się na poziomie use case. Długie operacje na plikach, ZIP lub zewnętrznych integracjach nie powinny blokować transakcji dłużej niż to konieczne.

### Domain
Odpowiada za:
- reguły biznesowe,
- encje,
- value objects,
- polityki,
- statusy i przejścia stanu,
- invariants.

Domena nie powinna znać HTTP, JPA repositories, systemu plików ani frameworkowych DTO.

### Repository
Odpowiada za:
- dostęp do danych,
- zapytania zawężone przez ownership lub kontekst domenowy,
- wydajne pobieranie danych,
- jawne fetchowanie relacji, gdy jest potrzebne.

Repozytorium nie powinno decydować, czy użytkownik ma prawo wykonać operację. Może jednak udostępniać metody, które technicznie wymuszają zawężenie po `ownerId`, `eventId` lub `galleryId`.

### Infrastructure
Odpowiada za:
- storage,
- e-mail,
- system plików,
- zewnętrzne integracje,
- generowanie QR,
- worker jobs,
- adaptery techniczne.

Integracje muszą mapować błędy zewnętrzne na przewidywalne błędy aplikacyjne i nie mogą logować sekretów.

## DTO i API
- DTO są obowiązkowe na granicy API.
- Nie zwracaj encji JPA z kontrolerów.
- Nie przyjmuj encji JPA jako request body.
- Używaj rekordów Java dla niemutowalnych requestów i response'ów, jeśli są odpowiednie.
- DTO powinny być stabilne względem kontraktu API, a nie względem wewnętrznego modelu encji.
- Mapowanie DTO może być ręczne albo przez MapStruct, jeśli ogranicza powtarzalny kod bez utraty czytelności.

## Walidacja
- Bean Validation stosuj na request DTO.
- Walidację biznesową umieszczaj w warstwie aplikacyjnej lub domenowej.
- Komunikaty walidacyjne powinny mapować się na spójny format błędów.
- Nie polegaj wyłącznie na walidacji frontendu.

## Obsługa błędów
- Stosuj centralną obsługę wyjątków.
- Format błędów musi być zgodny z [ERROR_HANDLING.md](ERROR_HANDLING.md).
- Błędy domenowe powinny mieć stabilne kody.
- Nie ujawniaj stack trace, ścieżek systemowych ani szczegółów infrastruktury w odpowiedzi HTTP.
- Nie używaj pustych `catch`.
- Nie używaj ogólnego `catch (Exception)` bez uzasadnienia i mapowania.
- Nie ukrywaj błędów przez zwracanie pustych wyników, jeśli operacja faktycznie się nie powiodła.

## Dane i wydajność
- Unikaj N+1.
- Jawnie fetchuj dane wymagane przez use case.
- Listy muszą mieć paginację albo świadome ograniczenie rozmiaru.
- Rozważ optimistic locking dla konfliktowych edycji wydarzeń, galerii, ustawień i zasobów współdzielonych.
- Używaj UUID albo jasno uzasadnionego typu identyfikatora.
- Nie używaj `Optional` jako pola encji.
- Zegar przekazuj przez `Clock`, jeśli logika zależy od czasu.
- Indeksy i constraints projektuj zgodnie z [DATABASE_CONVENTIONS.md](DATABASE_CONVENTIONS.md).

## Transakcje i idempotency
- Transakcje wyznaczaj na granicy przypadku użycia.
- Nie mieszaj długiej operacji IO z transakcją bazy, jeśli można rozdzielić zapis metadanych od przetwarzania pliku.
- Operacje, które mogą być ponawiane, powinny rozważać idempotency, np. upload, generowanie ZIP, wysyłka zaproszeń.
- `Idempotency-Key` powinien być rozważony dla operacji tworzących zasoby i narażonych na retry klienta.

## Logowanie
- Logi aplikacyjne muszą wspierać correlation ID.
- Nie loguj tokenów, haseł, pełnych sekretów, prywatnych linków ani pełnych danych wrażliwych.
- Logi nie zastępują `AuditLog`.
- Błędy integracji powinny mieć wystarczający kontekst operacyjny bez ujawniania danych wrażliwych.
- Operacje administracyjne wymagają audytu zgodnie z dokumentacją bezpieczeństwa.

## Security by design
Każdy przypadek użycia powinien jawnie określać:
- kto może go wykonać,
- do jakiego zasobu,
- na jakiej podstawie,
- czy wymagany jest ownership,
- czy wymagana jest rola systemowa,
- czy wymagana jest rola wydarzenia.

Autoryzacja nie może polegać wyłącznie na ukryciu przycisku w frontendzie. Kontrola musi istnieć po stronie backendu, najlepiej na poziomie use case i zapytań zawężonych po kontekście zasobu.

Wymagane kontrole:
- authentication,
- authorization,
- ownership,
- IDOR,
- rate limiting dla publicznych i wrażliwych endpointów,
- walidacja uploadu,
- bezpieczne logowanie,
- audyt działań administratora.

## Storage i integracje
- Logika domenowa nie zależy bezpośrednio od lokalnego filesystemu.
- Storage, e-mail, QR i background jobs są używane przez interfejsy tam, gdzie istnieje realna granica infrastruktury.
- Klient nie otrzymuje fizycznych ścieżek plików.
- Dostęp do plików musi przejść przez autoryzację biznesową.

## Testowanie backendu
Uwzględniaj:
- testy domeny,
- testy serwisów i use case,
- testy kontrolerów,
- testy integracyjne,
- Testcontainers dla bazy i zależności infrastrukturalnych, jeśli dotyczy,
- testy security,
- testy ownership,
- testy błędów,
- testy migracji,
- testy storage,
- testy idempotency i retry tam, gdzie ma to znaczenie.

Minimalna zasada: każda nowa reguła ownership i autoryzacji wymaga testu pozytywnego oraz negatywnego.

## Zakazane wzorce
- Field injection.
- Logika biznesowa w kontrolerze.
- Zwracanie encji JPA z API.
- Bezpośredni dostęp do storage z domeny.
- Puste catch blocks.
- Ogólny `catch (Exception)` bez uzasadnienia.
- Brak paginacji dla list.
- Ukrywanie błędów infrastruktury jako sukcesu.
- Tworzenie interfejsu dla każdej klasy bez realnej potrzeby.
- Refaktoryzacja niezwiązana z zadaniem.

## Powiązane dokumenty
- [API_CONVENTIONS.md](API_CONVENTIONS.md)
- [DATABASE_CONVENTIONS.md](DATABASE_CONVENTIONS.md)
- [ERROR_HANDLING.md](ERROR_HANDLING.md)
- [AUTHENTICATION_AND_AUTHORIZATION.md](AUTHENTICATION_AND_AUTHORIZATION.md)
- [../architecture/MODULES.md](../architecture/MODULES.md)
- [../architecture/MULTI_TENANCY.md](../architecture/MULTI_TENANCY.md)
- [../testing/BACKEND_TESTING.md](../testing/BACKEND_TESTING.md)

## Decyzje otwarte
- Czy MapStruct będzie standardem globalnym, czy narzędziem dopuszczonym selektywnie.
- Jak formalnie egzekwować granice modułów w kodzie wraz ze wzrostem projektu.
