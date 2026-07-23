import fs from "node:fs";
import path from "node:path";

const [, , command, root = "."] = process.argv;
const outputPath = process.env.GITHUB_OUTPUT;
const defaultPartDir = "quality-report-parts";

const COVERAGE_GATES = {
  backendInstructions: 90,
  backendBranches: 90,
  frontendStatements: 90,
  frontendBranches: 90,
  frontendFunctions: 90,
  frontendLines: 90,
};

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function readJson(filePath, fallback = {}) {
  if (!fs.existsSync(filePath)) return fallback;
  return JSON.parse(fs.readFileSync(filePath, "utf8"));
}

function writeJson(filePath, data) {
  ensureDir(filePath);
  fs.writeFileSync(filePath, `${JSON.stringify(data, null, 2)}\n`);
}

function setOutput(name, value) {
  const normalized = String(value ?? "");
  if (!outputPath) {
    process.stdout.write(`${name}=${normalized}\n`);
    return;
  }

  if (normalized.includes("\n")) {
    const delimiter = `ci_metrics_${name}_${Date.now()}`;
    fs.appendFileSync(
      outputPath,
      `${name}<<${delimiter}\n${normalized}\n${delimiter}\n`,
    );
    return;
  }

  fs.appendFileSync(outputPath, `${name}=${normalized}\n`);
}

function percentage(covered, missed) {
  const total = covered + missed;
  return total === 0 ? null : Number(((covered / total) * 100).toFixed(2));
}

function xmlAttribute(tag, name) {
  return Number(tag.match(new RegExp(`${name}="(\\d+)"`))?.[1] ?? 0);
}

function toInt(value) {
  return Number.isFinite(Number(value)) ? Number(value) : 0;
}

function passedCount(tests) {
  return Math.max(0, tests.total - tests.failed - tests.skipped);
}

function resultFromOutcome(outcome) {
  if (["success", "failure", "cancelled", "skipped"].includes(outcome))
    return outcome;
  return "skipped";
}

function writePart(name, data) {
  const explicitPath = process.env.CI_METRICS_JSON_PATH;
  const filePath = explicitPath ?? path.join(defaultPartDir, `${name}.json`);
  writeJson(filePath, data);
  setOutput("json_path", filePath);
}

function emitTestOutputs(tests) {
  setOutput("total", tests.total);
  setOutput("passed", tests.passed);
  setOutput("failed", tests.failed);
  setOutput("skipped", tests.skipped);
  setOutput("duration_ms", tests.durationMs);
}

function summarizeBackend() {
  const reportsDir = path.join(root, "target", "surefire-reports");
  const reportFiles = fs.existsSync(reportsDir)
    ? fs
        .readdirSync(reportsDir)
        .filter((file) => file.startsWith("TEST-") && file.endsWith(".xml"))
    : [];

  const tests = reportFiles.reduce(
    (summary, file) => {
      const xml = fs.readFileSync(path.join(reportsDir, file), "utf8");
      const suite = xml.match(/<testsuite\b[^>]*>/)?.[0] ?? "";
      summary.total += xmlAttribute(suite, "tests");
      summary.failed +=
        xmlAttribute(suite, "failures") + xmlAttribute(suite, "errors");
      summary.skipped += xmlAttribute(suite, "skipped");
      summary.durationMs += Math.round(
        Number(suite.match(/time="([\d.]+)"/)?.[1] ?? 0) * 1000,
      );
      return summary;
    },
    { total: 0, failed: 0, skipped: 0, durationMs: 0 },
  );
  tests.passed = passedCount(tests);

  const jacocoPath = path.join(root, "target", "site", "jacoco", "jacoco.xml");
  const jacoco = fs.existsSync(jacocoPath)
    ? fs.readFileSync(jacocoPath, "utf8")
    : "";
  const counters = [
    ...jacoco.matchAll(
      /<counter type="(INSTRUCTION|BRANCH)" missed="(\d+)" covered="(\d+)"\/>/g,
    ),
  ];
  const lastCounter = (type) =>
    counters.filter((match) => match[1] === type).at(-1);
  const instruction = lastCounter("INSTRUCTION");
  const branch = lastCounter("BRANCH");
  const coverage = {
    instructionsPct: instruction
      ? percentage(Number(instruction[3]), Number(instruction[2]))
      : null,
    branchesPct: branch
      ? percentage(Number(branch[3]), Number(branch[2]))
      : null,
  };

  emitTestOutputs(tests);
  setOutput("instruction_pct", coverage.instructionsPct ?? "");
  setOutput("branch_pct", coverage.branchesPct ?? "");
  writePart("backend", { result: "success", tests, coverage });
}

function summarizeFrontend() {
  const results = readJson(path.join(root, "vitest-results.json"));
  const coverage = readJson(
    path.join(root, "coverage", "coverage-summary.json"),
    { total: {} },
  ).total;
  const suiteStarts = (results.testResults ?? [])
    .map((suite) => Number(suite.startTime ?? 0))
    .filter(Boolean);
  const suiteEnds = (results.testResults ?? [])
    .map((suite) => Number(suite.endTime ?? 0))
    .filter(Boolean);
  const startedAt = Math.min(
    ...suiteStarts,
    Number(results.startTime ?? Infinity),
  );
  const finishedAt = suiteEnds.length > 0 ? Math.max(...suiteEnds) : startedAt;
  const tests = {
    total: results.numTotalTests ?? 0,
    passed: results.numPassedTests ?? 0,
    failed: results.numFailedTests ?? 0,
    skipped: results.numPendingTests ?? 0,
    durationMs: Number.isFinite(startedAt)
      ? Math.round(Math.max(0, finishedAt - startedAt))
      : 0,
  };
  const coverageSummary = {
    statementsPct: coverage.statements?.pct ?? null,
    branchesPct: coverage.branches?.pct ?? null,
    functionsPct: coverage.functions?.pct ?? null,
    linesPct: coverage.lines?.pct ?? null,
  };

  emitTestOutputs(tests);
  setOutput("statements_pct", coverageSummary.statementsPct ?? "");
  setOutput("branches_pct", coverageSummary.branchesPct ?? "");
  setOutput("functions_pct", coverageSummary.functionsPct ?? "");
  setOutput("lines_pct", coverageSummary.linesPct ?? "");
  writePart("frontend", {
    result: results.success === false ? "failure" : "success",
    tests,
    coverage: coverageSummary,
  });
}

function summarizePlaywright() {
  const report = readJson(
    path.join(root, "playwright-report", "results.json"),
    {
      suites: [],
    },
  );
  const summary = {
    total: 0,
    passed: 0,
    failed: 0,
    flaky: 0,
    skipped: 0,
    timedOut: 0,
    durationMs: 0,
  };
  const failedTests = [];

  const visitSuite = (suite, parents = []) => {
    const suitePath = suite.title ? [...parents, suite.title] : parents;
    for (const childSuite of suite.suites ?? [])
      visitSuite(childSuite, suitePath);

    for (const spec of suite.specs ?? []) {
      for (const test of spec.tests ?? []) {
        summary.total += 1;
        const results = Array.isArray(test.results) ? test.results : [];
        const lastResult = results.at(-1) ?? {};
        const statuses = new Set(results.map((result) => result.status));
        const outcome = test.outcome ?? lastResult.status ?? "unknown";
        summary.durationMs += results.reduce(
          (total, result) =>
            total + (typeof result.duration === "number" ? result.duration : 0),
          0,
        );

        if (outcome === "skipped" || lastResult.status === "skipped")
          summary.skipped += 1;
        else if (
          outcome === "flaky" ||
          (statuses.has("failed") && lastResult.status === "passed")
        )
          summary.flaky += 1;
        else if (lastResult.status === "timedOut" || outcome === "timedOut") {
          summary.timedOut += 1;
          failedTests.push(
            [...suitePath, spec.title].filter(Boolean).join(" > "),
          );
        } else if (lastResult.status === "passed" || outcome === "expected")
          summary.passed += 1;
        else {
          summary.failed += 1;
          failedTests.push(
            [...suitePath, spec.title].filter(Boolean).join(" > "),
          );
        }
      }
    }
  };

  for (const suite of report.suites ?? []) visitSuite(suite);
  const result =
    process.env.PLAYWRIGHT_OUTCOME === "success" ? "success" : "failure";
  setOutput("status", result === "success" ? "passed" : "failed");
  for (const [key, value] of Object.entries(summary)) {
    setOutput(
      key === "timedOut"
        ? "timed_out"
        : key.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`),
      value,
    );
  }
  setOutput(
    "failed_tests",
    failedTests.map((title) => `- ${title}`).join("\n"),
  );
  writePart("e2e", { result, tests: summary, failedTests });
}

function componentFallback(result) {
  return {
    result,
    tests: {
      total: 0,
      passed: 0,
      failed: 0,
      flaky: 0,
      skipped: 0,
      timedOut: 0,
      durationMs: 0,
    },
    coverage: {},
    failedTests: [],
  };
}

function pct(value) {
  return value == null || value === "" || Number.isNaN(Number(value))
    ? null
    : Number(value);
}

function gateItem(name, value, threshold) {
  const actual = pct(value);
  return {
    name,
    actualPct: actual,
    thresholdPct: threshold,
    passed: actual != null && actual >= threshold,
  };
}

function markdownReport(report) {
  const icon = (passed) => (passed ? "PASS" : "FAIL");
  const result = (value) =>
    value === "success" ? "PASS" : value.toUpperCase();
  const duration = (value) => `${(toInt(value) / 1000).toFixed(1)}s`;
  const percent = (value) =>
    pct(value) == null ? "n/a" : `${pct(value).toFixed(2)}%`;
  const threshold = (value) =>
    pct(value) == null ? "n/a" : `${pct(value).toFixed(0)}%`;
  const testRow = (name, component) =>
    `| ${name} | ${result(component.result)} | ${toInt(component.tests.passed)} | ${toInt(component.tests.failed) + toInt(component.tests.timedOut)} | ${toInt(component.tests.skipped)} | ${toInt(component.tests.total)} | ${duration(component.tests.durationMs)} |`;

  const lines = [
    "## Quality report",
    "",
    report.gates.passed
      ? "> PASS: all quality gates passed."
      : "> FAIL: quality gates require attention.",
    "",
    "### Quality gates",
    "| Gate | Actual | Required | Result |",
    "| --- | ---: | ---: | --- |",
    ...report.gates.items.map(
      (item) =>
        `| ${item.name} | ${percent(item.actualPct)} | ${threshold(item.thresholdPct)} | ${icon(item.passed)} |`,
    ),
    "",
    "### Test results",
    "| Suite | Result | Passed | Failed | Skipped | Total | Duration |",
    "| --- | --- | ---: | ---: | ---: | ---: | ---: |",
    testRow("Backend / Surefire", report.backend),
    testRow("Frontend / Vitest", report.frontend),
    testRow("E2E / Playwright", report.e2e),
    "",
    `Playwright stability: ${toInt(report.e2e.tests.flaky)} flaky, ${toInt(report.e2e.tests.timedOut)} timed out.`,
    "",
    "### Coverage",
    "| Scope | Coverage |",
    "| --- | ---: |",
    `| Backend instructions | ${percent(report.backend.coverage.instructionsPct)} |`,
    `| Backend branches | ${percent(report.backend.coverage.branchesPct)} |`,
    `| Frontend statements | ${percent(report.frontend.coverage.statementsPct)} |`,
    `| Frontend branches | ${percent(report.frontend.coverage.branchesPct)} |`,
    `| Frontend functions | ${percent(report.frontend.coverage.functionsPct)} |`,
    `| Frontend lines | ${percent(report.frontend.coverage.linesPct)} |`,
    "",
    "### Reports",
    "- Backend: `backend-coverage` artifact",
    "- Frontend: `frontend-coverage` artifact",
    "- Playwright: `playwright-report` artifact",
    "- Allure: `allure-report` artifact with Quality Dashboard link in environment metadata",
    "- Machine report: `quality-report-json` artifact",
    `- GitHub Pages dashboard: ${report.links?.qualityDashboardUrl ?? "not configured"}`,
  ];

  if ((report.e2e.failedTests ?? []).length > 0) {
    lines.push(
      "",
      "### Failed tests",
      ...report.e2e.failedTests.map((test) => `- ${test}`),
    );
  }

  lines.push(
    "",
    `Run: ${report.run.runUrl}`,
    `Commit: \`${report.run.sha.slice(0, 7)}\``,
  );
  return `${lines.join("\n")}\n`;
}

function aggregateReport() {
  const partDir = process.env.CI_METRICS_PARTS_DIR ?? defaultPartDir;
  const backend = {
    ...componentFallback(resultFromOutcome(process.env.BACKEND_RESULT)),
    ...readJson(path.join(partDir, "backend.json")),
    result: resultFromOutcome(process.env.BACKEND_RESULT),
  };
  const frontend = {
    ...componentFallback(resultFromOutcome(process.env.FRONTEND_RESULT)),
    ...readJson(path.join(partDir, "frontend.json")),
    result: resultFromOutcome(process.env.FRONTEND_RESULT),
  };
  const e2e = {
    ...componentFallback(resultFromOutcome(process.env.E2E_RESULT)),
    ...readJson(path.join(partDir, "e2e.json")),
    result: resultFromOutcome(process.env.E2E_RESULT),
  };
  const dockerResult = resultFromOutcome(process.env.DOCKER_RESULT);
  const e2eRequired = process.env.E2E_REQUIRED !== "false";
  const gates = [
    { name: "Backend job", passed: backend.result === "success" },
    { name: "Frontend job", passed: frontend.result === "success" },
    { name: "Docker job", passed: dockerResult === "success" },
    {
      name: "Playwright E2E job",
      passed:
        e2e.result === "success" || (!e2eRequired && e2e.result === "skipped"),
    },
    gateItem(
      "Backend instructions coverage",
      backend.coverage.instructionsPct,
      COVERAGE_GATES.backendInstructions,
    ),
    gateItem(
      "Backend branches coverage",
      backend.coverage.branchesPct,
      COVERAGE_GATES.backendBranches,
    ),
    gateItem(
      "Frontend statements coverage",
      frontend.coverage.statementsPct,
      COVERAGE_GATES.frontendStatements,
    ),
    gateItem(
      "Frontend branches coverage",
      frontend.coverage.branchesPct,
      COVERAGE_GATES.frontendBranches,
    ),
    gateItem(
      "Frontend functions coverage",
      frontend.coverage.functionsPct,
      COVERAGE_GATES.frontendFunctions,
    ),
    gateItem(
      "Frontend lines coverage",
      frontend.coverage.linesPct,
      COVERAGE_GATES.frontendLines,
    ),
  ];
  const repository = process.env.GITHUB_REPOSITORY ?? "";
  const [owner = "", repo = ""] = repository.split("/");
  const runId = process.env.GITHUB_RUN_ID ?? "local";
  const serverUrl = process.env.GITHUB_SERVER_URL ?? "https://github.com";
  const qualityDashboardUrl =
    process.env.QUALITY_DASHBOARD_URL ||
    (owner && repo ? `https://${owner}.github.io/${repo}/` : null);
  const report = {
    schemaVersion: 1,
    run: {
      id: runId,
      attempt: process.env.GITHUB_RUN_ATTEMPT ?? "1",
      event: process.env.GITHUB_EVENT_NAME ?? "local",
      branch:
        process.env.GITHUB_HEAD_REF ||
        (process.env.GITHUB_REF_NAME ?? process.env.GITHUB_REF ?? "local"),
      sha: process.env.GITHUB_SHA ?? "local",
      createdAt: new Date().toISOString(),
      runUrl: repository
        ? `${serverUrl}/${repository}/actions/runs/${runId}`
        : "local",
    },
    backend,
    frontend,
    e2e,
    docker: { result: dockerResult },
    links: {
      qualityDashboardUrl,
    },
    gates: {
      passed: gates.every((item) => item.passed),
      items: gates,
    },
  };
  const reportPath = process.env.QUALITY_REPORT_PATH ?? "quality-report.json";
  const markdownPath =
    process.env.QUALITY_REPORT_MARKDOWN_PATH ?? "quality-report.md";
  writeJson(reportPath, report);
  ensureDir(markdownPath);
  fs.writeFileSync(markdownPath, markdownReport(report));
  setOutput("report_path", reportPath);
  setOutput("markdown_path", markdownPath);
}

if (command === "backend") summarizeBackend();
else if (command === "frontend") summarizeFrontend();
else if (command === "playwright") summarizePlaywright();
else if (command === "aggregate") aggregateReport();
else throw new Error(`Unknown metrics command: ${command}`);
