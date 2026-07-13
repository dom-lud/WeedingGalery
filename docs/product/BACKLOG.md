# Backlog Produktu

## Cel dokumentu
Zbiera pełny backlog docelowej platformy w podziale na epiki, zwięzłe zadania i ich wymagania wejściowe.

## Status dokumentu
- Status: draft
- Zakres: backlog dla pełnej wizji produktu
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- Backlog opisuje planowane zadania i nie oznacza, że funkcje są już zaimplementowane.

## Stan docelowy
- Jeden spójny backlog prowadzący rozwój produktu od fundamentów do stabilnej produkcji.

## Zasady backlogu
- Każde zadanie ma unikalny identyfikator.
- Status początkowy nowych pozycji: `IDEA` albo `ANALYSIS`.
- Zależności odnoszą się do innych zadań, etapów roadmapy albo decyzji ADR.
- Jeśli zadanie wymaga decyzji architektonicznej, nie przechodzi do realizacji bez ADR lub jawnej decyzji o jego braku.

## FOUNDATION
### FND-001 - Uporządkowanie dokumentacji i procesu pracy
- Cel: stworzyć wspólny proces pracy dla ludzi i agentów AI.
- Opis: workflow, DoR, DoD, backlog, checklisty, prompty, mapa dokumentacji.
- Priorytet: wysoki.
- Zależności: brak.
- Wymagania funkcjonalne: spójne dokumenty rozwojowe i linkowanie względne.
- Kryteria akceptacji: komplet dokumentów istnieje i odwołuje się do właściwych źródeł.
- Wymagania bezpieczeństwa: brak sekretów w przykładach i promptach.
- Wymagane testy: weryfikacja kompletności plików i linków.
- Dokumenty powiązane: [FEATURE_ROADMAP.md](FEATURE_ROADMAP.md), [../development/WORKFLOW.md](../development/WORKFLOW.md).
- Status: `DONE`.
- ADR wymagany: nie.

### FND-002 - Szkielet techniczny monorepo
- Cel: przygotować techniczny szkielet repo zgodny z dokumentacją.
- Opis: backend, frontend, konfiguracja środowisk, podstawy repo i standardy builda.
- Priorytet: wysoki.
- Zależności: FND-001.
- Wymagania funkcjonalne: zgodność z planowanym stackiem i strukturą repo.
- Kryteria akceptacji: repo ma minimalny, działający szkielet zgodny z dokumentacją i ADR.
- Wymagania bezpieczeństwa: brak sekretów, bezpieczna konfiguracja startowa.
- Wymagane testy: build sanity check, podstawowe testy startowe.
- Dokumenty powiązane: [../architecture/REPOSITORY_STRUCTURE.md](../architecture/REPOSITORY_STRUCTURE.md), [../adr/0001-modular-monolith.md](../adr/0001-modular-monolith.md).
- Status: `DONE`.
- ADR wymagany: tak (ADR 0012 – Flyway).

## IDENTITY
### AUTH-001 - Rejestracja, logowanie i sesje użytkownika
- Cel: wdrożyć podstawową tożsamość użytkownika.
- Opis: administracyjne tworzenie kont, logowanie, wylogowanie, sesje serwerowe, CSRF dla SPA, blokady i polityka haseł.
- Priorytet: wysoki.
- Zależności: FND-002, ADR 0002.
- Wymagania funkcjonalne: konto użytkownika tworzone zgodnie z polityką admin-only, sesja, endpoint `/api/auth/me`, bootstrap CSRF, blokada po błędnych logowaniach.
- Kryteria akceptacji: administrator może utworzyć konto, użytkownik może zalogować się i zakończyć sesję, a flow auth jest pokryte testami realistycznymi dla UI i API.
- Wymagania bezpieczeństwa: bezpieczne hashowanie, rate limiting, brak enumeracji użytkowników.
- Wymagane testy: testy auth, security, sesji, błędnych logowań, CSRF oraz E2E bez sztucznego stabilizowania flow logowania.
- Dokumenty powiązane: [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md).
- Status: `DONE`.
- ADR wymagany: tak (ADR 0002).

### AUTH-002 - Weryfikacja e-mail i reset hasła
- Cel: zapewnić odzyskiwanie dostępu i potwierdzenie tożsamości.
- Opis: tokeny weryfikacyjne, reset hasła, wygasanie tokenów, unieważnianie po użyciu oraz transakcyjna wysyłka e-maili dla rejestracji, weryfikacji konta i odzyskiwania dostępu.
- Priorytet: wysoki.
- Zależności: AUTH-001.
- Wymagania funkcjonalne: wysłanie i potwierdzenie tokenu, reset hasła, dostarczenie e-maila weryfikacyjnego po rejestracji oraz e-maila resetującego hasło.
- Kryteria akceptacji: użytkownik może zweryfikować e-mail i bezpiecznie zresetować hasło.
- Wymagania bezpieczeństwa: bezpieczne tokeny, brak ujawniania istnienia konta.
- Wymagane testy: testy tokenów, wygasania i ścieżek błędów.
- Dokumenty powiązane: [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md), [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## USERS
### USER-001 - Profil użytkownika i preferencje
- Cel: udostępnić użytkownikowi zarządzanie własnym profilem.
- Opis: dane profilu, język, powiadomienia, lista wydarzeń, wykorzystanie miejsca.
- Priorytet: średni.
- Zależności: AUTH-001.
- Wymagania funkcjonalne: odczyt i edycja profilu, widok zaproszeń i wydarzeń.
- Kryteria akceptacji: użytkownik widzi i aktualizuje swój profil bez naruszania danych innych kont.
- Wymagania bezpieczeństwa: ownership profilu i danych subskrypcji.
- Wymagane testy: testy endpointów profilu, autoryzacji i walidacji.
- Dokumenty powiązane: [PRODUCT_VISION.md](PRODUCT_VISION.md), [../backend/API_ENDPOINTS.md](../backend/API_ENDPOINTS.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## EVENTS
### EVENT-001 - Tworzenie i zarządzanie wydarzeniami
- Cel: wdrożyć główny byt biznesowy systemu.
- Opis: tworzenie, edycja, archiwizacja i usuwanie wydarzeń przez właściciela.
- Priorytet: wysoki.
- Zależności: AUTH-001, USER-001.
- Wymagania funkcjonalne: nazwa, typ, data, opis, status i prywatność wydarzenia.
- Kryteria akceptacji: użytkownik może utworzyć i zarządzać wieloma wydarzeniami.
- Wymagania bezpieczeństwa: pełna kontrola ownership wydarzenia.
- Wymagane testy: testy use case, ownership, API i walidacji.
- Dokumenty powiązane: [../architecture/DATA_MODEL.md](../architecture/DATA_MODEL.md), [../architecture/MULTI_TENANCY.md](../architecture/MULTI_TENANCY.md).
- Status: `READY`.
- ADR wymagany: nie.

### EVENT-002 - Archiwizacja i retencja wydarzeń
- Cel: zdefiniować lifecycle wydarzenia po zakończeniu.
- Opis: archiwizacja, wygasanie, usuwanie logiczne i wpływ na galerie oraz media.
- Priorytet: średni.
- Zależności: EVENT-001, ADR 0006.
- Wymagania funkcjonalne: status archiwalny, wygaśnięcie i bezpieczne usuwanie.
- Kryteria akceptacji: wydarzenie może przejść do archiwum bez utraty spójności danych.
- Wymagania bezpieczeństwa: retencja, audyt, brak utraty dostępu do backupów.
- Wymagane testy: testy retencji, cleanupu i operacji granicznych.
- Dokumenty powiązane: [../security/PRIVACY_AND_DATA_RETENTION.md](../security/PRIVACY_AND_DATA_RETENTION.md).
- Status początkowy: `IDEA`.
- ADR wymagany: tak.

## MEMBERSHIPS
### MEMBER-001 - Role i członkostwo w wydarzeniu
- Cel: wdrożyć współzarządzanie wydarzeniem.
- Opis: owner, manager, delegacja uprawnień i historia zmian.
- Priorytet: wysoki.
- Zależności: EVENT-001.
- Wymagania funkcjonalne: dodawanie, zmiana roli, usunięcie członka, transfer własności.
- Kryteria akceptacji: członkostwo determinuje realny dostęp do zasobów wydarzenia.
- Wymagania bezpieczeństwa: ownership, brak możliwości usunięcia właściciela przez managera.
- Wymagane testy: testy autoryzacji, ownership i historii zmian.
- Dokumenty powiązane: [USER_ROLES.md](USER_ROLES.md), [PERMISSIONS_MATRIX.md](PERMISSIONS_MATRIX.md).
- Status: `READY`.
- ADR wymagany: nie.

## INVITATIONS
### INV-001 - Zaproszenia e-mail do wydarzenia
- Cel: umożliwić dołączanie współzarządzających do wydarzenia.
- Opis: wysłanie zaproszenia, akceptacja, odrzucenie, wygasanie i ponowna wysyłka.
- Priorytet: wysoki.
- Zależności: MEMBER-001, AUTH-002.
- Wymagania funkcjonalne: lifecycle zaproszenia i powiązanie z członkostwem.
- Kryteria akceptacji: zaproszenie kończy się poprawnym utworzeniem membership albo odrzuceniem.
- Wymagania bezpieczeństwa: bezpieczne tokeny, brak przejęcia zaproszenia.
- Wymagane testy: testy tokenów, ścieżek akceptacji i konfliktów.
- Dokumenty powiązane: [USER_JOURNEYS.md](USER_JOURNEYS.md), [../backend/API_ENDPOINTS.md](../backend/API_ENDPOINTS.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## GALLERIES
### GALLERY-001 - Wiele galerii na wydarzenie
- Cel: pozwolić organizatorowi rozdzielać media na logiczne zbiory.
- Opis: tworzenie i edycja galerii, kolejność, status, opis, daty publikacji i wygaśnięcia.
- Priorytet: wysoki.
- Zależności: EVENT-001.
- Wymagania funkcjonalne: wiele galerii per wydarzenie i konfiguracja ich ustawień.
- Kryteria akceptacji: wydarzenie może zawierać wiele galerii o różnych ustawieniach.
- Wymagania bezpieczeństwa: dostęp tylko w kontekście wydarzenia i membership.
- Wymagane testy: testy CRUD galerii, ownership i walidacji.
- Dokumenty powiązane: [../architecture/DATA_MODEL.md](../architecture/DATA_MODEL.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

### GALLERY-002 - Ustawienia widoczności i publikacji galerii
- Cel: sterować dostępnością galerii dla gości i organizatorów.
- Opis: public view, upload enabled, download enabled, moderation mode i access code.
- Priorytet: wysoki.
- Zależności: GALLERY-001, ADR 0010.
- Wymagania funkcjonalne: przełączanie trybów dostępu i publikacji.
- Kryteria akceptacji: ustawienia galerii wpływają na zachowanie publicznego widoku i uploadu.
- Wymagania bezpieczeństwa: brak ujawnienia prywatnych galerii i kodów.
- Wymagane testy: testy ustawień, dostępu publicznego i autoryzacji.
- Dokumenty powiązane: [../architecture/MULTI_TENANCY.md](../architecture/MULTI_TENANCY.md), [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md).
- Status początkowy: `IDEA`.
- ADR wymagany: tak.

## PUBLIC_ACCESS
### PUBLIC-001 - Publiczny dostęp przez slug, token i kod dostępu
- Cel: umożliwić prosty dostęp gości bez konta.
- Opis: wejście do galerii przez link, QR, token i opcjonalny kod.
- Priorytet: wysoki.
- Zależności: GALLERY-002, ADR 0010.
- Wymagania funkcjonalne: publiczny entrypoint, tokeny dostępu i kontrola dostępu.
- Kryteria akceptacji: gość ma dostęp tylko do konkretnej galerii zgodnie z jej ustawieniami.
- Wymagania bezpieczeństwa: ochrona przed enumeracją, rate limiting, brak eskalacji dostępu.
- Wymagane testy: testy publicznego API, kodów dostępu i negatywnych ścieżek.
- Dokumenty powiązane: [../adr/0010-gallery-access-strategy.md](../adr/0010-gallery-access-strategy.md).
- Status początkowy: `IDEA`.
- ADR wymagany: tak.

## UPLOADS
### UPLOAD-001 - Upload wieloplikowy zdjęć i filmów
- Cel: przyjmować media od gości i użytkowników.
- Opis: upload wielu plików, walidacja klienta i serwera, postęp, anulowanie, retry.
- Priorytet: wysoki.
- Zależności: PUBLIC-001, GALLERY-002.
- Wymagania funkcjonalne: wiele plików, status per plik, obsługa częściowych błędów.
- Kryteria akceptacji: użytkownik może przesłać wiele plików i zobaczyć wynik każdego z nich.
- Wymagania bezpieczeństwa: walidacja typu, rozmiaru, limitów i liczby plików.
- Wymagane testy: testy uploadu, limitów, retry i błędów.
- Dokumenty powiązane: [../frontend/UPLOAD_UX.md](../frontend/UPLOAD_UX.md), [../security/FILE_UPLOAD_SECURITY.md](../security/FILE_UPLOAD_SECURITY.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

### UPLOAD-002 - Upload sessions i gotowość na duże pliki
- Cel: przygotować model pod resumable lub multipart upload.
- Opis: upload session, śledzenie statusu, retry i przyszła obsługa dużych plików.
- Priorytet: średni.
- Zależności: UPLOAD-001.
- Wymagania funkcjonalne: upload session oraz możliwość rozwoju do resumable upload.
- Kryteria akceptacji: model danych i API nie blokują przyszłego rozszerzenia.
- Wymagania bezpieczeństwa: limity, idempotency, kontrola ownership session.
- Wymagane testy: testy sesji uploadu i powtórzeń żądań.
- Dokumenty powiązane: [../architecture/DATA_MODEL.md](../architecture/DATA_MODEL.md), [../backend/API_CONVENTIONS.md](../backend/API_CONVENTIONS.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## STORAGE
### STORAGE-001 - Abstrakcja storage i lokalny backend plików
- Cel: oddzielić logikę biznesową od fizycznego przechowywania plików.
- Opis: interfejs storage, implementacja lokalna, metadane, usuwanie i signed links.
- Priorytet: wysoki.
- Zależności: FND-002, ADR 0003.
- Wymagania funkcjonalne: zapis, odczyt, delete, exists, metadata, link tymczasowy.
- Kryteria akceptacji: backend używa storage przez abstrakcję, bez zwracania fizycznych ścieżek.
- Wymagania bezpieczeństwa: path traversal protection, brak publicznych ścieżek systemowych.
- Wymagane testy: testy storage i błędów integracyjnych.
- Dokumenty powiązane: [../architecture/FILE_STORAGE.md](../architecture/FILE_STORAGE.md).
- Status początkowy: `IDEA`.
- ADR wymagany: tak.

## MEDIA_PROCESSING
### MEDIA-001 - Pipeline przetwarzania mediów
- Cel: generować miniatury, metadane i statusy po uploadzie.
- Opis: MediaProcessingJob, retry, ekstrakcja metadanych, korekta orientacji, preview wideo opcjonalnie.
- Priorytet: wysoki.
- Zależności: UPLOAD-001, STORAGE-001, ADR 0004, ADR 0007.
- Wymagania funkcjonalne: asynchroniczne joby przetwarzania z widocznym statusem.
- Kryteria akceptacji: przesłane media przechodzą do statusu gotowego albo błędu z możliwością retry.
- Wymagania bezpieczeństwa: bezpieczne przetwarzanie plików, ograniczenie kosztownych operacji.
- Wymagane testy: testy jobów, retry i statusów processingu.
- Dokumenty powiązane: [../backend/MEDIA_PROCESSING.md](../backend/MEDIA_PROCESSING.md), [../architecture/BACKGROUND_JOBS.md](../architecture/BACKGROUND_JOBS.md).
- Status początkowy: `IDEA`.
- ADR wymagany: tak.

## MODERATION
### MOD-001 - Moderacja materiałów i statusy publikacji
- Cel: dać organizatorowi kontrolę nad publikacją materiałów.
- Opis: approve, reject, hide, restore i operacje zbiorcze.
- Priorytet: wysoki.
- Zależności: MEDIA-001, GALLERY-002.
- Wymagania funkcjonalne: statusy plików i tryby galerii.
- Kryteria akceptacji: właściciel lub manager może zmieniać widoczność materiałów zgodnie z uprawnieniami.
- Wymagania bezpieczeństwa: ownership, audyt i brak obejścia moderacji przez publiczne API.
- Wymagane testy: testy statusów, bulk actions, autoryzacji i audytu.
- Dokumenty powiązane: [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md), [../product/PERMISSIONS_MATRIX.md](PERMISSIONS_MATRIX.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## DOWNLOADS
### DOWNLOAD-001 - Pobieranie pojedynczych plików i archiwów ZIP
- Cel: umożliwić bezpieczne pobieranie materiałów przez uprawnionych użytkowników.
- Opis: pojedyncze pobranie, zaznaczone pliki, cała galeria jako ZIP, link czasowy.
- Priorytet: średni.
- Zależności: STORAGE-001, MEDIA-001, ADR 0009.
- Wymagania funkcjonalne: asynchroniczne przygotowanie ZIP i status archiwum.
- Kryteria akceptacji: użytkownik może pobrać plik lub archiwum zgodnie z ustawieniami galerii.
- Wymagania bezpieczeństwa: signed links, wygasanie, kontrola dostępu i limitów.
- Wymagane testy: testy pobrań, wygasania linków i niepowodzeń generacji ZIP.
- Dokumenty powiązane: [../architecture/BACKGROUND_JOBS.md](../architecture/BACKGROUND_JOBS.md), [../adr/0009-download-archive-generation.md](../adr/0009-download-archive-generation.md).
- Status początkowy: `IDEA`.
- ADR wymagany: tak.

## CUSTOMIZATION
### CUSTOM-001 - Personalizacja wyglądu galerii
- Cel: umożliwić bezpieczną konfigurację wyglądu bez wstrzykiwania kodu.
- Opis: motywy, kolory, tło, okładka, tekst powitalny i widoczność sekcji.
- Priorytet: średni.
- Zależności: GALLERY-001.
- Wymagania funkcjonalne: kontrolowane opcje personalizacji per galeria.
- Kryteria akceptacji: użytkownik zmienia wygląd galerii wyłącznie przez bezpieczne opcje.
- Wymagania bezpieczeństwa: brak dowolnego HTML, CSS i JavaScript.
- Wymagane testy: testy walidacji ustawień i renderowania wariantów UI.
- Dokumenty powiązane: [../frontend/UI_COMPONENTS.md](../frontend/UI_COMPONENTS.md), [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## QR_CODES
### QR-001 - Generowanie kodów QR dla wydarzeń i galerii
- Cel: uprościć wejście gości do galerii.
- Opis: generowanie PNG/SVG, wersja do druku, konfiguracja docelowego linku.
- Priorytet: średni.
- Zależności: PUBLIC-001.
- Wymagania funkcjonalne: QR dla wydarzenia i galerii.
- Kryteria akceptacji: organizator może pobrać QR do wykorzystania na wydarzeniu.
- Wymagania bezpieczeństwa: QR musi prowadzić do prawidłowego, kontrolowanego entrypointu.
- Wymagane testy: testy generacji i poprawności linków.
- Dokumenty powiązane: [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md), [../architecture/INTEGRATIONS.md](../architecture/INTEGRATIONS.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## STATISTICS
### STAT-001 - Statystyki wydarzenia i galerii
- Cel: dać organizatorowi i administratorowi użyteczne metryki.
- Opis: uploady, liczba zdjęć i filmów, wyświetlenia, pobrania, storage, błędy.
- Priorytet: średni.
- Zależności: UPLOAD-001, DOWNLOAD-001.
- Wymagania funkcjonalne: dashboard zdarzeń i podstawowe agregaty systemowe.
- Kryteria akceptacji: właściciel i administrator widzą przypisane im metryki bez naruszania prywatności gości.
- Wymagania bezpieczeństwa: anonimizacja lub ograniczenie danych gości, kontrola zakresu dostępu.
- Wymagane testy: testy agregacji, filtrów i autoryzacji.
- Dokumenty powiązane: [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md), [../operations/MONITORING.md](../operations/MONITORING.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## NOTIFICATIONS
### NOTIF-001 - Powiadomienia systemowe i zdarzeniowe
- Cel: dostarczać krytyczne komunikaty użytkownikom i administratorom.
- Opis: e-mail weryfikacyjny, reset hasła, zaproszenia, limit miejsca, gotowy ZIP, błędy processingu.
- Priorytet: średni.
- Zależności: AUTH-002, INV-001, DOWNLOAD-001.
- Wymagania funkcjonalne: rozróżnienie powiadomień obowiązkowych i opcjonalnych.
- Kryteria akceptacji: użytkownik otrzymuje kluczowe powiadomienia w przewidywalnych momentach.
- Wymagania bezpieczeństwa: brak wrażliwych danych w treści wiadomości i logach.
- Wymagane testy: testy generacji payloadu, wysyłki i scenariuszy błędów.
- Dokumenty powiązane: [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md), [../architecture/INTEGRATIONS.md](../architecture/INTEGRATIONS.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## SUBSCRIPTIONS
### PLAN-001 - Plany i limity użytkowników
- Cel: przygotować platformę do różnych poziomów dostępu i wykorzystania.
- Opis: plany FREE, BASIC, PREMIUM, ADMIN_ASSIGNED, CUSTOM oraz wymuszanie limitów.
- Priorytet: średni.
- Zależności: USER-001, STORAGE-001, STAT-001.
- Wymagania funkcjonalne: przypisanie planu, limity przestrzeni, liczby wydarzeń i galerii.
- Kryteria akceptacji: system potrafi egzekwować limity planu w use case.
- Wymagania bezpieczeństwa: brak obejścia limitów przez API publiczne i administracyjne.
- Wymagane testy: testy limitów, konfliktów i zmian planów.
- Dokumenty powiązane: [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md), [../architecture/DATA_MODEL.md](../architecture/DATA_MODEL.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## ADMIN
### ADMIN-001 - Dashboard i wyszukiwanie zasobów administracyjnych
- Cel: zapewnić administratorowi operacyjny wgląd w platformę.
- Opis: lista użytkowników, wydarzeń, galerii, plików, filtrowanie i szczegóły.
- Priorytet: wysoki.
- Zależności: AUTH-001, EVENT-001, GALLERY-001.
- Wymagania funkcjonalne: panel administracyjny i wyszukiwanie.
- Kryteria akceptacji: administrator może odnaleźć i przeanalizować kluczowe zasoby.
- Wymagania bezpieczeństwa: osobny tor administracyjny, silna autoryzacja i audyt.
- Wymagane testy: testy admin API, ról i paginacji.
- Dokumenty powiązane: [../product/PERMISSIONS_MATRIX.md](PERMISSIONS_MATRIX.md), [../operations/LOGGING.md](../operations/LOGGING.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

### ADMIN-002 - Administracyjne akcje operacyjne
- Cel: umożliwić bezpieczne działania operatorskie.
- Opis: blokowanie kont, retry processingu, zmiana limitów, dezaktywacja wydarzeń.
- Priorytet: wysoki.
- Zależności: ADMIN-001, AUDIT-001.
- Wymagania funkcjonalne: jawne akcje administracyjne z powodem i wynikiem.
- Kryteria akceptacji: każda akcja jest autoryzowana, audytowana i odwracalna tam, gdzie to możliwe.
- Wymagania bezpieczeństwa: ścisły audyt, ograniczenie ról admina, brak „silent actions”.
- Wymagane testy: testy uprawnień, audytu i negatywnych ścieżek.
- Dokumenty powiązane: [../architecture/MODULES.md](../architecture/MODULES.md), [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## AUDIT
### AUDIT-001 - Log audytowy działań użytkowników i administratorów
- Cel: utrwalać krytyczne zdarzenia systemowe.
- Opis: login, błędy loginu, wylogowania, zmiany ról, usunięcia, zmiany planów, akcje admina.
- Priorytet: wysoki.
- Zależności: AUTH-001, MEMBER-001, ADMIN-002.
- Wymagania funkcjonalne: spojny model `AuditLog` i `AdminAction`, z katalogiem eventow rozwijanym razem ze zmianami wrazliwych flow.
- Kryteria akceptacji: każda wrażliwa akcja zostawia ślad audytowy z wynikiem i kontekstem.
- Wymagania bezpieczeństwa: ograniczony dostęp do logu audytowego i retencja zgodna z prywatnością.
- Wymagane testy: testy zapisu audytu, filtrowania wpisow oraz regresji dla nowych eventow.
- Dokumenty powiązane: [../architecture/DATA_MODEL.md](../architecture/DATA_MODEL.md), [../operations/LOGGING.md](../operations/LOGGING.md).
- Status: `IN PROGRESS`.
- ADR wymagany: nie.

## SECURITY
### SEC-001 - Security baseline aplikacji webowej
- Cel: wdrożyć bazowe zabezpieczenia aplikacji i API.
- Opis: CSRF, CORS, secure headers, rate limiting, logi bezpieczeństwa, secret handling.
- Priorytet: wysoki.
- Zależności: AUTH-001, FND-002.
- Wymagania funkcjonalne: centralna konfiguracja bezpieczeństwa i polityk.
- Kryteria akceptacji: aplikacja spełnia uzgodnione minimum bezpieczeństwa dla prywatnych danych i publicznych endpointów.
- Wymagania bezpieczeństwa: pełny zakres tego zadania jest bezpieczeństwem.
- Wymagane testy: testy security, negatywne przypadki auth i limity żądań.
- Dokumenty powiązane: [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md), [../security/THREAT_MODEL.md](../security/THREAT_MODEL.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

### SEC-002 - Bezpieczeństwo uploadu i dostępu do plików
- Cel: zabezpieczyć najbardziej ryzykowny publiczny obszar systemu.
- Opis: MIME validation, file type restrictions, traversal protection, signed downloads, limity.
- Priorytet: wysoki.
- Zależności: UPLOAD-001, STORAGE-001.
- Wymagania funkcjonalne: bezpieczny upload i pobieranie.
- Kryteria akceptacji: system odrzuca niebezpieczne pliki i nie ujawnia zasobów spoza zakresu uprawnień.
- Wymagania bezpieczeństwa: IDOR, MIME spoofing, path traversal, zip bombs, brute force.
- Wymagane testy: testy negatywne uploadu i pobrań.
- Dokumenty powiązane: [../security/FILE_UPLOAD_SECURITY.md](../security/FILE_UPLOAD_SECURITY.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## OPERATIONS
### OPS-001 - Konfiguracja środowisk i operacyjna gotowość wdrożenia
- Cel: przygotować proces uruchamiania i utrzymania środowisk.
- Opis: development, test, production, env vars, health checks, log rotation, rollout i rollback.
- Priorytet: wysoki.
- Zależności: FND-002.
- Wymagania funkcjonalne: udokumentowany model środowisk i konfiguracji.
- Kryteria akceptacji: środowiska mają spójny model konfiguracji i podstawowe procedury operacyjne.
- Wymagania bezpieczeństwa: sekrety poza repozytorium, HTTPS, minimalne uprawnienia procesów.
- Wymagane testy: testy deployment readiness, smoke tests po wdrożeniu.
- Dokumenty powiązane: [../operations/DEPLOYMENT.md](../operations/DEPLOYMENT.md), [../operations/CONFIGURATION.md](../operations/CONFIGURATION.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## BACKUP
### BACKUP-001 - Backup i odtwarzanie platformy
- Cel: zapewnić odzyskiwanie bazy, storage i konfiguracji.
- Opis: harmonogram backupu, retencja, kopie poza VPS, testy restore, RPO/RTO.
- Priorytet: wysoki.
- Zależności: OPS-001, STORAGE-001.
- Wymagania funkcjonalne: procedury backup i restore dla bazy i storage.
- Kryteria akceptacji: istnieje udokumentowana i testowana procedura odtworzenia.
- Wymagania bezpieczeństwa: szyfrowanie backupu, kontrola dostępu i retencja.
- Wymagane testy: testy restore i zgodności danych po odtworzeniu.
- Dokumenty powiązane: [../operations/BACKUP_AND_RESTORE.md](../operations/BACKUP_AND_RESTORE.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## MONITORING
### MON-001 - Monitoring i observability
- Cel: zapewnić widoczność stanu aplikacji i infrastruktury.
- Opis: Actuator, health endpoints, logi strukturalne, correlation ID, alerty storage i błędów.
- Priorytet: wysoki.
- Zależności: OPS-001, MEDIA-001.
- Wymagania funkcjonalne: monitoring health, uploadów, zadań processingu i storage.
- Kryteria akceptacji: operator otrzymuje sygnał o awarii krytycznych obszarów.
- Wymagania bezpieczeństwa: logi bez danych wrażliwych, kontrola dostępu do metryk i health.
- Wymagane testy: testy health endpointów, smoke testy alertów i logowania.
- Dokumenty powiązane: [../operations/MONITORING.md](../operations/MONITORING.md), [../operations/LOGGING.md](../operations/LOGGING.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## PERFORMANCE
### PERF-001 - Wydajność galerii publicznej i operacji plikowych
- Cel: utrzymać akceptowalną wydajność przy rosnącej liczbie mediów.
- Opis: lazy loading, paginacja lub infinite scroll, indeksy, wydajność ZIP i processingu.
- Priorytet: średni.
- Zależności: GALLERY-001, MEDIA-001, DOWNLOAD-001.
- Wymagania funkcjonalne: wydajne listowanie, filtrowanie i pobieranie.
- Kryteria akceptacji: główne ścieżki działają płynnie dla docelowego wolumenu danych pierwszej fazy produkcji.
- Wymagania bezpieczeństwa: brak degradacji limitów i ochrony przy optymalizacjach.
- Wymagane testy: testy wydajnościowe, regresja krytycznych zapytań i widoków.
- Dokumenty powiązane: [../product/NON_FUNCTIONAL_REQUIREMENTS.md](NON_FUNCTIONAL_REQUIREMENTS.md), [../operations/MONITORING.md](../operations/MONITORING.md).
- Status początkowy: `IDEA`.
- ADR wymagany: nie.

## PRIVACY
### PRIV-001 - Techniczna prywatność i retencja danych
- Cel: zapewnić minimalizację danych i kontrolowane usuwanie.
- Opis: retencja logów, usuwanie konta, eksport danych, retencja mediów i ograniczenie danych gości.
- Priorytet: wysoki.
- Zależności: EVENT-002, AUDIT-001, BACKUP-001.
- Wymagania funkcjonalne: model usuwania i retencji danych użytkowników, wydarzeń i mediów.
- Kryteria akceptacji: system ma technicznie wykonalne procedury usuwania i retencji bez utraty spójności.
- Wymagania bezpieczeństwa: prywatność danych gości i właścicieli, kontrola dostępu, zgodność operacyjna.
- Wymagane testy: testy soft delete, hard delete, retencji i eksportu danych.
- Dokumenty powiązane: [../security/PRIVACY_AND_DATA_RETENTION.md](../security/PRIVACY_AND_DATA_RETENTION.md), [../adr/0006-soft-delete-and-retention.md](../adr/0006-soft-delete-and-retention.md).
- Status początkowy: `IDEA`.
- ADR wymagany: tak.

## Powiązane dokumenty
- [FEATURE_ROADMAP.md](FEATURE_ROADMAP.md)
- [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md)
- [../development/WORKFLOW.md](../development/WORKFLOW.md)
- [../development/DEFINITION_OF_READY.md](../development/DEFINITION_OF_READY.md)

## Decyzje otwarte
- Które zadania z obszaru performance i komercjalizacji powinny wejść do pierwszego produkcyjnego zakresu po stabilizacji fundamentów.
