# Monitoring i Observability

## Cel dokumentu
Opisuje metryki, logi, health checks i alerty dla platformy.

## Status dokumentu
- Status: draft
- Zakres: observability dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Mechanizmy monitoringu nie są jeszcze wdrożone.
- Zależność `Spring Boot Actuator` nie jest jeszcze częścią backendowego `pom.xml`.

## Stan docelowy
- Podstawowa obserwowalność gotowa do rozbudowy o Prometheus i Grafana.

## Wymagania
- Spring Boot Actuator
- Health endpoints
- Logi strukturalne
- Correlation ID
- Monitoring miejsca na dysku
- Monitoring liczby błędów uploadu i processingu
- Alerty dla storage, certyfikatu i błędów krytycznych

## Powiązane dokumenty
- [LOGGING.md](LOGGING.md)
- [DEPLOYMENT.md](DEPLOYMENT.md)
- [../architecture/BACKGROUND_JOBS.md](../architecture/BACKGROUND_JOBS.md)

## Decyzje otwarte
- Czy pierwsza wersja użyje tylko logów i actuatora, czy od razu prostego eksportu metryk do Prometheus.
