---
name: stage-3-events-and-memberships
description: "Skill prowadzacy przez Etap 3 - wydarzenia, ownership i czlonkostwo. Obejmuje EVENT-001 i MEMBER-001 bez e-mailowych zaproszen zaleznych od AUTH-002."
---

# Etap 3 - Wydarzenia i Czlonkowie

## Kiedy uzywac?
Uzyj tego skilla, gdy podstawowa identity z Etapu 2 jest juz domknieta, a celem pracy jest wdrozenie rdzenia domenowego wydarzen: tworzenia wydarzenia, ownership, czlonkostwa i rol wydarzenia.

## Decyzja o subagentach
Domyslnie nie uruchamiaj subagentow, jesli zmiana miesci sie w jednym spojnym zakresie `event + membership + API + testy + dokumentacja`.
Rozwaz subagentow tylko wtedy, gdy:
- osobno analizujesz model danych lub migracje,
- osobno prowadzisz security review ownership,
- zakres obejmuje jednoczesnie frontend, backend i nowe flow administracyjne.

## Zakres tego etapu
- `EVENT-001` - tworzenie i zarzadzanie wydarzeniami.
- `MEMBER-001` - role i czlonkostwo w wydarzeniu.
- Ownership zasobow oparty o `Event.owner_user_id` i czlonkostwo.
- Egzekwowanie dostepu w API, use case i repozytoriach.

## Poza zakresem pierwszego wejscia
- E-mailowe zaproszenia do wydarzenia.
- Tokeny zaproszen.
- Flow zalezne od `AUTH-002`, w tym maile transakcyjne i pelne potwierdzanie tozsamosci w zaproszeniach.
- Publiczna galeria i uploady.

## Dokumenty obowiazkowe przed startem
1. `AGENTS.md`
2. `docs/product/BACKLOG.md`
3. `docs/product/FEATURE_ROADMAP.md`
4. `docs/product/USER_ROLES.md`
5. `docs/product/PERMISSIONS_MATRIX.md`
6. `docs/architecture/DATA_MODEL.md`
7. `docs/architecture/MULTI_TENANCY.md`
8. `docs/architecture/MODULES.md`
9. `docs/backend/AUTHENTICATION_AND_AUTHORIZATION.md`
10. `api-contract/API_CONTRACT.md`

## Repo truth na wejscie
- Etap 2 dostarcza juz sesje, CSRF, admin-only tworzenie kont, login, logout, `/api/auth/me`, blokade po wielu blednych logowaniach oraz podstawowy audyt auth.
- Aktualne role systemowe w kodzie to `ADMIN` i `USER`.
- Role wydarzenia nie sa jeszcze zaimplementowane i nie wolno udawac, ze juz istnieja w runtime.

## Glowne decyzje wykonawcze
- Najpierw zaprojektuj kontrakt API i ownership, potem dopiero kontrolery.
- Kazde pobranie lub modyfikacja wydarzenia musi byc zawezone przez ownership albo czlonkostwo.
- Nie pozwalaj, aby sama znajomosc `eventId` dawla dostep do cudzego zasobu.
- Czlonkostwo i role wydarzenia maja byc egzekwowane w logice biznesowej, nie tylko w kontrolerze.

## Minimalna kolejnosc pracy
1. Zaktualizuj `api-contract/API_CONTRACT.md` dla endpointow event i membership.
2. Ustal model danych i migracje dla wydarzen oraz czlonkostwa.
3. Wdroz use case tworzenia wydarzenia z jednoznacznym ownership.
4. Wdroz odczyt i zarzadzanie wydarzeniem z negatywnymi przypadkami dostepu.
5. Wdroz czlonkostwo i role wydarzenia.
6. Dodaj audyt dla wrazliwych zmian ownership i membership.
7. Rozszerz testy backendowe, integracyjne i E2E lub rzetelne testy integracyjne flow.
8. Zaktualizuj dokumentacje i wykonaj self-review.

## Testy, ktore musza powstac
- Pozytywne scenariusze tworzenia i edycji wydarzenia przez wlasciciela.
- Negatywne scenariusze IDOR i braku ownership.
- Czlonkostwo: dodanie, zmiana roli, usuniecie i ograniczenia managera.
- Regresja dla przypadkow granicznych: obcy `eventId`, usuniety membership, duplikat czlonkostwa, proba modyfikacji przez zwyklego `USER` bez relacji do wydarzenia.

## Typowe pulapki
- Implementacja filtrowania tylko po `owner_id` bez uwzglednienia czlonkostwa.
- Wpychanie zaproszen e-mail do pierwszego slice'a Etapu 3 mimo zaleznosci od `AUTH-002`.
- Kontroler, ktory przyjmuje `eventId`, ale nie sprawdza ownership w use case.
- Testy pisane pod happy path bez negatywnych scenariuszy autoryzacji i ownership.

## Warunek uznania etapu za dobrze rozpoczety
- Istnieje pierwsza, dzialajaca sciezka utworzenia wydarzenia przez zalogowanego uzytkownika.
- Ownership jest realnie wymuszany.
- Dokumentacja, kontrakt i testy sa zsynchronizowane z kodem.
