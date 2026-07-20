import fs from "node:fs";
import path from "node:path";

const [, , command, root = "."] = process.argv;
const outputPath = process.env.GITHUB_OUTPUT;

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
  return total === 0 ? 0 : Number(((covered / total) * 100).toFixed(2));
}

function xmlAttribute(tag, name) {
  return Number(tag.match(new RegExp(`${name}="(\\d+)"`))?.[1] ?? 0);
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

  setOutput("total", tests.total);
  setOutput("passed", Math.max(0, tests.total - tests.failed - tests.skipped));
  setOutput("failed", tests.failed);
  setOutput("skipped", tests.skipped);
  setOutput("duration_ms", tests.durationMs);
  setOutput(
    "instruction_pct",
    instruction
      ? percentage(Number(instruction[3]), Number(instruction[2]))
      : "",
  );
  setOutput(
    "branch_pct",
    branch ? percentage(Number(branch[3]), Number(branch[2])) : "",
  );
}

function summarizeFrontend() {
  const resultsPath = path.join(root, "vitest-results.json");
  const coveragePath = path.join(root, "coverage", "coverage-summary.json");
  const results = fs.existsSync(resultsPath)
    ? JSON.parse(fs.readFileSync(resultsPath, "utf8"))
    : {};
  const coverage = fs.existsSync(coveragePath)
    ? (JSON.parse(fs.readFileSync(coveragePath, "utf8")).total ?? {})
    : {};
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

  setOutput("total", results.numTotalTests ?? 0);
  setOutput("passed", results.numPassedTests ?? 0);
  setOutput("failed", results.numFailedTests ?? 0);
  setOutput("skipped", results.numPendingTests ?? 0);
  setOutput(
    "duration_ms",
    Number.isFinite(startedAt) ? Math.round(Math.max(0, finishedAt - startedAt)) : 0,
  );
  for (const metric of ["statements", "branches", "functions", "lines"]) {
    setOutput(`${metric}_pct`, coverage[metric]?.pct ?? "");
  }
}

function summarizePlaywright() {
  const reportPath = path.join(root, "playwright-report", "results.json");
  const report = fs.existsSync(reportPath)
    ? JSON.parse(fs.readFileSync(reportPath, "utf8"))
    : { suites: [] };
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
            [...suitePath, spec.title].filter(Boolean).join(" › "),
          );
        } else if (lastResult.status === "passed" || outcome === "expected")
          summary.passed += 1;
        else {
          summary.failed += 1;
          failedTests.push(
            [...suitePath, spec.title].filter(Boolean).join(" › "),
          );
        }
      }
    }
  };

  for (const suite of report.suites ?? []) visitSuite(suite);
  setOutput(
    "status",
    process.env.PLAYWRIGHT_OUTCOME === "success" ? "passed" : "failed",
  );
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
}

if (command === "backend") summarizeBackend();
else if (command === "frontend") summarizeFrontend();
else if (command === "playwright") summarizePlaywright();
else throw new Error(`Unknown metrics command: ${command}`);
