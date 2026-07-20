# Testowanie Frontendu

## Cel dokumentu
Opisuje zakres testow komponentow, hookow, warstwy API i widokow.

## Status dokumentu
- Status: draft
- Zakres: testy frontendu dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Frontend nie posiada jeszcze docelowego zestawu testow.
- Repozytorium posiada podstawowy test smoke frontendu uruchamiany w CI.

## Stan docelowy
- Testy chronia krytyczne formularze, upload, routing i widoki zalezne od uprawnien.

## Zakres
- Testy komponentow
- Testy hookow
- Testy warstwy API
- Testy formularzy
- Testy galerii publicznej
- Testy uploadu i retry
- Testy stanow bledow i pustych danych
- Testy guardow widoku
- Testy lub manualna weryfikacja mobile-first dla krytycznych widokow
- Testy accessibility dla formularzy, modali, lightboxa i nawigacji

## Wzorzec dla Playwright E2E
- `npm run test:coverage` jest wymagane w CI.
- Minimalne progi startowe: statements 55%, branches 75%, functions 45%, lines 55%.
- Progi sa zapadka regresyjna; nie oznaczaja pelnego pokrycia i nie wolno ich
  obnizac bez udokumentowanego uzasadnienia.
- Krytyczne ekrany przechodza Playwright + axe dla WCAG A/AA. Test komponentu
  nadal sprawdza focus, keyboard, loading/error/retry i role zalezne od uprawnien.
- Scenariusze E2E powinny korzystac z page object pattern.
- Spec ma opisywac intencje biznesowe i oczekiwane zachowanie, a nie przechowywac selektory lub techniczne kroki UI.
- Selektory, akcje i asercje specyficzne dla widoku nalezy trzymac w klasach stron, np. `LoginPage`, `DashboardPage`.
- Jesli zmieni sie `id`, tekst przycisku albo struktura formularza, poprawka powinna zwykle byc potrzebna w jednym miejscu, a nie we wszystkich specach.
- Bezposrednie `page.click(...)`, `page.fill(...)` i podobne wywolania w plikach `*.spec.ts` nalezy ograniczac do wyjatkow, ktore sa jawnie uzasadnione przez nietypowy scenariusz testowy.
- Lokalnie testy Playwright powinny byc uruchamiane na przebudowanych komponentach dockerowych, a nie tylko na recznie odpalonych procesach dev. Standard: `backend\mvnw.cmd -DskipTests package`, potem `docker compose up --build`, dopiero nastepnie testy E2E.

## Powiazane dokumenty
- [TEST_STRATEGY.md](TEST_STRATEGY.md)
- [../frontend/UPLOAD_UX.md](../frontend/UPLOAD_UX.md)
- [../frontend/ACCESSIBILITY.md](../frontend/ACCESSIBILITY.md)
- [../frontend/CSS_AND_RESPONSIVE_GUIDELINES.md](../frontend/CSS_AND_RESPONSIVE_GUIDELINES.md)

## Decyzje otwarte
- Czy dodac wizualne snapshoty dla krytycznych widokow galerii.
