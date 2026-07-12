# ADR 0011: Material UI jako główny system UI frontendu

## Status
accepted

## Kontekst
Frontend będzie obejmował publiczną stronę wydarzenia, publiczną galerię, upload, panele użytkowników, panel współzarządzającego, panel administratora, ekrany auth, ustawienia i statystyki. Projekt potrzebuje jednego spójnego systemu UI, który ograniczy liczbę decyzji stylistycznych, przyspieszy implementację i ułatwi pracę agentów AI.

## Decyzja
Material UI jest jedyną główną biblioteką komponentów UI w frontendzie.

Implementacja UI ma stosować zasadę: MUI-first, custom CSS only by exception. Wszystkie ekrany korzystają ze wspólnego `ThemeProvider`, a theme jest centralnym źródłem kolorów, typografii, spacingu, breakpointów, cieni, promieni zaokrągleń, wariantów i override'ów.

Nie dodajemy Bootstrap, Ant Design, Chakra UI, Mantine, Tailwind UI ani innej pełnej biblioteki UI bez nowego zaakceptowanego ADR.

## Konsekwencje
- Frontend ma spójniejszy wygląd i mniej ręcznego CSS.
- Nowe komponenty domenowe muszą być budowane na MUI, o ile nie istnieje udokumentowany wyjątek.
- Publiczna galeria nie może pozostać w domyślnym stylu Material Design; wymaga dopracowanego theme i wariantów komponentów.
- Wyjątki dla specjalistycznych funkcji, np. lightboxa, virtualizacji, resumable uploadu lub odtwarzacza wideo, wymagają uzasadnienia w dokumentacji lub ADR.

## Alternatywy
- Własny system komponentów od zera: odrzucony z powodu kosztu utrzymania i ryzyka niespójności.
- Tailwind plus komponenty własne: odrzucone jako równoległy system stylistyczny wymagający dodatkowej dyscypliny i większej liczby decyzji projektowych.
- Inna pełna biblioteka UI: odrzucona na ten etap, żeby nie rozpraszać standardu frontendu.

## Powiązane dokumenty
- [../frontend/UI_SYSTEM.md](../frontend/UI_SYSTEM.md)
- [../frontend/UI_COMPONENTS.md](../frontend/UI_COMPONENTS.md)
- [../frontend/FRONTEND_ARCHITECTURE.md](../frontend/FRONTEND_ARCHITECTURE.md)
