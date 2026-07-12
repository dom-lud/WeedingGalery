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
- [ ] Przypadki brzegowe zostały przeanalizowane.
- [ ] Obsługa błędów jest zdefiniowana.

## Kryteria bezpieczeństwa i autoryzacji
- [ ] Ownership zasobów został sprawdzony.
- [ ] Autoryzacja endpointów i use case została sprawdzona.
- [ ] Brak nowych sekretów w repozytorium.
- [ ] Zmiana nie osłabia wymagań bezpieczeństwa.

## Kryteria danych i storage
- [ ] Wpływ na migracje został przeanalizowany.
- [ ] Limity i storage zostały uwzględnione.
- [ ] Retencja i usuwanie danych zostały uwzględnione, jeśli dotyczy.

## Kryteria testowe
- [ ] Dodano lub zaktualizowano testy adekwatne do zakresu.
- [ ] Build przechodzi.
- [ ] Lint przechodzi.
- [ ] Właściwe testy zostały uruchomione.

## Kryteria operacyjne
- [ ] Logowanie, monitoring lub audyt zostały uwzględnione, jeśli dotyczy.
- [ ] Konfiguracja środowiskowa została opisana.
- [ ] Wpływ na backup i operacje został oceniony, jeśli dotyczy.

## Kryteria dokumentacyjne
- [ ] Dokumentacja została zaktualizowana.
- [ ] Zmiana jest zgodna z ADR albo uzupełniono nowy ADR.
- [ ] Podsumowanie wykonanych działań wskazuje, co zostało zweryfikowane.

## Powiązane dokumenty
- [DEVELOPMENT_RULES.md](DEVELOPMENT_RULES.md)
- [testing/QUALITY_GATES.md](testing/QUALITY_GATES.md)
- [security/SECURITY_CHECKLIST.md](security/SECURITY_CHECKLIST.md)

## Decyzje otwarte
- Które elementy DoD będą automatycznie egzekwowane w CI w pierwszej iteracji.
