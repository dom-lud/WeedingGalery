# Role Uzytkownikow

## Cel dokumentu
Definiuje role systemowe i role kontekstowe oraz zakres ich odpowiedzialnosci.

## Status dokumentu
- Status: draft
- Zakres: role produktu i odpowiedzialnosci
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- W kodzie istnieja aktualnie tylko role systemowe `USER` i `ADMIN`.
- Role wydarzenia, takie jak `EventOwner` i `EventManager`, nie sa jeszcze zaimplementowane i stanowia wejscie do Etapu 3.

## Stan docelowy
- System rozroznia role systemowe i role w kontekscie wydarzenia.

## Podzial rol
| Rola | Typ | Zakres | Opis |
| --- | --- | --- | --- |
| Guest | kontekstowa | pojedyncza galeria | Osoba bez konta z ograniczonym dostepem przez link, QR lub token |
| User | systemowa | konto | Zarejestrowany uzytkownik platformy |
| EventOwner | kontekstowa | wydarzenie | Wlasciciel wydarzenia z pelna kontrola biznesowa |
| EventManager | kontekstowa | wydarzenie | Wspolzarzadzajacy z delegowanymi uprawnieniami |
| SystemAdministrator | systemowa | cala platforma | Operator panelu administracyjnego |
| SuperAdministrator | systemowa | cala platforma | Rola nadrzedna do zarzadzania administracja i ustawieniami krytycznymi |

## Guest
- Nie posiada konta.
- Moze wejsc do galerii przez slug, link, QR lub token.
- Moze zostac poproszony o kod dostepu.
- Moze uploadowac pliki, przegladac opublikowane materialy i pobierac pliki tylko w granicach ustawien galerii.
- Nie ma dostepu do panelu uzytkownika ani panelu administracyjnego.

## User
- Posiada konto i wlasny profil.
- Moze tworzyc wydarzenia lub otrzymywac zaproszenia do cudzych wydarzen.
- Moze miec wiele wydarzen i wiele rol w roznych wydarzeniach.

## EventOwner
- Tworzy wydarzenie albo otrzymuje wlasnosc wydarzenia.
- Zarzadza galeriami, ustawieniami prywatnosci, wspolzarzadzajacymi, limitami wynikajacymi z planu i publikacja materialow.
- Moze przeniesc wlasnosc wydarzenia.

## EventManager
- Dziala w granicach delegacji wlasciciela.
- Typowy zakres: galerie, moderacja, pobieranie, personalizacja, statystyki.
- Domyslnie bez uprawnien do usuniecia wlasciciela, zmiany subskrypcji wlasciciela lub usuniecia calego konta.

## SystemAdministrator
- Zarzadza uzytkownikami, wydarzeniami, galeriami, limitami i konfiguracja.
- Kazda wrazliwa akcja musi byc audytowana.
- Nie powinien uzywac uprawnien administracyjnych do dzialan operacyjnych bez uzasadnienia.

## SuperAdministrator
- Zarzadza kontami administratorow i ustawieniami krytycznymi.
- Wymaga dodatkowej kontroli dostepu, silniejszego audytu i najnizszego mozliwego grona uzytkownikow.

## Zasady modelu rol
- Role systemowe nie zastepuja ownership zasobow.
- Autoryzacja musi uwzgledniac zarowno role, jak i relacje uzytkownika do wydarzenia.
- Guest nie moze eskalowac do roli uzytkownika bez jawnego przeplywu rejestracji.
- Rola EventManager moze byc w przyszlosci parametryzowana zestawem uprawnien szczegolowych.

## Powiazane dokumenty
- [PERMISSIONS_MATRIX.md](PERMISSIONS_MATRIX.md)
- [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md)
- [../architecture/MULTI_TENANCY.md](../architecture/MULTI_TENANCY.md)

## Decyzje otwarte
- Czy EventManager ma miec profil z predefiniowanymi wariantami uprawnien, czy zestaw granularnych flag.
