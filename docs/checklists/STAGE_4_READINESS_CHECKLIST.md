# Checklista Gotowosci Etapu 4

## Cel dokumentu
Potwierdza Definition of Ready i zamraza minimalny zakres pierwszego wejscia
do Etapu 4: `GALLERY-001`.

## Status dokumentu
- Status: completed (historyczna checklista gotowosci; Etap 4 jest zakonczony)
- Zakres: uwierzytelnione zarzadzanie galeriami w kontekscie wydarzenia
- Ostatnia aktualizacja: 2026-07-20

## Warunki wejscia
- [x] Etap 3 dostarcza event ownership i aktywne membership managera.
- [x] Konflikty merge zostaly rozwiazane i regresja Etapu 3 przechodzi.
- [x] Schemat Etapow 1-3 zostal skonsolidowany do jednego V1 i sprawdzony na pustej bazie.
- [x] `GALLERY-001` ma jawny zakres, poza zakresem i macierz uprawnien.
- [x] Planowany kontrakt API zostal zapisany w `api-contract/API_CONTRACT.md` przed kodem Etapu 4.
- [x] Nie jest wymagany nowy ADR dla uwierzytelnionego management API.
- [x] ADR 0010 pozostaje wymaganym blockerem dla `GALLERY-002` i `PUBLIC-001`.

## Zamrozony zakres GALLERY-001
- [ ] Lista wielu galerii danego wydarzenia.
- [ ] Utworzenie galerii z ownership wynikajacym wylacznie z eventu.
- [ ] Odczyt i edycja nazwy, opisu oraz `sortOrder`.
- [ ] Stabilny, globalnie unikalny slug generowany przez serwer i nieuzywany jako dowod dostepu.
- [ ] Status `ACTIVE` po utworzeniu, archiwizacja i soft delete.
- [ ] Optimistic version oraz indeksy pod scoped queries.
- [ ] Owner i aktywny manager: list/read/create/update.
- [ ] Tylko owner: archive/delete.
- [ ] Audit eventy wymagane w transakcji biznesowej.
- [ ] MUI-first, theme-first i mobile-first UI z loading/error/empty states.

## Poza zakresem
- [x] Publiczne endpointy galerii.
- [x] Tokeny, access code i publiczne granty.
- [x] QR.
- [x] Upload, download, media i storage.
- [x] Moderacja, cover media, personalizacja i statystyki.
- [x] Niejawny bypass ownership dla administratora.

## Wymagane scenariusze testowe przed kodem testow
- [ ] Owner tworzy wiele galerii i widzi stabilna kolejnosc.
- [ ] Manager tworzy i edytuje metadane, ale nie archiwizuje ani nie usuwa.
- [ ] Removed manager natychmiast traci dostep.
- [ ] Outsider, nieistniejaca galeria i soft-deleted galeria maja ten sam `404`.
- [ ] `eventId A + galleryId B` nie pozwala na cross-event IDOR.
- [ ] Brak sesji daje `401`, a brak CSRF blokuje mutacje.
- [ ] Slug jest generowany po stronie serwera, unikalny i nie jest ponownie uzywany po soft delete.
- [ ] Konflikt optimistic locking zwraca stabilny `409`.
- [ ] Awaria wymaganego audytu wycofuje mutacje.
- [ ] Playwright potwierdza owner/manager/removed-manager na przebudowanym Docker Compose.

## Gate zakonczenia
- [ ] Backend unit i integracja z realna sesja oraz CSRF.
- [ ] Frontend lint, unit i build.
- [ ] Osobny Playwright E2E przez root Nginx.
- [ ] Regresja Etapu 3.
- [ ] Package, Docker build i smoke test wykonane w kolejnosci z TEST_STRATEGY.md.
- [ ] Self-review security/API/migracji/UI powtarzany do braku findings.
- [ ] Dokumentacja i completion checklist zaktualizowane na podstawie wynikow, nie planu.

## Powiazane dokumenty
- [../development/DEFINITION_OF_READY.md](../development/DEFINITION_OF_READY.md)
- [../DEFINITION_OF_DONE.md](../DEFINITION_OF_DONE.md)
- [../product/BACKLOG.md](../product/BACKLOG.md)
- [../product/FEATURE_ROADMAP.md](../product/FEATURE_ROADMAP.md)
- [../../api-contract/API_CONTRACT.md](../../api-contract/API_CONTRACT.md)
- [../adr/0010-gallery-access-strategy.md](../adr/0010-gallery-access-strategy.md)
