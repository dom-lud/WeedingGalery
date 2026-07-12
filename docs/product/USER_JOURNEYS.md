# Ścieżki Użytkowników

## Cel dokumentu
Opisuje główne przepływy użytkowników dla stanu docelowego produktu.

## Status dokumentu
- Status: draft
- Zakres: kluczowe user journeys
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Ścieżki opisane w tym dokumencie nie są jeszcze zaimplementowane end-to-end.

## Stan docelowy
- Spójne przepływy dla gościa, właściciela wydarzenia, współzarządzającego i administratora.

## Gość przesyła zdjęcia do galerii
1. Otwiera link lub skanuje kod QR.
2. Jeśli galeria wymaga kodu, podaje kod dostępu.
3. Widzi ekran powitalny z opisem wydarzenia i zasadami uploadu.
4. Wybiera zdjęcia i filmy lub używa drag and drop.
5. Obserwuje postęp uploadu każdego pliku.
6. W razie błędu ponawia wybrane pliki.
7. Jeśli galeria pozwala na przeglądanie, przechodzi do listy opublikowanych materiałów.

## Właściciel tworzy wydarzenie i galerie
1. Rejestruje konto i weryfikuje e-mail.
2. Tworzy wydarzenie, podaje nazwę, typ, datę i ustawienia prywatności.
3. Dodaje jedną lub wiele galerii.
4. Generuje linki i kody QR.
5. Włącza lub wyłącza upload, pobieranie i moderację.
6. Personalizuje ekran galerii.
7. Monitoruje uploady, statystyki i wykorzystanie miejsca.

## Właściciel zaprasza współzarządzającego
```mermaid
sequenceDiagram
    participant Owner as EventOwner
    participant API as Backend API
    participant Mail as Notification Service
    participant Manager as User

    Owner->>API: Utworzenie zaproszenia e-mail
    API->>API: Zapis EventInvitation
    API->>Mail: Wysłanie linku zaproszenia
    Mail-->>Manager: E-mail z zaproszeniem
    Manager->>API: Akceptacja zaproszenia
    API->>API: Utworzenie EventMembership
    API-->>Owner: Zaktualizowana lista członków
```

## Właściciel moderuje materiały
1. Otwiera panel wydarzenia.
2. Filtruje materiały po statusie `PENDING_APPROVAL`.
3. Wykonuje zatwierdzenie, odrzucenie lub ukrycie pojedynczo albo zbiorczo.
4. System aktualizuje widoczność galerii publicznej i zapisuje audyt.

## Administrator reaguje na incydent
1. Otrzymuje alert o błędzie uploadu lub zgłoszeniu nadużycia.
2. Wyszukuje użytkownika, wydarzenie, galerię lub plik.
3. Analizuje audyt, logi i statusy zadań przetwarzania.
4. Podejmuje akcję administracyjną z obowiązkowym powodem.
5. System zapisuje wpis `AdminAction` i `AuditLog`.

## Powiązane dokumenty
- [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md)
- [../architecture/BACKGROUND_JOBS.md](../architecture/BACKGROUND_JOBS.md)
- [../backend/API_ENDPOINTS.md](../backend/API_ENDPOINTS.md)

## Decyzje otwarte
- Czy galeria publiczna ma wspierać mechanizm ulubionych już w pierwszym wydaniu.
