# Prywatność i Retencja Danych

## Cel dokumentu
Opisuje techniczne założenia prywatności, retencji i usuwania danych.

## Status dokumentu
- Status: draft
- Zakres: prywatność techniczna i retencja
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Zasady są planowane, nie wdrożone.

## Stan docelowy
- Minimalizacja danych, kontrolowany dostęp i przewidywalne usuwanie danych.

## Zasady prywatności
- Galerie mają charakter prywatny, nawet jeśli są dostępne linkiem.
- Nie zbieramy zbędnych danych gości.
- Adres IP zapisujemy tylko w uzasadnionych przypadkach bezpieczeństwa lub audytu.
- Eksport i usunięcie konta muszą być możliwe operacyjnie.

## Retencja
- Media mogą mieć retencję wynikającą z planu lub ustawień wydarzenia.
- Soft delete poprzedza hard delete tam, gdzie potrzebna jest możliwość cofnięcia lub audyt.
- Backupi mają osobną politykę retencji i szyfrowania.

## Usuwanie danych
- Konto użytkownika
- Wydarzenie
- Galeria
- MediaFile
- Sesje i tokeny
- Powiadomienia

## Powiązane dokumenty
- [../architecture/DATA_MODEL.md](../architecture/DATA_MODEL.md)
- [../operations/BACKUP_AND_RESTORE.md](../operations/BACKUP_AND_RESTORE.md)
- [../adr/0006-soft-delete-and-retention.md](../adr/0006-soft-delete-and-retention.md)

## Decyzje otwarte
- Jak długo przechowywać soft deleted media przed hard delete w planach płatnych i darmowych.
