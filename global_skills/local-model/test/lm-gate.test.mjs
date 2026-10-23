#!/usr/bin/env node
// Self-test for lm-gate.mjs compress mode and lm-compress.mjs, with no local model
// (LOCAL_MODEL_LOCAL=0, so the word-overlap fallback picks blocks).
//   node global_skills/local-model/test/lm-gate.test.mjs     (exit 0 = all passed)
import { spawnSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";

process.env.LOCAL_MODEL_LOCAL = "0";
const here = path.dirname(fileURLToPath(import.meta.url));
const { compress, collapseRepeats, crushJson, stripAnsi } = await import("../lm-compress.mjs");
const hook = path.join(here, "..", "lm-gate.mjs");
const log = path.join(os.tmpdir(), `lm-gate-test-${process.pid}.log`);
const env = { ...process.env, LOCAL_MODEL_LOCAL: "0", LOCAL_MODEL_USAGE_LOG_FILE: log };
for (const k of ["LOCAL_MODEL_HOOKS", "LOCAL_MODEL_GATE", "LOCAL_MODEL_COMPRESS"]) delete env[k];

let failed = 0;
const check = (ok, name) => {
  if (!ok) failed++;
  console.log(`${ok ? "ok  " : "FAIL"} ${name}`);
};
const run = (command, stdout, description = "") => {
  const r = spawnSync("node", [hook], {
    input: JSON.stringify({ tool_name: "Bash", tool_input: { command, description }, tool_response: { stdout, stderr: "" } }),
    env,
  });
  const s = r.stdout.toString();
  return s ? JSON.parse(s).hookSpecificOutput.updatedToolOutput.stdout : null;
};

// lm-compress pieces
check(stripAnsi("\x1b[31mred\x1b[0m\n10%\r50%\r100%") === "red\n100%", "ANSI and progress frames dropped");
const runs = Array.from({ length: 50 }, (_, i) => `2026-10-12 10:00:${i} INFO fetched item ${i} in ${i * 3} ms`).join("\n");
const cr = collapseRepeats(runs);
check(cr.split("\n").length === 4 && cr.includes("[... 47 similar lines ...]"), "run of similar lines collapsed to 4");
const spread = ["warning: deprecated API used here", "a", "warning: deprecated API used here", "b", "warning: deprecated API used here"].join("\n");
check(collapseRepeats(spread) === "warning: deprecated API used here  [x3]\na\nb", "repeated long line kept once with count");
const j = crushJson(JSON.stringify({ items: Array.from({ length: 100 }, (_, i) => ({ id: i, name: `n${i}` })), total: 100 }));
check(j.includes('"total": 100') && j.includes("97 more items") && !j.includes('"n50"'), "JSON array cut to 3 items, keys kept");
check(crushJson("not json") === null, "non-JSON left to the other steps");

const noisy = [
  "Starting",
  ...Array.from({ length: 300 }, (_, i) => `line ${"x".repeat(40)} filler text block ${i % 7 === 0 ? "" : "z"}`.replace(/\d+/, String(i))),
].join("\n");
// Prose-like lines that do not collapse as repeats, so block selection has to do the work.
const L = Array.from({ length: 300 }, (_, i) =>
  i % 31 === 0 ? "" : `${["alpha", "beta", "gamma", "delta"][i % 4]} section on topic${i % 13} with w${(i * 7) % 97} and q${(i * 3) % 89}`,
);
L.splice(150, 0, "", "The kompress model handles prose compression for the proxy.", "");
L.splice(200, 0, "E/AndroidRuntime: FATAL EXCEPTION: main");
const mixed = L.join("\n");
const c = await compress(mixed, { purpose: "how does kompress compression work", budget: 3000 });
check(c.bytesOut < c.bytesIn / 2, `mixed text shrank (${c.bytesIn} -> ${c.bytesOut})`);
check(c.text.includes("FATAL EXCEPTION") && c.text.includes("kompress model") && c.text.includes("lines omitted"), "error line and matching block kept, gaps marked");

// The hook
const big = mixed;
const out = run("curl -s https://example.com/docs", big, "Fetch docs about kompress compression");
check(out?.startsWith("[lm-gate] Compressed") && out.includes("Full output ("), "big curl output compressed, names the saved file");
check(run("cat src/Main.kt", big) === null, "cat of a source file: never compressed");
check(run('cd x && D=y; cat $D/A.kt $D/B.kt | rg -v "^import"', big) === null, "cat later in the line: never compressed");
check(run("cd x && cat build/app.log", big) !== null, "cat of a log: compressed");
check(run("git diff origin/main", big) === null, "git diff: never compressed");
check(run("~/.local-model/jgl q", big) === null, "jgl: never compressed");
check(run("LM_GATE=0 curl -s x", big) === null, "LM_GATE=0: off");
check(run("curl -s x", "small output") === null, "small output: unchanged");
check(run("curl -s x", noisy.slice(0, 9000).replace(/\d+/g, "")) !== undefined, "no crash on odd input");
const line = fs.readFileSync(log, "utf8").trim().split("\n")[0].split("\t");
check(line[1] === "lm-gate" && /^compressed 0\.\d+ \d+$/.test(line[4]), `usage.log line for /lm-savings: ${line[4]}`);
fs.rmSync(log, { force: true });

console.log(failed ? `${failed} failed` : "all passed");
process.exit(failed ? 1 : 0);
