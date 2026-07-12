# Komponenty UI

## Cel dokumentu
Opisuje planowany zestaw komponentów i zasady ich projektowania.

## Status dokumentu
- Status: draft
- Zakres: system komponentów frontendu
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Material UI został wybrany jako docelowy główny system UI, ale theme i komponenty domenowe nie są jeszcze wdrożone.

## Stan docelowy
- Spójny zestaw komponentów dla publicznej galerii, panelu użytkownika i panelu administratora, zbudowany na Material UI.

## System UI
- Material UI jest jedyną główną biblioteką komponentów UI.
- Nie twórz równoległego systemu komponentów i stylów od zera.
- Stosuj hierarchię: gotowy komponent MUI, props, `sx`, `styled()`, komponent domenowy oparty na MUI, theme overrides, własny CSS tylko jako wyjątek.
- Wszystkie ekrany korzystają ze wspólnego `ThemeProvider`.
- Theme jest źródłem kolorów, typografii, odstępów, breakpointów, zaokrągleń, cieni, wariantów i override'ów.
- Szczegółowe zasady opisuje [UI_SYSTEM.md](UI_SYSTEM.md).

## Komponenty bazowe
- `Button`
- `TextField`
- `Select`
- `Dialog`
- `Snackbar`
- `Alert`
- `Menu`
- `Chip`
- `Tabs`
- `Pagination`
- `LinearProgress`
- domenowy `UploadDropzone` oparty na MUI

## Komponenty domenowe
- EventCard
- GalleryCard
- MediaGrid
- MediaLightbox
- UploadQueue
- ModerationToolbar
- StatsPanel
- AccessCodePrompt
- QrCodeCard

## Zasady
- Komponenty powinny wspierać dostępność klawiaturową.
- Nie zakładamy desktop-first.
- Komponenty personalizacji korzystają wyłącznie z bezpiecznych tokenów motywu.
- Nie dodawaj Bootstrap, Ant Design, Chakra UI, Mantine, Tailwind UI ani innej pełnej biblioteki UI bez zaakceptowanego ADR.
- Publiczna galeria ma korzystać z MUI, ale przez własny theme i warianty nie powinna wyglądać jak domyślny panel Material Design.

## Powiązane dokumenty
- [UI_SYSTEM.md](UI_SYSTEM.md)
- [ACCESSIBILITY.md](ACCESSIBILITY.md)
- [RESPONSIVE_DESIGN.md](RESPONSIVE_DESIGN.md)
- [UPLOAD_UX.md](UPLOAD_UX.md)
- [../adr/0011-material-ui-frontend-system.md](../adr/0011-material-ui-frontend-system.md)

## Decyzje otwarte
- Docelowy kształt theme dla publicznej galerii na wspólnych tokenach.
