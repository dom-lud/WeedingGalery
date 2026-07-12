# CSS i Responsywność

## Cel dokumentu
Opisuje szczegółowe zasady implementacji frontendu mobile-first, CSS, layoutu, React i TypeScript.

## Status dokumentu
- Status: draft
- Zakres: mobile-first, breakpointy, Flexbox, CSS Grid, CSS, React, TypeScript, stany UI i accessibility
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Frontend ma szkic React/Vite.
- Docelowe layouty, komponenty domenowe i testy responsywności nie są jeszcze wdrożone.

## Stan docelowy
- Każdy istotny widok jest projektowany od małego ekranu, rozszerzany przez breakpointy i budowany zgodnie z MUI-first.
- UI działa poprawnie na telefonie, tablecie, laptopie i dużym ekranie bez poziomego scrollowania.

## Mobile-first
Interfejs musi być projektowany i implementowany od najmniejszego wspieranego widoku.

Zasady:
- podstawowe style dotyczą urządzeń mobilnych,
- media queries rozszerzają układ na większe ekrany,
- nie buduj najpierw desktopu i później nie naprawiaj telefonu,
- kluczowe flow musi być w pełni dostępne na telefonie,
- upload zdjęć i filmów jest przede wszystkim flow mobilnym,
- elementy dotykowe muszą być odpowiednio duże,
- nie polegaj na hover,
- formularze powinny działać z klawiaturą ekranową,
- uwzględniaj safe areas,
- unikaj poziomego scrollowania,
- testuj pionową i poziomą orientację,
- testuj długie nazwy plików i komunikaty.

## Breakpointy
- Nie definiuj breakpointów na podstawie konkretnych modeli telefonów.
- Breakpoint powinien wynikać z miejsca, w którym układ przestaje działać.
- Używaj breakpointów z theme MUI, jeśli nie ma uzasadnionej potrzeby inaczej.
- Ogranicz liczbę breakpointów; zbyt wiele wariantów zwiększa koszt utrzymania.
- Jeżeli projekt przyjmuje własny zestaw breakpointów w theme, musi być udokumentowany w [UI_SYSTEM.md](UI_SYSTEM.md).

## Flexbox
Stosuj Flexbox do układów jednowymiarowych:
- rząd lub kolumna,
- nawigacja,
- toolbar,
- grupa przycisków,
- rozmieszczenie elementów formularza,
- wyrównanie elementów komponentu.

W MUI preferuj `Stack` albo `Box` z `display: flex`.

## CSS Grid
Stosuj CSS Grid do układów dwuwymiarowych:
- galeria zdjęć,
- dashboard,
- układ kart,
- formularze wymagające kontroli wierszy i kolumn,
- layout strony na większych ekranach.

W MUI preferuj `Grid` albo `Box` z `display: grid`.

## Łączenie Grid i Flexbox
Można używać obu technologii:
- Grid do głównego układu,
- Flexbox lub `Stack` wewnątrz komponentów.

Nie wybieraj jednej technologii dla całej aplikacji. Wybieraj narzędzie wynikające z charakteru konkretnego układu.

## CSS
Zasady:
- Preferuj nowoczesny CSS.
- Używaj `rem`, `%`, `min()`, `max()`, `clamp()` tam, gdzie jest to uzasadnione.
- Unikaj sztywnych szerokości.
- Stosuj `max-width` dla treści.
- Używaj responsywnych obrazów.
- Stosuj `object-fit` dla mediów.
- Zachowuj poprawne proporcje mediów przez `aspect-ratio` albo kontrolowane kontenery.
- Nie dodawaj globalnych stylów łamiących komponenty.
- Stosuj spójny system spacingu.
- Używaj tokenów dla kolorów, typografii i odstępów.
- Ograniczaj specyficzność selektorów.
- Nie używaj `!important` bez uzasadnienia.
- Nie używaj inline styles dla stałych stylów.
- Obsługuj `prefers-reduced-motion`.
- Zapewnij czytelny focus state.
- Sprawdzaj kontrast.
- Sprawdzaj tryb wysokiego powiększenia.

Własny CSS jest wyjątkiem wobec MUI-first i powinien być uzasadniony zgodnie z [UI_SYSTEM.md](UI_SYSTEM.md).

## React i TypeScript
Zasady:
- TypeScript strict jest wymagany.
- Dane API mają jawne typy.
- Warstwa API jest poza komponentami.
- Nie wykonuj bezpośrednich requestów w wielu komponentach.
- Komponenty powinny być małe i czytelne.
- Stosuj composition over inheritance.
- Używaj custom hooks dla współdzielonej logiki.
- Nie nadużywaj globalnego state.
- Oddziel server state od local UI state.
- Nie przechowuj wartości pochodnych w state bez potrzeby.
- Formularze muszą mieć walidację.
- Komunikaty błędów muszą być kontrolowane.
- Używaj error boundaries dla odpowiednich obszarów.
- Stosuj lazy loading tras i ciężkich komponentów.
- Optymalizuj dopiero na podstawie rzeczywistej potrzeby.
- Unikaj bezrefleksyjnego `useMemo` i `useCallback`.

## Stany interfejsu
Każdy ekran pobierający dane powinien uwzględniać:
- initial state,
- loading state,
- success state,
- empty state,
- partial state,
- error state,
- retry state,
- unauthorized state,
- forbidden state,
- offline lub interrupted state, jeśli dotyczy.

Stany nie mogą być traktowane jako dopisek po implementacji happy path. Są częścią projektu widoku.

## Accessibility
Uwzględniaj:
- semantyczny HTML,
- obsługę klawiatury,
- etykiety formularzy,
- opisy błędów,
- focus management,
- aria tylko tam, gdzie semantyczny HTML nie wystarcza,
- dostępny modal i lightbox,
- alt text,
- napisy lub opis dla mediów, jeśli będą dostępne,
- WCAG 2.2 AA jako cel projektu.

## Weryfikacja responsywności
Każda istotna funkcja frontendowa powinna być sprawdzona co najmniej dla:
- małego telefonu,
- standardowego telefonu,
- tabletu,
- laptopa,
- dużego ekranu.

Nie wymagaj konkretnego urządzenia. Sprawdzaj zachowanie układu przy różnych szerokościach.

## Checklista mobile-first
- [ ] Najważniejsza akcja użytkownika jest dostępna na małym ekranie.
- [ ] Bazowy layout działa bez breakpointów desktopowych.
- [ ] Nie ma poziomego scrolla.
- [ ] Przyciski i pola formularzy mają wygodny obszar dotykowy.
- [ ] Formularz działa z klawiaturą ekranową.
- [ ] Długie nazwy plików, komunikaty i etykiety nie łamią layoutu.
- [ ] Orientacja pionowa i pozioma zostały sprawdzone.
- [ ] Breakpointy wynikają z układu, nie z modelu urządzenia.
- [ ] Loading, empty, error i retry states działają na telefonie.
- [ ] Focus state i obsługa klawiatury są poprawne.
- [ ] Motion uwzględnia `prefers-reduced-motion`.

## Powiązane dokumenty
- [FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md)
- [RESPONSIVE_DESIGN.md](RESPONSIVE_DESIGN.md)
- [UI_SYSTEM.md](UI_SYSTEM.md)
- [ACCESSIBILITY.md](ACCESSIBILITY.md)
- [UPLOAD_UX.md](UPLOAD_UX.md)
- [../testing/FRONTEND_TESTING.md](../testing/FRONTEND_TESTING.md)

## Decyzje otwarte
- Docelowy zestaw breakpointów, jeśli domyślne breakpointy MUI okażą się niewystarczające.
