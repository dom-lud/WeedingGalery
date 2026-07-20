# ADR 0006: Soft Delete i Retencja

## Status dokumentu
- Status: proposed
- Ostatnia aktualizacja: 2026-07-20

## Cel dokumentu
Opisuje podejście do usuwania danych i retencji.

## Stan obecny
- Soft delete i archiwizacja sa wdrozone dla wydarzen i galerii, a slugi galerii pozostaja zarezerwowane po usunieciu.
- Upload ma cleanup wygaslych sesji, przerwanych zapisow i plikow tymczasowych.
- Pelne okresy retencji, hard delete uzytkownikow/mediow, eksport danych i spojny cleanup backupow nie sa jeszcze zatwierdzone; dlatego ADR pozostaje `proposed` dla docelowej polityki.

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
