import fs from 'node:fs'
import path from 'node:path'

const resultsDir = process.env.ALLURE_RESULTS_DIR ?? 'allure-results'
const repository = process.env.GITHUB_REPOSITORY ?? ''
const [owner = '', repo = ''] = repository.split('/')
const serverUrl = process.env.GITHUB_SERVER_URL ?? 'https://github.com'
const runId = process.env.GITHUB_RUN_ID ?? 'local'
const runNumber = process.env.GITHUB_RUN_NUMBER ?? runId
const runAttempt = process.env.GITHUB_RUN_ATTEMPT ?? '1'
const runUrl = repository ? `${serverUrl}/${repository}/actions/runs/${runId}` : 'local'
const dashboardUrl =
  process.env.QUALITY_DASHBOARD_URL ||
  (owner && repo ? `https://${owner}.github.io/${repo}/` : 'not published')
const branch =
  process.env.GITHUB_HEAD_REF || process.env.GITHUB_REF_NAME || process.env.GITHUB_REF || 'local'

function escapeProperty(value) {
  return String(value ?? '')
    .replaceAll('\\', '\\\\')
    .replaceAll('\n', '\\n')
    .replaceAll('\r', '\\r')
    .replaceAll('=', '\\=')
    .replaceAll(':', '\\:')
}

fs.mkdirSync(resultsDir, { recursive: true })

const environment = {
  Quality_Dashboard_URL: dashboardUrl,
  GitHub_Actions_Run_URL: runUrl,
  Machine_Quality_Report: 'quality-report-json artifact in this workflow run',
  Repository: repository || 'local',
  Branch: branch,
  Commit: process.env.GITHUB_SHA ?? 'local',
  Workflow: process.env.GITHUB_WORKFLOW ?? 'local',
  Run_Attempt: runAttempt,
  Node: process.version,
}

const environmentProperties = Object.entries(environment)
  .map(([key, value]) => `${escapeProperty(key)}=${escapeProperty(value)}`)
  .join('\n')
fs.writeFileSync(path.join(resultsDir, 'environment.properties'), `${environmentProperties}\n`)

const executor = {
  name: 'GitHub Actions',
  type: 'github',
  url: repository ? `${serverUrl}/${repository}/actions` : serverUrl,
  buildName: `${process.env.GITHUB_WORKFLOW ?? 'local'} #${runNumber}`,
  buildUrl: runUrl,
  reportName: 'WeedingGallery Playwright E2E',
  reportUrl: dashboardUrl,
}
fs.writeFileSync(path.join(resultsDir, 'executor.json'), `${JSON.stringify(executor, null, 2)}\n`)

console.log(`Allure metadata written to ${resultsDir}`)
