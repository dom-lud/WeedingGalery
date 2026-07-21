# Mapa Dokumentacji

## Cel dokumentu

Zbiera wszystkie kluczowe dokumenty projektu, wskazuje ich przeznaczenie, odbiorców i moment aktualizacji.

## Status dokumentu

- Status: draft
- Zakres: mapa dokumentacji, checklist, promptów, ADR i skills
- Ostatnia aktualizacja: 2026-07-20

## Stan obecny

- Dokumentacja opisuje planowany system i proces pracy, ale czesc dokumentow zawiera juz takze precyzyjny stan zaimplementowany.
- Przy zmianach identity, auth, testow i audytu trzeba utrzymywac spojnosc miedzy dokumentami produktowymi, backendowymi, operacyjnymi i skillami.

## Stan docelowy

- Nowa osoba lub agent AI może szybko znaleźć właściwy dokument do konkretnego typu zadania.

## Produkt

| Dokument                    | Przeznaczenie                | Kto czyta                | Kiedy aktualizować                           | Link                                                                                  |
| --------------------------- | ---------------------------- | ------------------------ | -------------------------------------------- | ------------------------------------------------------------------------------------- |
| Product vision              | Wizja i granice produktu     | wszyscy                  | przy zmianie wizji                           | [docs/product/PRODUCT_VISION.md](product/PRODUCT_VISION.md)                           |
| User roles                  | Role systemowe i wydarzenia  | backend, frontend, QA    | przy zmianie ról                             | [docs/product/USER_ROLES.md](product/USER_ROLES.md)                                   |
| User journeys               | Główne przepływy użytkownika | frontend, backend, QA    | przy zmianie procesu                         | [docs/product/USER_JOURNEYS.md](product/USER_JOURNEYS.md)                             |
| Functional requirements     | Docelowe funkcje systemu     | wszyscy                  | przy zmianie zakresu produktu                | [docs/product/FUNCTIONAL_REQUIREMENTS.md](product/FUNCTIONAL_REQUIREMENTS.md)         |
| Non-functional requirements | Wymagania jakościowe         | architekt, backend, ops  | przy zmianie wymagań jakościowych            | [docs/product/NON_FUNCTIONAL_REQUIREMENTS.md](product/NON_FUNCTIONAL_REQUIREMENTS.md) |
| Permissions matrix          | Macierz uprawnień            | backend, security, QA    | przy zmianie auth                            | [docs/product/PERMISSIONS_MATRIX.md](product/PERMISSIONS_MATRIX.md)                   |
| Feature roadmap             | Etapy implementacji          | product, tech lead, AI   | przy zmianie etapu, jego statusu lub zakresu | [docs/product/FEATURE_ROADMAP.md](product/FEATURE_ROADMAP.md)                         |
| Backlog                     | Zadania i epiki              | product, AI, programiści | przy dodaniu lub zmianie zadań               | [docs/product/BACKLOG.md](product/BACKLOG.md)                                         |
| Glossary                    | Terminologia projektu        | wszyscy                  | przy nowych pojęciach                        | [docs/product/GLOSSARY.md](product/GLOSSARY.md)                                       |

## Architektura

| Dokument             | Przeznaczenie             | Kto czyta               | Kiedy aktualizować          | Link                                                                              |
| -------------------- | ------------------------- | ----------------------- | --------------------------- | --------------------------------------------------------------------------------- |
| System architecture  | Widok systemu i przepływy | architekt, backend, ops | przy zmianach architektury  | [docs/architecture/SYSTEM_ARCHITECTURE.md](architecture/SYSTEM_ARCHITECTURE.md)   |
| Modules              | Granice modułów           | backend, architekt      | przy zmianach modułowych    | [docs/architecture/MODULES.md](architecture/MODULES.md)                           |
| Repository structure | Struktura monorepo        | wszyscy                 | przy zmianie struktury repo | [docs/architecture/REPOSITORY_STRUCTURE.md](architecture/REPOSITORY_STRUCTURE.md) |
| Data model           | Encje, relacje, ownership | backend, DBA, QA        | przy zmianie modelu danych  | [docs/architecture/DATA_MODEL.md](architecture/DATA_MODEL.md)                     |
| File storage         | Abstrakcja storage        | backend, ops, security  | przy zmianie storage        | [docs/architecture/FILE_STORAGE.md](architecture/FILE_STORAGE.md)                 |
| Background jobs      | Zadania asynchroniczne    | backend, ops            | przy zmianie pipeline'u     | [docs/architecture/BACKGROUND_JOBS.md](architecture/BACKGROUND_JOBS.md)           |
| Multi tenancy        | Izolacja danych           | backend, security       | przy zmianie ownership      | [docs/architecture/MULTI_TENANCY.md](architecture/MULTI_TENANCY.md)               |
| Integrations         | Integracje zewnętrzne     | backend, ops            | przy nowych integracjach    | [docs/architecture/INTEGRATIONS.md](architecture/INTEGRATIONS.md)                 |

## Backend

| Dokument                         | Przeznaczenie                                             | Kto czyta             | Kiedy aktualizować                                       | Link                                                                                            |
| -------------------------------- | --------------------------------------------------------- | --------------------- | -------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| Backend guidelines               | Standard implementacji backendu                           | backend, AI           | przy zmianie standardów                                  | [docs/backend/BACKEND_GUIDELINES.md](backend/BACKEND_GUIDELINES.md)                             |
| API conventions                  | Konwencje REST                                            | backend, frontend     | przy zmianie kontraktów                                  | [docs/backend/API_CONVENTIONS.md](backend/API_CONVENTIONS.md)                                   |
| API endpoints                    | Katalog endpointów                                        | backend, frontend, QA | przy dodaniu endpointów                                  | [docs/backend/API_ENDPOINTS.md](backend/API_ENDPOINTS.md)                                       |
| Database conventions             | Zasady schematu, migracji i pracy z relacyjną bazą danych | backend, DBA          | przy zmianie zasad danych                                | [docs/backend/DATABASE_CONVENTIONS.md](backend/DATABASE_CONVENTIONS.md)                         |
| Error handling                   | Format błędów                                             | backend, frontend     | przy zmianie błędów API                                  | [docs/backend/ERROR_HANDLING.md](backend/ERROR_HANDLING.md)                                     |
| Authentication and authorization | Model authN/authZ oraz aktualny stan Etapu 2              | backend, security     | przy zmianie auth, sesji, rol lub audit eventow identity | [docs/backend/AUTHENTICATION_AND_AUTHORIZATION.md](backend/AUTHENTICATION_AND_AUTHORIZATION.md) |
| Media processing                 | Pipeline mediów                                           | backend, ops          | przy zmianie processingu                                 | [docs/backend/MEDIA_PROCESSING.md](backend/MEDIA_PROCESSING.md)                                 |

## Frontend

| Dokument                      | Przeznaczenie                                  | Kto czyta            | Kiedy aktualizować                        | Link                                                                                        |
| ----------------------------- | ---------------------------------------------- | -------------------- | ----------------------------------------- | ------------------------------------------------------------------------------------------- |
| Frontend architecture         | Architektura SPA                               | frontend, AI         | przy zmianie struktury UI                 | [docs/frontend/FRONTEND_ARCHITECTURE.md](frontend/FRONTEND_ARCHITECTURE.md)                 |
| CSS and responsive guidelines | Szczegółowe zasady mobile-first, CSS i layoutu | frontend, AI, QA     | przy zmianie zasad CSS lub responsywności | [docs/frontend/CSS_AND_RESPONSIVE_GUIDELINES.md](frontend/CSS_AND_RESPONSIVE_GUIDELINES.md) |
| Routing                       | Trasy i guardy                                 | frontend             | przy zmianie routingu                     | [docs/frontend/ROUTING.md](frontend/ROUTING.md)                                             |
| State management              | Zarządzanie stanem                             | frontend             | przy zmianie modelu stanu                 | [docs/frontend/STATE_MANAGEMENT.md](frontend/STATE_MANAGEMENT.md)                           |
| UI system                     | Decyzja MUI-first, theme i wyjątki UI          | frontend, AI, design | przy zmianie systemu UI                   | [docs/frontend/UI_SYSTEM.md](frontend/UI_SYSTEM.md)                                         |
| UI components                 | Zestaw komponentów                             | frontend, design     | przy zmianie biblioteki UI                | [docs/frontend/UI_COMPONENTS.md](frontend/UI_COMPONENTS.md)                                 |
| Upload UX                     | Doświadczenie uploadu                          | frontend, product    | przy zmianie flow uploadu                 | [docs/frontend/UPLOAD_UX.md](frontend/UPLOAD_UX.md)                                         |
| Accessibility                 | Wymagania dostępności                          | frontend, QA         | przy zmianie krytycznych widoków          | [docs/frontend/ACCESSIBILITY.md](frontend/ACCESSIBILITY.md)                                 |
| Responsive design             | Zasady mobile-first                            | frontend             | przy zmianie layoutów                     | [docs/frontend/RESPONSIVE_DESIGN.md](frontend/RESPONSIVE_DESIGN.md)                         |

## Bezpieczeństwo

| Dokument                   | Przeznaczenie                  | Kto czyta                | Kiedy aktualizować             | Link                                                                                  |
| -------------------------- | ------------------------------ | ------------------------ | ------------------------------ | ------------------------------------------------------------------------------------- |
| Security requirements      | Baseline bezpieczeństwa        | backend, frontend, ops   | przy zmianie security baseline | [docs/security/SECURITY_REQUIREMENTS.md](security/SECURITY_REQUIREMENTS.md)           |
| File upload security       | Zasady upload security         | backend, security        | przy zmianie uploadu           | [docs/security/FILE_UPLOAD_SECURITY.md](security/FILE_UPLOAD_SECURITY.md)             |
| Threat model               | Główne zagrożenia              | security, architekt, ops | przy nowych ryzykach           | [docs/security/THREAT_MODEL.md](security/THREAT_MODEL.md)                             |
| Privacy and data retention | Prywatność i retencja          | backend, ops, security   | przy zmianie retencji          | [docs/security/PRIVACY_AND_DATA_RETENTION.md](security/PRIVACY_AND_DATA_RETENTION.md) |
| Security checklist         | Lista kontrolna bezpieczeństwa | reviewer, ops            | przy review lub release        | [docs/security/SECURITY_CHECKLIST.md](security/SECURITY_CHECKLIST.md)                 |

## Testowanie

| Dokument                        | Przeznaczenie                                                                            | Kto czyta                       | Kiedy aktualizować                                        | Link                                                                                          |
| ------------------------------- | ---------------------------------------------------------------------------------------- | ------------------------------- | --------------------------------------------------------- | --------------------------------------------------------------------------------------------- |
| Test strategy                   | Strategia testów                                                                         | backend, frontend, QA           | przy zmianie modelu testów                                | [docs/testing/TEST_STRATEGY.md](testing/TEST_STRATEGY.md)                                     |
| Backend testing                 | Zasady testów backendu                                                                   | backend                         | przy zmianie standardów testów backendu                   | [docs/testing/BACKEND_TESTING.md](testing/BACKEND_TESTING.md)                                 |
| Frontend testing                | Zasady testow frontendu                                                                  | frontend                        | przy zmianie standardow testow UI lub page object pattern | [docs/testing/FRONTEND_TESTING.md](testing/FRONTEND_TESTING.md)                               |
| E2E scenarios                   | Kluczowe scenariusze E2E                                                                 | QA, frontend, backend           | przy zmianie krytycznych flow auth, UX lub kontraktu      | [docs/testing/E2E_SCENARIOS.md](testing/E2E_SCENARIOS.md)                                     |
| Stage 4/5 test matrix           | Powiazanie wymagan galerii, public access, uploadu i storage z testami                   | QA, backend, frontend, security | przy zmianie Etapow 4, 4B lub 5                           | [docs/testing/STAGE_4_5_TEST_MATRIX.md](testing/STAGE_4_5_TEST_MATRIX.md)                     |
| Stage 0-5 closure test brief    | Macierz ryzyk hardeningu zamykajacego Etapy 0-5                                          | QA, backend, security, ops      | przy zmianie closure sprintu 0-5                          | [docs/testing/STAGE_0_5_CLOSURE_TEST_BRIEF.md](testing/STAGE_0_5_CLOSURE_TEST_BRIEF.md)       |
| Coverage hardening test brief   | Macierz ryzyk i kryteria podnoszenia zapadek coverage                                    | QA, backend, frontend, AI       | przy zmianie progow lub istotnym rozszerzeniu testow      | [docs/testing/COVERAGE_HARDENING_TEST_BRIEF.md](testing/COVERAGE_HARDENING_TEST_BRIEF.md)     |
| Coverage 90 test brief          | Corner case'y i warunki podniesienia wszystkich metryk do 90%                            | QA, backend, frontend, security | przy rozszerzaniu testow i bramek do 90%                  | [docs/testing/COVERAGE_90_TEST_BRIEF.md](testing/COVERAGE_90_TEST_BRIEF.md)                   |
| CI quality reporting test brief | Ryzyka i weryfikacja raportow JSON, komentarzy PR, nightly quality oraz dashboardu Pages | QA, ops, AI                     | przy zmianie raportowania CI i dashboardu jakosci         | [docs/testing/CI_QUALITY_REPORTING_TEST_BRIEF.md](testing/CI_QUALITY_REPORTING_TEST_BRIEF.md) |
| Quality gates                   | Warunki jakości                                                                          | wszyscy                         | przy zmianie procesu release                              | [docs/testing/QUALITY_GATES.md](testing/QUALITY_GATES.md)                                     |

## Operacje

| Dokument           | Przeznaczenie                            | Kto czyta                  | Kiedy aktualizować                                    | Link                                                                      |
| ------------------ | ---------------------------------------- | -------------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------- |
| Deployment         | Model wdrożenia                          | ops, backend               | przy zmianie deploymentu                              | [docs/operations/DEPLOYMENT.md](operations/DEPLOYMENT.md)                 |
| CI/CD strategy     | Strategia GitHub Actions i quality gates | ops, backend, frontend, QA | przy zmianie pipeline lub quality gates               | [docs/operations/CI_CD_STRATEGY.md](operations/CI_CD_STRATEGY.md)         |
| Environments       | Środowiska uruchomieniowe                | ops, backend, frontend     | przy zmianie środowisk                                | [docs/operations/ENVIRONMENTS.md](operations/ENVIRONMENTS.md)             |
| Configuration      | Zasady konfiguracji                      | ops, backend               | przy zmianie env i sekretów                           | [docs/operations/CONFIGURATION.md](operations/CONFIGURATION.md)           |
| Backup and restore | Procedury backup i restore               | ops                        | przy zmianie backupu                                  | [docs/operations/BACKUP_AND_RESTORE.md](operations/BACKUP_AND_RESTORE.md) |
| Monitoring         | Monitoring i observability               | ops, backend               | przy zmianie metryk i alertów                         | [docs/operations/MONITORING.md](operations/MONITORING.md)                 |
| Logging            | Logi operacyjne i audytowe               | backend, ops               | przy zmianie logowania technicznego lub audit eventow | [docs/operations/LOGGING.md](operations/LOGGING.md)                       |
| Incident response  | Obsługa incydentów                       | ops, security              | przy zmianie procedur incydentowych                   | [docs/operations/INCIDENT_RESPONSE.md](operations/INCIDENT_RESPONSE.md)   |
| Maintenance        | Rutyny utrzymaniowe                      | ops                        | przy zmianie maintenance                              | [docs/operations/MAINTENANCE.md](operations/MAINTENANCE.md)               |

## Workflow i rozwój

| Dokument               | Przeznaczenie                           | Kto czyta               | Kiedy aktualizować                 | Link                                                                                |
| ---------------------- | --------------------------------------- | ----------------------- | ---------------------------------- | ----------------------------------------------------------------------------------- |
| Development rules      | Zasady rozwoju                          | wszyscy                 | przy zmianie zasad pracy           | [docs/DEVELOPMENT_RULES.md](DEVELOPMENT_RULES.md)                                   |
| Definition of Done     | Kryteria zakończenia zadania            | wszyscy                 | przy zmianie jakości procesu       | [docs/DEFINITION_OF_DONE.md](DEFINITION_OF_DONE.md)                                 |
| Contributing           | Zasady kontrybucji                      | programiści, AI         | przy zmianie procesu współpracy    | [docs/CONTRIBUTING.md](CONTRIBUTING.md)                                             |
| Workflow               | Standardowy flow zadania                | wszyscy                 | przy zmianie procesu wykonawczego  | [docs/development/WORKFLOW.md](development/WORKFLOW.md)                             |
| Subagent orchestration | Standard pracy Lead Agenta i subagentów | AI, reviewer, tech lead | przy zmianie orkiestracji agentów  | [docs/development/SUBAGENT_ORCHESTRATION.md](development/SUBAGENT_ORCHESTRATION.md) |
| Feature lifecycle      | Statusy funkcji                         | product, AI, tech lead  | przy zmianie lifecycle             | [docs/development/FEATURE_LIFECYCLE.md](development/FEATURE_LIFECYCLE.md)           |
| Definition of Ready    | Kryteria gotowości zadania              | product, AI, tech lead  | przy zmianie wejścia do realizacji | [docs/development/DEFINITION_OF_READY.md](development/DEFINITION_OF_READY.md)       |
| AI task planning       | Zasady planowania przez AI              | AI, reviewer            | przy zmianie standardu planów      | [docs/development/AI_TASK_PLANNING.md](development/AI_TASK_PLANNING.md)             |

## Checklisty

| Dokument                     | Przeznaczenie                                               | Kto czyta                      | Kiedy aktualizować                      | Link                                                                                          |
| ---------------------------- | ----------------------------------------------------------- | ------------------------------ | --------------------------------------- | --------------------------------------------------------------------------------------------- |
| Feature checklist            | Checklista funkcji                                          | autor, reviewer                | przy realizacji funkcji                 | [docs/checklists/FEATURE_CHECKLIST.md](checklists/FEATURE_CHECKLIST.md)                       |
| Stage 3 completion checklist | Wykonany DoD i dowody EVENT-001/MEMBER-001                  | autor, reviewer                | przy domknieciu Etapu 3                 | [docs/checklists/STAGE_3_COMPLETION_CHECKLIST.md](checklists/STAGE_3_COMPLETION_CHECKLIST.md) |
| Stage 4 readiness checklist  | DoR i granice pierwszego zakresu GALLERY-001                | autor, reviewer                | przed rozpoczeciem Etapu 4              | [docs/checklists/STAGE_4_READINESS_CHECKLIST.md](checklists/STAGE_4_READINESS_CHECKLIST.md)   |
| Stage 4 completion checklist | Historyczne dowody domkniecia GALLERY-001                   | autor, reviewer                | przy audycie Etapu 4                    | [docs/checklists/STAGE_4_COMPLETION_CHECKLIST.md](checklists/STAGE_4_COMPLETION_CHECKLIST.md) |
| Stage 5 completion checklist | Dowody domkniecia Etapow 4B/5                               | autor, reviewer                | przy domknieciu public access i uploadu | [docs/checklists/STAGE_5_COMPLETION_CHECKLIST.md](checklists/STAGE_5_COMPLETION_CHECKLIST.md) |
| Stage 0-5 closure checklist  | Dowody hardeningu i koncowej regresji zamykajacej Etapy 0-5 | autor, reviewer, security, ops | przed rozpoczeciem Etapu 6              | [docs/checklists/STAGE_0_5_CLOSURE_CHECKLIST.md](checklists/STAGE_0_5_CLOSURE_CHECKLIST.md)   |
| Code review checklist        | Checklista review                                           | reviewer                       | przy review kodu                        | [docs/checklists/CODE_REVIEW_CHECKLIST.md](checklists/CODE_REVIEW_CHECKLIST.md)               |
| Security review checklist    | Checklista security                                         | reviewer, security             | przy security review                    | [docs/checklists/SECURITY_REVIEW_CHECKLIST.md](checklists/SECURITY_REVIEW_CHECKLIST.md)       |
| Database change checklist    | Checklista zmian bazy                                       | backend, DBA                   | przy migracjach                         | [docs/checklists/DATABASE_CHANGE_CHECKLIST.md](checklists/DATABASE_CHANGE_CHECKLIST.md)       |
| API review checklist         | Checklista API                                              | backend, frontend              | przy projektowaniu API                  | [docs/checklists/API_REVIEW_CHECKLIST.md](checklists/API_REVIEW_CHECKLIST.md)                 |
| Frontend review checklist    | Checklista frontendu                                        | frontend, reviewer             | przy zmianach UI                        | [docs/checklists/FRONTEND_REVIEW_CHECKLIST.md](checklists/FRONTEND_REVIEW_CHECKLIST.md)       |
| Upload security checklist    | Checklista uploadu                                          | backend, security              | przy uploadach                          | [docs/checklists/UPLOAD_SECURITY_CHECKLIST.md](checklists/UPLOAD_SECURITY_CHECKLIST.md)       |
| Release checklist            | Checklista releasu                                          | ops, tech lead                 | przed releasem                          | [docs/checklists/RELEASE_CHECKLIST.md](checklists/RELEASE_CHECKLIST.md)                       |
| Deployment checklist         | Checklista wdrożenia                                        | ops                            | przed rolloutem                         | [docs/checklists/DEPLOYMENT_CHECKLIST.md](checklists/DEPLOYMENT_CHECKLIST.md)                 |
| Incident checklist           | Checklista incydentu                                        | ops, security                  | przy incydencie                         | [docs/checklists/INCIDENT_CHECKLIST.md](checklists/INCIDENT_CHECKLIST.md)                     |
| Backup restore checklist     | Checklista backup/restore                                   | ops                            | przy testach restore                    | [docs/checklists/BACKUP_RESTORE_CHECKLIST.md](checklists/BACKUP_RESTORE_CHECKLIST.md)         |

## Szablony i prompty

| Dokument               | Przeznaczenie                                    | Kto czyta                | Kiedy aktualizować                    | Link                                                                        |
| ---------------------- | ------------------------------------------------ | ------------------------ | ------------------------------------- | --------------------------------------------------------------------------- |
| Task template          | Standard opisu zadania                           | product, AI, programiści | przy zmianie standardu tasków         | [docs/templates/TASK_TEMPLATE.md](templates/TASK_TEMPLATE.md)               |
| Feature implementation | Prompt dla implementacji funkcji                 | AI                       | przy zmianie workflow                 | [docs/prompts/FEATURE_IMPLEMENTATION.md](prompts/FEATURE_IMPLEMENTATION.md) |
| Stage 4 galleries      | Gotowy prompt wykonawczy minimalnego GALLERY-001 | AI, reviewer             | przy rozpoczeciu Etapu 4              | [docs/prompts/STAGE_4_GALLERIES.md](prompts/STAGE_4_GALLERIES.md)           |
| Bug fix                | Prompt dla naprawy błędów                        | AI                       | przy zmianie standardów debugowania   | [docs/prompts/BUG_FIX.md](prompts/BUG_FIX.md)                               |
| Code review            | Prompt dla review                                | AI, reviewer             | przy zmianie standardu review         | [docs/prompts/CODE_REVIEW.md](prompts/CODE_REVIEW.md)                       |
| Security review        | Prompt dla security review                       | AI, security             | przy zmianie standardu security       | [docs/prompts/SECURITY_REVIEW.md](prompts/SECURITY_REVIEW.md)               |
| Database change        | Prompt dla zmian danych                          | AI, backend              | przy zmianie standardu migracji       | [docs/prompts/DATABASE_CHANGE.md](prompts/DATABASE_CHANGE.md)               |
| API design             | Prompt dla projektowania API                     | AI, backend              | przy zmianie konwencji API            | [docs/prompts/API_DESIGN.md](prompts/API_DESIGN.md)                         |
| Backend feature        | Prompt dla funkcji backendowej                   | AI, backend              | przy zmianie standardu backend        | [docs/prompts/BACKEND_FEATURE.md](prompts/BACKEND_FEATURE.md)               |
| Frontend feature       | Prompt dla funkcji frontendowej                  | AI, frontend             | przy zmianie standardu UI             | [docs/prompts/FRONTEND_FEATURE.md](prompts/FRONTEND_FEATURE.md)             |
| Refactoring            | Prompt dla refaktoryzacji                        | AI, backend, frontend    | przy zmianie standardu refaktoru      | [docs/prompts/REFACTORING.md](prompts/REFACTORING.md)                       |
| Documentation update   | Prompt dla dokumentacji                          | AI, autor                | przy zmianie procesu dokumentacyjnego | [docs/prompts/DOCUMENTATION_UPDATE.md](prompts/DOCUMENTATION_UPDATE.md)     |
| Release preparation    | Prompt dla release                               | AI, ops                  | przy zmianie releasu                  | [docs/prompts/RELEASE_PREPARATION.md](prompts/RELEASE_PREPARATION.md)       |
| Incident analysis      | Prompt dla incydentu                             | AI, ops, security        | przy zmianie procedury incydentowej   | [docs/prompts/INCIDENT_ANALYSIS.md](prompts/INCIDENT_ANALYSIS.md)           |

## ADR

| Dokument   | Przeznaczenie                           | Kto czyta                   | Kiedy aktualizować                 | Link                                                                                    |
| ---------- | --------------------------------------- | --------------------------- | ---------------------------------- | --------------------------------------------------------------------------------------- |
| ADR README | Zasady pracy z ADR                      | architekt, backend, AI      | przy zmianie procesu ADR           | [docs/adr/README.md](adr/README.md)                                                     |
| ADR 0001   | Modularny monolit                       | architekt, backend          | przy zmianie modelu architektury   | [docs/adr/0001-modular-monolith.md](adr/0001-modular-monolith.md)                       |
| ADR 0002   | Strategia uwierzytelniania              | backend, security           | przy decyzji auth                  | [docs/adr/0002-authentication-strategy.md](adr/0002-authentication-strategy.md)         |
| ADR 0003   | Abstrakcja storage                      | backend, ops                | przy zmianie storage               | [docs/adr/0003-file-storage-abstraction.md](adr/0003-file-storage-abstraction.md)       |
| ADR 0004   | Przetwarzanie mediów                    | backend, ops                | przy zmianie processingu           | [docs/adr/0004-media-processing-strategy.md](adr/0004-media-processing-strategy.md)     |
| ADR 0005   | Izolacja danych                         | backend, security           | przy zmianie multi-tenancy         | [docs/adr/0005-multi-tenant-data-isolation.md](adr/0005-multi-tenant-data-isolation.md) |
| ADR 0006   | Soft delete i retencja                  | backend, ops, security      | przy zmianie retencji              | [docs/adr/0006-soft-delete-and-retention.md](adr/0006-soft-delete-and-retention.md)     |
| ADR 0007   | Background jobs                         | backend, ops                | przy zmianie jobów                 | [docs/adr/0007-background-jobs.md](adr/0007-background-jobs.md)                         |
| ADR 0008   | Format błędów API                       | backend, frontend           | przy zmianie błędów                | [docs/adr/0008-api-error-format.md](adr/0008-api-error-format.md)                       |
| ADR 0009   | Generowanie ZIP                         | backend, ops                | przy zmianie pobrań                | [docs/adr/0009-download-archive-generation.md](adr/0009-download-archive-generation.md) |
| ADR 0010   | Dostęp do galerii                       | backend, security, frontend | przy zmianie public access         | [docs/adr/0010-gallery-access-strategy.md](adr/0010-gallery-access-strategy.md)         |
| ADR 0011   | Material UI jako system UI              | frontend, design, AI        | przy zmianie biblioteki UI         | [docs/adr/0011-material-ui-frontend-system.md](adr/0011-material-ui-frontend-system.md) |
| ADR 0012   | Flyway jako mechanizm migracji schematu | backend, DBA, AI            | przy zmianie podejścia do migracji | [docs/adr/0012-flyway-schema-migrations.md](adr/0012-flyway-schema-migrations.md)       |

## Skills

| Dokument                             | Przeznaczenie                                           | Kto czyta                 | Kiedy aktualizować                                 | Link                                                                                                                |
| ------------------------------------ | ------------------------------------------------------- | ------------------------- | -------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| Backend feature skill                | Realizacja funkcji backendowej                          | AI, backend               | przy zmianie standardów backendu                   | [.agents/skills/backend-feature/SKILL.md](../.agents/skills/backend-feature/SKILL.md)                               |
| Frontend feature skill               | Realizacja funkcji frontendowej                         | AI, frontend              | przy zmianie standardów frontendu                  | [.agents/skills/frontend-feature/SKILL.md](../.agents/skills/frontend-feature/SKILL.md)                             |
| Project foundation skill             | Etap 0 i techniczne fundamenty repozytorium             | AI, tech lead             | przy starcie implementacji i stabilizacji repo     | [.agents/skills/project-foundation/SKILL.md](../.agents/skills/project-foundation/SKILL.md)                         |
| Stage 1 foundation skill             | Etap 1 – szkielet techniczny monorepo (FND-002)         | AI, backend, tech lead    | przy realizacji Etapu 1 i rozszerzaniu fundamentów | [.agents/skills/stage-1-foundation/SKILL.md](../.agents/skills/stage-1-foundation/SKILL.md)                         |
| Stage 2 identity skill               | Etap 2 - auth, sesje, CSRF, testy auth i audyt identity | AI, backend, frontend, QA | przy realizacji lub rozszerzaniu Etapu 2           | [.agents/skills/stage-2-identity/SKILL.md](../.agents/skills/stage-2-identity/SKILL.md)                             |
| Stage 3 events and memberships skill | Etap 3 - wydarzenia, ownership i czlonkostwo            | AI, backend, frontend, QA | przy starcie lub rozszerzaniu Etapu 3              | [.agents/skills/stage-3-events-and-memberships/SKILL.md](../.agents/skills/stage-3-events-and-memberships/SKILL.md) |
| Task orchestration skill             | Koordynacja subagentów i integracja wyników             | AI, reviewer              | przy zmianie standardu orkiestracji                | [.agents/skills/task-orchestration/SKILL.md](../.agents/skills/task-orchestration/SKILL.md)                         |
| Database migration skill             | Zmiany danych i migracje                                | AI, backend               | przy zmianie standardów danych                     | [.agents/skills/database-migration/SKILL.md](../.agents/skills/database-migration/SKILL.md)                         |
| API design skill                     | Projektowanie API                                       | AI, backend               | przy zmianie API conventions                       | [.agents/skills/api-design/SKILL.md](../.agents/skills/api-design/SKILL.md)                                         |
| Security review skill                | Przegląd bezpieczeństwa                                 | AI, security              | przy zmianie security review                       | [.agents/skills/security-review/SKILL.md](../.agents/skills/security-review/SKILL.md)                               |
| Testing skill                        | Dobór i wykonanie testów                                | AI, QA, programiści       | przy zmianie strategii testów                      | [.agents/skills/testing/SKILL.md](../.agents/skills/testing/SKILL.md)                                               |
| Documentation update skill           | Aktualizacja dokumentacji                               | AI, autorzy dokumentacji  | przy zmianie procesu dokumentacyjnego              | [.agents/skills/documentation-update/SKILL.md](../.agents/skills/documentation-update/SKILL.md)                     |
| Release preparation skill            | Przygotowanie releasu                                   | AI, ops                   | przy zmianie procesu release                       | [.agents/skills/release-preparation/SKILL.md](../.agents/skills/release-preparation/SKILL.md)                       |

## Dokumenty wejściowe repozytorium

| Dokument | Przeznaczenie                    | Kto czyta | Kiedy aktualizować                  | Link                      |
| -------- | -------------------------------- | --------- | ----------------------------------- | ------------------------- |
| AGENTS   | Krótki punkt wejścia dla agentów | wszyscy   | przy zmianie procesu pracy          | [AGENTS.md](../AGENTS.md) |
| README   | Punkt wejścia do repozytorium    | wszyscy   | przy zmianie mapy repo lub produktu | [README.md](../README.md) |

## Powiązane dokumenty

- [AGENTS.md](../AGENTS.md)
- [README.md](../README.md)
- [development/WORKFLOW.md](development/WORKFLOW.md)

## Decyzje otwarte

- Czy mapa dokumentacji ma obejmować także pliki niebędące Markdown, jeśli staną się częścią procesu rozwojowego.
