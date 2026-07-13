# Definition of Done

## Cel dokumentu
Definiuje kompletne kryteria ukonczenia zadania dla zmian w projekcie.

## Status dokumentu
- Status: draft
- Zakres: DoD dla stanu docelowego procesu wytworczego
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Czesc kryteriow nie moze byc jeszcze wykonywana automatycznie, ale nadal pozostaje wymagana projektowo.

## Stan docelowy
- Kazde zadanie konczy sie zmiana spojna technicznie, testowo i dokumentacyjnie.

## Kryteria funkcjonalne
- [ ] Zakres funkcjonalny jest ukonczony i spojny z wymaganiami.
- [ ] Kryteria akceptacji zostaly spelnione.
- [ ] Przypadki brzegowe zostaly przeanalizowane.
- [ ] Obsluga bledow jest zdefiniowana.
- [ ] Nieweryfikowane obszary zostaly jawnie opisane.
- [ ] Na koncu pracy wykonano jawne sprawdzenie wymagan punkt po punkcie.

## Kryteria bezpieczenstwa i autoryzacji
- [ ] Ownership zasobow zostal sprawdzony.
- [ ] Autoryzacja endpointow i use case zostala sprawdzona.
- [ ] Testy bezpieczenstwa lub security review zostaly wykonane, jesli zakres tego wymaga.
- [ ] Brak nowych sekretow w repozytorium.
- [ ] Zmiana nie oslabi wymagan bezpieczenstwa.

## Kryteria danych i storage
- [ ] Wplyw na migracje zostal przeanalizowany.
- [ ] Nowe migracje zostaly dodane zamiast modyfikowania starych, jesli dotyczy.
- [ ] Limity i storage zostaly uwzglednione.
- [ ] Retencja i usuwanie danych zostaly uwzglednione, jesli dotyczy.

## Kryteria testowe
- [ ] Dodano lub zaktualizowano testy adekwatne do zakresu.
- [ ] Testy wynikaja z wymagan, kontraktu, regresji i ryzyk, a nie z dopasowania do aktualnej implementacji.
- [ ] Testy obejmuja co najmniej jedna probe obalenia blednego zachowania, jesli zakres zmiany to uzasadnia.
- [ ] Testy sa projektowane niezaleznie od kodu i maja realna szanse wykryc bug, a nie tylko potwierdzic obecny sposob implementacji.
- [ ] Dla krytycznych flow dodano warianty negatywne, brzegowe albo nieidealne zachowania uzytkownika, jesli takie ryzyko istnieje.
- [ ] Build przechodzi.
- [ ] Lint przechodzi.
- [ ] Testy integracyjne zostaly uruchomione, jesli dotyczy.
- [ ] Wlasciwe testy zostaly uruchomione.
- [ ] Smoke test zostal wykonany, jesli zmiana wplywa na przeplyw uzytkownika lub wdrozenie.

## Kryteria operacyjne
- [ ] Logowanie, monitoring lub audyt zostaly uwzglednione, jesli dotyczy.
- [ ] Konfiguracja srodowiskowa zostala opisana.
- [ ] Monitoring zostal zaktualizowany, jesli zmiana wplywa na obserwowalnosc.
- [ ] Wplyw na backup i operacje zostal oceniony, jesli dotyczy.

## Kryteria dokumentacyjne
- [ ] Dokumentacja zostala zaktualizowana.
- [ ] Zmiana jest zgodna z ADR albo uzupelniono nowy lub zaktualizowany ADR.
- [ ] Self-review zostal wykonany po implementacji i przed finalnym podsumowaniem.
- [ ] Code review zostal wykonany.
- [ ] Podsumowanie wykonanych dzialan wskazuje, co zostalo zweryfikowane.

## Zgodnosc z checklistami
- [ ] Wykonano wlasciwe checklisty dla feature, review, bazy, API, bezpieczenstwa lub releasu, jesli dotyczy.

## Powiazane dokumenty
- [DEVELOPMENT_RULES.md](DEVELOPMENT_RULES.md)
- [development/DEFINITION_OF_READY.md](development/DEFINITION_OF_READY.md)
- [testing/QUALITY_GATES.md](testing/QUALITY_GATES.md)
- [security/SECURITY_CHECKLIST.md](security/SECURITY_CHECKLIST.md)
- [checklists/FEATURE_CHECKLIST.md](checklists/FEATURE_CHECKLIST.md)

## Decyzje otwarte
- Ktore elementy DoD beda automatycznie egzekwowane w CI w pierwszej iteracji.
