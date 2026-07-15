# Storage Plików

## Cel dokumentu
Opisuje docelową abstrakcję storage, model przechowywania plików i zasady dostępu.

## Status dokumentu
- Status: draft
- Zakres: storage lokalny i przyszłe storage obiektowe
- Ostatnia aktualizacja: 2026-07-15

## Stan obecny
- `StorageService` i `LocalFilesystemStorage` obsługują zapis uploadów, usuwanie kompensacyjne, `exists` i metadane bez ujawniania ścieżek fizycznych.
- Plik jest najpierw zapisywany do pliku tymczasowego, a następnie atomowo przenoszony pod losowy klucz ograniczony do skonfigurowanego katalogu root.
- Docker Compose utrzymuje `/data/media` w nazwanym volume `media_data`; backend działa jako użytkownik nie-root.
- Publiczny odczyt, streaming i signed links nie są jeszcze zaimplementowane i należą do `DOWNLOAD-001`/Etapu 7.

## Stan docelowy
- Storage ukryty za interfejsem domenowym, z pierwszą implementacją lokalną na VPS i możliwością przejścia na storage obiektowy bez zmiany logiki biznesowej.

## Założenia
- Pliki nie są przechowywane jako BLOB w relacyjnej bazie danych.
- Klient nie otrzymuje fizycznych ścieżek systemowych.
- Backend odpowiada za autoryzację i generowanie bezpiecznych linków lub streaming.

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

Aktualny interfejs realizuje operacje potrzebne do bezpiecznego uploadu. `open` i link tymczasowy są celowo odroczone do zaprojektowania kontraktu pobierania.

## Klucz storage
- Klucz powinien być stabilny i niezgadnialny.
- Zalecany schemat: `tenant/{ownerId}/events/{eventId}/galleries/{galleryId}/media/{mediaId}/original`
- Miniatury i ZIP używają osobnych wariantów klucza.

## Zasady bezpieczeństwa
- Walidacja typu pliku odbywa się przed finalnym zapisaniem jako materiał opublikowany.
- Dostęp publiczny nie powinien oznaczać bezpośredniego publicznego bucketu bez kontroli.
- Linki tymczasowe powinny wygasać i być podpisane.

## Operacje usuwania
- Soft delete w bazie nie usuwa pliku natychmiast.
- Hard delete wykonuje job sprzątający i zapisuje wynik.
- Częściowy błąd usuwania wymaga retry oraz wpisu audytowego lub operacyjnego.

## Monitorowanie storage
- Zajętość globalna, per użytkownik, per wydarzenie i per galeria.
- Liczba osieroconych plików i niespójności baza-storage.
- Alert przy zbliżaniu się do limitu dysku.

## Powiązane dokumenty
- [BACKGROUND_JOBS.md](BACKGROUND_JOBS.md)
- [../security/FILE_UPLOAD_SECURITY.md](../security/FILE_UPLOAD_SECURITY.md)
- [../adr/0003-file-storage-abstraction.md](../adr/0003-file-storage-abstraction.md)

## Decyzje otwarte
- Czy pierwsza implementacja pobierania plików będzie oparta głównie o streaming przez backend, czy o krótkie signed links.
