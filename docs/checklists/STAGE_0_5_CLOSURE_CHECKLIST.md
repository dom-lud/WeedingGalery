# Checklista domkniecia Etapow 0-5

## Status dokumentu
- Status: completed
- Zakres: hardening i regresja zakonczonych zakresow Etapow 0-5
- Ostatnia aktualizacja: 2026-07-21

## Zakres i proces
- [x] Zakres oraz poza zakresem zapisano w `../testing/STAGE_0_5_CLOSURE_TEST_BRIEF.md`.
- [x] Jawnie zdecydowano o pracy bez subagentow zgodnie z `../development/SUBAGENT_ORCHESTRATION.md`; zmiany migracji, konfiguracji, runtime i E2E wymagaly jednej zależnej petli.
- [x] Kontrakt HTTP nie zostal rozszerzony; sposob utworzenia pierwszego admina opisano w SSOT `../../api-contract/API_CONTRACT.md`.
- [x] Etapy 6+ oraz pelny hardening produkcyjny pozostaja poza tym zakresem.

## Bezpieczenstwo i dane
- [x] Historyczna V1 pozostala zgodna z istniejacymi bazami; nowa forward-only V5 neutralizuje znany credential.
- [x] Czysta migracja V1-V5 nie pozostawia uzytkownika testowego.
- [x] Upgrade V4-V5 zachowuje legacy admina z zaleznymi danymi, blokuje znany credential i pozwala na jednorazowa aktywacje bootstrapem.
- [x] Bootstrap jest opt-in, waliduje dane, nie promuje USER, nie tworzy drugiego admina i nie resetuje zwyklego admina przy replay.
- [x] Utworzenie lub aktywacja admina i wymagany `ADMIN_BOOTSTRAPPED` sa atomowe transakcyjnie.
- [x] Profil prod wymaga jawnych danych bazy i storage; nie ma produkcyjnych sekretow domyslnych.
- [x] Produkcyjne cookies sesji/CSRF maja `Secure`, `HttpOnly` tam, gdzie wymagane, i `SameSite=Lax`; CORS jest allowlista, a pusta konfiguracja oznacza same-origin.
- [x] Ownership, autoryzacja, idempotency, quota, kompensacja storage i public grant zostaly ponownie obronione pelnym backendem i E2E.

## Dowody testowe
- [x] Backend `clean verify`: 51 testow, 0 failures/errors, JaCoCo pass; lokalny Testcontainers zostal pominiety z powodu niekompatybilnego wykrywania Docker Desktop przez klienta Java.
- [x] Kontrakty Flyway H2: fresh V1-V5 oraz upgrade V4-V5 przechodza.
- [x] Realny MySQL 8.4 Compose: V5 `success=1`, zachowany wolumen, konto aktywowane, wymagany audyt zapisany, backend healthy.
- [x] Frontend Vitest: 24/24, progi V8 przechodza bez obnizania.
- [x] Frontend lint, Prettier na Windows i build przechodza.
- [x] Docker Compose config, build obrazow, health calego stacku i produkcyjny config contract przechodza.
- [x] Playwright: 15/15, w tym auth, ownership/membership, galerie, public upload, WCAG A/AA, klawiatura/focus i responsywnosc.

## Self-review i znalezione problemy
- [x] Niepoprawne zalozenie o usuwaniu seeda przez edycje V1 zostalo wycofane; dodano V5 i test upgrade.
- [x] H2 zaakceptowal rok 9999 dla `TIMESTAMP`, ale MySQL 8.4 go odrzucil; sentinel zmieniono na `2038-01-18` i ponownie zweryfikowano na realnym silniku.
- [x] V5 poczatkowo zależala od oryginalnego e-maila; selektor ograniczono do historycznego ID, roli i znanego hasha, a test obejmuje konto po zmianie e-maila.
- [x] Best-effort audit bootstrapu zastapiono wymaganym audytem w tej samej transakcji.
- [x] Test CORS i asercje flag cookies sprawdzaja rzeczywiste wartosci, nie tylko obecność naglowka/cookie.
- [x] Usunieto generator znanego hasha oraz usunieto znane haslo z historycznego credentialu po pelnej migracji.
- [x] Po poprawkach nie pozostaja findingi P0/P1 w zakresie closure sprintu.

## Jawnie odroczone
- Etap 6: media processing, background jobs, warianty obrazow i decyzja o pipeline.
- Etap 7+: publiczne listowanie/download mediow, moderacja, personalizacja i kolejne funkcje roadmapy.
- Etap 13: backup/restore drill, HTTPS rollout, alerting oraz testy obciazeniowe.
- Ostrzezenie builda o paczce frontendowej ok. 619 kB: code splitting jest długiem wydajnosciowym przed publicznym widokiem Etapu 7, nie blockerem zakresu 0-5.
- Lokalny skip Testcontainers na tym hoście: kontrakt pozostaje w repo/CI, a migracje zweryfikowano dodatkowo na działającym MySQL 8.4 Compose.
