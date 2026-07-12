# Struktura Repozytorium

## Cel dokumentu
Opisuje aktualną strukturę repozytorium oraz planowany kierunek jej rozwoju.

## Status dokumentu
- Status: draft
- Zakres: struktura repozytorium docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Repozytorium zawiera katalogi `backend/`, `frontend/`, `nginx/`, `scripts/` i `docs/`.
- W repozytorium nie ma obecnie katalogu `infrastructure/`; wzmianki o nim należy traktować jako plan, nie jako stan zaimplementowany.
- Nie wszystkie istniejące katalogi mają już docelową zawartość.

## Stan docelowy
```text
wedding-gallery-platform/
├── backend/
├── frontend/
├── nginx/
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
| `backend/` | Kod aplikacji backendowej Spring Boot, testy i konfiguracja; obecnie bez wdrożonego systemu migracji schematu |
| `frontend/` | Kod SPA React/TypeScript, testy frontendu i assets |
| `nginx/` | Konfiguracja reverse proxy i serwowania frontendu |
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
| `docker-compose.yml` | Aktualny szkic konfiguracji lokalnej z usługami `mysql`, `backend`, `frontend` i `nginx` |
| `docker-compose.prod.yml` | Aktualny szkic konfiguracji produkcyjnej lub preprodukcyjnej |
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
- Czy w repozytorium w ogóle powstanie osobny katalog `infrastructure/`, czy artefakty operacyjne pozostaną przy `docker-compose*`, `nginx/` i dokumentacji.
