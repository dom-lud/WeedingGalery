# Test Design Brief: Etap 10 Admin API

## Zakres i założenia

Admin API działa wyłącznie pod `/api/admin/**`. Zwykłe endpointy domenowe
pozostają bez zmian i nie dostają ownership bypass. W obecnym modelu istnieje
jedna rola administracyjna `ADMIN`; kontrola roli musi być wykonywana na
podstawie zalogowanego użytkownika z bazy, a nie na podstawie samego URL-a.

## Macierz ryzyk

| Ryzyko | Błędna implementacja, którą test ma wykryć | Najniższa wiarygodna warstwa | Scenariusze |
|---|---|---|---|
| Brak uwierzytelnienia | Anonim otrzymuje dashboard lub listę | Integracja HTTP/Spring Security | dashboard/list users/events/media: `401` |
| Eskalacja USER -> ADMIN | Zwykły użytkownik odczytuje lub mutuje dane admina | Integracja HTTP | każdy odczyt i każda akcja: `403` |
| Fałszywy admin | Rola wynika z parametru albo nazwy ścieżki | Serwis/controller | USER z identyfikatorem admina nadal `403` |
| Wyciek sekretów | Lista użytkowników lub mediów zwraca hash hasła, storage key albo checksum | Test DTO/HTTP | odpowiedź nie zawiera pól wrażliwych |
| Nielimitowane odczyty | Parametr size powoduje kosztowną odpowiedź lub błąd | Unit + integracja | `size` jest ograniczany do bezpiecznego maksimum; page/size są stabilne |
| Cross-domain admin action | Akcja wydarzenia/media dotyka obcego lub nieistniejącego zasobu | Serwis | `404`, brak zapisu i brak sukcesowego audytu |
| Nieaudytowana mutacja | Zmiana stanu kończy się bez wymaganego audytu | Serwis/integracja | lock/unlock/archive/hide zapisują audyt fail-closed |
| Niebezpieczna akcja | Admin endpoint zmienia hasło, ownership lub storage bez jawnego kontraktu | Serwis | zakres akcji ograniczony do lock/unlock/archive/hide; brak ownership transferu i fizycznego usuwania |
| Brak idempotencji | Powtórzenie akcji tworzy dodatkowe skutki lub drugi audit | Serwis | powtórne lock/archive/hide nie psuje stanu i nie dubluje skutku |

## Scenariusze dashboardu

- poprawne agregaty użytkowników, wydarzeń, galerii, mediów i storage;
- zero danych nie powoduje błędu ani dzielenia przez zero;
- agregaty nie ujawniają danych osobowych ani sekretów.

## Kryteria akceptacji

- kontrola `ADMIN` jest fail-closed;
- odczyty są paginowane i zwracają bezpieczne DTO;
- każda udana akcja ma odpowiadający istniejący audit event;
- testy obejmują pozytywne, negatywne, graniczne i replay scenariusze;
- testy nie wyłączają CSRF ani autoryzacji jako substytutu prawdziwego flow.
