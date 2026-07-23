# Demo Seed Test Design Brief

## Status dokumentu
- Status: draft
- Zakres: Flyway seed kont i danych demo do recznego klikania
- Ostatnia aktualizacja: 2026-07-21

## Wymaganie
- Seed demo ma byc wersjonowana migracja Flyway, a nie runtime runnerem aplikacji.
- Seed ma tworzyc przewidywalne konta `admin@example.com`, `owner@example.com`, `manager@example.com` i `guest-tester@example.com` z jednym haslem demo.
- Seed ma przygotowac wydarzenie, galerie publiczna `guest-uploads-demo-2945795a` z uploadem, manager membership, access code i deterministyczny token publiczny do recznego testowania.
- Seed ma bezpiecznie przejsc takze na bazie, w ktorej czesc danych demo istnieje juz po recznym klikaniu.
- Seed ma przywracac znany publiczny token demo na istniejacych wolumenach po manualnej rotacji, zeby smoke Docker i reczne klikanie byly powtarzalne.

## Macierz ryzyk

| Ryzyko | Bledna implementacja, ktora test ma wykryc | Scenariusze | Najnizsza wiarygodna warstwa |
| --- | --- | --- | --- |
| Seed nie jest czescia kontraktu migracji | Dane demo sa tworzone runtime runnerem zamiast Flyway | pelna migracja V1-V9 na H2 i MySQL | test kontraktu Flyway |
| Hasla demo sa zapisane jako plaintext w bazie | Seed przypisuje `password_hash=password123!` | pelna migracja, hashe kont i galerii | test kontraktu Flyway |
| Migracja koliduje z lokalnymi danymi | Seed bezwarunkowo insertuje duplikaty po e-mailu, slug albo tokenie | baza czysta, dane demo czesciowo istniejace | test kontraktu Flyway, review SQL |
| Manager nie ma dostepu | Seed tworzy usera, ale nie membership | event owner, manager membership aktywny | test kontraktu Flyway |
| Publiczny flow nie jest gotowy | Galeria nie ma tokenu, kodu albo upload flagi | public view, upload, access code, token hash | test kontraktu Flyway |
| Admin nie jest gotowy do recznego klikania | Migracja pomija konto admina albo nie ustawia roli `ADMIN` | pelna migracja, konto admina | test kontraktu Flyway |
| Docker zostawia dwa demo slugi po starszym seedzie | Znany token dziala na innym slugu niz adres uzywany do recznego klikania | migracja V8 kanonizuje slug do `guest-uploads-demo-2945795a` i przepina legacy media/access | test kontraktu Flyway, weryfikacja Docker/MySQL |
| Manualna rotacja uniewaznia demo smoke | Istniejacy wolumen ma znany token z `revoked_at`, a publiczny access zwraca 404 | migracja V9 podlacza token do kanonicznej galerii i ustawia `revoked_at = NULL` | test kontraktu Flyway, weryfikacja Docker/MySQL |
| Kontrakt legacy admina jest kruchy na nowe migracje | Test V5 liczy dokladna liczbe migracji po V4 i pada po dodaniu kolejnych seedow, mimo ze credential jest bezpiecznie zablokowany | upgrade z V4 do aktualnej wersji, co najmniej V5 wykonana, legacy admin nadal `BOOTSTRAP_DISABLED` i zablokowany do 2038 | test kontraktu Flyway |

## Weryfikacja
- Kontrakt Flyway H2: pelna migracja V1-V9, konta, role, galeria publiczna, access token hash.
- Kontrakt legacy admina: upgrade bazy z V4 do aktualnej wersji nie zalezy od dokladnej liczby pozniejszych migracji, ale nadal broni zablokowania znanego credentiala.
- Kontrakt Flyway MySQL: pelna migracja V1-V9 na produkcyjnym silniku, tabele, constraints i seed demo.
