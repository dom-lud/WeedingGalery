# Test Design Brief - hardening coverage

## Cel
Podniesc coverage testami, ktore wykrywaja realne regresje kontraktu, UI,
ownership i walidacji uploadu, a nastepnie podniesc blokujace zapadki CI.

## Zakres
- frontend: warstwa API, `Dashboard`, `AppShell`, routing i stany bledu/loading,
- backend: walidacja typow i sygnatur plikow, bootstrap runner oraz brakujace
  sciezki bezpieczenstwa o wysokim koszcie bledu,
- konfiguracja JaCoCo/Vitest, CI, skills i dokumentacja standardu coverage.

## Poza zakresem
- sztuczne wykluczanie kodu produkcyjnego z raportu,
- testy bez istotnych asercji wykonywane tylko dla licznika,
- Etap 6 i nowe zachowania produktowe,
- dazenie do 100% kosztem testow kruchych lub sprzezonych z implementacja.

## Macierz ryzyk

| Wymaganie / ryzyko | Bledna implementacja do wykrycia | Scenariusze | Warstwa |
| --- | --- | --- | --- |
| API zachowuje kontrakt HTTP | zla metoda, URL, body, brak idempotency key lub credentials | kazda operacja event/gallery/public/upload; replay i upload multipart | Vitest unit/contract |
| Dashboard respektuje ownership i stany UI | obcy manager dostaje akcje owner-only; blad API znika; pusta lista blokuje utworzenie | loading, empty, error, owner, manager, create/edit/lifecycle/membership | Vitest component |
| Shell i routing sa dostepne | logout nie dziala, drawer nie zamyka sie, role/nawigacja znikaja | desktop/mobile navigation, logout success/failure, focusable controls | Vitest component + istniejacy E2E |
| Walidator uploadu odrzuca spoofing | plik ma poprawne MIME/extension, ale zla sygnature lub uszkodzona strukture | JPEG/PNG/WebP/MP4 pozytywne; mismatch, truncation, malformed, unsupported, puste dane | JUnit unit |
| Limity pozostaja bronione na granicy | off-by-one rozmiaru/nazwy lub brak pliku | `N-1`, `N`, `N+1`, empty/null, poprawny limit | JUnit unit/integration |
| Bootstrap uruchamia sie tylko jawnie | runner mylnie raportuje wynik albo modyfikuje replay | provisioned i already exists; delegacja danych bez logowania hasla | JUnit unit |
| Coverage jest zapadka, nie celem | progi pozostaja historycznie niskie albo sa obnizane | raport po pelnych testach; minima podniesione z marginesem ponizej wyniku | JaCoCo + Vitest + CI |

## Warunek zakonczenia
- nowe testy przechodza i maja asercje kontraktu/zachowania,
- frontend osiaga co najmniej 85% statements/lines, 80% branches i 70% functions,
- backend osiaga co najmniej 85% instructions i 60% branches,
- progi CI sa podniesione i nie wyzsze od stabilnie osiagnietego wyniku,
- full backend verify, frontend coverage/lint/build oraz E2E przechodza,
- skills i dokumentacja wymagaja analizy raportu per plik oraz ratchet policy,
- self-review nie pozostawia findingow P0/P1.

## Wynik

- frontend: 38 testow; 89.33% statements/lines, 81.37% branches i 72.41% functions,
- backend: 58 testow, w tym 1 lokalnie pominiety Testcontainers; 88.53% instructions i 68.57% branches,
- `UploadFileValidator`: 95.3% instructions i 69.9% branches po dodaniu przypadkow poprawnych, uszkodzonych, granicznych i przeciążenia walidatora,
- bramki w Vitest, JaCoCo, CI, dokumentacji i skills zostaly podniesione oraz zsynchronizowane.
