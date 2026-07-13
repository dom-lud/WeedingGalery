---
name: stage-2-identity
description: "Skill prowadzacy przez Etap 2 - Tozsamosc uzytkownika i autoryzacja (AUTH-001 i okolice). Obejmuje sesje Spring Security, CSRF dla SPA, administracyjne tworzenie kont, testy auth i audit eventy."
---

# Etap 2 - Tozsamosc i Autoryzacja

## Kiedy uzywac?
Uzyj tego skilla, gdy repozytorium ma juz dzialajacy szkielet techniczny, a Twoim celem jest implementacja lub rozszerzenie obszaru identity: logowanie, sesje, CSRF, zarzadzanie kontami, testy auth albo audyt zdarzen identity.

## Decyzja o subagentach
Domyslnie nie uruchamiaj subagentow, jesli zmiana miesci sie w jednym spojnym obszarze auth + frontend + testy + dokumentacja i da sie ja zweryfikowac w jednym przebiegu.
Uruchom subagentow tylko wtedy, gdy:
- trzeba rownolegle prowadzic niezalezne watki, np. osobny audit security i osobny audit testow,
- zakres obejmuje wiele modulow poza identity,
- potrzebny jest osobny reviewer po zakonczeniu implementacji.

## Repo truth na dzis
- Strategia auth jest oparta o sesje serwerowe Spring Security.
- SPA korzysta z CSRF i bootstrapuje token przez `GET /api/auth/csrf`.
- Aktualne endpointy identity w kodzie i kontrakcie to:
  - `GET /api/auth/csrf`
  - `POST /api/auth/login`
  - `GET /api/auth/me`
  - `POST /api/auth/register`
- `POST /api/auth/register` nie jest publiczna rejestracja. Konto tworzy zalogowany administrator.
- Frontend nie powinien eksponowac publicznego przycisku rejestracji.
- Aktualny audyt auth obejmuje `USER_REGISTERED` i `USER_LOGGED_IN`.

## Cel
Wdrozenie i dalsze uszczelnianie tozsamosci uzytkownika, tak aby przyszle endpointy panelu wydarzen i administracji byly chronione, zgodne z kontraktem FE-BE i pokryte testami, ktore realnie wykrywaja regresje.

## Obowiazkowa kolejnosc pracy
1. Przeczytaj `AGENTS.md`, dokument auth, kontrakt API i test strategy.
2. Sprawdz, czy zmiana wymaga aktualizacji `api-contract/API_CONTRACT.md`.
3. Sprawdz, czy zmienia sie katalog audit eventow i czy trzeba rozszerzyc `EventType`.
4. Zaimplementuj backend i frontend zgodnie z aktualnym kontraktem.
5. Rozszerz testy jednostkowe, integracyjne i E2E na podstawie wymagan biznesowych oraz ryzyk.
6. Zaktualizuj dokumentacje, roadmape, backlog i ten skill, jezeli zmienia sie etap lub standard pracy.
7. Wykonaj self-review w roli reviewera, popraw problemy i powtorz review az wynik bedzie akceptowalny.

## Zakres implementacyjny

### 1. Backend
- Utrzymuj sesje Spring Security i CSRF zgodne ze SPA.
- Publiczny dostep bez konta dotyczy galerii, nie auth uzytkownikow systemowych.
- Traktuj administracyjne tworzenie kont oddzielnie od przyszlych flow typu reset hasla czy weryfikacja e-mail.
- Hasla musza byc bezpiecznie hashowane.
- Przy zmianach endpointow lub payloadow najpierw aktualizuj `api-contract/API_CONTRACT.md`.

### 2. Frontend
- Logowanie powinno byc zgodne z kontraktem i nie moze zakladac "idealnej" synchronizacji z backendem.
- Publiczny przycisk rejestracji nie powinien wracac, dopoki polityka kont pozostaje admin-only.
- Dla formularzy auth utrzymuj dobra ergonomie i dostepnosc, w tym mozliwosc podgladu hasla.
- Stosuj page object pattern w E2E, aby selektory i akcje byly utrzymywane w jednym miejscu.

### 3. Audyt
- Wrazliwe akcje identity i admina musza byc ocenione pod katem wpisu audytowego.
- Przy kazdym nowym flow sprawdz:
  - czy trzeba dodac nowy `EventType`,
  - czy potrzebny jest bardziej szczegolowy `details`,
  - czy testy i dokumentacja odzwierciedlaja nowy audit.
- W audycie nie zapisuj sekretow ani wrazliwych payloadow.

### 4. Testy
- Testy maja bronic wymagan i kontraktu, a nie aktualnej implementacji.
- Dla logowania musza istniec scenariusze realistyczne, w tym szybki klik po otwarciu strony.
- Page Object ma porzadkowac API testowe, ale nie moze maskowac bugow przez sztuczne czekanie na odpowiedzi, ktorych uzytkownik jeszcze nie wyzwolil.
- Minimalny zestaw powinien laczyc:
  - testy jednostkowe logiki auth,
  - testy integracyjne/security kontraktu,
  - testy E2E flow UI.

## Co zwykle nalezy zaktualizowac razem ze zmiana
- `api-contract/API_CONTRACT.md`
- `docs/backend/AUTHENTICATION_AND_AUTHORIZATION.md`
- `docs/operations/LOGGING.md`
- `docs/testing/TEST_STRATEGY.md`
- `docs/testing/FRONTEND_TESTING.md`
- `docs/testing/E2E_SCENARIOS.md`
- `docs/product/BACKLOG.md`
- `docs/product/FEATURE_ROADMAP.md`

## Typowe pulapki
- Pisanie testu, ktory czeka na `/api/auth/csrf` przed akcja logowania i przez to nie odtwarza realnego szybkiego kliku.
- Pozostawienie publicznej rejestracji w UI po zmianie polityki na admin-only.
- Rozszerzenie auth bez dopisania audit eventu albo bez aktualizacji dokumentacji.
- Aktualizacja kodu bez aktualizacji kontraktu FE-BE.

## Co jeszcze nie jest domkniete w Etapie 2
- Weryfikacja e-mail.
- Reset hasla.
- Pelny logout i ewentualne zarzadzanie wieloma sesjami.
- Obsluga maili transakcyjnych.
- Szerszy katalog audit eventow, np. nieudane logowania i akcje administracyjne na kontach.
