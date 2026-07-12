# Logowanie Operacyjne

## Cel dokumentu
Opisuje zasady logowania aplikacyjnego, strukturalnego i audytowego.

## Status dokumentu
- Status: draft
- Zakres: logi aplikacyjne i operacyjne
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Standard logowania nie jest jeszcze uzgodniony.

## Stan docelowy
- Logi strukturalne z correlation ID i kontrolą danych wrażliwych.

## Zasady
- Logi aplikacyjne nie zastępują `AuditLog`.
- Dane osobowe i tokeny są redagowane.
- Wpisy błędów muszą zawierać correlation ID.
- Logi powinny wspierać analizę uploadów, ZIP i błędów integracji.

## Powiązane dokumenty
- [MONITORING.md](MONITORING.md)
- [INCIDENT_RESPONSE.md](INCIDENT_RESPONSE.md)
- [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md)

## Decyzje otwarte
- Czy przyjąć jednolity format JSON logs we wszystkich środowiskach poza developmentem.
