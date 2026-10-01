#!/usr/bin/env node
// PostToolUse hook: shortens long build and test output before the agent reads it.
//
// Runs on a Bash call whose command is a build, test or lint run (gradlew, npm test, pytest,
// ci-local.sh ...) and whose output is longer than LOCAL_MODEL_GATE_LINES (default 200).
// The full output is saved in ~/.local-model/gate/ (newest 30 kept), then the local model is asked
// "did this succeed?" about its tail:
//   yes >= 0.9 and a result line (BUILD SUCCESSFUL, "12 passed") -> the agent sees PASSED, the
//       result lines and the last 5 lines.
//   otherwise, when error lines are found -> the agent sees the error lines and the last 30 lines.
//   otherwise, no model (lm-ask exit 2) or anything unexpected -> the output is left unchanged.
// Every shortened output names the saved file, so the agent can still read all of it.
//
// Off: LOCAL_MODEL_HOOKS=0 or LOCAL_MODEL_GATE=0 in the environment, or LM_GATE=0 in the command
// (`LM_GATE=0 ./gradlew test`). Never uses Jev. One line per shortened call in usage.log.
import { spawnSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";

const home = path.join(os.homedir(), ".local-model");
const env = process.env;
if (env.LOCAL_MODEL_HOOKS === "0" || env.LOCAL_MODEL_GATE === "0") process.exit(0);

const VERDICT_CMD =
  /(^|[\s;&|(\/])(gradlew(\.bat)?|gradle|gradle-agent\.sh|ci-local\.sh|mvnw?|make|ctest|pytest|tox|mypy|ruff|tsc|eslint|jest|vitest)(\s|$)|\b(npm|pnpm|yarn|bun) (test|run|ci|install|build)\b|\bcargo (build|test|check|clippy)\b|\bgo (build|test|vet)\b|\bdotnet (build|test)\b|\bflutter (build|test)\b|\bmaestro test\b|\bam instrument\b/;
const RESULT_LINE =
  /BUILD SUCCESSFUL|BUILD OK|\b\d+ (tests? )?passed\b|\bTests?:\s+\d+ passed|\bOK \(\d+ tests?\)|tests? completed|All checks passed|\b0 failures\b|Finished .*(release|dev|test)|\bPASSED\b/i;
// A failure line starts a block: it and the lines after it up to a blank line (at most 8), so
// a failed test keeps its assertion message and the first stack frames.
const ERROR_LINE =
  /^e: |\bFAILED\b|What went wrong|\berror(\[\w+\])?:|\bERROR\b|^FAIL\b|fatal:|panicked|Traceback|AssertionError|Caused by:/;

let input;
try {
  input = JSON.parse(fs.readFileSync(0, "utf8"));
} catch {
  process.exit(0);
}
if (input.tool_name !== "Bash") process.exit(0);
const command = String(input.tool_input?.command ?? "");
if (!VERDICT_CMD.test(command) || /\bLM_GATE=0\b/.test(command)) process.exit(0);

// Output over ~30 KB reaches the hook cut to its start; the full text is in persistedOutputPath.
const r = input.tool_response;
let text =
  typeof r === "string" ? r : r && typeof r === "object" ? [r.stdout, r.stderr].filter(Boolean).join("\n") : "";
if (r?.persistedOutputPath) {
  try {
    text = fs.readFileSync(r.persistedOutputPath, "utf8");
  } catch {
    process.exit(0); // only the start of the output: no verdict possible
  }
}
const lines = text.replace(/\r\n/g, "\n").split("\n");
const minLines = Number(env.LOCAL_MODEL_GATE_LINES || 200);
if (lines.length <= minLines) process.exit(0);

const t0 = Date.now();
const dir = path.join(home, "gate");
let saved;
try {
  fs.mkdirSync(dir, { recursive: true });
  const stamp = new Date().toISOString().replace(/[:.]/g, "-");
  saved = path.join(dir, `${stamp}-${String(input.tool_use_id ?? process.pid).slice(-8)}.log`);
  fs.writeFileSync(saved, `$ ${command}\n\n${text}`);
  const old = fs.readdirSync(dir).filter((f) => f.endsWith(".log")).sort().slice(0, -30);
  for (const f of old) fs.rmSync(path.join(dir, f), { force: true });
} catch {
  process.exit(0); // no saved copy, no shortening
}

// The verdict: the tail is what the model reads, so cut it to the last 150 lines.
const lmAsk = path.join(home, "lm-ask");
const ask = spawnSync(
  "bash",
  [lmAsk, "yesno", "Did this command succeed, with no failed tests, build errors or crashes?"],
  {
    input: lines.slice(-150).join("\n"),
    encoding: "utf8",
    timeout: 20000,
    env: { ...env, LOCAL_MODEL_ALLOW_JEV: "0", LOCAL_MODEL_USAGE_LOG: "0", LOCAL_MODEL_TIMEOUT: "15" },
  },
);
const m = ask.status === 0 ? /^(yes|no) ([0-9.]+)/.exec(ask.stdout.trim()) : null;
const pYes = m ? Number(Number(m[2]).toFixed(3)) : NaN;

const uniq = (xs) => [...new Set(xs)];
const results = uniq(lines.filter((l) => RESULT_LINE.test(l))).slice(-3);
const errors = [];
for (let i = 0; i < lines.length && errors.length < 80; i++) {
  if (!ERROR_LINE.test(lines[i])) continue;
  let j = i;
  while (j < lines.length && j < i + 8 && (j === i || lines[j].trim())) errors.push(lines[j++]);
  i = j - 1;
}
const where = `Full output (${lines.length} lines): ${saved}`;

let out = null;
let verdict;
if (pYes >= 0.9 && results.length) {
  verdict = `passed ${pYes}`;
  out = [
    `[lm-gate] PASSED (local model, yes ${pYes}). ${lines.length} lines shortened.`,
    ...results,
    "--- last 5 lines ---",
    ...lines.slice(-5),
    where,
  ].join("\n");
} else if (m && errors.length) {
  verdict = `failed ${pYes}`;
  out = [
    `[lm-gate] Not a clean pass (local model, yes ${pYes}). Error lines, then the last 20 of ${lines.length} lines.`,
    "--- error lines ---",
    ...errors,
    "--- last 20 lines ---",
    ...lines.slice(-20),
    where,
  ].join("\n");
}
if (!out || out.length >= text.length) process.exit(0);

if (env.LOCAL_MODEL_USAGE_LOG !== "0") {
  const log = env.LOCAL_MODEL_USAGE_LOG_FILE || path.join(home, "usage.log");
  const flat = (s) => s.replace(/[\t\r\n]+/g, " ");
  try {
    fs.appendFileSync(
      log,
      [new Date().toISOString(), "lm-gate", Date.now() - t0, Buffer.byteLength(text), `${verdict} ${Buffer.byteLength(out)}`, process.cwd(), flat(command)].join("\t") + "\n",
    );
  } catch {}
}
// The replacement must have the tool's own shape: Claude Code 2.1.278 rejects a string for Bash
// ("expected object") and keeps the original output. Drop the persisted copy, or it is shown instead.
let updated = out;
if (r && typeof r === "object") {
  const { persistedOutputPath, persistedOutputSize, ...rest } = r;
  updated = { ...rest, stdout: out, stderr: "" };
}
process.stdout.write(
  JSON.stringify({ hookSpecificOutput: { hookEventName: "PostToolUse", updatedToolOutput: updated } }),
);
