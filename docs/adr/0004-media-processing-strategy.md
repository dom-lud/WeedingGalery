# ADR 0004: Strategia Przetwarzania Mediow

## Status dokumentu
- Status: accepted
- Ostatnia aktualizacja: 2026-07-21

## Cel dokumentu
Opisuje kierunek przetwarzania zdjec i filmow po uploadzie.

## Stan obecny
- Pierwsza iteracja pipeline'u jest zaimplementowana jako trwale joby DB z in-process workerem.
- Etap 5 zapisuje oryginaly w storage, a Etap 6 ustawia pliki na `PROCESSING` i aktualizuje wynik po background jobie.

## Stan docelowy
- Asynchroniczne przetwarzanie z zachowaniem oryginalu, retry i widocznym statusem.

## Kontekst
Generowanie miniaturek i odczyt metadanych moze byc kosztowne i nie powinno opozniac odpowiedzi HTTP.

## Decyzja
Przetwarzanie mediow odbywa sie jako background jobs uruchamiane po udanym zapisie pliku.

Pierwsza iteracja Etapu 6 obejmuje obrazy i bezpieczny status dla filmow:
- JPEG/PNG: ekstrakcja podstawowych metadanych oraz generowanie miniatury `SMALL`.
- WebP: upload pozostaje dozwolony, ale processing zalezy od dostepnosci providera `ImageIO`; bez niego konczy sie `MEDIA_PROCESSING_UNSUPPORTED_FORMAT`.
- MP4: bez transkodowania i bez generowania preview w pierwszej iteracji; plik przechodzi przez job metadanych/statusu, a analiza kodeka pozostaje jako kontrola bezpieczenstwa/hardening.
- Oryginalny obiekt storage pozostaje nienaruszony; warianty pochodne sa zapisywane pod osobnymi kluczami storage.
- Niepowodzenie wariantu pochodnego nie usuwa oryginalu i konczy sie przewidywalnym statusem oraz kodem bledu.

## Konsekwencje
- Lepsza responsywnosc API.
- Koniecznosc sledzenia statusow i bledow jobow.
- Potrzebne sa migracje do `media_processing_jobs`, `media_thumbnails` oraz pol processingowych w `media_files`.
- Publiczne listowanie, download, streaming i signed links pozostaja poza Etapem 6.

## Alternatywy
- Synchroniczne generowanie miniaturek: odrzucone jako mniej odporne i ryzykowne dla czasu odpowiedzi uploadu.
- Pelne transkodowanie/preview wideo od razu: odlozone z powodu kosztu CPU, pamieci, zaleznosci binarnych i potrzeby osobnych limitow operacyjnych.

## Decyzje otwarte
- Pelne preview/transkodowanie wideo pozostaje poza pierwsza iteracja i wymaga osobnej decyzji po pomiarach kosztu CPU, pamieci oraz czasu przetwarzania.
- Gwarantowane WebP thumbnail wymaga decyzji o zaleznosci runtime albo osobnego dekodera.
