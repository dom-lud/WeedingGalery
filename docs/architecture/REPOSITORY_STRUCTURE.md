# Struktura Repozytorium

## Cel dokumentu
Opisuje docelową strukturę monorepo oraz przeznaczenie katalogów i plików głównych.

## Status dokumentu
- Status: draft
- Zakres: struktura repozytorium docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Repozytorium zawiera część katalogów docelowych i podstawowe szkielety technologiczne.
- Nie wszystkie katalogi mają już docelową zawartość.

## Stan docelowy
```text
wedding-gallery-platform/
├── backend/
├── frontend/
├── nginx/
├── infrastructure/
├── scripts/
├── docs/
│   ├── product/
│   ├── architecture/
│   ├── backend/
│   ├── frontend/
│   ├── security/
│   ├── testing/
│   ├── operations/
│   └── adr/
├── docker-compose.yml
├── docker-compose.prod.yml
├── .env.example
├── .gitignore
├── AGENTS.md
└── README.md
```

## Opis katalogów
| Ścieżka | Przeznaczenie |
| --- | --- |
| `backend/` | Kod aplikacji backendowej Spring Boot, testy, migracje i konfiguracja |
| `frontend/` | Kod SPA React/TypeScript, testy frontendu i assets |
| `nginx/` | Konfiguracja reverse proxy i serwowania frontendu |
| `infrastructure/` | Artefakty infrastrukturalne wykraczające poza lokalne compose, np. przykładowe deployment notes |
| `scripts/` | Skrypty pomocnicze do operacji developerskich i administracyjnych |
| `docs/` | Dokumentacja projektowa i operacyjna |
| `docs/product/` | Wizja produktu, role, wymagania, roadmapa |
| `docs/architecture/` | Architektura systemu, dane, storage, multi-tenancy |
| `docs/backend/` | Standardy backendowe, API, błędy, bezpieczeństwo backendu |
| `docs/frontend/` | Architektura frontendu, routing, UX uploadu, dostępność |
| `docs/security/` | Wymagania bezpieczeństwa, threat model, retencja danych |
| `docs/testing/` | Strategia testów i quality gates |
| `docs/operations/` | Wdrożenie, konfiguracja, monitoring, backup, maintenance |
| `docs/adr/` | Architecture Decision Records |

## Opis plików głównych
| Plik | Przeznaczenie |
| --- | --- |
| `README.md` | Główny opis repozytorium i mapa dokumentacji |
| `AGENTS.md` | Punkt wejścia dla agentów AI i nowych osób |
| `.env.example` | Dokumentacja zmiennych środowiskowych bez sekretów |
| `docker-compose.yml` | Planowana konfiguracja lokalna i developerska |
| `docker-compose.prod.yml` | Planowana konfiguracja produkcyjna lub preprodukcyjna |
| `.gitignore` | Zasady ignorowania plików lokalnych i build artifacts |

## Zasady utrzymania struktury
- Dokumentacja musi wyprzedzać większe zmiany architektoniczne.
- Nowe katalogi wymagają opisu w tym dokumencie lub w ADR.
- Nie umieszczamy sekretów ani danych produkcyjnych w repozytorium.

## Powiązane dokumenty
- [../DEVELOPMENT_RULES.md](../DEVELOPMENT_RULES.md)
- [../operations/ENVIRONMENTS.md](../operations/ENVIRONMENTS.md)
- [../operations/CONFIGURATION.md](../operations/CONFIGURATION.md)

## Decyzje otwarte
- Czy `infrastructure/` będzie trzymać wyłącznie dokumenty i przykłady, czy również artefakty deploymentowe poza Docker Compose.
