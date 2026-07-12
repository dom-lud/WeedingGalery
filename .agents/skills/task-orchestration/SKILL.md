# Task Orchestration

## Cel dokumentu
Skill wspierający wybór i koordynację subagentów przy złożonych zadaniach projektowych.

## Status dokumentu
- Status: draft
- Zakres: task orchestration skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Orkiestracja subagentów jest opisana dokumentacyjnie i nie oznacza obowiązku używania subagentów w każdym zadaniu.

## Stan docelowy
- Agent używa tego skill przy zadaniach wieloobszarowych, ryzykownych lub wymagających niezależnych analiz.

## Kiedy używać
- Gdy zadanie dotyka wielu obszarów, np. backend, frontend, baza, security i CI.
- Gdy analiza architektury, bezpieczeństwa lub testów powinna być niezależna.
- Gdy praca może być bezpiecznie podzielona na niezależne części.
- Gdy trzeba zebrać rekomendacje, rozwiązać sprzeczności i wykonać końcową integrację.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/development/WORKFLOW.md`
- `docs/development/AI_TASK_PLANNING.md`
- `docs/development/SUBAGENT_ORCHESTRATION.md`
- odpowiednie dokumenty domenowe i ADR

## Zasady wyboru ról
- Nie uruchamiaj wszystkich subagentów domyślnie.
- Dobierz role do ryzyka i zakresu zmiany.
- Dla nowego endpointu rozważ Backend, Security i QA.
- Dla zmiany tabeli rozważ Database, Backend i QA.
- Dla uploadu rozważ Architecture, Backend, Frontend, Security, QA i DevOps.
- Dla zmiany deploymentu rozważ DevOps, Security i QA.
- Dla zmiany tekstu w UI zwykle wystarczy Frontend i ewentualnie QA.

## Podział zadania
1. Określ cel i granice zadania.
2. Wypisz obszary wpływu.
3. Wybierz minimalny zestaw subagentów.
4. Przydziel każdemu agentowi odrębny zakres.
5. Zdefiniuj pliki do analizy i pliki do modyfikacji.
6. Wyklucz pliki wspólne, jeśli grożą konfliktem.
7. Zaplanuj integrację wyników i końcowe testy.

## Unikanie konfliktów plików
- Nie pozwalaj kilku subagentom modyfikować tych samych plików.
- Równolegle zlecaj przede wszystkim niezależne analizy.
- Jeśli potrzebne są niezależne implementacje, użyj osobnych branchy lub worktree.
- Jeżeli kilka ról dotyczy jednego pliku, niech subagenci przygotują rekomendacje, a Lead Agent wykona finalną zmianę.

## Przekazywanie kontekstu
Każdy subagent powinien dostać:
- rolę,
- cel,
- kontekst,
- dokładny zakres,
- elementy poza zakresem,
- dokumenty do przeczytania,
- pliki do analizy,
- pliki do modyfikacji,
- oczekiwany format wyniku,
- wymagane testy,
- warunki zakończenia.

Nie przekazuj subagentowi nieograniczonego mandatu. Subagent nie może samodzielnie rozszerzać zakresu.

## Zbieranie wyników
Wymagaj od subagenta:
- podsumowania analizy,
- problemów,
- rekomendowanych zmian,
- listy plików,
- wpływu na bezpieczeństwo,
- wpływu na testy,
- ryzyk,
- nierozstrzygniętych decyzji,
- wyników komend lub testów,
- informacji, czego nie udało się zweryfikować.

## Rozwiązywanie sprzeczności
- Porównaj rekomendacje z dokumentacją i ADR.
- Preferuj najprostsze rozwiązanie zgodne z architekturą.
- Odrzuć sugestie rozszerzające zakres bez podstawy w zadaniu.
- W razie konfliktu bezpieczeństwa i wygody wybierz wariant bezpieczniejszy albo zapisz decyzję do ADR.
- Jeśli sprzeczności nie da się rozstrzygnąć, zgłoś decyzję otwartą zamiast udawać pewność.

## Końcowa integracja
- Lead Agent zatwierdza lub odrzuca rekomendacje.
- Lead Agent wykonuje integrację zmian albo pilnuje integracji z branchy/worktree.
- Po scaleniu uruchom końcowe testy i quality gates.
- Podsumuj, którzy subagenci byli użyci i jakie obszary pozostały nieweryfikowane.

## Checklista
- [ ] Czy zadanie da się podzielić?
- [ ] Czy części są niezależne?
- [ ] Czy subagenci nie będą zmieniać tych samych plików?
- [ ] Czy każdy ma wyraźny zakres?
- [ ] Czy określono wynik?
- [ ] Czy określono pliki do analizy i modyfikacji?
- [ ] Czy zaplanowano integrację?
- [ ] Czy zaplanowano końcowe testy?
- [ ] Czy wiadomo, kto rozwiązuje sprzeczne rekomendacje?

## Zakazane działania
- Uruchamianie wszystkich subagentów bez potrzeby.
- Delegowanie odpowiedzialności za finalny wynik poza Lead Agenta.
- Pozwalanie subagentom na niekontrolowane rozszerzenie zakresu.
- Równoległa edycja tych samych plików bez branchy lub worktree.
- Pomijanie końcowych testów po integracji.

## Oczekiwany format wyniku
- użyte role,
- podział pracy,
- rekomendacje przyjęte i odrzucone,
- konflikty i sposób rozwiązania,
- wykonane testy,
- nieweryfikowane obszary.

## Decyzje otwarte
- Czy wyniki subagentów mają być zapisywane jako artefakty PR.
