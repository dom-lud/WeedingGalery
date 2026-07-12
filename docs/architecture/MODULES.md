# Moduły Systemu

## Cel dokumentu
Opisuje docelowy podział odpowiedzialności między moduły backendowe i ich granice.

## Status dokumentu
- Status: draft
- Zakres: odpowiedzialności modułów docelowego monolitu
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Moduły nie są jeszcze wyodrębnione w kodzie.

## Stan docelowy
- Modularny monolit z jawnymi granicami domenowymi i integracyjnymi.

| Moduł | Odpowiedzialność |
| --- | --- |
| `identity` | konta, hasła, sesje, tokeny weryfikacyjne, reset hasła |
| `user` | profil użytkownika, preferencje, wykorzystanie konta |
| `event` | tworzenie, edycja, status i lifecycle wydarzeń |
| `membership` | członkostwo, role wydarzenia, zaproszenia, transfer własności |
| `gallery` | galerie, widoczność, publikacja, personalizacja kontekstu galerii |
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
