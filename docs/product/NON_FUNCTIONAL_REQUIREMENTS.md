# Wymagania Niefunkcjonalne

## Cel dokumentu
Opisuje jakościowe wymagania systemu, które wpływają na architekturę, bezpieczeństwo i operacje.

## Status dokumentu
- Status: draft
- Zakres: wymagania niefunkcjonalne dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Wymagania nie są jeszcze spełnione przez istniejący szkic projektu.

## Stan docelowy
- System przewidywalny operacyjnie, bezpieczny i gotowy na stopniowy wzrost ruchu.

## Dostępność i niezawodność
- Priorytetem jest stabilny upload i pobieranie plików.
- Błędy częściowe nie mogą blokować całych sesji uploadu.
- Operacje asynchroniczne muszą być odporne na restart procesu.

## Wydajność
- Publiczna galeria powinna być używalna na urządzeniach mobilnych i przy słabszym łączu.
- Lazy loading i stronicowanie muszą ograniczać nadmierne transfery.
- Generowanie ZIP i przetwarzanie mediów nie może blokować wątków obsługi API.

## Skalowalność
- Start od pojedynczego VPS i modularnego monolitu.
- Storage i background jobs projektowane tak, aby można było je rozdzielić w przyszłości.
- Model danych i indeksy muszą wspierać wieloużytkownikowość.

## Bezpieczeństwo
- Ochrona przed OWASP Top 10.
- Silna walidacja uploadu i dostępu do plików.
- Jawny audyt działań administracyjnych.

## Prywatność
- Minimalizacja danych gości.
- Domyślne ograniczenie ekspozycji danych i zasobów.
- Brak zwracania fizycznych ścieżek storage klientowi.

## Utrzymywalność
- Jasny podział modułów i odpowiedzialności.
- Jednolity format błędów i konwencje API.
- Dokumentacja i ADR aktualizowane razem ze zmianami architektonicznymi.

## Obserwowalność
- Health checks, logi strukturalne, correlation ID.
- Monitoring storage, uploadów, błędów przetwarzania i zadań tła.

## Przenośność
- Środowiska developerskie, testowe i produkcyjne mają korzystać z tej samej logiki konfiguracji.
- Storage ma być ukryty za interfejsem abstrakcyjnym.

## Testowalność
- Testy jednostkowe, integracyjne, E2E i scenariusze bezpieczeństwa.
- Testcontainers dla zależności backendowych.

## Powiązane dokumenty
- [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md)
- [../operations/MONITORING.md](../operations/MONITORING.md)
- [../testing/QUALITY_GATES.md](../testing/QUALITY_GATES.md)

## Decyzje otwarte
- Docelowe SLO dla produkcji po uruchomieniu pierwszej stabilnej wersji.
