# Projekt Responsywny

## Cel dokumentu
Opisuje zasady mobile-first dla frontendu platformy.

## Status dokumentu
- Status: draft
- Zakres: responsive design dla widoków publicznych i panelowych
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Docelowy responsive design nie jest jeszcze zaprojektowany.

## Stan docelowy
- UI czytelne na telefonie, tablecie i desktopie, z priorytetem dla mobilnego uploadu i przeglądania galerii.
- Responsywność oparta na breakpointach i narzędziach Material UI.

## Zasady
- Mobile-first layout.
- Bazowe style odpowiadają najmniejszym ekranom; większe ekrany obsługuj przez breakpointy MUI.
- Używaj responsywnych wartości w `sx`, `useMediaQuery`, `theme.breakpoints` i responsywnych propsów komponentów.
- Nie twórz breakpointów pod konkretne modele urządzeń.
- Stosuj `Stack` albo `Box` z `display: flex` dla układów jednowymiarowych.
- Stosuj `Grid` albo `Box` z `display: grid` dla galerii, dashboardów, kart i układów wielokolumnowych.
- Minimalizujemy ciężkie elementy w publicznej galerii na słabym łączu.
- Nawigacja panelu użytkownika musi działać również na małych ekranach.
- Grid mediów dopasowuje się do orientacji i liczby plików.
- Szczegółowe zasady CSS, Flexbox/Grid, breakpointów i checklistę mobile-first opisuje [CSS_AND_RESPONSIVE_GUIDELINES.md](CSS_AND_RESPONSIVE_GUIDELINES.md).

## Krytyczne widoki mobilne
- Ekran wejścia do galerii
- Ekran uploadu
- Przeglądanie galerii
- Formularz logowania
- Podstawowy dashboard właściciela wydarzenia

## Powiązane dokumenty
- [FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md)
- [CSS_AND_RESPONSIVE_GUIDELINES.md](CSS_AND_RESPONSIVE_GUIDELINES.md)
- [UI_SYSTEM.md](UI_SYSTEM.md)
- [UPLOAD_UX.md](UPLOAD_UX.md)
- [ACCESSIBILITY.md](ACCESSIBILITY.md)

## Decyzje otwarte
- Czy panel administratora ma mieć pełny wariant mobilny, czy ograniczony wariant read-only na małych ekranach.
