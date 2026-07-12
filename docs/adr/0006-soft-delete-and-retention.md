# ADR 0006: Soft Delete i Retencja

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Opisuje podejście do usuwania danych i retencji.

## Stan obecny
- Polityka usuwania nie została wdrożona.

## Stan docelowy
- Soft delete dla wybranych bytów, cleanup asynchroniczny i jawna polityka retencji.

## Kontekst
Produkt przetwarza prywatne media, a jednocześnie wymaga możliwości przywrócenia, audytu i kontrolowanego cleanupu.

## Decyzja
Stosujemy soft delete dla `User`, `Event`, `Gallery`, `MediaFile` oraz oddzielne zadania hard delete po upływie retencji.

## Konsekwencje
- Lepsza kontrola nad przypadkowym usunięciem.
- Większa złożoność zapytań i cleanupów.

## Alternatywy
- Natychmiastowy hard delete: odrzucony dla kluczowych bytów.

## Decyzje otwarte
- Domyślne okresy retencji dla mediów i backupów wymagają zatwierdzenia biznesowego.
