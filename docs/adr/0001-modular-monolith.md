# ADR 0001: Modularny Monolit

## Status dokumentu
- Status: accepted
- Ostatnia aktualizacja: 2026-07-12

## Cel dokumentu
Opisuje decyzję o budowie systemu jako modularnego monolitu.

## Stan obecny
- Projekt jest na etapie planowania architektury.

## Stan docelowy
- Jedna aplikacja backendowa z wyraźnymi granicami modułów domenowych.

## Kontekst
System ma obsługiwać wiele domen biznesowych, ale startuje jako produkt rozwijany przez mały zespół i wdrażany początkowo na jednym VPS.

## Decyzja
Budujemy system jako modularny monolit z granicami modułów opisanymi w dokumentacji, bez wprowadzania mikroserwisowej złożoności na początku.

## Konsekwencje
- Prostsze wdrożenie i utrzymanie.
- Mniejszy narzut operacyjny.
- Wymagane pilnowanie granic modułów wewnątrz jednego repozytorium i procesu.

## Alternatywy
- Mikroserwisy od początku: odrzucone na obecnym etapie jako zbyt złożone.
- Monolit bez modularności: odrzucony jako zbyt trudny do utrzymania przy wzroście domeny.

## Decyzje otwarte
- Jak formalnie egzekwować granice modułów w kodzie wraz ze wzrostem liczby pakietów.
