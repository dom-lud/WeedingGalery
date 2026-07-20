# Macierz testow Etapow 4, 4B i 5

## Cel
Powiazac wymagania `GALLERY-001`, `GALLERY-002`, `PUBLIC-001`, `UPLOAD-001`,
minimalnego `UPLOAD-002` i uploadowej czesci `STORAGE-001` z testami, ktore
potrafia wykryc bledna implementacje.

## Zasada oceny
- Zielony zestaw testow potwierdza tylko opisane scenariusze; nie oznacza pelnego pokrycia.
- Kazda regula security, ownership, limitu, idempotencji lub kompensacji wymaga
  przynajmniej jednego scenariusza negatywnego.
- Krytyczny kontrakt API jest broniony testem integracyjnym; E2E pozostaje
  testem przeplywu uzytkownika, a nie zamiennikiem testow API.
- Dlugie testy typu "caly etap" nalezy dzielic, gdy awaria jednej asercji ukrywa
  wynik niezaleznych regul.

## Macierz

| Obszar | Scenariusze obowiazkowe | Minimalny poziom |
| --- | --- | --- |
| GALLERY-001 | owner/manager CRUD, outsider i cross-event IDOR, CSRF, lifecycle owner-only, soft delete, stabilny slug, kolejnosc, granice walidacji, audit fail-closed | unit + API integration + E2E smoke |
| GALLERY-002 | manager read-only, owner mutations, token wymagany do publikacji, upload wymaga public view, okno publikacji, stale version, brak sekretow w odpowiedzi | unit + API integration |
| PUBLIC-001 | poprawny token/kod, brak i bledny kod, prywatna/przyszla/wygasla/zarchiwizowana/usunieta galeria jako ten sam `404`, rotacja uniewaznia grant, rate limit | API integration + E2E krytycznego wejscia |
| UPLOAD-001 | wiele plikow, partial failure, retry, cancel, replay bez duplikatu, MIME/extension/magic/size mismatch, puste i zabronione typy | unit + API integration + E2E |
| UPLOAD-002 | wymagany i walidowany idempotency key, ten sam manifest zwraca ten sam zasob, konflikt manifestu, izolacja grantu, expiry, maks. aktywnych sesji | unit + API integration |
| STORAGE-001 | losowy server key, traversal i symlink, brak overwrite, quota, write failure, kompensacja, `CLEANUP_REQUIRED`, brak sciezek fizycznych w API | unit + integration |
| UI | loading/empty/error/retry/forbidden, focus po dialogu, keyboard flow, 360/390/1024 px, brak overflow, automatyczny audit accessibility krytycznych ekranow | component + Playwright |
| Runtime danych | migracje na rzeczywistym MySQL i zgodnosc constraints/indeksow | Testcontainers integration |

## Dowody i braki
Przy domykaniu etapu raportuj:
1. liczbe testow wedlug warstwy,
2. uruchomione komendy i wynik,
3. mapowanie nowych testow na wiersze tej macierzy,
4. pokrycie krytycznych pakietow, jezeli jest mierzone,
5. jawna liste nieweryfikowanych scenariuszy i powod.

