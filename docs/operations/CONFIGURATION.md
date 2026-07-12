# Konfiguracja

## Cel dokumentu
Opisuje zasady konfiguracji aplikacji, sekrety i zmienne środowiskowe.

## Status dokumentu
- Status: draft
- Zakres: konfiguracja runtime i build
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Konfiguracja docelowa nie jest jeszcze uporządkowana.

## Stan docelowy
- Konfiguracja przez środowisko, bez sekretów w repozytorium.

## Zasady
- `.env.example` dokumentuje wymagane zmienne bez wartości wrażliwych.
- Sekrety są dostarczane poza repozytorium.
- Każda nowa zmienna wymaga opisu i ownera.

## Główne obszary konfiguracji
- Baza danych
- Sesje i tokeny
- Storage
- Upload i limity
- E-mail
- QR
- Monitoring i logowanie
- Domeny i HTTPS

## Powiązane dokumenty
- [ENVIRONMENTS.md](ENVIRONMENTS.md)
- [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md)
- [../architecture/FILE_STORAGE.md](../architecture/FILE_STORAGE.md)

## Decyzje otwarte
- Czy część ustawień limitów ma być przeniesiona z env do `SystemSetting`.
