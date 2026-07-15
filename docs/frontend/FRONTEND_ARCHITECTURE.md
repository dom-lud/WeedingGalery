# Architektura Frontendu

## Cel dokumentu
Opisuje docelową strukturę SPA, główne obszary interfejsu i odpowiedzialności warstw frontendowych.

## Status dokumentu
- Status: draft
- Zakres: frontend docelowy
- Ostatnia aktualizacja: 2026-07-15

## Stan obecny
- Aplikacja działa jako SPA React/TypeScript/Vite z React Router, kontekstem sesji i chronionymi trasami.
- Material UI, wspólny theme i `AppShell` tworzą bazę panelu; widoki domenowe korzystają z wydzielonych klientów API.
- Zaimplementowane są: logowanie, dashboard wydarzeń i członkostw, zarządzanie galeriami oraz publiczny flow dostępu i uploadu.
- Layouty i dialogi są mobile-first; publiczny upload obsługuje stany dostępu, offline, kolejki, częściowego powodzenia i retry.

## Stan docelowy
- React + TypeScript + Vite + React Router.
- Material UI jako jedyny główny system UI frontendu.
- Mobile-first, z trzema głównymi obszarami UI: panel użytkownika, galeria publiczna, panel administratora.

## Warstwy frontendu
- Routing i guardy
- Feature modules
- API client i kontrakty DTO
- Zarządzanie stanem widoku i stanem serwera
- Biblioteka komponentów aplikacyjnych zbudowana na Material UI

## Główne obszary
- Public gallery
- User dashboard
- Event management
- Media moderation
- Admin panel

## Zasady
- Dedykowana warstwa komunikacji z API, bez bezpośrednich `fetch` w komponentach widokowych.
- Komponenty renderujące nie zawierają logiki autoryzacji biznesowej.
- Mobile-first dla uploadu i publicznej galerii.
- Widoki administracyjne i użytkownika mają wspólne standardy, ale nie muszą współdzielić całego UI.
- UI implementuj zgodnie z [UI_SYSTEM.md](UI_SYSTEM.md): MUI-first, wspólny `ThemeProvider`, theme jako źródło tokenów i własny CSS tylko jako wyjątek.
- Szczegółowe zasady CSS, responsywności, Flexbox/Grid, stanów UI i React/TypeScript opisuje [CSS_AND_RESPONSIVE_GUIDELINES.md](CSS_AND_RESPONSIVE_GUIDELINES.md).
- Każdy ekran pobierający dane musi projektować initial, loading, success, empty, partial, error, retry, unauthorized, forbidden oraz offline/interrupted state, jeśli dotyczy.
- Server state powinien być oddzielony od local UI state.
- Optymalizacje React, takie jak `useMemo` i `useCallback`, stosuj dopiero przy realnej potrzebie.

## Powiązane dokumenty
- [ROUTING.md](ROUTING.md)
- [STATE_MANAGEMENT.md](STATE_MANAGEMENT.md)
- [UI_SYSTEM.md](UI_SYSTEM.md)
- [CSS_AND_RESPONSIVE_GUIDELINES.md](CSS_AND_RESPONSIVE_GUIDELINES.md)
- [UPLOAD_UX.md](UPLOAD_UX.md)

## Decyzje otwarte
- Czy biblioteka komponentów będzie lokalna i lekka, czy zbudowana na bazie wybranego systemu UI.
