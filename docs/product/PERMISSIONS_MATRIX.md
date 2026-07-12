# Macierz Uprawnień

## Cel dokumentu
Prezentuje wysokopoziomową macierz uprawnień dla ról systemowych i ról wydarzenia.

## Status dokumentu
- Status: draft
- Zakres: matryca uprawnień produktu
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Uprawnienia nie są jeszcze zaimplementowane.

## Stan docelowy
- Spójny model autoryzacji oparty o role systemowe, role wydarzenia i ownership zasobów.

## Zasady interpretacji
- `TAK` oznacza możliwość wykonania akcji w typowym przypadku.
- `OGR` oznacza dostęp warunkowy zależny od konfiguracji galerii lub delegacji.
- `NIE` oznacza brak uprawnienia.

| Akcja | Guest | User | EventManager | EventOwner | SystemAdministrator | SuperAdministrator |
| --- | --- | --- | --- | --- | --- | --- |
| Rejestracja konta | NIE | TAK | TAK | TAK | TAK | TAK |
| Logowanie do panelu użytkownika | NIE | TAK | TAK | TAK | TAK | TAK |
| Tworzenie wydarzenia | NIE | TAK | TAK | TAK | TAK | TAK |
| Edycja własnego profilu | NIE | TAK | TAK | TAK | TAK | TAK |
| Przegląd własnych wydarzeń | NIE | TAK | TAK | TAK | TAK | TAK |
| Wejście do publicznej galerii | OGR | NIE | NIE | NIE | OGR | OGR |
| Upload do galerii | OGR | OGR | OGR | OGR | OGR | OGR |
| Przegląd opublikowanych mediów | OGR | OGR | TAK | TAK | TAK | TAK |
| Pobieranie plików z galerii | OGR | OGR | TAK | TAK | TAK | TAK |
| Tworzenie galerii | NIE | NIE | TAK | TAK | TAK | TAK |
| Zmiana ustawień galerii | NIE | NIE | OGR | TAK | TAK | TAK |
| Moderacja materiałów | NIE | NIE | OGR | TAK | TAK | TAK |
| Zarządzanie członkami wydarzenia | NIE | NIE | OGR | TAK | TAK | TAK |
| Transfer własności wydarzenia | NIE | NIE | NIE | TAK | TAK | TAK |
| Usunięcie wydarzenia | NIE | NIE | NIE | TAK | TAK | TAK |
| Zmiana planu użytkownika | NIE | NIE | NIE | NIE | TAK | TAK |
| Zarządzanie administratorami | NIE | NIE | NIE | NIE | OGR | TAK |
| Przegląd audytu systemowego | NIE | NIE | NIE | NIE | TAK | TAK |
| Zmiana ustawień krytycznych | NIE | NIE | NIE | NIE | OGR | TAK |

## Uwagi domenowe
- Guest nie ma stałej tożsamości systemowej; jego uprawnienia wynikają z `GalleryAccess`.
- User bez członkostwa w wydarzeniu nie ma dostępu do cudzych zasobów.
- EventManager działa w granicach delegacji zapisanej w członkostwie.
- Administrator nie zastępuje ownership w logice biznesowej; używa odrębnego toru administracyjnego.

## Powiązane dokumenty
- [USER_ROLES.md](USER_ROLES.md)
- [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md)
- [../architecture/MULTI_TENANCY.md](../architecture/MULTI_TENANCY.md)

## Decyzje otwarte
- Czy administrator powinien móc przejmować widok właściciela wydarzenia, czy wyłącznie wykonywać jawne akcje administracyjne.
