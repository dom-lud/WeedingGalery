# Definition of Done

## Cel dokumentu
Definiuje kompletne kryteria ukończenia zadania dla zmian w projekcie.

## Status dokumentu
- Status: draft
- Zakres: DoD dla stanu docelowego procesu wytwórczego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Część kryteriów nie może być jeszcze wykonywana automatycznie, ale nadal pozostaje wymagana projektowo.

## Stan docelowy
- Każde zadanie kończy się zmianą spójną technicznie, testowo i dokumentacyjnie.

## Kryteria funkcjonalne
- [ ] Zakres funkcjonalny jest ukończony i spójny z wymaganiami.
- [ ] Kryteria akceptacji zostały spełnione.
- [ ] Przypadki brzegowe zostały przeanalizowane.
- [ ] Obsługa błędów jest zdefiniowana.
- [ ] Nieweryfikowane obszary zostały jawnie opisane.
- [ ] Na końcu pracy wykonano jawne sprawdzenie wymagań punkt po punkcie.

## Kryteria bezpieczeństwa i autoryzacji
- [ ] Ownership zasobów został sprawdzony.
- [ ] Autoryzacja endpointów i use case została sprawdzona.
- [ ] Testy bezpieczeństwa lub security review zostały wykonane, jeśli zakres tego wymaga.
- [ ] Brak nowych sekretów w repozytorium.
- [ ] Zmiana nie osłabia wymagań bezpieczeństwa.

## Kryteria danych i storage
- [ ] Wpływ na migracje został przeanalizowany.
- [ ] Nowe migracje zostały dodane zamiast modyfikowania starych, jeśli dotyczy.
- [ ] Limity i storage zostały uwzględnione.
- [ ] Retencja i usuwanie danych zostały uwzględnione, jeśli dotyczy.

## Kryteria testowe
- [ ] Dodano lub zaktualizowano testy adekwatne do zakresu.
- [ ] Testy wynikają z wymagań, kontraktu, regresji i ryzyk, a nie z dopasowania do aktualnej implementacji.
- [ ] Testy obejmują co najmniej jedną próbę obalenia błędnego zachowania, jeśli zakres zmiany to uzasadnia.
- [ ] Build przechodzi.
- [ ] Lint przechodzi.
- [ ] Testy integracyjne zostały uruchomione, jeśli dotyczy.
- [ ] Właściwe testy zostały uruchomione.
- [ ] Smoke test został wykonany, jeśli zmiana wpływa na przepływ użytkownika lub wdrożenie.

## Kryteria operacyjne
- [ ] Logowanie, monitoring lub audyt zostały uwzględnione, jeśli dotyczy.
- [ ] Konfiguracja środowiskowa została opisana.
- [ ] Monitoring został zaktualizowany, jeśli zmiana wpływa na obserwowalność.
- [ ] Wpływ na backup i operacje został oceniony, jeśli dotyczy.

## Kryteria dokumentacyjne
- [ ] Dokumentacja została zaktualizowana.
- [ ] Zmiana jest zgodna z ADR albo uzupełniono nowy lub zaktualizowany ADR.
- [ ] Self-review został wykonany po implementacji i przed finalnym podsumowaniem.
- [ ] Code review został wykonany.
- [ ] Podsumowanie wykonanych działań wskazuje, co zostało zweryfikowane.

## Zgodność z checklistami
- [ ] Wykonano właściwe checklisty dla feature, review, bazy, API, bezpieczeństwa lub releasu, jeśli dotyczy.

## Powiązane dokumenty
- [DEVELOPMENT_RULES.md](DEVELOPMENT_RULES.md)
- [development/DEFINITION_OF_READY.md](development/DEFINITION_OF_READY.md)
- [testing/QUALITY_GATES.md](testing/QUALITY_GATES.md)
- [security/SECURITY_CHECKLIST.md](security/SECURITY_CHECKLIST.md)
- [checklists/FEATURE_CHECKLIST.md](checklists/FEATURE_CHECKLIST.md)

## Decyzje otwarte
- Które elementy DoD będą automatycznie egzekwowane w CI w pierwszej iteracji.
