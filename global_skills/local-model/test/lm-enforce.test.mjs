#!/usr/bin/env node
// Self-test for lm-enforce.mjs: feeds it hook events and checks deny / allow / notes.
//   node global_skills/local-model/test/lm-enforce.test.mjs     (exit 0 = all passed)
// JEV_API_KEY=x makes the "can lm-ask answer?" check pass without Ollama; nothing is sent anywhere.
import { spawnSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";

const here = path.dirname(fileURLToPath(import.meta.url));
const hook = path.join(here, "..", "lm-enforce.mjs");
const sid = `selftest-${process.pid}`;
const state = path.join(os.homedir(), ".local-model", "state", `enforce-${sid}.json`);
const cwd = path.join(here, ".."); // has a folder (mod/) and files (SKILL.md)
const env = { ...process.env, JEV_API_KEY: "x", LOCAL_MODEL_USAGE_LOG: "0" };
for (const k of ["LOCAL_MODEL_HOOKS", "LOCAL_MODEL_ENFORCE"]) delete env[k];

const run = (o) => {
  const r = spawnSync("node", [hook], { input: JSON.stringify({ session_id: sid, cwd, ...o }), env, cwd });
  return r.stdout.toString() + r.stderr.toString();
};
const pre = (tool_name, tool_input) => run({ hook_event_name: "PreToolUse", tool_name, tool_input }).includes('"deny"');
const post = (tool_name, tool_input, tool_response = { stdout: "ok" }) =>
  run({ hook_event_name: "PostToolUse", tool_name, tool_input, tool_response });

const DENY = [
  "adb logcat -d",
  "adb -s emulator-5554 logcat -d -v time",
  "adb shell logcat -d",
  "cat tmp/ci.log",
  "tail -n 200 tmp/gradle-agent-1.log",
  "tail -f app.log",
  "tail -n +5 x.log",
  "sed -n '1,500p' tmp/a1.log",
  "rg -n ERROR tmp/app.log",
  "grep -i fatal tmp/*.log",
  "adb logcat -d | rg FATAL",
  "cd x && cat tmp/all.log",
  "cat tombstone_03",
  "rg Exception -- hs_err_pid123.log",
  "adb logcat -d | tee x.txt",
  "rg -A 5 'a|b' tmp/ci.log",
  "less build/crash-dump.txt",
  "cat < tmp/a1.log",
  // wrappers
  'bash -c "cat tmp/ci.log"',
  "sh -c 'adb logcat -d'",
  'echo "$(cat tmp/ci.log)"',
  "echo `tail -n 500 x.log`",
  'powershell -Command "Get-Content tmp/ci.log"',
  'pwsh -Command "Select-String -Path tmp/ci.log -Pattern ERROR"',
  'bash -c "echo $(cat tmp/ci.log)"',
];
const ALLOW = [
  "adb logcat -d > tmp/logcat.txt",
  "adb logcat -d | ~/.local-model/lm-ask yesno 'crash?'",
  "adb logcat -d | rg -c FATAL",
  "adb logcat -d -t 20",
  "tail -n 20 tmp/ci.log",
  "tail tmp/ci.log",
  "head -30 tmp/ci.log",
  "sed -n '100,120p' tmp/ci.log",
  "sed -i 's/a/b/' x.log",
  "rg -c ERROR tmp/app.log",
  "rg -l ERROR tmp/",
  "rg -m 10 ERROR tmp/app.log",
  "rg -n ERROR tmp/app.log | head -20",
  "rg crash src/",
  "rg -n 'tombstone' app/src",
  "cat README.md",
  "~/.local-model/lm-ask choice 'root?' a=b c=d < tmp/x.log",
  "bash ~/.local-model/lm-ask yesno q < tmp/x.log",
  "LM_ENFORCE=0 cat tmp/ci.log",
  "adb logcat",
  "grep -nc x a.log",
  "wc -l tmp/*.log",
  "rg -n x a.log 2>&1 | tail -n 15",
  "cat x.log > /dev/null",
  "git log --oneline",
  "echo 'cat a.log'",
  // wrappers
  'bash -c "cat tmp/ci.log" | head -5',
  'bash -c "tail -n 10 tmp/ci.log"',
  "n=$(rg -c ERROR tmp/ci.log)",
  "~/.local-model/lm-ask yesno 'ok?' <<< \"$(cat tmp/ci.log)\"",
  "echo '$(cat tmp/ci.log)'",
  'powershell -Command "Get-Content tmp/ci.log -Tail 20"',
  'powershell -Command "Get-Content tmp/ci.log | Measure-Object -Line"',
  'pwsh -Command "Select-String -Path tmp/ci.log -Pattern ERROR -Quiet"',
];

const fails = [];
for (const c of DENY) if (!pre("Bash", { command: c })) fails.push(`should deny: ${c}`);
for (const c of ALLOW) if (pre("Bash", { command: c })) fails.push(`should allow: ${c}`);
const check = (ok, what) => ok || fails.push(what);
check(pre("Read", { file_path: "C:/x/tmp/ci.log" }), "Read log: deny");
check(!pre("Read", { file_path: "C:/x/tmp/ci.log", limit: 20 }), "Read log limit 20: allow");
check(!pre("Read", { file_path: "C:/x/a.kt" }), "Read .kt: allow");
check(pre("Grep", { pattern: "E", path: "tmp/ci.log", output_mode: "content" }), "Grep log content: deny");
check(!pre("Grep", { pattern: "E", path: "tmp/ci.log", output_mode: "count" }), "Grep log count: allow");
check(!pre("Grep", { pattern: "E", path: "tmp/ci.log", output_mode: "content", head_limit: 20 }), "Grep log head_limit 20: allow");

// big output: no note any more (lm-gate compresses it)
const big = { stdout: "x".repeat(6000) };
check(!post("Bash", { command: "git diff" }, big).includes("KB"), "6 KB output: no note");

// search counter: only code searches count
const SEARCH = "text searches";
const reset = () => post("Bash", { command: "~/.local-model/jgl q" });
const third = (a, b, c) => {
  reset();
  post(...a);
  post(...b);
  return post(...c).includes(SEARCH);
};
check(third(["Bash", { command: "rg foo" }], ["Grep", { pattern: "x" }], ["Bash", { command: "rg bar mod" }]), "3 code searches: note");
check(!third(["Bash", { command: "rg foo" }], ["Bash", { command: "rg bar" }], ["Bash", { command: "grep -c x SKILL.md" }]), "grep -c on a named file: not a search");
check(!third(["Bash", { command: "rg foo" }], ["Bash", { command: "rg bar" }], ["Bash", { command: "git status | grep -n M" }]), "pipe filter: not a search");
check(!third(["Bash", { command: "rg foo" }], ["Bash", { command: "rg bar" }], ["Bash", { command: "rg -n x lm-enforce.mjs" }]), "rg in a named file: not a search");
check(!third(["Bash", { command: "rg foo" }], ["Bash", { command: "rg bar" }], ["Grep", { pattern: "x", output_mode: "count" }]), "Grep count: not a search");
check(!third(["Bash", { command: "rg foo" }], ["Bash", { command: "rg bar" }], ["Grep", { pattern: "x", path: "SKILL.md" }]), "Grep in a named file: not a search");
check(third(["Bash", { command: "rg foo" }], ["Bash", { command: "grep -rn x ." }], ["Bash", { command: "rg x 'src/*.kt'" }]), "grep -r and glob: searches");
reset();
post("Bash", { command: "rg a" });
post("Edit", {});
post("Bash", { command: "rg b" });
check(!post("Bash", { command: "rg c" }).includes(SEARCH), "edit resets the count");

fs.rmSync(state, { force: true });
const total = DENY.length + ALLOW.length + 17;
if (fails.length) {
  console.log(fails.join("\n"));
  console.log(`${fails.length} of ${total} FAILED`);
  process.exit(1);
}
console.log(`all ${total} passed`);
