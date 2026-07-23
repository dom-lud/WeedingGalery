# Storage Plikow

## Cel dokumentu
Opisuje docelowa abstrakcje storage, model przechowywania plikow i zasady dostepu.

## Status dokumentu
- Status: draft
- Zakres: storage lokalny i przyszle storage obiektowe
- Ostatnia aktualizacja: 2026-07-23

## Stan obecny
- `StorageService` i `LocalFilesystemStorage` obsluguja zapis uploadow, odczyt strumieniowy, usuwanie kompensacyjne, `exists` i metadane bez ujawniania sciezek fizycznych.
- Plik jest najpierw zapisywany do pliku tymczasowego, a nastepnie atomowo publikowany przez hard link pod losowy klucz ograniczony do skonfigurowanego katalogu root; istniejacy obiekt nie jest nadpisywany.
- Docker Compose utrzymuje `/data/media` w nazwanym volume `media_data`; backend dziala jako uzytkownik nie-root.
- Publiczny i zarzadzany podglad mediow dziala przez kontrolowany streaming backendu, po sprawdzeniu grantu publicznego albo membership/ownership wydarzenia.
- Owner moze pobrac ZIP galerii generowany strumieniowo przez backend. Signed links pozostaja odroczone do pelnego `DOWNLOAD-001`/kolejnego etapu.

## Stan docelowy
- Storage ukryty za interfejsem domenowym, z pierwsza implementacja lokalna na VPS i mozliwoscia przejscia na storage obiektowy bez zmiany logiki biznesowej.

## Zalozenia
- Pliki nie sa przechowywane jako BLOB w relacyjnej bazie danych.
- Klient nie otrzymuje fizycznych sciezek systemowych.
- Backend odpowiada za autoryzacje i generowanie bezpiecznych linkow lub streaming.

## Implementacje docelowe
- `LocalFilesystemStorage`
- `S3CompatibleStorage`
- `AzureBlobStorage`
- `MinioStorage`
- `CloudflareR2Storage`

## Interfejs storage
```text
StorageService
- save(inputStream, objectKey, metadata)
- open(objectKey)
- delete(objectKey)
- exists(objectKey)
- getMetadata(objectKey)
- generateTemporaryDownloadLink(objectKey, ttl)
```

Aktualny interfejs realizuje operacje potrzebne do bezpiecznego uploadu i kontrolowanego streamingu przez backend. Link tymczasowy jest celowo odroczony do pelnego kontraktu pobierania/signed links.

## Klucz storage
- Klucz powinien byc stabilny i niezgadnialny.
- Zalecany schemat: `tenant/{ownerId}/events/{eventId}/galleries/{galleryId}/media/{mediaId}/original`
- Miniatury i ZIP uzywaja osobnych wariantow klucza.

## Zasady bezpieczenstwa
- Walidacja typu pliku odbywa sie przed finalnym zapisaniem jako material opublikowany.
- Dostep publiczny nie powinien oznaczac bezposredniego publicznego bucketu bez kontroli.
- `thumbnailUrl`, `contentUrl` i ZIP galerii sa endpointami API sprawdzajacymi grant/role; klient nie dostaje `storageKey`, checksumow ani sciezek fizycznych.
- Linki tymczasowe powinny wygasac i byc podpisane.

## Operacje usuwania
- Soft delete w bazie nie usuwa pliku natychmiast.
- Hard delete wykonuje job sprzatajacy i zapisuje wynik.
- Czesciowy blad usuwania wymaga retry oraz wpisu audytowego lub operacyjnego.

## Monitorowanie storage
- Zajetosc globalna, per uzytkownik, per wydarzenie i per galeria.
- Liczba osieroconych plikow i niespojnosci baza-storage.
- Alert przy zblizaniu sie do limitu dysku.

## Powiazane dokumenty
- [BACKGROUND_JOBS.md](BACKGROUND_JOBS.md)
- [../security/FILE_UPLOAD_SECURITY.md](../security/FILE_UPLOAD_SECURITY.md)
- [../adr/0003-file-storage-abstraction.md](../adr/0003-file-storage-abstraction.md)

## Decyzje otwarte
- Czy pelna implementacja pobierania plikow bedzie oparta glownie o streaming przez backend, czy o krotkie signed links.
