# Documentation Update

## Cel dokumentu
Skill wspierający aktualizację dokumentacji bez powielania treści.

## Status dokumentu
- Status: draft
- Zakres: documentation update skill
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Skill pomaga utrzymać dokumentację projektu jako źródło prawdy.

## Stan docelowy
- Agent używa go przy każdej zmianie wpływającej na architekturę, API, dane, proces lub backlog.

## Kiedy używać
- Przy aktualizacji dokumentów domenowych, API, modelu danych, roadmapy, backlogu lub ADR.

## Wymagane dokumenty
- `AGENTS.md`
- `docs/DOCUMENTATION_MAP.md`
- `docs/development/WORKFLOW.md`
- `docs/DEFINITION_OF_DONE.md`
- odpowiednie dokumenty domenowe i ADR

## Wymagane kroki
1. Zidentyfikuj źródłowe dokumenty do zmiany.
2. Zaktualizuj dokumenty domenowe, API, model danych lub roadmapę, jeśli dotyczy.
3. Zaktualizuj ADR, jeśli decyzja się zmieniła.
4. Usuń lub ogranicz duplikację treści.
5. Sprawdź linki względne.

## Checklista
- [ ] Dokumenty domenowe są aktualne.
- [ ] API jest aktualne, jeśli dotyczy.
- [ ] Model danych jest aktualny, jeśli dotyczy.
- [ ] Roadmapa lub backlog są aktualne, jeśli dotyczy.
- [ ] ADR jest aktualny, jeśli dotyczy.
- [ ] Treść nie została zdublowana.

## Zakazane działania
- Twierdzenie, że funkcja istnieje, jeśli nie została wdrożona.
- Kopiowanie dużych sekcji między dokumentami zamiast linkowania.
- Pomijanie aktualizacji dokumentacji po zmianie architektonicznej.

## Oczekiwany format wyniku
- zaktualizowane pliki,
- zakres zmian dokumentacyjnych,
- linki powiązane,
- otwarte decyzje.

## Decyzje otwarte
- Czy dodać kontrolę spójności dokumentacji jako osobny etap CI.
