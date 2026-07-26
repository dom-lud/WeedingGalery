# Production Checklist: Stages 7-10

## Purpose

This checklist records production-readiness evidence for Stages 7-10. It
distinguishes repository-verifiable work from checks requiring Docker, MySQL,
or a deployed environment.

## Current status

- Stages 7-10: `DONE_FIRST_ITERATION` (potwierdzone zielonym CI 2026-07-26).
- Production promotion remains blocked by the `CI_REQUIRED` and `OPEN` rows
  below; this is an operational gate, not an application-logic defect.

## Repository checks

| Check | Status | Evidence |
| --- | --- | --- |
| Test design brief and risk matrix | `PASS` | [STAGES_7_10_TEST_DESIGN_BRIEF.md](../testing/STAGES_7_10_TEST_DESIGN_BRIEF.md) |
| API contract | `PASS` | [API_CONTRACT.md](../../api-contract/API_CONTRACT.md) |
| Backend compile and stage tests | `PASS_RECORDED` | Backend tests under `backend/src/test` |
| Frontend tests and coverage gates | `PASS_RECORDED` | Vitest/V8 thresholds in CI |
| Frontend lint and build | `PASS_RECORDED` | PR/Main workflows |
| Accessibility, keyboard, focus, responsive UI | `PASS_RECORDED` | Component tests and Playwright axe suite |
| Documentation consistency | `PASS` | `.github/scripts/validate-stage-readiness.mjs` |
| Stage 7-10 E2E contract scenarios | `PASS_RECORDED` | Playwright: 21 scenariuszy przeszlo w CI; `frontend/tests/e2e/stages-7-10.spec.ts` |

## Checks requiring Docker or a deployed environment

| Check | Status | Required evidence |
| --- | --- | --- |
| MySQL migration and schema compatibility | `PASS_RECORDED` | `MySqlFlywayMigrationContractTest` przeszedl w zielonym CI |
| Stages 7-10 HTTP integration on MySQL/Compose | `CI_REQUIRED` | Owner, manager, outsider, guest, and admin scenarios |
| Public media privacy and download policy | `CI_REQUIRED` | Approved-only visibility, hidden/rejected exclusion, owner-only ZIP |
| Moderation boundaries and audit persistence | `CI_REQUIRED` | Transitions, 0/1/99/100/101 bulk items, replay/conflict, audit rows |
| Customization persistence and version conflict | `CI_REQUIRED` | MySQL GET/PUT, stale version, cross-gallery cover rejection |
| Customization event scoping | `OPEN_SECURITY` | Current endpoint is `/api/galleries/{galleryId}/customization`; verify eventId scoping before production `DONE` |
| Admin RBAC and audit persistence | `CI_REQUIRED` | Anonymous/USER/ADMIN matrix and mutation audit rows |
| WCAG and responsive E2E for new screens | `PASS_RECORDED` | Playwright + axe, keyboard and responsive scenarios passed in CI |
| Large-gallery performance and transfer budget | `OPEN` | Load-test report with agreed thresholds |
| Production smoke, backup/restore, rollback, monitoring | `OPEN` | Stage 13 operational evidence |

## CI interpretation

The repository may label Stages 7-10 `DONE_FIRST_ITERATION` after repository
checks pass. They must not be promoted to production `DONE` until every
`CI_REQUIRED` row has evidence from a Docker-capable runner and the `OPEN`
operational rows are addressed by Stage 13.
