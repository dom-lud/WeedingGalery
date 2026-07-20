# System UI

## Cel dokumentu
Opisuje obowiązujący system UI frontendu oraz zasady użycia Material UI.

## Status dokumentu
- Status: draft
- Zakres: biblioteka UI, theme, layout, responsywność, wyjątki
- Ostatnia aktualizacja: 2026-07-20

## Stan obecny
- Frontend korzysta ze wspólnego `ThemeProvider` i jasnego, redakcyjnego theme MUI opartego na kolorach śliwkowym, szampańskim i ciepłym tle neutralnym.
- Theme centralizuje paletę, typografię systemową, focus state, ograniczenie animacji, promienie, cienie i warianty bazowych komponentów.
- Subtelny motion korzysta z gotowego przejścia MUI `Fade` wyłącznie dla nieinteraktywnych nagłówków i dekoracji; `prefers-reduced-motion` wyłącza efekt również na poziomie propsów komponentu.
- Logowanie, chroniony shell aplikacji, zarządzanie wydarzeniami i galeriami oraz publiczny upload są zbudowane na MUI.
- Dashboard korzysta z lekkiego układu master-detail: kompaktowa lista wydarzeń, kontekstowy nagłówek wybranego wydarzenia oraz zakładki `Galleries`, `People` i `Settings` ograniczają liczbę jednocześnie widocznych akcji.
- Karty galerii eksponują jedną akcję główną, a operacje drugorzędne i destrukcyjne umieszczają w menu MUI; stan archiwalny pozostaje widoczny bez dominującej czerwonej powierzchni.
- Wspólne komponenty `AppShell` i `ConfirmDialog` zapewniają spójny layout oraz bezpieczne potwierdzanie operacji zmieniających cykl życia zasobów.
- Własny globalny CSS jest ograniczony do resetu dokumentu i zabezpieczenia przed poziomym overflow.

## Stan docelowy
- Material UI jest jedynym głównym systemem UI dla wszystkich obszarów aplikacji.
- Wszystkie ekrany korzystają ze wspólnego `ThemeProvider`.
- Publiczna galeria może mieć własny wariant wizualny, ale nadal ma być zbudowana na MUI i wspólnych zasadach theme.

## Zakres użycia MUI
MUI jest domyślną biblioteką interfejsu dla:
- publicznej strony wydarzenia,
- publicznej galerii,
- formularza uploadu,
- panelu użytkownika,
- panelu współzarządzającego,
- panelu administratora,
- ekranów uwierzytelniania,
- ustawień i statystyk.

Nie twórz równoległego własnego systemu komponentów i stylów od zera, jeśli element może zostać poprawnie zbudowany lub rozszerzony przy użyciu MUI.

## Hierarchia rozwiązań
Przy implementacji UI stosuj kolejność:
1. Gotowy komponent MUI.
2. Gotowy komponent MUI skonfigurowany przez props.
3. Komponent MUI rozszerzony przez `sx`.
4. Komponent MUI rozszerzony przez `styled()`.
5. Własny komponent domenowy zbudowany na komponentach MUI.
6. Rozszerzenie globalnego theme, wariantów lub `components.styleOverrides`.
7. Własny CSS tylko jako wyjątek.

Zasada przewodnia: MUI-first, custom CSS only by exception.

## Theme
Theme jest centralnym źródłem:
- kolorów,
- typografii,
- odstępów,
- breakpointów,
- promieni zaokrągleń,
- cieni,
- wariantów komponentów,
- globalnych override'ów.

Nie używaj przypadkowych kolorów, spacingów ani fontów bezpośrednio w komponentach, jeśli mogą pochodzić z theme. Powtarzalne style przenoś do theme, wariantów, `styled()` lub wspólnych komponentów domenowych. Nie używaj `!important`, chyba że problem integracyjny jest udokumentowany.

## Biblioteki UI
- MUI jest jedyną główną biblioteką komponentów UI.
- Nie dodawaj Bootstrap, Ant Design, Chakra UI, Mantine, Tailwind UI ani innej pełnej biblioteki UI bez zaakceptowanego ADR.
- Preferuj publiczne API MUI: props, `slotProps`, `slots`, `sx`, `styled()` i `components` w theme.
- Nie nadpisuj wewnętrznych klas MUI opartych na niestabilnych selektorach.
- Nie używaj inline styles, jeśli styl jest stały lub powtarzalny.

## Layout
MUI jest podstawą layoutu:
- `Container` do ograniczania szerokości treści,
- `Box` jako elastyczny element bazowy,
- `Stack` do układów jednowymiarowych,
- `Grid` lub `Box` z `display: grid` do układów dwuwymiarowych,
- `Drawer` do nawigacji paneli,
- `AppBar` i `Toolbar` tam, gdzie odpowiadają semantyce,
- `Paper` i `Card` do grupowania treści,
- `Dialog` i `Modal` zgodnie z ich przeznaczeniem.

Stosuj `Stack` albo `Box` z `display: flex` dla rzędów, kolumn, toolbarów, przycisków, nawigacji, wyrównywania elementów i prostych formularzy.

Stosuj `Grid` albo `Box` z `display: grid` dla galerii zdjęć, dashboardów, layoutów kart, układów wielu kolumn i sekcji wymagających kontroli nad wierszami oraz kolumnami. Dozwolone jest łączenie Grid i Flexbox, np. Grid dla galerii i Stack wewnątrz karty.

## Mobile-first
Każdy ekran projektuj najpierw dla urządzeń mobilnych:
- bazowe style odpowiadają najmniejszym ekranom,
- rozszerzenia dla większych ekranów dodawaj przez breakpointy MUI,
- nie polegaj na hover,
- kluczowe akcje muszą być wygodne dotykowo,
- formularze muszą działać z klawiaturą ekranową,
- unikaj poziomego scrollowania,
- upload projektuj przede wszystkim pod telefon,
- przyciski i kontrolki muszą mieć wystarczający obszar dotykowy,
- dialogi muszą poprawnie działać na małych ekranach,
- tabele w panelu muszą mieć mobilny wariant albo alternatywną reprezentację.

Używaj breakpointów z theme przez responsywne wartości w `sx`, `useMediaQuery`, `theme.breakpoints` lub responsywne propsy komponentów. Nie twórz breakpointów pod konkretne modele urządzeń.

## Publiczna galeria
Publiczna galeria również korzysta z MUI, ale nie powinna pozostać w domyślnym stylu Material Design. Powinna wykorzystywać:
- własny theme lub wariant theme,
- warianty komponentów,
- dopasowaną typografię,
- własne karty mediów zbudowane na MUI,
- `ImageList`, CSS Grid albo `Box` z `display: grid`,
- `Dialog` albo dedykowany lightbox oparty na MUI,
- `Skeleton` podczas ładowania,
- `Snackbar` i `Alert` dla komunikatów,
- `Button`, `IconButton`, `Menu`, `Chip` i inne komponenty MUI.

Zewnętrzna biblioteka jest dopuszczalna tylko dla specjalistycznych funkcji, takich jak zaawansowany lightbox, virtualizacja bardzo dużej galerii, resumable upload albo odtwarzacz wideo. Taki wyjątek musi być uzasadniony w dokumentacji lub ADR.

## Komponenty domenowe
Twórz komponenty domenowe na MUI, np.:
- `GalleryCard`,
- `MediaTile`,
- `UploadDropzone`,
- `UploadProgressItem`,
- `EventHeader`,
- `StorageUsageCard`,
- `ModerationToolbar`,
- `AdminStatCard`.

Komponent domenowy powinien ukrywać szczegóły konfiguracji MUI, zapewniać spójny wygląd, mieć jasno określone propsy, nie duplikować logiki, wspierać responsywność i accessibility.

## Formularze
Formularze buduj na komponentach MUI:
- `TextField`,
- `Select`,
- `Autocomplete`,
- `Checkbox`,
- `Radio`,
- `Switch`,
- `FormControl`,
- `FormHelperText`,
- `DatePicker`, jeśli zostanie dodany pakiet MUI X.

Błędy walidacji prezentuj przez standardowe mechanizmy MUI. Nie twórz własnych inputów od zera, jeśli nie jest to konieczne.

## Accessibility
Wykorzystuj domyślne możliwości accessibility MUI, ale sprawdzaj:
- etykiety,
- role,
- kolejność focusu,
- obsługę klawiatury,
- kontrast,
- focus state,
- dostępność dialogów,
- komunikaty błędów,
- opisy ikon,
- `aria-label` dla przycisków ikonowych.

## Wyjątki
Własny CSS albo inna biblioteka może zostać użyta wyłącznie, gdy:
- MUI nie obsługuje wymaganej funkcji,
- implementacja przez MUI byłaby istotnie mniej czytelna,
- wymagana jest specjalistyczna wydajność,
- wymagany jest niestandardowy rendering,
- rozwiązanie zostało uzasadnione w dokumentacji lub ADR.

Przed wyjątkiem opisz:
1. czego brakuje w MUI,
2. jakie rozwiązania MUI zostały rozważone,
3. dlaczego nie są wystarczające,
4. wpływ na bundle,
5. wpływ na utrzymanie,
6. czy rozwiązanie zachowuje theme i accessibility.

## Powiązane dokumenty
- [FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md)
- [UI_COMPONENTS.md](UI_COMPONENTS.md)
- [RESPONSIVE_DESIGN.md](RESPONSIVE_DESIGN.md)
- [ACCESSIBILITY.md](ACCESSIBILITY.md)
- [../adr/0011-material-ui-frontend-system.md](../adr/0011-material-ui-frontend-system.md)

## Decyzje otwarte
- Czy publiczna galeria wymaga osobnego wariantu obecnego theme po wdrożeniu przeglądania mediów.
- Czy dodać MUI X dla DatePickerów i zaawansowanych tabel.
