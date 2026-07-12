# ADR 0003: Abstrakcja Storage Plików

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Opisuje decyzję o ukryciu storage za interfejsem.

## Stan obecny
- Implementacja storage nie istnieje.

## Stan docelowy
- Lokalne storage na VPS jako pierwsza implementacja, z możliwością przejścia na S3-kompatybilny backend.

## Kontekst
Produkt startuje na jednym VPS, ale musi zachować możliwość migracji do storage obiektowego bez przepisywania logiki biznesowej.

## Decyzja
Wprowadzamy abstrakcję `StorageService` i zakazujemy zależności logiki domenowej od szczegółów plikowego storage.

## Konsekwencje
- Łatwiejsza migracja storage.
- Potrzeba dodatkowej warstwy mapowania błędów i metadanych.

## Alternatywy
- Bezpośredni zapis na dysk z wielu miejsc: odrzucony.
- BLOB w PostgreSQL: odrzucony.

## Decyzje otwarte
- Czy signed download links będą podstawowym mechanizmem pobierania już w pierwszej wersji.
