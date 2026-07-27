# Baseline obciążeniowy etapów 11 i 13

Skrypt wykonuje kontrolowany test stałego napływu żądań do health endpointu. Nie zawiera danych logowania ani tokenów. Jest przeznaczony do uruchomienia przeciwko lokalnemu Compose lub środowisku testowemu:

```bash
k6 run performance/k6/stage-11-13-baseline.js
```

Parametry można ustawić przez `BASE_URL`, `HEALTH_PATH`, `RATE`, `DURATION`, `PREALLOCATED_VUS` i `MAX_VUS`. Ten baseline jest bramką infrastrukturalną; obciążenie konkretnych endpointów administratora wymaga sesji testowej uzyskanej przez dedykowany setup, a nie sekretów zapisanych w skrypcie.
