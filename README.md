# Wedding Gallery Platform

## Cel dokumentu
Glowny punkt wejscia do repozytorium i dokumentacji projektowej planowanej platformy webowej do obslugi wielu prywatnych wydarzen, w szczegolnosci wesel.

## Status dokumentu
- Status: draft
- Zakres: opis repozytorium, wizji produktu i mapy dokumentacji
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Repozytorium zawiera backend Spring Boot, frontend React/Vite oraz podstawowe pliki uruchomieniowe dla Dockera i Nginx.
- Etap 1 zostal uruchomiony i repo ma dzialajacy szkielet techniczny zgodny z aktualnym stackiem.
- Etap 2 jest domkniety w zakresie podstawowej identity: istnieje logowanie i wylogowanie oparte o sesje, CSRF dla SPA, endpoint `GET /api/auth/me`, administracyjne tworzenie kont, blokada po wielu blednych logowaniach oraz audit eventy auth.
- Etap 3 jest domkniety w minimalnym zakresie `EVENT-001` i `MEMBER-001`: wydarzenia maja ownera, managerow, izolacje ownership, audyt oraz flow UI/API/E2E.
- Etap 4 jest domkniety w zakresie `GALLERY-001`: wiele galerii na wydarzenie ma bezpieczne management API, stabilne slugi, kolejnosc, lifecycle, soft delete, audyt oraz flow UI/E2E dla ownera i managera.
- Nie wszystkie elementy docelowego systemu sa jeszcze zaimplementowane end-to-end.

## Stan docelowy
- Modularny monolit w monorepo.
- Platforma wielouzytkownikowa obslugujaca wiele kont, wydarzen, galerii, upload plikow, moderacje, storage, panel administratora i operacje produkcyjne.
- Dokumentacja w katalogu `docs/` jest nadrzednym zrodlem wiedzy o planowanym systemie.

## Czym jest system
System sluzy do zbierania, organizowania i bezpiecznego udostepniania zdjec oraz filmow z wesel i innych prywatnych wydarzen. Para mloda lub inny organizator bedzie zarzadzac swoim kontem, wydarzeniami i galeriami, a goscie beda przesylac materialy przez link, kod QR lub token dostepu bez koniecznosci rejestracji.

## Najwazniejsze zalozenia
- Projekt od poczatku zaklada obsluge wielu niezaleznych uzytkownikow i wydarzen.
- Dokumentacja opisuje pelna wizje produktu, a nie tylko MVP.
- Backend obecnie: Java 25, Spring Boot 4, Spring Security, Spring Data JPA i MySQL.
- Frontend obecnie: React, TypeScript, Vite i Material UI jako glowny system UI.
- Identity obecnie: sesje serwerowe, CSRF dla SPA, admin-only tworzenie kont, logout, blokada po blednych logowaniach i podstawowy audyt auth.
- Domena obecnie: prywatne wydarzenia, role kontekstowe `OWNER`/`MANAGER`, bezposrednie membership istniejacych kont, transfer ownership oraz uwierzytelnione zarzadzanie wieloma galeriami z lifecycle i soft delete.
- Infrastruktura docelowo: Docker Compose, Nginx, Linux VPS, lokalny storage z mozliwoscia przejscia na storage obiektowy.

## Mapa dokumentacji
- Start dla agentow i programistow: [AGENTS.md](AGENTS.md)
- Wizja produktu: [docs/product/PRODUCT_VISION.md](docs/product/PRODUCT_VISION.md)
- Role i uprawnienia: [docs/product/USER_ROLES.md](docs/product/USER_ROLES.md), [docs/product/PERMISSIONS_MATRIX.md](docs/product/PERMISSIONS_MATRIX.md)
- Architektura systemu: [docs/architecture/SYSTEM_ARCHITECTURE.md](docs/architecture/SYSTEM_ARCHITECTURE.md)
- Model danych: [docs/architecture/DATA_MODEL.md](docs/architecture/DATA_MODEL.md)
- API: [docs/backend/API_ENDPOINTS.md](docs/backend/API_ENDPOINTS.md)
- Kontrakt FE-BE: [api-contract/API_CONTRACT.md](api-contract/API_CONTRACT.md)
- Bezpieczenstwo: [docs/security/SECURITY_REQUIREMENTS.md](docs/security/SECURITY_REQUIREMENTS.md)
- Operacje: [docs/operations/DEPLOYMENT.md](docs/operations/DEPLOYMENT.md)
- CI/CD: [docs/operations/CI_CD_STRATEGY.md](docs/operations/CI_CD_STRATEGY.md)
- ADR: [docs/adr/README.md](docs/adr/README.md)

## Planowana struktura repozytorium
```text
wedding-gallery-platform/
|-- backend/
|-- frontend/
|-- nginx/
|-- scripts/
|-- docs/
|-- api-contract/
|-- docker-compose.yml
|-- docker-compose.prod.yml
|-- .env.example
|-- .gitignore
|-- AGENTS.md
`-- README.md
```

Aktualna struktura i plan dalszego rozwoju znajduja sie w [docs/architecture/REPOSITORY_STRUCTURE.md](docs/architecture/REPOSITORY_STRUCTURE.md).

## Jak czytac dokumentacje
1. Przeczytaj [AGENTS.md](AGENTS.md).
2. Przejdz przez dokumenty produktowe w `docs/product/`.
3. Przeczytaj dokumenty architektoniczne w `docs/architecture/`.
4. Sprawdz standardy backendu, frontendu, bezpieczenstwa, testow i operacji.
5. Zanim podejmiesz decyzje implementacyjna, sprawdz [ADR](docs/adr/README.md).
6. Przy zadaniach zlozonych sprawdz [docs/development/SUBAGENT_ORCHESTRATION.md](docs/development/SUBAGENT_ORCHESTRATION.md).

## Decyzje otwarte
- Rozszerzenie Etapu 4 poza gotowy zakres `GALLERY-001` o publiczny dostep po zaakceptowaniu ADR 0010.
- Konkretny mechanizm background jobs w pierwszej wersji implementacyjnej.
- Zakres pierwszego wydania produkcyjnego wzgledem funkcji rozszerzonych.

## Powiazane dokumenty
- [docs/product/FEATURE_ROADMAP.md](docs/product/FEATURE_ROADMAP.md)
- [docs/DEVELOPMENT_RULES.md](docs/DEVELOPMENT_RULES.md)
- [docs/DEFINITION_OF_DONE.md](docs/DEFINITION_OF_DONE.md)
- [docs/development/GIT_HOOKS.md](docs/development/GIT_HOOKS.md)

## Lokalne hooki Git
Aktywacja repozytoryjnego `pre-commit`:

```powershell
git config core.hooksPath .githooks
```

Hook przed commitem uruchamia:
- `frontend`: `npm run format:check`
- `backend`: `mvnw.cmd -B spotless:check`
