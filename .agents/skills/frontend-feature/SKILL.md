---
name: frontend-feature
description: Implementacja i audyt funkcji frontendowych WeedingGallery, w tym widoki, komponenty, formularze, routing i integracje REST z mobile-first, accessibility oraz rygorystycznymi testami zachowania i coverage.
---

# Frontend Feature

## Obowiązkowy standard testów i coverage

- Przed testami zapisz Test Design Brief z ryzykami stanów, ról, kontraktu, race conditions i dostępności.
- Testuj zachowanie komponentów i kontrakt wrapperów API; nie mockuj zachowania będącego przedmiotem testu.
- Przejrzyj raport globalnie, per zmieniony plik oraz po niepokrytych gałęziach.
- Utrzymaj blokujące bramki `90%` dla statements, branches, functions i lines. Pokryj klawiaturę/focus/responsywność oraz stany empty/loading/success/error/retry, anulowanie, wielokrotne kliknięcie, utratę sieci, odświeżenie i granice danych; sam wynik coverage nie zamyka zadania.
- Krytyczny flow musi przejść E2E, automatyczny audit WCAG A/AA, klawiaturę, focus i responsywność.
- Nie obniżaj progów, nie wyłączaj sztucznie plików i nie dodawaj testów bez istotnych asercji.

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
- `docs/frontend/CSS_AND_RESPONSIVE_GUIDELINES.md`
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
1. Najpierw zaprojektuj flow mobilny.
2. Określ najważniejszą akcję użytkownika.
3. Zidentyfikuj trasę, widoki i komponenty.
4. Dobierz komponenty MUI i potrzebne komponenty domenowe.
5. Ustal warstwę API i kontrakty danych.
6. Zaimplementuj bazowy układ dla małego ekranu.
7. Wybierz Flexbox lub Grid na podstawie charakteru układu.
8. Dodaj rozszerzenia dla większych ekranów.
9. Dodaj loading, empty, error i retry states oraz forbidden/offline, jeśli dotyczą przepływu.
10. Sprawdź accessibility i focus.
11. Sprawdź brak poziomego scrolla.
12. Uruchom testy.
13. Wykonaj weryfikację responsywności.
14. Zaktualizuj dokumentację UI, jeśli zmiana wpływa na UX.

## Checklista
- [ ] TypeScript strict został zachowany.
- [ ] MUI zostało użyte jako domyślna biblioteka UI.
- [ ] Nie dodano drugiej biblioteki UI.
- [ ] Theme zostało użyte zamiast wartości hardcoded, jeśli to możliwe.
- [ ] Podejście mobile-first zostało uwzględnione.
- [ ] Bazowy układ działa na małym ekranie.
- [ ] Najważniejsza akcja użytkownika jest wygodna dotykowo.
- [ ] Wybrano Flexbox lub Grid na podstawie układu, a nie preferencji.
- [ ] Dodano rozszerzenia dla większych ekranów przez breakpointy.
- [ ] Warstwa API jest wydzielona.
- [ ] Loading state istnieje.
- [ ] Error state istnieje.
- [ ] Empty state istnieje, jeśli dotyczy.
- [ ] Retry, forbidden i offline state istnieją, jeśli dotyczą przepływu.
- [ ] Accessibility została sprawdzona.
- [ ] Responsywność została sprawdzona.
- [ ] Brak poziomego scrolla został sprawdzony.
- [ ] Długie nazwy plików, etykiety i komunikaty nie łamią layoutu.
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
