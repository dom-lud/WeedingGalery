# Planowanie Zadań przez Agentów AI

## Cel dokumentu
Opisuje, jak agent AI powinien przygotować plan przed rozpoczęciem implementacji lub innej zmiany technicznej.

## Status dokumentu
- Status: draft
- Zakres: standard planu dla agentów AI
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Agenci działają na podstawie dokumentacji, ale planowanie nie jest jeszcze ustandaryzowane jednym szablonem.

## Stan docelowy
- Każdy agent przygotowuje plan techniczny o konkretnej, nieogólnikowej strukturze.

## Zasady
- Plan nie może być ogólnikiem typu „zrobię backend i frontend”.
- Plan musi być osadzony w aktualnym stanie kodu i dokumentacji.
- Jeśli zadanie wymaga wcześniejszej decyzji, plan powinien to wskazać zamiast przechodzić od razu do implementacji.
- Plan musi jawnie wskazać, czy potrzebni są subagenci; jeśli nie, plan ma to powiedzieć wprost.
- Plan musi zawierać sposób self-review i warunek ponawiania iteracji po wykryciu problemów.
- Plan musi wskazać testy, które mogą obalić błędną implementację, a nie tylko potwierdzić aktualny kod.

## Struktura planu
1. Cel zadania.
2. Stan obecny.
3. Powiązane dokumenty.
4. Powiązane moduły.
5. Pliki do utworzenia lub zmiany.
6. Zmiany bazy danych.
7. Zmiany API.
8. Zmiany frontendowe.
9. Autoryzacja.
10. Ownership.
11. Bezpieczeństwo.
12. Testy.
13. Dokumentacja.
14. Ryzyka.
15. Decyzja o subagentach.
16. Plan self-review.
17. Kolejność implementacji.
18. Warunki zakończenia.

## Minimalny standard jakości planu
- Wskazuje konkretne moduły i pliki.
- Rozróżnia stan obecny od docelowego.
- Wskazuje, czego agent nie zweryfikował.
- Wskazuje zależności i blokery.
- Wskazuje jak zostanie zweryfikowane spełnienie wymagań na końcu pracy.

## Powiązane dokumenty
- [WORKFLOW.md](WORKFLOW.md)
- [DEFINITION_OF_READY.md](DEFINITION_OF_READY.md)
- [../templates/TASK_TEMPLATE.md](../templates/TASK_TEMPLATE.md)

## Decyzje otwarte
- Czy plan przygotowany przez agenta ma być przechowywany jako artefakt zadania w repozytorium czy tylko w konwersacji.
