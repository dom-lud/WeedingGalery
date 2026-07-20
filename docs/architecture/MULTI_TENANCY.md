# Wieloużytkownikowość i Izolacja Danych

## Cel dokumentu
Definiuje model logicznej separacji danych wielu użytkowników, wydarzeń i galerii.

## Status dokumentu
- Status: draft
- Zakres: multi-tenancy na poziomie aplikacji i danych
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Izolacja wydarzen jest zaimplementowana przez `events.owner_user_id` i aktywne `event_memberships`.
- Scoped queries, use case i testy IDOR maskuja cudzy oraz nieistniejacy `eventId` tym samym `404`.
- Izolacja galerii jest zaimplementowana w GALLERY-001: management API dziedziczy dostep przez event ownership albo aktywne membership, a zapytania zasobu sa zawsze scoped przez `eventId + galleryId` i maskuja cross-event IDOR. Media i dostep publiczny pozostaja zakresem kolejnych etapow.

## Stan docelowy
- Jedna instancja aplikacji i jedna baza obsługują wiele niezależnych kont z logiczną izolacją danych.

## Zasady izolacji
- Brak współdzielonych danych biznesowych między tenantami poza globalnymi konfiguracjami systemowymi.
- Każde zapytanie biznesowe musi być zawężone przez ownership zasobu, `event_id`, członkostwo albo uprawnienie administratora.
- Publiczny dostęp do galerii nie może pozwalać na przejście do innych zasobów tego samego właściciela bez jawnych uprawnień.

## Model izolacji
- Tenantem logicznym jest konto użytkownika wraz z należącymi do niego wydarzeniami.
- Dodatkową granicą bezpieczeństwa jest `event_id`.
- `EventMembership` wprowadza dostęp współdzielony tylko do wybranego wydarzenia.

## Wymagania implementacyjne
- Metody repozytoriów powinny wspierać filtrowanie po ownership.
- Use case musi sprawdzać kontekst wydarzenia, nie tylko identyfikator zasobu.
- Endpointy administracyjne działają na osobnym torze z pełnym audytem.

## Przykładowe ryzyka
- Pobranie pliku po samym `mediaId` bez sprawdzenia `event_id`
- Lista galerii zwracająca zasoby po `owner_id` bez kontroli membership
- Niewłaściwe użycie slugów umożliwiające enumerację galerii

## Powiązane dokumenty
- [DATA_MODEL.md](DATA_MODEL.md)
- [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md)
- [../adr/0005-multi-tenant-data-isolation.md](../adr/0005-multi-tenant-data-isolation.md)

## Decyzje otwarte
- Czy dla części tabel analitycznych wprowadzić materializowane agregaty per tenant.
