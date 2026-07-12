# Frontend Feature

## Cel dokumentu
Skill wspierający implementację funkcji frontendowej z naciskiem na mobile-first i jakość stanów UI.

## Status dokumentu
- Status: draft
- Zakres: frontend feature skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Skill opisuje sposób pracy nad UI i warstwą API po stronie frontendu.

## Stan docelowy
- Agent używa go przy nowych widokach, komponentach i integracjach z REST API.

## Kiedy używać
- Przy dodawaniu widoku, komponentu, routingu, formularza lub integracji z API.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/development/WORKFLOW.md`
- `docs/frontend/FRONTEND_ARCHITECTURE.md`
- `docs/frontend/UI_SYSTEM.md`
- `docs/frontend/STATE_MANAGEMENT.md`
- `docs/frontend/ACCESSIBILITY.md`
- `docs/frontend/RESPONSIVE_DESIGN.md`

## Obowiązkowa strategia UI
- Użyj MUI jako domyślnej biblioteki.
- Nie twórz komponentu od zera, zanim nie sprawdzisz odpowiednika w MUI.
- Najpierw spróbuj konfiguracji props.
- Następnie `sx`.
- Następnie `styled()`.
- Następnie własny komponent domenowy oparty na MUI.
- Własny CSS stosuj wyłącznie jako wyjątek.
- Nie dodawaj drugiej biblioteki UI.
- Używaj theme zamiast wartości hardcoded.
- Implementuj mobile-first.
- Używaj `Stack` albo Flexbox dla układów jednowymiarowych.
- Używaj `Grid` albo CSS Grid dla układów dwuwymiarowych.
- Sprawdź loading, empty, error, retry, forbidden i offline states.
- Sprawdź accessibility i focus.

## Wymagane kroki
1. Zidentyfikuj trasę, widoki i komponenty.
2. Dobierz komponenty MUI i potrzebne komponenty domenowe.
3. Ustal warstwę API i kontrakty danych.
4. Zaprojektuj loading, error, empty, retry, forbidden i offline state.
5. Sprawdź accessibility i responsywność.
6. Dodaj testy komponentów lub widoków.
7. Zaktualizuj dokumentację UI, jeśli zmiana wpływa na UX.

## Checklista
- [ ] TypeScript strict został zachowany.
- [ ] MUI zostało użyte jako domyślna biblioteka UI.
- [ ] Nie dodano drugiej biblioteki UI.
- [ ] Theme zostało użyte zamiast wartości hardcoded, jeśli to możliwe.
- [ ] Podejście mobile-first zostało uwzględnione.
- [ ] Warstwa API jest wydzielona.
- [ ] Loading state istnieje.
- [ ] Error state istnieje.
- [ ] Empty state istnieje, jeśli dotyczy.
- [ ] Retry, forbidden i offline state istnieją, jeśli dotyczą przepływu.
- [ ] Accessibility została sprawdzona.
- [ ] Responsywność została sprawdzona.
- [ ] Testy zostały dodane lub zaktualizowane.

## Zakazane działania
- Bezpośredni `fetch` w wielu komponentach bez wspólnej warstwy API.
- Pomijanie stanów błędów.
- Wstrzykiwanie niebezpiecznego HTML.
- Desktop-first redesign bez uzasadnienia.

## Oczekiwany format wyniku
- widoki i komponenty,
- stany UI,
- integracja z API,
- testy,
- dokumentacja,
- ryzyka.

## Decyzje otwarte
- Czy dodać osobny wariant skill dla publicznej galerii.
