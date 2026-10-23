#!/usr/bin/env node
// Loop stop: catches an agent re-running a failing Bash command without progress.
// One script, three Claude Code hooks (install.sh adds them):
//   PostToolUse         Bash|Edit|Write|MultiEdit|NotebookEdit   records successes and edits
//   PostToolUseFailure  Bash                                       records the failure; warns
//   PreToolUse          Bash                                       blocks one blind retry
//
// Per session (state in ~/.local-model/state/), for each command (spaces collapsed):
//   - 3 failures in a row with the same error and nothing in between (no file edit, no other
//     successful command) -> warning; the next
//     identical run is denied once ("stop and ask the user, or change the approach"). After that
//     one denial the count restarts, so the agent is never locked out.
//   - 3 failures in a row with edits in between -> the local model is asked whether the three
//     errors are the same failure; yes >= 0.9 -> warning that the edits are not changing it.
// A success of the command resets its count. Silent on anything unexpected.
// Off: LOCAL_MODEL_HOOKS=0 or LOCAL_MODEL_LOOP=0. LOCAL_MODEL_LOOP_MAX (default 3).
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { createHash } from "node:crypto";
import { yesno, usage } from "./lm-client.mjs";

const env = process.env;
if (env.LOCAL_MODEL_HOOKS === "0" || env.LOCAL_MODEL_LOOP === "0") process.exit(0);
const MAX = Number(env.LOCAL_MODEL_LOOP_MAX || 3);
const EDIT_TOOLS = new Set(["Edit", "Write", "MultiEdit", "NotebookEdit"]);
const ERROR_LINE =
  /^e: |\bFAILED\b|What went wrong|\berror(\[\w+\])?:|\bERROR\b|^FAIL\b|fatal:|panicked|Traceback|Exception|not found|No such file|denied/i;

let input;
try {
  input = JSON.parse(fs.readFileSync(0, "utf8"));
} catch {
  process.exit(0);
}
const event = input.hook_event_name;
const tool = input.tool_name;
const sid = String(input.session_id || "").replace(/[^\w-]/g, "");
if (!sid) process.exit(0);

const dir = path.join(os.homedir(), ".local-model", "state");
const file = path.join(dir, `loop-${sid}.json`);
let state = { cmds: {}, edits: 0 };
try {
  state = JSON.parse(fs.readFileSync(file, "utf8"));
} catch {}
const save = () => {
  try {
    fs.mkdirSync(dir, { recursive: true });
    fs.writeFileSync(file, JSON.stringify(state));
    // Old sessions: drop state files untouched for 2 days.
    const cut = Date.now() - 2 * 86400e3;
    for (const f of fs.readdirSync(dir))
      if (f.startsWith("loop-") && fs.statSync(path.join(dir, f)).mtimeMs < cut) fs.rmSync(path.join(dir, f), { force: true });
  } catch {}
};
const emit = (o) => process.stdout.write(JSON.stringify(o));

if (EDIT_TOOLS.has(tool)) {
  if (event === "PostToolUse") {
    state.edits++;
    save();
  }
  process.exit(0);
}
if (tool !== "Bash") process.exit(0);
const cmd = String(input.tool_input?.command ?? "").replace(/\s+/g, " ").trim();
if (!cmd) process.exit(0);
const key = createHash("sha1").update(cmd).digest("hex").slice(0, 16);
const c = (state.cmds[key] ??= { fails: [], denied: false });
const short = cmd.length > 120 ? cmd.slice(0, 117) + "..." : cmd;

if (event === "PostToolUse") {
  delete state.cmds[key]; // it worked
  // Any other command that ran may have changed things (sed -i, git checkout, adb install), so it
  // counts as an edit: only a retry with nothing at all in between is called blind.
  state.edits++;
  save();
  process.exit(0);
}

if (event === "PreToolUse") {
  const f = c.fails;
  const blind =
    f.length >= MAX &&
    !c.denied &&
    f.slice(-MAX).every((x) => x.sig === f[f.length - 1].sig) &&
    f[f.length - 1].edits === state.edits &&
    f[f.length - MAX].edits === state.edits;
  if (!blind) process.exit(0);
  c.denied = true;
  c.fails = [];
  save();
  await usage("lm-loop", 0, 0, "denied", cmd);
  emit({
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: `[lm-loop] \`${short}\` failed ${MAX} times in a row with the same error and nothing changed in between. Running it again will fail the same way. Change the approach, or tell the user what is blocking you and ask. (This block applies once; the next run is allowed.)`,
    },
  });
  process.exit(0);
}

if (event !== "PostToolUseFailure") process.exit(0);
const out = String(input.tool_output ?? input.tool_response?.stdout ?? input.tool_response ?? "");
const errText = `${input.tool_error ?? input.error ?? ""}\n${out}`;
const lines = errText.replace(/\r/g, "").split("\n").filter((l) => l.trim());
const errs = lines.filter((l) => ERROR_LINE.test(l));
const tail = (errs.length ? errs : lines).slice(-5);
// Numbers (times, line counts, pids) differ between identical failures; drop them.
const sig = createHash("sha1").update(tail.join("\n").replace(/\d+/g, "#")).digest("hex").slice(0, 12);
c.fails.push({ sig, edits: state.edits, tail: tail.join("\n").slice(-1500) });
c.fails = c.fails.slice(-MAX);
c.denied = false;
save();
if (c.fails.length < MAX) process.exit(0);

const same = c.fails.every((x) => x.sig === sig);
const edited = c.fails[0].edits !== state.edits;
let msg = null;
if (same && !edited) {
  msg = `[lm-loop] \`${short}\` has now failed ${MAX} times in a row with the same error and nothing changed in between. Do not run it again unchanged: read the error, change the approach, or ask the user.`;
} else if (edited) {
  const t0 = Date.now();
  const text = c.fails.map((x, i) => `--- failure ${i + 1} ---\n${x.tail}`).join("\n");
  const p = await yesno(text, "Do all of these failures show the same error?", { timeoutMs: 8000 });
  if (p !== null) await usage("lm-loop", Date.now() - t0, Buffer.byteLength(text), `same ${p.toFixed(3)}`, cmd);
  if (p !== null && p >= 0.9)
    msg = `[lm-loop] \`${short}\` failed ${MAX} times in a row with the same error although files were edited in between (local model, ${p.toFixed(2)}). The edits are not reaching the cause: re-read the error, check the assumption behind the fix, or ask the user.`;
}
if (msg) {
  await usage("lm-loop", 0, 0, "warned", cmd);
  emit({ hookSpecificOutput: { hookEventName: "PostToolUseFailure", additionalContext: msg } });
}
