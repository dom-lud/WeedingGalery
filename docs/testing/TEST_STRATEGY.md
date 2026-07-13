# Strategia Testow

## Cel dokumentu
Opisuje docelowa strategie testowania platformy na poziomie backendu, frontendu i end-to-end.

## Status dokumentu
- Status: draft
- Zakres: test strategy dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Istniejacy szkielet projektu nie zapewnia jeszcze pelnego pokrycia testowego.

## Stan docelowy
- Testy pokrywaja krytyczne sciezki biznesowe, bezpieczenstwo, ownership, storage i zadania asynchroniczne.

## Piramida testow
- Testy jednostkowe dla logiki domenowej i UI
- Testy integracyjne dla API, repozytoriow, storage i security
- Testy E2E dla glownych przeplywow uzytkownika

## Priorytety
- Ownership i autoryzacja
- Upload i przetwarzanie mediow
- Retencja i usuwanie danych
- Operacje administracyjne
- Stabilnosc kontraktow API

## Zasady jakosci testow
- Test ma bronic wymagania, kontraktu albo regresji, nie aktualnej struktury kodu.
- Zanim napiszesz kod testu, zawsze wykonaj analize zmian i zaplanuj scenariusze testowe, uwzgledniajac sciezki negatywne i edge case'y.
- Test powinien umiec obalic bledna implementacje, a nie tylko potwierdzic szczesliwa sciezke.
- Dla krytycznych zmian preferuj testy negatywne, graniczne i regresyjne obok testow pozytywnych.
- Nie pisz testu tylko dlatego, ze latwo go dopasowac do obecnego kodu.
- Test musi byc tworzony niezaleznie od implementacji, na podstawie potrzeb biznesowych, kontraktu i ryzyk, tak aby realnie mial szanse znalezc buga.
- Sam fakt, ze test przechodzi, nie oznacza jeszcze dobrej jakosci. Dobry test powinien byc w stanie zawiesc, gdy zachowanie produktu odchyla sie od wymagan.
- Przy krytycznych flow nalezy dodawac warianty szybkie, nieidealne i uzytkownikocentryczne, a nie wylacznie scenariusz "strona sie ustabilizowala i wszystko poszlo idealnie".
- Jesli test jest zbyt mocno sprzezony z detalem implementacyjnym, trzeba to uzasadnic.
- Nie mieszaj testow API z testami E2E UI. Nalezy je utrzymywac oddzielnie, uzywajac innych narzedzi, np. Playwright do E2E, a Spring Boot Test lub REST Assured do API.
- W testach Playwright preferowany jest page object pattern, aby selektory i techniczne kroki byly utrzymywane centralnie, a specy pozostawaly opisem zachowania biznesowego.
- Wyniki Playwright w CI powinny byc widoczne zarowno w artefakcie HTML, jak i w komentarzu PR z podsumowaniem przebiegu, aby reviewer od razu widzial skale problemu bez przeklikiwania calego workflow.

## Powiazane dokumenty
- [BACKEND_TESTING.md](BACKEND_TESTING.md)
- [FRONTEND_TESTING.md](FRONTEND_TESTING.md)
- [E2E_SCENARIOS.md](E2E_SCENARIOS.md)

## Decyzje otwarte
- Zakres automatycznych testow wydajnosciowych przed pierwsza produkcja.
