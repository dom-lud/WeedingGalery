# ADR 0003: Abstrakcja Storage Plików

## Status dokumentu
- Status: accepted
- Ostatnia aktualizacja: 2026-07-13

## Cel dokumentu
Opisuje decyzję o ukryciu storage za interfejsem.

## Stan obecny
- Pierwsza implementacja lokalnego storage jest wdrażana w Etapie 5.

## Stan docelowy
- Lokalne storage na VPS jako pierwsza implementacja, z możliwością przejścia na S3-kompatybilny backend.

## Kontekst
Produkt startuje na jednym VPS, ale musi zachować możliwość migracji do storage obiektowego bez przepisywania logiki biznesowej.

## Decyzja
Wprowadzamy abstrakcję `StorageService` i zakazujemy zależności logiki domenowej
od szczegółów plikowego storage. Pierwsza implementacja zapisuje dane na trwałym
volume poza web rootem, pod kluczem generowanym wyłącznie przez serwer.

Zapis przebiega przez plik tymczasowy na tym samym filesystemie, walidację oraz
atomowy move. Implementacja blokuje traversal i symlinki, nie ujawnia fizycznych
ścieżek i nigdy nie używa nazwy przesłanej przez klienta jako klucza obiektu.

`StorageService` odpowiada za zapis, odczyt, usunięcie, istnienie i metadane.
Autoryzacja oraz czasowe linki dostępowe należą do warstwy aplikacyjnej. W Etapie
5 media pozostają nieopublikowane, a publiczne pobieranie i signed download URL
wchodzą do `DOWNLOAD-001`/publicznej galerii.

## Konsekwencje
- Łatwiejsza migracja storage.
- Potrzeba dodatkowej warstwy mapowania błędów i metadanych.
- Baza i filesystem nie tworzą jednej transakcji; implementacja wymaga kompensacji
  i wykrywania stanów wymagających cleanupu.
- Docker i wdrożenie muszą montować trwały volume oraz uruchamiać backend jako
  użytkownik bez uprawnień roota.

## Alternatywy
- Bezpośredni zapis na dysk z wielu miejsc: odrzucony.
- BLOB w relacyjnej bazie danych: odrzucony.
- Publiczny katalog statyczny: odrzucony z powodu ominięcia autoryzacji.

## Decyzje otwarte
- Backend streaming czy signed links zostanie rozstrzygnięte przy `DOWNLOAD-001`.
