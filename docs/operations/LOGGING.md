# Logowanie Operacyjne

## Cel dokumentu
Opisuje zasady logowania aplikacyjnego, strukturalnego i audytowego.

## Status dokumentu
- Status: draft
- Zakres: logi aplikacyjne i operacyjne
- Ostatnia aktualizacja: 2026-07-13

## Stan obecny
- W kodzie istnieje juz techniczne logowanie przez `@Slf4j` oraz podstawowy biznesowy audyt oparty o `AuditService`.
- Aktualna tabela audytu to `audit_events`.
- Obecnie zapisywane eventy audytowe to `USER_REGISTERED`, `USER_LOGGED_IN`, `USER_LOGIN_FAILED`, `USER_LOGIN_BLOCKED` i `USER_LOGGED_OUT`.

## Stan docelowy
- Logi strukturalne z correlation ID, kontrola danych wrazliwych oraz rozszerzony log audytowy dla wszystkich krytycznych operacji.

## Rozroznienie odpowiedzialnosci
- Logi aplikacyjne sluza do diagnostyki technicznej.
- Audyt sluzy do odtworzenia wrazliwych dzialan biznesowych i administracyjnych.
- Logi aplikacyjne nie zastepuja `AuditEvent`, a audyt nie zastepuje logow technicznych.

## Zasady
- Dane osobowe, tokeny i sekrety sa redagowane lub pomijane.
- Wpisy bledow powinny zawierac correlation ID, gdy mechanizm zostanie dodany.
- Logi powinny wspierac analize auth, uploadow, ZIP i bledow integracji.
- Kazda nowa wrazliwa akcja powinna byc oceniona pod katem potrzeby wpisu audytowego.
- Rozszerzenie katalogu eventow audytowych wymaga jednoczesnej aktualizacji kodu, testow i dokumentacji.

## Minimalny zakres audytu aktualnie zaimplementowany
- Udane utworzenie konta przez administratora.
- Udane zalogowanie uzytkownika.
- Nieudane logowania dla istniejacego konta.
- Blokada konta po wielu blednych logowaniach.
- Wylogowanie i zakonczenie sesji.

## Zakres audytu do dalszego rozszerzenia
- Zmiany rol i innych uprawnien.
- Akcje administracyjne wykonywane na kontach i zasobach.

## Powiazane dokumenty
- [MONITORING.md](MONITORING.md)
- [INCIDENT_RESPONSE.md](INCIDENT_RESPONSE.md)
- [../backend/AUTHENTICATION_AND_AUTHORIZATION.md](../backend/AUTHENTICATION_AND_AUTHORIZATION.md)
- [../security/SECURITY_REQUIREMENTS.md](../security/SECURITY_REQUIREMENTS.md)

## Decyzje otwarte
- Czy przyjac jednolity format JSON logs we wszystkich srodowiskach poza developmentem.
