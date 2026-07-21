import fs from "node:fs";
import path from "node:path";

const reportPath = process.env.QUALITY_REPORT_PATH ?? "quality-report.json";
const outputDir = process.env.QUALITY_DASHBOARD_DIR ?? "quality-dashboard";
const previousHistoryPath =
  process.env.QUALITY_HISTORY_PATH ?? path.join(outputDir, "history.json");
const historyLimit = Number(process.env.QUALITY_HISTORY_LIMIT ?? 365);

function readJson(filePath, fallback) {
  if (!fs.existsSync(filePath)) return fallback;
  return JSON.parse(fs.readFileSync(filePath, "utf8"));
}

function writeJson(filePath, data) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  fs.writeFileSync(filePath, `${JSON.stringify(data, null, 2)}\n`);
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function pct(value) {
  return value == null || Number.isNaN(Number(value))
    ? "n/a"
    : `${Number(value).toFixed(2)}%`;
}

function metricPoint(report) {
  return {
    run: report.run,
    gatesPassed: report.gates.passed,
    backend: {
      result: report.backend.result,
      tests: report.backend.tests,
      coverage: report.backend.coverage,
    },
    frontend: {
      result: report.frontend.result,
      tests: report.frontend.tests,
      coverage: report.frontend.coverage,
    },
    e2e: {
      result: report.e2e.result,
      tests: report.e2e.tests,
      failedTests: report.e2e.failedTests ?? [],
    },
    docker: report.docker,
  };
}

function sparkline(values, color) {
  const numeric = values.filter((value) => typeof value === "number");
  if (numeric.length < 2) return "<span class=\"muted\">za malo danych</span>";
  const width = 240;
  const height = 48;
  const min = Math.min(...numeric);
  const max = Math.max(...numeric);
  const range = max - min || 1;
  const points = numeric
    .map((value, index) => {
      const x = (index / Math.max(1, numeric.length - 1)) * width;
      const y = height - ((value - min) / range) * height;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(" ");
  return `<svg viewBox="0 0 ${width} ${height}" role="img" aria-label="trend"><polyline fill="none" stroke="${color}" stroke-width="3" points="${points}"/></svg>`;
}

const report = readJson(reportPath, null);
if (!report) throw new Error(`Missing quality report: ${reportPath}`);

const previousHistory = readJson(previousHistoryPath, []);
const history = [...previousHistory, metricPoint(report)]
  .sort((a, b) => String(a.run.createdAt).localeCompare(String(b.run.createdAt)))
  .filter(
    (entry, index, entries) =>
      entries.findIndex(
        (candidate) =>
          candidate.run.id === entry.run.id &&
          candidate.run.attempt === entry.run.attempt,
      ) === index,
  )
  .slice(-historyLimit);
const latest = history.at(-1);
const runDir = path.join(outputDir, "runs");

fs.mkdirSync(runDir, { recursive: true });
writeJson(path.join(outputDir, "history.json"), history);
writeJson(path.join(outputDir, "latest.json"), latest);
writeJson(path.join(runDir, `${report.run.id}-${report.run.attempt}.json`), report);

const coverageHistory = {
  backendInstructions: history.map((entry) => entry.backend.coverage.instructionsPct),
  backendBranches: history.map((entry) => entry.backend.coverage.branchesPct),
  frontendStatements: history.map((entry) => entry.frontend.coverage.statementsPct),
  frontendBranches: history.map((entry) => entry.frontend.coverage.branchesPct),
  frontendFunctions: history.map((entry) => entry.frontend.coverage.functionsPct),
  frontendLines: history.map((entry) => entry.frontend.coverage.linesPct),
};
const testDurationHistory = history.map(
  (entry) =>
    ((entry.backend.tests.durationMs ?? 0) +
      (entry.frontend.tests.durationMs ?? 0) +
      (entry.e2e.tests.durationMs ?? 0)) /
    1000,
);

const failedTests = (latest.e2e.failedTests ?? [])
  .slice(0, 20)
  .map((test) => `<li>${escapeHtml(test)}</li>`)
  .join("");

const rows = history
  .slice()
  .reverse()
  .slice(0, 30)
  .map(
    (entry) => `
      <tr>
        <td>${escapeHtml(new Date(entry.run.createdAt).toISOString().slice(0, 19).replace("T", " "))}</td>
        <td>${escapeHtml(entry.run.event)}</td>
        <td>${escapeHtml(entry.run.branch)}</td>
        <td><a href="${escapeHtml(entry.run.runUrl)}">${escapeHtml(entry.run.id)}</a></td>
        <td class="${entry.gatesPassed ? "ok" : "bad"}">${entry.gatesPassed ? "PASS" : "FAIL"}</td>
        <td>${pct(entry.backend.coverage.instructionsPct)} / ${pct(entry.backend.coverage.branchesPct)}</td>
        <td>${pct(entry.frontend.coverage.statementsPct)} / ${pct(entry.frontend.coverage.branchesPct)} / ${pct(entry.frontend.coverage.functionsPct)} / ${pct(entry.frontend.coverage.linesPct)}</td>
        <td>${entry.e2e.tests.passed ?? 0}/${entry.e2e.tests.total ?? 0}</td>
      </tr>`,
  )
  .join("");

const html = `<!doctype html>
<html lang="pl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>WeedingGallery quality dashboard</title>
  <style>
    :root { color-scheme: light dark; font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; }
    body { margin: 0; background: #f7f8fb; color: #172033; }
    main { max-width: 1180px; margin: 0 auto; padding: 32px 20px 48px; }
    header { display: flex; justify-content: space-between; gap: 16px; align-items: end; margin-bottom: 24px; }
    h1 { font-size: clamp(1.7rem, 4vw, 2.6rem); margin: 0 0 8px; letter-spacing: 0; }
    h2 { margin: 0 0 12px; font-size: 1.05rem; }
    p { margin: 0; }
    a { color: #1d4ed8; }
    .muted { color: #667085; }
    .status { padding: 8px 12px; border-radius: 8px; font-weight: 700; background: #e8f7ef; color: #067647; }
    .status.bad { background: #fdecec; color: #b42318; }
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(230px, 1fr)); gap: 14px; margin: 18px 0; }
    .card { background: white; border: 1px solid #dfe4ee; border-radius: 8px; padding: 16px; box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04); }
    .metric { font-size: 1.8rem; font-weight: 750; margin-top: 8px; }
    .ok { color: #067647; font-weight: 700; }
    .bad { color: #b42318; font-weight: 700; }
    .charts { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 14px; }
    svg { width: 100%; height: 54px; overflow: visible; }
    table { width: 100%; border-collapse: collapse; background: white; border: 1px solid #dfe4ee; border-radius: 8px; overflow: hidden; }
    th, td { padding: 10px 12px; border-bottom: 1px solid #e7ebf3; text-align: left; font-size: 0.92rem; vertical-align: top; }
    th { background: #eef2f7; color: #344054; }
    ul { margin: 8px 0 0; padding-left: 18px; }
    @media (prefers-color-scheme: dark) {
      body { background: #111827; color: #e5e7eb; }
      .card, table { background: #182230; border-color: #344054; }
      th { background: #243244; color: #d0d5dd; }
      td { border-color: #344054; }
      .muted { color: #98a2b3; }
      a { color: #93c5fd; }
    }
  </style>
</head>
<body>
  <main>
    <header>
      <div>
        <h1>WeedingGallery quality dashboard</h1>
        <p class="muted">Historia z main, nightly i workflow_dispatch. Coverage jest zapadka regresyjna, nie zastepuje macierzy ryzyk.</p>
      </div>
      <div class="status ${latest.gatesPassed ? "" : "bad"}">${latest.gatesPassed ? "PASS" : "FAIL"}</div>
    </header>

    <section class="grid">
      <article class="card"><h2>Backend tests</h2><p class="metric">${latest.backend.tests.passed ?? 0}/${latest.backend.tests.total ?? 0}</p><p class="muted">${escapeHtml(latest.backend.result)}</p></article>
      <article class="card"><h2>Frontend tests</h2><p class="metric">${latest.frontend.tests.passed ?? 0}/${latest.frontend.tests.total ?? 0}</p><p class="muted">${escapeHtml(latest.frontend.result)}</p></article>
      <article class="card"><h2>E2E tests</h2><p class="metric">${latest.e2e.tests.passed ?? 0}/${latest.e2e.tests.total ?? 0}</p><p class="muted">${latest.e2e.tests.flaky ?? 0} flaky, ${latest.e2e.tests.timedOut ?? 0} timed out</p></article>
      <article class="card"><h2>Latest run</h2><p class="metric"><a href="${escapeHtml(latest.run.runUrl)}">${escapeHtml(latest.run.id)}</a></p><p class="muted">${escapeHtml(latest.run.event)} / ${escapeHtml(latest.run.branch)}</p></article>
    </section>

    <section class="charts">
      <article class="card"><h2>Backend instructions</h2>${sparkline(coverageHistory.backendInstructions, "#2563eb")}<p>${pct(latest.backend.coverage.instructionsPct)}</p></article>
      <article class="card"><h2>Backend branches</h2>${sparkline(coverageHistory.backendBranches, "#7c3aed")}<p>${pct(latest.backend.coverage.branchesPct)}</p></article>
      <article class="card"><h2>Frontend statements</h2>${sparkline(coverageHistory.frontendStatements, "#0891b2")}<p>${pct(latest.frontend.coverage.statementsPct)}</p></article>
      <article class="card"><h2>Frontend branches</h2>${sparkline(coverageHistory.frontendBranches, "#c2410c")}<p>${pct(latest.frontend.coverage.branchesPct)}</p></article>
      <article class="card"><h2>Frontend functions</h2>${sparkline(coverageHistory.frontendFunctions, "#16a34a")}<p>${pct(latest.frontend.coverage.functionsPct)}</p></article>
      <article class="card"><h2>Frontend lines</h2>${sparkline(coverageHistory.frontendLines, "#9333ea")}<p>${pct(latest.frontend.coverage.linesPct)}</p></article>
      <article class="card"><h2>Total test time</h2>${sparkline(testDurationHistory, "#475467")}<p>${testDurationHistory.at(-1)?.toFixed(1) ?? "0.0"}s</p></article>
    </section>

    <section class="card" style="margin-top: 14px;">
      <h2>Failed tests</h2>
      ${failedTests ? `<ul>${failedTests}</ul>` : "<p class=\"muted\">Brak awarii w ostatnim raporcie.</p>"}
    </section>

    <section style="margin-top: 18px;">
      <h2>Recent runs</h2>
      <table>
        <thead><tr><th>Created</th><th>Event</th><th>Branch</th><th>Run</th><th>Gates</th><th>Backend cov</th><th>Frontend cov</th><th>E2E</th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
    </section>
  </main>
</body>
</html>`;

fs.writeFileSync(path.join(outputDir, "index.html"), html);
console.log(`Rendered ${path.join(outputDir, "index.html")} with ${history.length} history entries.`);
