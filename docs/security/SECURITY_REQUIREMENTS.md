# Wymagania Bezpieczeństwa

## Cel dokumentu
Opisuje wymagania bezpieczeństwa dla całej platformy.

## Status dokumentu
- Status: draft
- Zakres: security baseline dla stanu docelowego
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Wymagania bezpieczeństwa są planowane, nie wdrożone.

## Stan docelowy
- System zabezpieczony zgodnie z praktykami web security dla aplikacji przetwarzającej prywatne zdjęcia i filmy.

## Główne obszary
- OWASP Top 10
- Bezpieczne uwierzytelnianie i sesje
- Ochrona uploadu plików
- Autoryzacja oparta na ownership
- Audyt działań administracyjnych
- Bezpieczna konfiguracja produkcyjna

## Wymagania
- Hashowanie haseł Argon2id lub BCrypt.
- Silne tokeny i bezpieczna rotacja.
- CSRF protection dla sesji cookie.
- Ograniczony CORS.
- Ochrona przed XSS, SQL injection i path traversal.
- Walidacja MIME, rozszerzeń i sygnatur plików.
- Ograniczenia dla SVG, plików wykonywalnych i zip bombs.
- Rate limiting na logowanie, reset hasła, publiczny upload i dostęp do galerii.
- HSTS, CSP, `X-Content-Type-Options`, `Referrer-Policy`, `X-Frame-Options` lub nowsza polityka frame ancestors.
- HTTPS wszędzie poza lokalnym developmentem.
- Skanowanie zależności i aktualizacje bezpieczeństwa.

## Publiczne endpointy
- Muszą być objęte limitami, walidacją i monitoringiem nadużyć.
- Nie mogą ujawniać niepotrzebnych informacji o użytkownikach i zasobach.

## Powiązane dokumenty
- [FILE_UPLOAD_SECURITY.md](FILE_UPLOAD_SECURITY.md)
- [THREAT_MODEL.md](THREAT_MODEL.md)
- [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md)

## Decyzje otwarte
- Czy pierwsza wersja produkcyjna wymaga dodatkowego WAF poza Nginx i kontrolami aplikacyjnymi.
