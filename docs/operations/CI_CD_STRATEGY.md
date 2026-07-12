# Strategia CI/CD

## Cel dokumentu
Opisuje docelową strategię CI/CD dla GitHub Actions bez implementowania workflow YAML.

## Status dokumentu
- Status: draft
- Zakres: pull request flow, quality gates, pipeline build/test/deploy, artefakty i rollback
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Repozytorium ma workflow PR validation oraz workflow buildowy dla `main`.
- Backend testy, frontend lint/test/build, walidacja `docker compose config` i budowa obrazów backendu oraz frontendu są egzekwowane automatycznie w GitHub Actions.
- Quality gates są częściowo zautomatyzowane; obszary biznesowe, security review i przyszłe E2E nadal wymagają dalszej rozbudowy.

## Stan docelowy
- Każda zmiana przechodzi przez pull request, automatyczne quality gates i review.
- `main` jest zawsze w stanie możliwym do zbudowania.
- Deployment wykonuje się z wersjonowanych artefaktów po przejściu kontroli jakości.

## Pull request flow
Każda zmiana powinna przechodzić przez pull request.

Zakładany flow:

```text
feature branch
→ pull request
→ automatyczne quality gates
→ code review
→ opcjonalny Codex review
→ merge do main
→ build artefaktów
→ deployment na środowisko docelowe
→ smoke tests
```

## Workflow

### PR validation
Uruchamiany dla pull requestów do `main`.

Zakres:
- checkout,
- setup Java i Node,
- cache Maven i npm,
- backend format (Spotless check) i testy,
- frontend format (Prettier check), lint, testy i build,
- walidacja `docker compose config`,
- budowa obrazu backendu,
- budowa obrazu frontendu.

### Main build
Uruchamiany po merge do `main`.

Zakres:
- backend format (Spotless check), testy i budowa JAR,
- frontend format (Prettier check), lint, testy i build,
- testy backendowe,
- testy frontendowe,
- budowa obrazów Docker,
- publikacja artefaktów CI jako GitHub Actions artifacts,
- przygotowanie pod późniejszą publikację do registry, jeśli registry zostanie wybrane.

### Deployment
Uruchamiany ręcznie albo po zatwierdzeniu środowiska.

Zakres:
- pobranie wersjonowanych artefaktów,
- weryfikacja konfiguracji i sekretów,
- backup przed migracją, jeśli zmiana dotyczy danych,
- migracje,
- aktualizacja kontenerów,
- health checks,
- smoke tests,
- monitoring po wdrożeniu,
- możliwość rollbacku.

## Obowiązkowe quality gates

### Backend
- Maven build przechodzi.
- Testy jednostkowe przechodzą.
- Testy integracyjne przechodzą, jeśli zmiana dotyczy API, bazy, security, storage lub background jobs.
- Testy security i ownership przechodzą, jeśli zmiana dotyczy autoryzacji lub danych użytkowników.
- Migracje schematu są walidowane, jeśli zmiana dotyczy danych i repozytorium zawiera już aktywny mechanizm migracji.
- Brak znanych krytycznych podatności w zależnościach.

### Frontend
- TypeScript build przechodzi.
- Lint przechodzi.
- Testy frontendu przechodzą.
- Testy komponentów, hooków lub widoków przechodzą, jeśli zmiana dotyczy UI.
- Kluczowe stany UI są pokryte: loading, empty, error, retry, forbidden i offline, jeśli dotyczą przepływu.
- Mobile-first i accessibility są sprawdzone zgodnie z dokumentacją.
- Frontend używa MUI zgodnie z [../frontend/UI_SYSTEM.md](../frontend/UI_SYSTEM.md).

### Docker i operacje
- `docker compose config` przechodzi.
- Obrazy backendu i frontendu budują się.
- Health checks są zdefiniowane dla usług runtime, gdy zostaną wdrożone.
- Zmiany deploymentu mają plan rollbacku.
- Zmiany wymagające sekretów nie zapisują sekretów w repozytorium.

### Dokumentacja
- Dokumentacja jest zaktualizowana, jeśli zmiana wpływa na architekturę, API, model danych, security, operacje, workflow lub UX.
- ADR jest dodany lub zaktualizowany, jeśli decyzja wpływa na architekturę, bezpieczeństwo, dane albo operacje.
- Linki względne w nowych dokumentach są poprawne.

## Gates blokujące i ostrzegawcze
Docelowo blokujące:
- backend build,
- frontend build,
- lint,
- testy wymagane zakresem zmiany,
- Docker build dla zmian infrastrukturalnych lub release,
- brak sekretów w repo,
- brak krytycznych podatności.

Początkowo ostrzegawcze mogą być:
- pełne E2E,
- skanowanie licencji,
- rozbudowane testy wydajnościowe,
- automatyczne sprawdzanie linków dokumentacji.

Zmiana gate z ostrzegawczego na blokujący powinna zostać odnotowana w dokumentacji.

## Cache i artefakty
- Cache Maven powinien być oparty o `backend/pom.xml`.
- Cache npm powinien być oparty o `frontend/package-lock.json`.
- Artefakty builda muszą być powiązane z commit SHA.
- Artefakty backendu i frontendu na `main` powinny być dostępne do pobrania z workflow jako punkt odniesienia dla dalszych etapów delivery.
- Obrazy Docker powinny być tagowane co najmniej przez commit SHA; tag `latest` nie może być jedynym identyfikatorem produkcyjnym.

## Sekrety CI
- Sekrety przechowujemy w GitHub Actions secrets albo docelowym mechanizmie secret management.
- Sekrety nie mogą trafiać do logów.
- Workflow nie może wypisywać pełnych wartości env.
- Środowiska produkcyjne powinny wymagać ręcznego zatwierdzenia lub environment protection rules.

## Migracje i dane
- Migracje schematu uruchamiamy przed startem nowej wersji aplikacji albo jako jawny etap deploymentu, gdy mechanizm migracji zostanie wdrożony.
- Destrukcyjne migracje wymagają planu danych, backupu i rollbacku.
- Zmiany schematu muszą być kompatybilne z procesem rollbacku albo mieć osobny plan operacyjny.

## Deployment i rollback
- Deployment powinien używać wersjonowanych obrazów lub artefaktów.
- Przed deploymentem zmian danych wymagany jest backup.
- Rollback musi wskazywać wersję aplikacji, migracje, konfigurację i wpływ na dane.
- Po deploymentcie wymagane są smoke tests dla API, frontendu i krytycznych ścieżek.

## Smoke tests
Minimalne smoke tests po deploymentcie:
- frontend odpowiada przez Nginx,
- backend health endpoint odpowiada,
- połączenie z bazą działa,
- endpoint publiczny galerii zwraca przewidywalny status,
- logowanie lub endpoint auth zwraca przewidywalną odpowiedź,
- upload testowy jest wykonywany na środowisku testowym, gdy jest dostępny.

## Powiązane dokumenty
- [DEPLOYMENT.md](DEPLOYMENT.md)
- [CONFIGURATION.md](CONFIGURATION.md)
- [ENVIRONMENTS.md](ENVIRONMENTS.md)
- [BACKUP_AND_RESTORE.md](BACKUP_AND_RESTORE.md)
- [../testing/QUALITY_GATES.md](../testing/QUALITY_GATES.md)
- [../development/WORKFLOW.md](../development/WORKFLOW.md)

## Decyzje otwarte
- Docelowy registry obrazów Docker.
- Czy staging będzie obowiązkowy przed pierwszym produkcyjnym wdrożeniem.
- Kiedy E2E staną się gate blokującym.
