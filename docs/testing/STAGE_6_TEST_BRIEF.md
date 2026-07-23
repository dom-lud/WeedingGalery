# Etap 6 - Test Design Brief

## Status dokumentu
- Status: completed-first-iteration
- Zakres: `MEDIA-001`, background jobs i processing mediow
- Ostatnia aktualizacja: 2026-07-23

## Zakres
- Trwaly `MediaProcessingJob` tworzony po udanym uploadzie.
- Worker jobow z retry, idempotencja, blokowaniem rownoleglego wykonania i widocznym statusem.
- Miniatury/metadane dla obrazow obslugiwanych przez runtime `ImageIO`; w bazowej konfiguracji gwarantowane JPEG/PNG, WebP jako przewidywalny unsupported processing bez dodatkowego providera.
- Bezpieczna obsluga MP4 bez transkodowania/preview w pierwszej iteracji.
- Aktualizacja statusow pliku: `PROCESSING`, `PROCESSED`, `PROCESSING_FAILED`.

## Poza zakresem
- Publiczne listowanie mediow i lightbox.
- Download, streaming i signed links.
- Moderacja, bulk actions i admin retry endpoint.
- Pelne preview/transkodowanie wideo.

## Macierz ryzyk

| Wymaganie / ryzyko | Bledna implementacja, ktora test ma wykryc | Scenariusze | Najnizsza wiarygodna warstwa |
| --- | --- | --- | --- |
| Job powstaje po udanym zapisie oryginalu | Upload konczy sie `STORED`, ale po restarcie nie ma zadania processingu | sukces uploadu, replay uploadu, konflikt idempotency key | integracyjny backend z baza |
| Job jest idempotentny | Replay albo restart tworzy duplikaty jobow lub miniaturek | pierwszy zapis, replay, rownolegly retry | integracyjny backend z MySQL |
| Worker claimuje job atomowo | Dwa workery przetwarzaja ten sam plik i nadpisuja warianty | dwa rownolegle claimy, porzucony lock, retry locka | integracyjny backend z MySQL |
| Retry tylko dla bledow przejsciowych | Blad walidacyjny jest retryowany w petli albo transient konczy sie trwale za szybko | transient N-1/N/N+1 prob, permanent failure, retry exhausted | unit + integracyjny service |
| Oryginal pozostaje zachowany | Blad miniatury usuwa oryginal albo psuje `storage_key` | blad zapisu thumbnaila, blad metadanych, cleanup po awarii | integracyjny storage/service |
| Wariant thumbnaila nie przecieka sciezek | API lub log zwraca fizyczna sciezke storage | odpowiedz upload session, blad processingu, log/audit context | integracyjny API + review logowania |
| Status pliku jest spojny | Plik zostaje na `PROCESSING` po sukcesie albo pokazuje `PROCESSED` mimo bledu | sukces, blad permanentny, retry exhausted | unit + integracyjny API |
| Wynik processingu zostawia slad audytowy | Worker konczy job bez `MEDIA_PROCESSING_SUCCEEDED`/`MEDIA_PROCESSING_FAILED` albo audyt terminalnej porazki nie zawiera stabilnego kodu | sukces, permanent failure, retry exhausted, retry przejsciowy bez eventu terminalnego | unit worker + integracyjny audit |
| Retry nie zalewa audytu biznesowego | Kazda proba transient zapisuje audit event i zaciemnia rzeczywisty wynik | N-1/N/N+1 prob, przejscie `RETRY_SCHEDULED` bez eventu terminalnego | unit worker |
| MP4 bez preview jest przewidywalne | Worker probuje transkodowac MP4 i blokuje CPU lub status nigdy sie nie konczy | MP4 valid, MP4 unsupported codec jesli wykrywany, blad metadanych | service/integracyjny |
| Limity kosztownych operacji | Wiele obrazow uruchamia dekodery bez ograniczenia wspolbieznosci | N-1/N/N+1 limitu workerow/dekoderow | unit polityki + integracyjny worker |
| Ownership nie jest obchodzone | Status joba lub diagnostyka ujawnia cudze media | owner/manager dozwolony, outsider zabroniony, guest grant scoped | integracyjny API jesli endpoint powstaje |
| Migracje sa zgodne z MySQL | Schemat dziala na H2, ale nie na MySQL lock/indeksy/enumy | fresh migration, upgrade V5->V6, constraints i indeksy | test migracji MySQL |
| E2E upload nadal dziala | Front widzi nieznany status i psuje kolejke uploadu | guest upload PNG, polling session do `PROCESSED` albo kontrolowanego `PROCESSING` | Playwright E2E |
| Accessibility statusow UI | Nowy status jest niewidoczny dla czytnika lub focus sie gubi | loading/status/error/retry, keyboard/focus, mobile | frontend component + Playwright axe |

## Minimalny zestaw testow
- Unit: polityka retry/backoff, klasyfikacja bledow, przejscia statusow joba i pliku.
- Unit: audyt systemowy sukcesu i terminalnej porazki processingu bez audytu dla samego retry.
- Integracyjne backend: upload tworzy job, worker przetwarza obraz, replay nie duplikuje, retry N-1/N/N+1, storage failure i brak orphanow.
- Migracje: fresh i upgrade na MySQL-compatible runtime; jesli lock workerow zalezy od MySQL, test na realnym MySQL.
- Frontend: statusy `PROCESSING`, `PROCESSED`, `PROCESSING_FAILED` w kolejce uploadu, error/retry/empty, brak regresji publicznego entrypointu.
- E2E: owner publikuje galerie, guest uzyskuje grant, uploaduje PNG, a system pokazuje wynik processingu bez ujawniania sciezek.

## Weryfikacja koncowa Etapu 6
- Backend targeted tests dla media/job/upload.
- Backend `verify` z JaCoCo 90/90 bez obnizania progow.
- Frontend coverage 90/90/90/90 bez obnizania progow.
- Docker Compose rebuild i Playwright E2E dla public upload + processing.
- Accessibility WCAG A/AA i keyboard/focus dla zmienionych statusow UI.

## Wynik pierwszej iteracji
- Etap 6 jest zamkniety dla pierwszej iteracji `MEDIA-001`.
- Backend ma testy workerow processingu, upload -> job, statusow, retry, audytu terminalnego wyniku, storage failure i public/owner preview/download.
- Migracje i krytyczne ograniczenia jobow/thumbnaili sa weryfikowane na MySQL przez `MySqlFlywayMigrationContractTest`.
- Ostatnia lokalna weryfikacja backendu: `./mvnw.cmd verify`, 115 testow, 0 failures/errors, 1 skip, JaCoCo branch coverage 90.06%.
- Powiazane regresje publicznego preview/download i UX uploadu sa opisane w `MEDIA_GALLERY_PREVIEW_DOWNLOAD_TEST_BRIEF.md` oraz `PUBLIC_GALLERY_UPLOAD_REGRESSION_TEST_BRIEF.md`.

## Nieweryfikowane obszary do jawnego opisania przy implementacji
- Pelne transkodowanie/preview wideo.
- Gwarantowany WebP thumbnail bez dodatkowego providera `ImageIO`.
- Signed links i rozbudowane pobieranie publiczne.
- Manualny retry przez admin API.
- Testy obciazeniowe dlugich kolejek processingu poza pierwszym budzetem.
