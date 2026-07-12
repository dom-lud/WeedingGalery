# Konwencje Bazy Danych

## Cel dokumentu
Opisuje standardy projektowania schematu PostgreSQL, migracji Flyway i zapytań.

## Status dokumentu
- Status: draft
- Zakres: standardy danych i migracji
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Docelowy schemat PostgreSQL nie został jeszcze utworzony.

## Stan docelowy
- Stabilny, audytowalny model danych rozwijany migracjami Flyway.

## Zasady nazewnictwa
- Tabele w liczbie pojedynczej lub mnogiej muszą być konsekwentne w całym projekcie; rekomendacja: liczba pojedyncza.
- Klucze główne: `id`.
- Klucze obce: `{entity}_id`.
- Znaczniki czasu: `created_at`, `updated_at`, `deleted_at`.

## Migracje Flyway
- Tylko migracje do przodu.
- Nie modyfikujemy zatwierdzonych migracji.
- Zmiany destrukcyjne wymagają planu migracyjnego i retencji.

## Typy danych
- UUID jako preferowany identyfikator zewnętrzny.
- `jsonb` wyłącznie dla ograniczonych, uzasadnionych pól konfiguracyjnych lub metadanych.
- Rozmiary plików i limity w `bigint`.
- Statusy jako czytelne enumy aplikacyjne mapowane przewidywalnie.

## Indeksy
- Każdy klucz obcy wymaga analizy indeksu.
- Tabele wielotenantowe muszą mieć indeksy wspierające filtrowanie po `owner_id`, `event_id`, `gallery_id`.
- Rozważaj indeksy częściowe dla statusów aktywnych.

## Soft delete
- Tabele z retencją mają `deleted_at`.
- Zapytania aplikacyjne muszą domyślnie ukrywać rekordy usunięte logicznie.

## Audyt
- Krytyczne operacje biznesowe i administracyjne zapisujemy w tabelach audytowych, a nie wyłącznie w logach tekstowych.

## Powiązane dokumenty
- [../architecture/DATA_MODEL.md](../architecture/DATA_MODEL.md)
- [../security/PRIVACY_AND_DATA_RETENTION.md](../security/PRIVACY_AND_DATA_RETENTION.md)
- [../adr/0006-soft-delete-and-retention.md](../adr/0006-soft-delete-and-retention.md)

## Decyzje otwarte
- Czy część tabel analitycznych będzie od początku zmaterializowana lub odświeżana wsadowo.
