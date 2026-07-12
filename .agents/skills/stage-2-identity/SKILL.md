---
name: stage-2-identity
description: "Skill prowadzący przez Etap 2 – Tożsamość użytkownika i autoryzacja (AUTH-001). Implementuje Spring Security, model usera i proces logowania/rejestracji."
---

# Etap 2 - Tożsamość i Autoryzacja

## Kiedy używać?
Użyj tego skilla, gdy Etap 1 jest zakończony i repozytorium zawiera gotowy szkielet techniczny, a Twoim celem jest implementacja **AUTH-001 - Rejestracja, logowanie i sesje użytkownika**.

## Cel
Wdrożenie tożsamości użytkownika (organizatora/managera), umożliwiające zabezpieczenie dostępu do przyszłych endpointów panelu zarządzania wydarzeniami. Zgodnie z ADR 0002, opieramy się na natywnym mechanizmie sesji Spring Security.

## Kroki implementacyjne

### 1. Zatwierdzenie Architektury
Upewnij się, że ADR 0002 został zmodyfikowany na status `accepted` i precyzuje wykorzystanie sesji opartych o ciastka HTTP-only oraz ochronę CSRF (jako że frontend jest oddzielnym SPA).

### 2. Rozbudowa Bazy Danych
Utwórz kolejną migrację Flyway (np. `V2__create_users_table.sql` w `backend/src/main/resources/db/migration/`).
- Tabela `users`: klucz główny `id`, unikalny `email`, `password_hash`, rola, czasy utworzenia i modyfikacji, pola pomocnicze (np. logowania).

### 3. Implementacja Backendu (Spring Security)
Dodaj w `pom.xml` pakiety `spring-boot-starter-security` oraz `spring-boot-starter-data-jpa`.
- Skonfiguruj `SecurityFilterChain`.
- Włącz i skonfiguruj odpowiednio obsługę CORS (zezwolenie na credentials, domena frontendu).
- Włącz CSRF w trybie kompatybilnym ze SPA (np. `CookieCsrfTokenRepository.withHttpOnlyFalse()` jeśli React ma to odczytywać ze specyficznego ciastka XSRF-TOKEN).
- Stwórz własną logikę kontrolera auth (`/api/auth/register`, `/api/auth/me`), a logowanie (jeśli to możliwe) obsłuż natywnymi filtrami Springa lub dedykowanym endpointem dla SPA `/api/auth/login`.
- Zakoduj logikę użytkownika: domena (Entity `User`), repozytorium (Spring Data), serwis (rejestracja i ładowanie do `UserDetailsService`). Hasła muszą być bezpiecznie hashowane (BCrypt/Argon2).

### 4. Implementacja Frontendu (React)
- Zainstaluj bibliotekę do routingu (np. `react-router-dom`).
- Zainstaluj i skonfiguruj bibliotekę do stanów lub żądań (np. `axios`, `react-query` lub stan w Context API), aby przesyłała ciasteczka uwierzytelniające (`withCredentials: true`).
- Stwórz komponenty widokowe zgodne ze stackiem (w przyszłości Material UI wg ADR, na razie mogą być to podstawowe schludne komponenty CSS):
  - Formularz rejestracji
  - Formularz logowania
  - Prosty dashboard / ekran powitalny, widoczny tylko dla zalogowanych.
- Przygotuj mechanizm pobierania początkowego stanu uwierzytelnienia (sprawdzanie `/api/auth/me` podczas startu aplikacji).

### 5. Walidacja, Testy E2E i Self-Review
Po zakończeniu implementacji musisz **OBOWIĄZKOWO** wykonać następujące czynności:
1. Napisz i uruchom **testy API / E2E** (Playwright we frontendzie i REST Assured lub w pełni zintegrowany MockMvc w backendzie).
2. Przeprowadź jawne, rygorystyczne **Self-Review** wchodząc w rolę niezależnego audytora Security/Code, szukając błędów logicznych, luk CSRF/XSS i odstępstw od architektury. Skomentuj wyniki w podsumowaniu.
3. Potwierdź, czy cały proces rejestracji i logowania funkcjonuje poprawnie w rzeczywistym flow. Upewnij się, że dodane zostały nowe testy jednostkowe (AuthService) i integracyjne (Security config).

## Ograniczenia i zasady
- Nie wchodzimy jeszcze w implementację modułów wydarzeń ani zaproszeń (to kolejne zadania).
- Zawsze korzystaj z najświeższych standardów Spring Boota 3+ i React.
- Zachowaj minimalizm i modularną strukturę pakietów w Javie (pakiet `pl.backend.weddinggallery.identity` dla modułu auth).
