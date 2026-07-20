# Checklist Funkcji

## Cel dokumentu
Lista kontrolna dla realizacji funkcji i większych zmian technicznych.

## Status dokumentu
- Status: draft
- Zakres: feature checklist
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Checklisty są wykonywane ręcznie.

## Stan docelowy
- Każda funkcja przechodzi przez checklistę przed uznaniem za zakończoną.

## Lista kontrolna
- [ ] Wymagania i kryteria akceptacji są jasne.
- [ ] Zakres i poza zakresem są opisane.
- [ ] Jawnie zdecydowano, czy zadanie wymaga subagentów.
- [ ] Autoryzacja została sprawdzona.
- [ ] Ownership zasobów został sprawdzony.
- [ ] Walidacja wejścia została uwzględniona.
- [ ] Obsługa błędów została zdefiniowana.
- [ ] Testy zostały dodane lub zaktualizowane.
- [ ] Przed implementacją testów powstał test design brief i macierz wymaganie/ryzyko -> błędne zachowanie -> scenariusz -> warstwa testu.
- [ ] Testy nie są „pod kod”, tylko pod wymagania, kontrakt i regresje.
- [ ] Istotne ryzyka mają wariant pozytywny, negatywny i graniczny; limity obejmują `N-1`, `N`, `N+1`.
- [ ] Ownership obejmuje dostęp dozwolony i zabroniony, a idempotency replay i konflikt, jeśli dotyczy.
- [ ] Integracje zależne od silnika bazy lub storage zostały sprawdzone na realnej zależności, jeśli H2/mock nie jest równoważny.
- [ ] Krytyczne ekrany przeszły automatyczny audit accessibility oraz test klawiatury/focusu, jeśli dotyczy.
- [ ] Progi coverage przeszły i nie zostały obniżone bez udokumentowanego uzasadnienia.
- [ ] Po uruchomieniu testów porównano wynik z macierzą i jawnie opisano nieweryfikowane obszary.
- [ ] Wykonano self-review i poprawiono wykryte problemy.
- [ ] Dokumentacja została zaktualizowana.
- [ ] Monitoring, logowanie lub audyt zostały uwzględnione, jeśli dotyczy.
- [ ] Migracje zostały przeanalizowane, jeśli dotyczy.
- [ ] Storage i limity zostały przeanalizowane, jeśli dotyczy.
- [ ] Brak sekretów w repozytorium.
- [ ] Na końcu sprawdzono spełnienie wymagań i DoD punkt po punkcie.

## Powiązane dokumenty
- [../development/WORKFLOW.md](../development/WORKFLOW.md)
- [../DEFINITION_OF_DONE.md](../DEFINITION_OF_DONE.md)
- Wykonanie dla Etapu 3: [STAGE_3_COMPLETION_CHECKLIST.md](STAGE_3_COMPLETION_CHECKLIST.md)
- Wykonanie dla Etapu 4: [STAGE_4_COMPLETION_CHECKLIST.md](STAGE_4_COMPLETION_CHECKLIST.md)

## Decyzje otwarte
- Czy część punktów będzie automatycznie weryfikowana w CI.
