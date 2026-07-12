# Checklist Frontend Review

## Cel dokumentu
Lista kontrolna dla przeglądu zmian frontendowych.

## Status dokumentu
- Status: draft
- Zakres: frontend review checklist
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Checklista służy do manualnej oceny zmian UI.

## Stan docelowy
- Frontend jest oceniany nie tylko wizualnie, ale też pod kątem stanów, dostępności i integracji.

## Lista kontrolna
- [ ] TypeScript jest poprawny i spójny z istniejącym stylem.
- [ ] UI korzysta z Material UI jako domyślnej biblioteki komponentów.
- [ ] Nie dodano drugiej pełnej biblioteki UI bez zaakceptowanego ADR.
- [ ] Komponenty i style stosują hierarchię MUI: komponent, props, `sx`, `styled()`, komponent domenowy, theme overrides, własny CSS jako wyjątek.
- [ ] Theme jest używany zamiast hardcoded kolorów, spacingów i fontów, jeśli to możliwe.
- [ ] Mobile-first i responsywność zostały uwzględnione.
- [ ] Layout używa `Container`, `Box`, `Stack`, `Grid`, `Drawer`, `AppBar`, `Paper`, `Card`, `Dialog` lub innych odpowiednich komponentów MUI.
- [ ] Warstwa API została użyta zamiast rozproszonego `fetch`.
- [ ] Loading state istnieje.
- [ ] Error state istnieje.
- [ ] Empty state istnieje, jeśli dotyczy.
- [ ] Retry, forbidden i offline state zostały uwzględnione, jeśli dotyczą przepływu.
- [ ] Accessibility i obsługa klawiatury zostały uwzględnione.
- [ ] Przyciski ikonowe mają `aria-label`, a focus state i dialogi są dostępne.
- [ ] Guardy auth i ownership w UI są poprawne.
- [ ] Testy komponentów lub widoków zostały zaktualizowane.
- [ ] Dokumentacja została zaktualizowana, jeśli zmiana wpływa na UX lub routing.

## Powiązane dokumenty
- [../frontend/FRONTEND_ARCHITECTURE.md](../frontend/FRONTEND_ARCHITECTURE.md)
- [../frontend/UI_SYSTEM.md](../frontend/UI_SYSTEM.md)
- [../prompts/FRONTEND_FEATURE.md](../prompts/FRONTEND_FEATURE.md)

## Decyzje otwarte
- Czy dla publicznej galerii potrzebna jest osobna checklista wydajności mobilnej.
