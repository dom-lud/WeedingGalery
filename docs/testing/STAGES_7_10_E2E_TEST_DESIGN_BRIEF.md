# Etapy 7-10: Test Design Brief E2E

## Zakres

Playwright tests cover the public gallery, moderation boundary, gallery customization,
administration namespace, authentication, CSRF and accessibility. The tests use the
real HTTP contract and do not change backend code or seed data.

## Risk matrix

| Risk | Incorrect implementation | E2E evidence |
| --- | --- | --- |
| Public IDOR | Gallery data is readable without a grant | Unauthenticated public read is rejected; token exchange then permits the same slug |
| CSRF bypass | Mutating admin or gallery endpoints accept a browser session without the token | Mutation without `X-XSRF-TOKEN` returns `403` |
| Ownership bypass | Administrator can use an ordinary event/media endpoint outside ownership | Access to another user's scoped resource is rejected |
| Moderation contract drift | Wrong route or missing scoped identifiers | Moderation endpoint returns the domain response/error rather than a routing error |
| Unsafe customization | HTML is interpreted as markup or JavaScript | Welcome text is submitted as text and the UI contains no injected element |
| Admin separation | Ordinary protected route exposes admin data | `/api/admin/**` is checked separately and `/admin` renders only for ADMIN |
| Accessibility regression | New dialogs/pages contain WCAG A/AA violations | axe WCAG 2.0/2.1 A/AA scan on admin and public states |

## Environment blockers

These tests require the same services as CI: backend on `localhost:8080`, frontend on
`localhost:5173`, a migrated database and the bootstrap admin credentials. When those
services are unavailable, Playwright must report the connection failure rather than
silently converting the scenario into a mock test.

## Local audit result

On 2026-07-26 Chromium was available and Playwright discovered all 21 tests (15
existing plus 6 added here). The six new tests were attempted and all stopped before
the first assertion because this workspace had no running web service: the default
local configuration resolved to `http://localhost` and returned `ECONNREFUSED ::1:80`.
Docker was unavailable, so a migrated MySQL runtime and the CI-style backend/frontend
startup could not be supplied locally. This is an infrastructure blocker, not a
passed or failed application assertion.
