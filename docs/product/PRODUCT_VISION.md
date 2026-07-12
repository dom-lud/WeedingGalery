# Wizja Produktu

## Cel dokumentu
Opisuje pełną wizję produktu, problem biznesowy, grupy użytkowników oraz zakres docelowej platformy.

## Status dokumentu
- Status: draft
- Zakres: wizja docelowa produktu
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Produkt nie jest jeszcze zaimplementowany w docelowej postaci.
- Repozytorium zawiera tylko szkic techniczny, który nie pokrywa pełnej wizji.

## Stan docelowy
- Wieloużytkownikowa platforma webowa do zbierania, organizowania i udostępniania mediów z prywatnych wydarzeń.
- Główny przypadek użycia: wesele.
- System gotowy również na inne typy wydarzeń prywatnych.

## Problem do rozwiązania
- Goście wykonują zdjęcia i filmy rozproszone na wielu urządzeniach.
- Organizatorzy potrzebują prostego, mobilnego sposobu zebrania materiałów bez wymuszania rejestracji gości.
- Potrzebna jest kontrola prywatności, moderacji, retencji danych i limitów storage.

## Wizja produktu
Platforma umożliwia organizatorom tworzenie wydarzeń i galerii, zapraszanie współzarządzających, udostępnianie galerii przez linki i kody QR oraz odbieranie zdjęć i filmów od gości bez konieczności zakładania konta przez gości. System zapewnia moderację, publiczne lub prywatne przeglądanie, pobieranie materiałów, personalizację wyglądu i bezpieczne zarządzanie plikami.

## Główne grupy użytkowników
- Gość wydarzenia
- Zarejestrowany użytkownik platformy
- Właściciel wydarzenia
- Współzarządzający wydarzeniem
- Administrator systemu
- Superadministrator

## Zasady produktowe
- Prywatność i kontrola dostępu są ważniejsze niż maksymalna otwartość.
- Użytkownik systemu może mieć wiele wydarzeń i wiele galerii.
- Gość nie musi posiadać konta.
- Każdy zasób posiada właściciela lub kontekst wydarzenia.
- Storage i limity są integralną częścią domeny.
- Funkcje planowane na później muszą pasować do jednej spójnej architektury.

## Zakres funkcjonalny
- Tożsamość użytkownika: rejestracja, logowanie, sesje, reset hasła
- Zarządzanie wydarzeniami i członkostwem
- Zarządzanie wieloma galeriami w ramach wydarzenia
- Publiczny dostęp do galerii przez slug, QR, link i opcjonalny kod dostępu
- Upload zdjęć i filmów z retry, walidacją i statusem
- Moderacja materiałów i przetwarzanie mediów
- Pobieranie pojedynczych plików i archiwów ZIP
- Personalizacja galerii i statystyki
- Panel administratora, audyt i konfiguracja
- Plany, limity i gotowość do przyszłej komercjalizacji

## Granice produktu
- Produkt nie jest publicznym portalem społecznościowym.
- Produkt nie służy do edycji zdjęć lub montażu filmów.
- Płatności mogą zostać dodane później, ale nie są wymagane w pierwszym uruchomieniu.
- Dokumentacja nie stanowi porady prawnej.

## Miary sukcesu produktu
- Wysoki odsetek skutecznych uploadów mobilnych
- Niski próg wejścia dla gości
- Czytelne zarządzanie prywatnością i moderacją
- Niska liczba błędów związanych z ownership i autoryzacją
- Przewidywalne wykorzystanie storage i retencji

## Powiązane dokumenty
- [USER_ROLES.md](USER_ROLES.md)
- [USER_JOURNEYS.md](USER_JOURNEYS.md)
- [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md)
- [../architecture/SYSTEM_ARCHITECTURE.md](../architecture/SYSTEM_ARCHITECTURE.md)

## Decyzje otwarte
- Zakres funkcji dostępnych w planie darmowym.
- Zakres personalizacji dostępnej dla użytkownika końcowego.
