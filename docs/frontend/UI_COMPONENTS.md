# Komponenty UI

## Cel dokumentu
Opisuje planowany zestaw komponentów i zasady ich projektowania.

## Status dokumentu
- Status: draft
- Zakres: system komponentów frontendu
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Brak docelowej biblioteki komponentów.

## Stan docelowy
- Spójny zestaw komponentów dla publicznej galerii, panelu użytkownika i panelu administratora.

## Komponenty bazowe
- Button
- Input
- Select
- Dialog
- Toast
- Dropdown
- Badge
- Tabs
- Pagination
- ProgressBar
- FileDropzone

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

## Powiązane dokumenty
- [ACCESSIBILITY.md](ACCESSIBILITY.md)
- [RESPONSIVE_DESIGN.md](RESPONSIVE_DESIGN.md)
- [UPLOAD_UX.md](UPLOAD_UX.md)

## Decyzje otwarte
- Czy budować osobny visual language dla publicznej galerii i panelu administracyjnego na wspólnych tokenach.
