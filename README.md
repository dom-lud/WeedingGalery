# Wedding Gallery Platform

## Cel dokumentu
Główny punkt wejścia do repozytorium i dokumentacji projektowej planowanej platformy webowej do obsługi wielu prywatnych wydarzeń, w szczególności wesel.

## Status dokumentu
- Status: draft
- Zakres: opis repozytorium, wizji produktu i mapy dokumentacji
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Repozytorium zawiera szkic backendu Spring Boot, szkic frontendu React/Vite oraz podstawowe pliki uruchomieniowe dla Dockera i Nginx.
- Brak gotowej implementacji docelowego systemu opisanego w dokumentacji.
- Część istniejących plików technicznych nie odpowiada jeszcze docelowemu stackowi i nie powinna być traktowana jako decyzja końcowa.

## Stan docelowy
- Modularny monolit w monorepo.
- Platforma wieloużytkownikowa obsługująca wiele kont, wydarzeń, galerii, upload plików, moderację, storage, panel administratora i operacje produkcyjne.
- Dokumentacja w katalogu `docs/` jest nadrzędnym źródłem wiedzy o planowanym systemie.

## Czym jest system
System służy do zbierania, organizowania i bezpiecznego udostępniania zdjęć oraz filmów z wesel i innych prywatnych wydarzeń. Para młoda lub inny organizator zakłada konto, tworzy wydarzenie i galerie, a goście przesyłają materiały przez link, kod QR lub token dostępu bez konieczności rejestracji.

## Najważniejsze założenia
- Projekt od początku zakłada obsługę wielu niezależnych użytkowników i wydarzeń.
- Dokumentacja opisuje pełną wizję produktu, a nie tylko MVP.
- Backend obecnie: Java 25, Spring Boot 4, Spring Security, Spring Data JPA i MySQL.
- Backend docelowo: modularny monolit Spring Boot rozwijany na bazie aktualnego szkieletu; ewentualna zmiana silnika bazy lub narzędzia migracji wymaga decyzji architektonicznej i zmian w kodzie.
- Frontend docelowo: React, TypeScript, Vite, React Router, mobile-first.
- Infrastruktura docelowo: Docker Compose, Nginx, Linux VPS, lokalny storage z możliwością przejścia na storage obiektowy.

## Mapa dokumentacji
- Start dla agentów i programistów: [AGENTS.md](AGENTS.md)
- Wizja produktu: [docs/product/PRODUCT_VISION.md](docs/product/PRODUCT_VISION.md)
- Role i uprawnienia: [docs/product/USER_ROLES.md](docs/product/USER_ROLES.md), [docs/product/PERMISSIONS_MATRIX.md](docs/product/PERMISSIONS_MATRIX.md)
- Architektura systemu: [docs/architecture/SYSTEM_ARCHITECTURE.md](docs/architecture/SYSTEM_ARCHITECTURE.md)
- Model danych: [docs/architecture/DATA_MODEL.md](docs/architecture/DATA_MODEL.md)
- API: [docs/backend/API_ENDPOINTS.md](docs/backend/API_ENDPOINTS.md)
- Bezpieczeństwo: [docs/security/SECURITY_REQUIREMENTS.md](docs/security/SECURITY_REQUIREMENTS.md)
- Operacje: [docs/operations/DEPLOYMENT.md](docs/operations/DEPLOYMENT.md)
- CI/CD: [docs/operations/CI_CD_STRATEGY.md](docs/operations/CI_CD_STRATEGY.md)
- ADR: [docs/adr/README.md](docs/adr/README.md)

## Planowana struktura repozytorium
```text
wedding-gallery-platform/
├── backend/
├── frontend/
├── nginx/
├── scripts/
├── docs/
├── docker-compose.yml
├── docker-compose.prod.yml
├── .env.example
├── .gitignore
├── AGENTS.md
└── README.md
```

Aktualna struktura i plan dalszego rozwoju znajdują się w [docs/architecture/REPOSITORY_STRUCTURE.md](docs/architecture/REPOSITORY_STRUCTURE.md).

## Jak czytać dokumentację
1. Przeczytaj [AGENTS.md](AGENTS.md).
2. Przejdź przez dokumenty produktowe w `docs/product/`.
3. Przeczytaj dokumenty architektoniczne w `docs/architecture/`.
4. Sprawdź standardy backendu, frontendu, bezpieczeństwa, testów i operacji.
5. Zanim podejmiesz decyzję implementacyjną, sprawdź [ADR](docs/adr/README.md).
6. Przy zadaniach złożonych sprawdź [docs/development/SUBAGENT_ORCHESTRATION.md](docs/development/SUBAGENT_ORCHESTRATION.md).

## Decyzje otwarte
- Ostateczny wybór strategii uwierzytelniania: sesja serwerowa vs JWT.
- Konkretny mechanizm background jobs w pierwszej wersji implementacyjnej.
- Zakres pierwszego wydania produkcyjnego względem funkcji rozszerzonych.

## Powiązane dokumenty
- [docs/product/FEATURE_ROADMAP.md](docs/product/FEATURE_ROADMAP.md)
- [docs/DEVELOPMENT_RULES.md](docs/DEVELOPMENT_RULES.md)
- [docs/DEFINITION_OF_DONE.md](docs/DEFINITION_OF_DONE.md)
