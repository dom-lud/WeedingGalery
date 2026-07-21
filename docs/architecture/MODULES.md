# Moduły Systemu

## Cel dokumentu
Opisuje docelowy podział odpowiedzialności między moduły backendowe i ich granice.

## Status dokumentu
- Status: draft
- Zakres: odpowiedzialności modułów docelowego monolitu
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Kod jest organizowany pakietami domenowymi; Etap 3 ma oddzielne pakiety `event` i `membership`, korzystajace z `audit`, `user` i wspolnych bledow.
- Aktualny pakiet `auth` implementuje pierwszy zakres docelowego modulu `identity`: administracyjne tworzenie kont, logowanie, sesje i audit identity.
- `publicaccess` jest wydzielonym pakietem dla publicznego dostepu do galerii: tokeny, kody, grant sesyjny i rate limiting wejscia publicznego.
- `security` jest pakietem przekrojowym Spring Security, a nie domena biznesowa; nie powinien zawierac use case domenowych.
- Formalne egzekwowanie granic modulow narzedziem architektonicznym pozostaje do zrobienia.

## Stan docelowy
- Modularny monolit z jawnymi granicami domenowymi i integracyjnymi.

| Moduł | Odpowiedzialność |
| --- | --- |
| `identity` | konta, hasła, sesje, tokeny weryfikacyjne, reset hasła |
| `auth` | obecna implementacja pierwszego zakresu `identity`; docelowo do scalenia nazewniczego albo opisania jako adapter identity |
| `user` | profil użytkownika, preferencje, wykorzystanie konta |
| `event` | tworzenie, edycja, status i lifecycle wydarzeń |
| `membership` | członkostwo, role wydarzenia, zaproszenia, transfer własności |
| `gallery` | galerie, widoczność, publikacja, personalizacja kontekstu galerii |
| `publicaccess` | publiczny dostep do galerii przez slug, token, kod i grant sesyjny |
| `media` | metadane plików, statusy, moderacja, miniatury |
| `storage` | abstrakcja zapisu, odczytu i usuwania plików |
| `upload` | sesje uploadu, walidacja, retry, limity |
| `download` | pobrania, archiwa ZIP, linki czasowe |
| `moderation` | polityki publikacji i operacje moderacyjne |
| `customization` | motywy, kolory, teksty i bezpieczne opcje personalizacji |
| `notification` | szablony i wysyłka e-maili systemowych |
| `analytics` | statystyki zdarzeń i dashboardy |
| `subscription` | plany, limity, wykorzystanie i przypisania planów |
| `admin` | panel administracyjny i operacje operatorskie |
| `audit` | log audytowy i korelacja działań |
| `security` | konfiguracja i adaptery Spring Security, filtry oraz handlery techniczne |
| `configuration` | ustawienia systemowe i feature toggles |
| `common` | komponenty współdzielone, bez logiki domenowej specyficznej dla jednego modułu |

## Zasady zależności
- Moduł domenowy nie zależy bezpośrednio od kontrolerów ani storage.
- `common` nie może stać się koszem na przypadkowe zależności.
- `admin` używa publicznych kontraktów modułów biznesowych albo jawnych use case administracyjnych.
- `audit` powinien być wywoływany przez use case, nie przez warstwę transportową.

## Przykładowe przypadki użycia
- `CreateEventUseCase` w module `event`
- `InviteEventMemberUseCase` w module `membership`
- `UploadMediaUseCase` w module `upload`
- `ApproveMediaUseCase` w module `moderation`
- `GenerateGalleryArchiveUseCase` w module `download`

## Kiedy wydzielać moduł
- Gdy ma własne pojęcia domenowe i lifecycle.
- Gdy wymaga osobnych polityk bezpieczeństwa lub retencji.
- Gdy zbyt duża liczba use case utrudnia utrzymanie jednego modułu.

## Powiązane dokumenty
- [SYSTEM_ARCHITECTURE.md](SYSTEM_ARCHITECTURE.md)
- [../backend/BACKEND_GUIDELINES.md](../backend/BACKEND_GUIDELINES.md)
- [../adr/0001-modular-monolith.md](../adr/0001-modular-monolith.md)

## Decyzje otwarte
- Czy `moderation` powinno pozostać osobnym modułem czy częścią `media`.
