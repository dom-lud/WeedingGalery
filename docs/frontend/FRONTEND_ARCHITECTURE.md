# Architektura Frontendu

## Cel dokumentu
Opisuje docelową strukturę SPA, główne obszary interfejsu i odpowiedzialności warstw frontendowych.

## Status dokumentu
- Status: draft
- Zakres: frontend docelowy
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- W repozytorium istnieje szkic aplikacji React/Vite.
- Docelowa architektura paneli i galerii publicznej nie jest jeszcze wdrożona.

## Stan docelowy
- React + TypeScript + Vite + React Router.
- Mobile-first, z trzema głównymi obszarami UI: panel użytkownika, galeria publiczna, panel administratora.

## Warstwy frontendu
- Routing i guardy
- Feature modules
- API client i kontrakty DTO
- Zarządzanie stanem widoku i stanem serwera
- Biblioteka komponentów aplikacyjnych

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

## Powiązane dokumenty
- [ROUTING.md](ROUTING.md)
- [STATE_MANAGEMENT.md](STATE_MANAGEMENT.md)
- [UPLOAD_UX.md](UPLOAD_UX.md)

## Decyzje otwarte
- Czy biblioteka komponentów będzie lokalna i lekka, czy zbudowana na bazie wybranego systemu UI.
