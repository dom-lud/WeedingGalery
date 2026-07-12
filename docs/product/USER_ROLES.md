# Role Użytkowników

## Cel dokumentu
Definiuje role systemowe i role kontekstowe oraz zakres ich odpowiedzialności.

## Status dokumentu
- Status: draft
- Zakres: role produktu i odpowiedzialności
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Role nie są jeszcze zaimplementowane.

## Stan docelowy
- System rozróżnia role systemowe i role w kontekście wydarzenia.

## Podział ról
| Rola | Typ | Zakres | Opis |
| --- | --- | --- | --- |
| Guest | kontekstowa | pojedyncza galeria | Osoba bez konta z ograniczonym dostępem przez link, QR lub token |
| User | systemowa | konto | Zarejestrowany użytkownik platformy |
| EventOwner | kontekstowa | wydarzenie | Właściciel wydarzenia z pełną kontrolą biznesową |
| EventManager | kontekstowa | wydarzenie | Współzarządzający z delegowanymi uprawnieniami |
| SystemAdministrator | systemowa | cała platforma | Operator panelu administracyjnego |
| SuperAdministrator | systemowa | cała platforma | Rola nadrzędna do zarządzania administracją i ustawieniami krytycznymi |

## Guest
- Nie posiada konta.
- Może wejść do galerii przez slug, link, QR lub token.
- Może zostać poproszony o kod dostępu.
- Może uploadować pliki, przeglądać opublikowane materiały i pobierać pliki tylko w granicach ustawień galerii.
- Nie ma dostępu do panelu użytkownika ani panelu administracyjnego.

## User
- Posiada konto i własny profil.
- Może tworzyć wydarzenia lub otrzymywać zaproszenia do cudzych wydarzeń.
- Może mieć wiele wydarzeń i wiele ról w różnych wydarzeniach.

## EventOwner
- Tworzy wydarzenie albo otrzymuje własność wydarzenia.
- Zarządza galeriami, ustawieniami prywatności, współzarządzającymi, limitami wynikającymi z planu i publikacją materiałów.
- Może przenieść własność wydarzenia.

## EventManager
- Działa w granicach delegacji właściciela.
- Typowy zakres: galerie, moderacja, pobieranie, personalizacja, statystyki.
- Domyślnie bez uprawnień do usunięcia właściciela, zmiany subskrypcji właściciela lub usunięcia całego konta.

## SystemAdministrator
- Zarządza użytkownikami, wydarzeniami, galeriami, limitami i konfiguracją.
- Każda wrażliwa akcja musi być audytowana.
- Nie powinien używać uprawnień administracyjnych do działań operacyjnych bez uzasadnienia.

## SuperAdministrator
- Zarządza kontami administratorów i ustawieniami krytycznymi.
- Wymaga dodatkowej kontroli dostępu, silniejszego audytu i najniższego możliwego grona użytkowników.

## Zasady modelu ról
- Role systemowe nie zastępują ownership zasobów.
- Autoryzacja musi uwzględniać zarówno rolę, jak i relację użytkownika do wydarzenia.
- Guest nie może eskalować do roli użytkownika bez jawnego przepływu rejestracji.
- Rola EventManager może być w przyszłości parametryzowana zestawem uprawnień szczegółowych.

## Powiązane dokumenty
- [PERMISSIONS_MATRIX.md](PERMISSIONS_MATRIX.md)
- [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md)
- [../architecture/MULTI_TENANCY.md](../architecture/MULTI_TENANCY.md)

## Decyzje otwarte
- Czy EventManager ma mieć profil z predefiniowanymi wariantami uprawnień, czy zestaw granularnych flag.
