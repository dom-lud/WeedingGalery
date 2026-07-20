# Konfiguracja

## Cel dokumentu
Opisuje zasady konfiguracji aplikacji, sekrety i zmienne środowiskowe.

## Status dokumentu
- Status: draft
- Zakres: konfiguracja runtime i build
- Ostatnia aktualizacja: 2026-07-15

## Stan obecny
- Profil prod przyjmuje `STORAGE_LOCAL_ROOT` (w Compose `/data/media`). Limity uploadu i public access maja bezpieczne wartosci domyslne w `application.yml` i moga byc nadpisane zewnetrzna konfiguracja Spring.
- Parametry cleanupu: `app.upload.cleanup-interval-ms`, `cleanup-initial-delay-ms`, `temp-cleanup-interval-ms` i `temp-cleanup-initial-delay-ms`.
- Profil prod wymaga jawnych `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` i `STORAGE_LOCAL_ROOT`; brak zmiennej nie uruchamia aplikacji z testowym sekretem.
- Produkcja domyslnie ustawia bezpieczne cookies sesji i CSRF. `SESSION_COOKIE_SECURE=false` jest dopuszczalne wylacznie dla lokalnego HTTP.
- CORS jest konfigurowany przez `CORS_ALLOWED_ORIGIN_PATTERNS`; pusta wartosc oznacza same-origin only.
- Migracje nie tworza kont. Pierwszy administrator wymaga jednorazowego `BOOTSTRAP_ADMIN_ENABLED=true`, poprawnego e-maila i hasla o co najmniej 12 znakach. Po utworzeniu konta flage trzeba wylaczyc i usunac haslo ze srodowiska runtime.

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
