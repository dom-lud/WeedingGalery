# Prompt: Incident Analysis

## Cel dokumentu
Szablon promptu do analizy incydentu operacyjnego lub bezpieczeństwa.

## Status dokumentu
- Status: draft
- Zakres: prompt dla analizy incydentu
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Szablon wspiera analizę incydentów, ale nie zastępuje procedur operacyjnych.

## Stan docelowy
- Każdy incydent ma uporządkowaną analizę przyczyny, skutków i działań naprawczych.

## Szablon promptu
```md
Przeanalizuj incydent: [opis / identyfikator].

Przed pracą przeczytaj:
- AGENTS.md
- docs/operations/INCIDENT_RESPONSE.md
- docs/operations/LOGGING.md
- docs/operations/MONITORING.md
- docs/checklists/INCIDENT_CHECKLIST.md
- [powiązane dokumenty domenowe]

Zasady analizy:
- oddziel fakty od hipotez,
- wskaż zakres wpływu, przyczynę źródłową i działania ograniczające,
- oceń wpływ na bezpieczeństwo, prywatność, storage, monitoring i backup.

Zakres:
- [okno czasowe / systemy / moduły]

Poza zakresem:
- niepotwierdzone założenia przedstawiane jako fakty.

Wymagania testowe:
- scenariusz reprodukcji albo test regresyjny, jeśli możliwy.

Wymagania bezpieczeństwa:
- uwzględnij dane osobowe, auth, ownership i audyt.

Format końcowego podsumowania:
- objaw,
- przyczyna,
- wpływ,
- działania natychmiastowe,
- działania długoterminowe,
- luki w obserwowalności.
```

## Powiązane dokumenty
- [../operations/INCIDENT_RESPONSE.md](../operations/INCIDENT_RESPONSE.md)
- [../checklists/INCIDENT_CHECKLIST.md](../checklists/INCIDENT_CHECKLIST.md)

## Decyzje otwarte
- Czy analizy incydentów będą utrzymywane jako osobne dokumenty w repozytorium.
