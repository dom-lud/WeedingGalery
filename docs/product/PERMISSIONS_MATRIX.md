# Macierz Uprawnien

## Cel dokumentu
Prezentuje wysokopoziomowa macierz uprawnien dla rol systemowych i rol wydarzenia.

## Status dokumentu
- Status: draft
- Zakres: matryca uprawnien produktu
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Zaimplementowany jest podstawowy model auth dla kont systemowych.
- W praktyce wdrozone sa obecnie tylko uprawnienia potrzebne do logowania, wylogowania, odczytu `/api/auth/me` i administracyjnego tworzenia kont.
- Uprawnienia `EventOwner` i `EventManager` oraz ownership wydarzenia sa wdrozone w zakresie Etapu 3.
- Osobny tor administracyjny wydarzen nie jest jeszcze zaimplementowany; rola `ADMIN` nie omija ownership w zwyklym `/api/events`.

## Stan docelowy
- Spojny model autoryzacji oparty o role systemowe, role wydarzenia i ownership zasobow.

## Zasady interpretacji
- `TAK` oznacza mozliwosc wykonania akcji w typowym przypadku.
- `OGR` oznacza dostep warunkowy zalezny od konfiguracji galerii lub delegacji.
- `NIE` oznacza brak uprawnienia.

| Akcja | Guest | User | EventManager | EventOwner | SystemAdministrator | SuperAdministrator |
| --- | --- | --- | --- | --- | --- | --- |
| Rejestracja konta | NIE | TAK | TAK | TAK | TAK | TAK |
| Logowanie do panelu uzytkownika | NIE | TAK | TAK | TAK | TAK | TAK |
| Tworzenie wydarzenia | NIE | TAK | TAK | TAK | TAK | TAK |
| Edycja wlasnego profilu | NIE | TAK | TAK | TAK | TAK | TAK |
| Przeglad wlasnych wydarzen | NIE | TAK | TAK | TAK | TAK | TAK |
| Wejscie do publicznej galerii | OGR | NIE | NIE | NIE | OGR | OGR |
| Upload do galerii | OGR | OGR | OGR | OGR | OGR | OGR |
| Przeglad opublikowanych mediow | OGR | OGR | TAK | TAK | TAK | TAK |
| Pobieranie plikow z galerii | OGR | OGR | TAK | TAK | TAK | TAK |
| Tworzenie galerii | NIE | NIE | TAK | TAK | TAK | TAK |
| Zmiana ustawien galerii | NIE | NIE | OGR | TAK | TAK | TAK |
| Moderacja materialow | NIE | NIE | OGR | TAK | TAK | TAK |
| Zarzadzanie czlonkami wydarzenia | NIE | NIE | OGR | TAK | TAK | TAK |
| Transfer wlasnosci wydarzenia | NIE | NIE | NIE | TAK | TAK | TAK |
| Usuniecie wydarzenia | NIE | NIE | NIE | TAK | TAK | TAK |
| Zmiana planu uzytkownika | NIE | NIE | NIE | NIE | TAK | TAK |
| Zarzadzanie administratorami | NIE | NIE | NIE | NIE | OGR | TAK |
| Przeglad audytu systemowego | NIE | NIE | NIE | NIE | TAK | TAK |
| Zmiana ustawien krytycznych | NIE | NIE | NIE | NIE | OGR | TAK |

## Uwagi domenowe
- Guest nie ma stalej tozsamosci systemowej; jego uprawnienia wynikaja z `GalleryAccess`.
- User bez czlonkostwa w wydarzeniu nie ma dostepu do cudzych zasobow.
- EventManager dziala w granicach delegacji zapisanej w czlonkostwie.
- Administrator nie zastepuje ownership w logice biznesowej; uzywa odrebnego toru administracyjnego.

## Powiazane dokumenty
- [USER_ROLES.md](USER_ROLES.md)
- [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md)
- [../architecture/MULTI_TENANCY.md](../architecture/MULTI_TENANCY.md)

## Decyzje otwarte
- Czy administrator powinien moc przejmowac widok wlasciciela wydarzenia, czy wylacznie wykonywac jawne akcje administracyjne.
