import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'

const root = process.cwd()
const requiredFiles = [
  'docs/DEFINITION_OF_DONE.md',
  'docs/testing/TEST_STRATEGY.md',
  'docs/testing/STAGES_7_10_TEST_DESIGN_BRIEF.md',
  'docs/checklists/STAGES_7_10_PRODUCTION_CHECKLIST.md',
  'api-contract/API_CONTRACT.md',
  'docs/product/FEATURE_ROADMAP.md',
  '.github/workflows/pr-validation.yml',
  '.github/workflows/nightly-quality.yml',
]

const missing = requiredFiles.filter((file) => !existsSync(join(root, file)))
if (missing.length > 0) {
  console.error(`Missing production-readiness files:\n${missing.join('\n')}`)
  process.exit(1)
}

const roadmap = readFileSync(join(root, 'docs/product/FEATURE_ROADMAP.md'), 'utf8')
const contract = readFileSync(join(root, 'api-contract/API_CONTRACT.md'), 'utf8')
const checklist = readFileSync(join(root, 'docs/checklists/STAGES_7_10_PRODUCTION_CHECKLIST.md'), 'utf8')
const errors = []

for (const stage of ['7', '8', '9', '10']) {
  const section = roadmap.match(new RegExp(`## Etap ${stage}[^\\n]*([\\s\\S]*?)(?=\\n## Etap |\\n## Zasady roadmapy)`))?.[1] ?? ''
  if (!section.includes('DONE_FIRST_ITERATION')) errors.push(`Roadmap Etap ${stage} is not marked DONE_FIRST_ITERATION`)
}
for (const marker of [
  '## Kontrakt MEDIA-002 - Etap 8 moderacji publikacji',
  'Status: `IMPLEMENTED_FIRST_ITERATION`.',
  '## Kontrakt Etap 9/10',
]) {
  if (!contract.includes(marker)) errors.push(`API contract marker missing: ${marker}`)
}
for (const marker of ['## Repository checks', '## Checks requiring Docker or a deployed environment', 'CI_REQUIRED']) {
  if (!checklist.includes(marker)) errors.push(`Checklist marker missing: ${marker}`)
}

if (errors.length > 0) {
  console.error(errors.join('\n'))
  process.exit(1)
}

console.log('Stage 7-10 readiness documentation is internally consistent.')
console.log('Docker/MySQL and operational evidence remain explicitly gated.')

