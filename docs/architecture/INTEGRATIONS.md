# Integracje

## Cel dokumentu
Opisuje planowane integracje z systemami zewnętrznymi i wewnętrzne punkty rozszerzeń.

## Status dokumentu
- Status: draft
- Zakres: integracje docelowe i kontrakty techniczne
- Ostatnia aktualizacja: 2026-07-12

## Stan obecny
- Integracje są planowane, nie gotowe.

## Stan docelowy
- Niewielka liczba integracji o jasno zdefiniowanych kontraktach i odpowiedzialnościach.

## Integracje zewnętrzne
| Integracja | Cel |
| --- | --- |
| Provider e-mail | weryfikacja e-mail, reset hasła, zaproszenia, alerty |
| Local filesystem | początkowe przechowywanie plików |
| S3/R2/MinIO/Azure Blob | przyszłe storage obiektowe |
| Generator QR | generowanie PNG/SVG dla wydarzeń i galerii |
| Let's Encrypt | certyfikaty HTTPS |

## Integracje wewnętrzne
- Storage service
- Notification service
- Background jobs executor
- Thumbnail generator
- ZIP archive generator

## Zasady integracyjne
- Integracje ukrywamy za interfejsami domenowymi lub infrastrukturalnymi.
- Błędy integracji muszą być mapowane na przewidywalne błędy aplikacyjne.
- Dane wrażliwe nie mogą trafiać do logów integracyjnych.

## Powiązane dokumenty
- [FILE_STORAGE.md](FILE_STORAGE.md)
- [../operations/CONFIGURATION.md](../operations/CONFIGURATION.md)
- [../backend/MEDIA_PROCESSING.md](../backend/MEDIA_PROCESSING.md)

## Decyzje otwarte
- Czy provider e-mail w pierwszej fazie będzie zewnętrzną usługą transakcyjną czy SMTP zewnętrznego hostingu.
