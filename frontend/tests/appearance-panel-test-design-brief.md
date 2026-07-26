# Test Design Brief: Appearance Panel (Etap 9)

## Wymagania i macierz ryzyk

| Ryzyko         | Bledna implementacja wykrywana przez test                                     | Scenariusze                                                        | Najnizsza wiarygodna warstwa |
| -------------- | ----------------------------------------------------------------------------- | ------------------------------------------------------------------ | ---------------------------- |
| Kontrakt API   | Wrapper wysyla zly path, payload albo pomija wersje                           | GET, PUT, retry, odpowiedz z wersja                                | API/component                |
| Allowlista     | API lub formularz akceptuje dowolny enum albo kolor                           | poprawne opcje; nieznany enum; kolor spoza `#RRGGBB`               | API/unit                     |
| Tekst          | Tekst przekracza limit albo limit jest niejawny                               | `N-1`, `N`, `N+1`, pusty tekst                                     | component                    |
| XSS            | Tekst powitalny jest interpretowany jako markup lub kod                       | `<img onerror>`, `<script>`, `javascript:` i znaki specjalne       | component/security           |
| Ownership/RBAC | Manager dostaje edycje bez potwierdzenia backendu albo owner nie moze zapisac | owner read/write; manager read; manager PUT 403 -> read-only + GET | component/API                |
| Konflikt       | `409` jest pokazany jak sukces lub nadpisuje dane                             | conflict po zapisie; ponowne odczytanie                            | component                    |
| Stany sieci    | Bledny odczyt blokuje panel bez retry                                         | loading; error; retry; success; wielokrotne klikniecie             | component                    |
| Accessibility  | Formularz/dialog nie ma etykiet, focusu lub obslugi klawiatury                | role dialog; label dla select/color/textarea; Escape; tab/focus    | component/axe                |
| Responsive     | Preview lub dlugi tekst powoduje overflow                                     | 360px, 390px, 768px, 1024px; dlugi tekst                           | component/E2E                |

## Scenariusze poza mozliwoscia tego zakresu

Nie uruchamiam testu Playwright z prawdziwym backendem ani testu integracyjnego na MySQL, poniewaz zadanie jawnie ogranicza zmiany do frontendu i testow frontendu. Wrapper zachowuje kontrakt sciezki opisany w dokumentacji endpointow; zgodnosc DTO z backendem pozostaje zaleznoscia zewnetrzna.
