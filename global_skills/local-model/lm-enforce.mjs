#!/usr/bin/env node
// Enforce: makes the agent use lm-ask, jgl and lm-rank instead of pouring logs into its context.
// Instructions and memory do not do this on their own; a refused call does. One script, two hooks
// (install.sh adds them):
//
//   PreToolUse   Bash|Read|Grep   denies a log dump before it runs, and names the fix:
//     - `adb logcat -d` (no -t N), `cat`/`less`/`tac` on a log, `head`/`tail -n` over the line
//       limit, `tail -f`, `sed` on a log (except -i or a small `-n 'a,bp'` range), `rg`/`grep` on a
//       log without -c/-l/-q or -m N. Allowed when a later pipe stage cuts it down (lm-ask, lm-rank,
//       wc, rg -c, head/tail within the limit) or stdout goes to a file (`> tmp/x.log`).
//     - Read of a log with no `limit` or one over the line limit; Grep content mode on a log
//       with no head_limit within the line limit.
//     The body of `bash -c "..."`, `sh -c`, `powershell -Command "..."` (Get-Content, Select-String),
//     $(...) and backticks is checked the same way, two levels deep.
//     A log is a path matching LOCAL_MODEL_ENFORCE_LOGS (default: *.log, *.trace, tmp/gradle-agent-*,
//     tombstones, hs_err_pid*, *crash*.txt, anr*.txt, logcat*.txt, bugreport*.txt).
//     Nothing is denied when neither the local model nor a Jev key could answer lm-ask.
//   PostToolUse  Bash|Grep|Edit|Write|MultiEdit|NotebookEdit   adds a note to the agent's context:
//     - LOCAL_MODEL_ENFORCE_SEARCHES (default 3) code searches in a row with no jgl or lm-rank (a
//       file edit also resets the count) -> "use jgl / lm-rank". A code search is rg/grep over a
//       folder, a glob or the working folder as the first pipeline stage, or the Grep tool on a
//       folder outside count mode; `cmd | grep x` and a look inside a named file do not count.
//
// Hooks also run inside subagents, so this covers them. Off: LOCAL_MODEL_HOOKS=0 or
// LOCAL_MODEL_ENFORCE=0 in the environment, or LM_ENFORCE=0 in one command.
// LOCAL_MODEL_ENFORCE_LINES (default 30) is the line limit. One line per deny or note in usage.log.
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { usage } from "./lm-client.mjs";

const env = process.env;
if (env.LOCAL_MODEL_HOOKS === "0" || env.LOCAL_MODEL_ENFORCE === "0") process.exit(0);
const LINES = Number(env.LOCAL_MODEL_ENFORCE_LINES || 30);
const SEARCHES = Number(env.LOCAL_MODEL_ENFORCE_SEARCHES || 3);
const LOG_RE = new RegExp(
  env.LOCAL_MODEL_ENFORCE_LOGS ||
    String.raw`(\.log(\.\d+)?|\.trace)$|(^|/)tmp/gradle-agent-[^/]*$|(^|/)(tombstone[^/]*|hs_err_pid[^/]*|[^/]*crash[^/]*\.txt|anr[^/]*\.txt|logcat[^/]*\.txt|bugreport[^/]*\.txt)$`,
  "i",
);
const LM = "~/.local-model/lm-ask";
const EDIT_TOOLS = new Set(["Edit", "Write", "MultiEdit", "NotebookEdit"]);

let input;
try {
  input = JSON.parse(fs.readFileSync(0, "utf8"));
} catch {
  process.exit(0);
}
const event = input.hook_event_name;
const tool = input.tool_name;
const ti = input.tool_input || {};
const isLog = (p) => typeof p === "string" && LOG_RE.test(p.replace(/\\/g, "/"));
const emit = (o) => process.stdout.write(JSON.stringify(o));

// ---- shell parsing: enough to find pipeline stages, their words and redirects ----------------
function lex(s) {
  const out = [];
  let w = null;
  let q = null;
  const push = () => {
    if (w !== null) out.push({ w });
    w = null;
  };
  for (let i = 0; i < s.length; i++) {
    const ch = s[i];
    if (q) {
      if (ch === q) q = null;
      else if (ch === "\\" && q === '"' && i + 1 < s.length) w += s[++i];
      else w += ch;
      continue;
    }
    if (ch === "'" || ch === '"') {
      q = ch;
      w ??= "";
      continue;
    }
    if (ch === "\\" && i + 1 < s.length) {
      w = (w ?? "") + s[++i];
      continue;
    }
    if (ch === " " || ch === "\t" || ch === "\r") {
      push();
      continue;
    }
    if (!"|;&\n<>()".includes(ch)) {
      w = (w ?? "") + ch;
      continue;
    }
    let op = ch;
    if ((ch === ">" || ch === "<") && w !== null && /^\d+$/.test(w)) {
      op = w + ch;
      w = null;
    } else push();
    const two = s.slice(i, i + 2);
    if (["||", "&&", "|&", ">>", "&>", "<<"].includes(two)) {
      op = op.slice(0, -1) + two;
      i++;
    }
    if (op.endsWith(">") && s[i + 1] === "&") {
      // 2>&1, >&2: a descriptor copy, not a file
      const m = /^&[\d-]+/.exec(s.slice(i + 1));
      if (m) {
        op += m[0];
        i += m[0].length;
      }
    }
    out.push({ op });
  }
  push();
  return out;
}

const SEP = new Set([";", "&&", "||", "&", "\n", "(", ")"]);
const PREFIX = new Set(["sudo", "time", "env", "command", "exec", "nice", "nohup", "timeout"]);
const base = (p) => path.basename(String(p).replace(/\\/g, "/")).replace(/\.(exe|cmd|bat)$/i, "");

// $(...) and `...` -> placeholder words; returns [text, [inner strings]]. Not inside '...'.
function subs(s) {
  const inner = [];
  let out = "";
  let q = null;
  for (let i = 0; i < s.length; i++) {
    const ch = s[i];
    if (q === "'") {
      if (ch === "'") q = null;
      out += ch;
      continue;
    }
    if (ch === "\\" && i + 1 < s.length) {
      out += ch + s[++i];
      continue;
    }
    if (ch === "'" && !q) q = "'";
    else if (ch === '"') q = q ? null : '"';
    let end = -1;
    if (ch === "$" && s[i + 1] === "(") {
      let depth = 0;
      for (let j = i + 1; j < s.length; j++) {
        if (s[j] === "(") depth++;
        else if (s[j] === ")" && --depth === 0) {
          end = j;
          break;
        }
      }
      if (end > 0) inner.push(s.slice(i + 2, end));
    } else if (ch === "`") {
      end = s.indexOf("`", i + 1);
      if (end > 0) inner.push(s.slice(i + 1, end));
    }
    if (end > 0) {
      out += ` __LMSUB${inner.length - 1}__ `;
      i = end;
    } else out += ch;
  }
  return [out, inner];
}

// -> [[stage, ...], ...]: statements, each a pipeline of { cmd, args, out, in, inner }.
// `inner` holds the pipelines of a `bash -c "..."` / `powershell -Command "..."` body or of a
// $(...) / `...` inside the stage, parsed the same way down to depth 2.
function parse(command, depth = 0) {
  const [text, inner] = depth < 2 ? subs(command) : [command, []];
  const pipelines = [];
  let pipe = [];
  let cur = { words: [], redirs: [] };
  const endStage = () => {
    if (cur.words.length || cur.redirs.length) pipe.push(cur);
    cur = { words: [], redirs: [] };
  };
  const toks = lex(text);
  for (let i = 0; i < toks.length; i++) {
    const t = toks[i];
    if (t.w !== undefined) cur.words.push(t.w);
    else if (t.op === "|" || t.op === "|&") endStage();
    else if (SEP.has(t.op)) {
      endStage();
      if (pipe.length) pipelines.push(pipe);
      pipe = [];
    } else if (/[<>]/.test(t.op)) {
      if (/&[\d-]+$/.test(t.op)) continue;
      const target = toks[i + 1]?.w;
      if (target !== undefined) i++;
      cur.redirs.push({ op: t.op, target: target ?? "" });
    }
  }
  endStage();
  if (pipe.length) pipelines.push(pipe);
  return pipelines.map((p) =>
    p.map(({ words, redirs }) => {
      const nested = [];
      for (const w of words)
        for (const m of w.matchAll(/__LMSUB(\d+)__/g))
          if (inner[m[1]] !== undefined) nested.push(...parse(inner[m[1]], depth + 1)); // else the outer level's
      let k = 0;
      while (k < words.length && (/^\w+=/.test(words[k]) || PREFIX.has(words[k]))) k++;
      let cmd = base(words[k] ?? "").toLowerCase();
      if (/^(bash|sh|zsh|powershell|pwsh)$/.test(cmd) && depth < 2) {
        const c = words.findIndex((x, j) => j > k && /^-(c|command)$/i.test(x));
        if (c > 0 && words[c + 1] !== undefined) nested.push(...parse(words.slice(c + 1).join(" "), depth + 1));
        else if (words[k + 1] && !words[k + 1].startsWith("-")) cmd = base(words[++k]).toLowerCase();
      }
      if (cmd === "git" && words[k + 1] === "grep") cmd = "git-grep", k++;
      return {
        cmd,
        args: words.slice(k + 1),
        out: redirs.some((r) => /^(1?>>?|&>>?)$/.test(r.op)), // stdout to a file
        in: redirs.filter((r) => r.op === "<").map((r) => r.target),
        inner: nested,
      };
    }),
  );
}

// ---- what counts as a dump, and what cuts one down ---------------------------------------------
const num = (s) => (/^\+?\d+$/.test(s) ? Number(s.replace("+", "")) : NaN);

// head/tail: lines it prints (Infinity for -f or +N), 10 by default
function headTailLines(st) {
  let n = 10;
  const a = st.args;
  for (let i = 0; i < a.length; i++) {
    const x = a[i];
    if (/^-[a-zA-Z]*f/.test(x) || x === "--follow" || x.startsWith("--follow=")) return Infinity;
    let v = null;
    if (x === "-n" || x === "--lines") v = a[++i];
    else if (/^-n.+/.test(x)) v = x.slice(2);
    else if (x.startsWith("--lines=")) v = x.slice(8);
    else if (/^-\d+$/.test(x)) v = x.slice(1);
    else if (x === "-c" || x === "--bytes" || /^-c\d/.test(x) || x.startsWith("--bytes=")) return 1; // bytes: small enough
    if (v === null) continue;
    if (String(v).startsWith("+") && st.cmd === "tail") return Infinity;
    n = num(String(v).replace(/^-/, ""));
    if (Number.isNaN(n)) return Infinity;
  }
  return n;
}

const GREPS = new Set(["rg", "grep", "egrep", "fgrep", "zgrep", "git-grep", "ag", "ack"]);
// rg/grep options whose next word is a value, not a file or the pattern
const GREP_VAL = new Set(["-A", "-B", "-C", "-m", "-e", "-f", "-g", "-t", "-T", "-d", "-D", "-M", "-j",
  "--glob", "--iglob", "--type", "--type-not", "--max-count", "--regexp", "--file", "--context",
  "--after-context", "--before-context", "--max-columns", "--threads", "--replace", "-r"]);

// rg/grep: { files, small } -- small when it only counts, lists or stops early
function grepInfo(st) {
  const files = [];
  let small = false;
  let patternGiven = false;
  let maxCount = Infinity;
  const a = st.args;
  for (let i = 0; i < a.length; i++) {
    const x = a[i];
    if (x === "--") {
      files.push(...a.slice(i + 1 + (patternGiven ? 0 : 1)));
      break;
    }
    if (/^--(count|count-matches|files-with-matches|files-without-match|quiet|stats)$/.test(x)) small = true;
    else if (/^-[a-zA-Z]+$/.test(x) && /[clLq]/.test(x.replace(/[ABCmefgtTdDMj].*$/, ""))) small = true;
    if (x === "-m" || x === "--max-count") maxCount = num(a[i + 1] ?? "");
    else if (/^-m\d+$/.test(x)) maxCount = num(x.slice(2));
    else if (x.startsWith("--max-count=")) maxCount = num(x.slice(12));
    if (x === "-e" || x === "--regexp" || x === "-f" || x === "--file" || x.startsWith("--regexp=")) patternGiven = true;
    if (x === "-r" && st.cmd !== "rg") continue; // grep -r: recursive, no value
    if (GREP_VAL.has(x)) {
      i++;
      continue;
    }
    if (x.startsWith("-")) continue;
    if (!patternGiven) {
      patternGiven = true;
      continue;
    }
    files.push(x);
  }
  if (maxCount <= LINES) small = true;
  return { files: [...files, ...st.in], small };
}

// stage -> kind of dump it is ("logcat" | "read" | "search") or null
function dump(st) {
  const { cmd, args } = st;
  if (cmd === "adb") {
    const words = args.join(" ").split(/\s+/);
    const at = words.indexOf("logcat");
    if (at < 0) return null;
    const after = words.slice(at + 1);
    const dumpFlag = after.some((x) => x === "-d" || x === "--dump" || /^-[a-zA-Z]*d[a-zA-Z]*$/.test(x));
    const t = after.findIndex((x) => x === "-t" || x === "-T");
    const tail = t >= 0 ? num(after[t + 1] ?? "") : NaN;
    if (dumpFlag && !(tail <= LINES)) return "logcat";
    return null;
  }
  const files = () => [...args.filter((x) => !x.startsWith("-")), ...st.in];
  if (["cat", "tac", "less", "more", "bat", "batcat", "nl", "type", "strings"].includes(cmd))
    return files().some(isLog) ? "read" : null;
  if (cmd === "head" || cmd === "tail")
    return files().some(isLog) && headTailLines(st) > LINES ? "read" : null;
  if (cmd === "sed") {
    if (!files().some(isLog) || args.some((x) => /^-[a-zA-Z]*i/.test(x) || x.startsWith("--in-place"))) return null;
    const script = args.find((x) => /^\d+(,\d+)?p$/.test(x));
    if (args.includes("-n") && script) {
      const [a, b = a] = script.slice(0, -1).split(",").map(Number);
      if (b - a + 1 <= LINES) return null;
    }
    return "read";
  }
  if (GREPS.has(cmd)) {
    const g = grepInfo(st);
    return g.files.some(isLog) && !g.small ? "search" : null;
  }
  // PowerShell, reached through `powershell -Command "..."`
  if (cmd === "get-content" || cmd === "gc") {
    if (!files().some(isLog)) return null;
    const n = args.findIndex((x) => /^-(tail|totalcount|head|first|last)$/i.test(x));
    const small = n >= 0 && num(args[n + 1] ?? "") <= LINES && !args.some((x) => /^-wait$/i.test(x));
    return small ? null : "read";
  }
  if (cmd === "select-string" || cmd === "sls")
    return files().some(isLog) && !args.some((x) => /^-(quiet|list)$/i.test(x)) ? "search" : null;
  return null;
}

// a later stage that cuts the output down to a verdict, a count or a few lines
function cuts(st) {
  if (["lm-ask", "lm-rank", "wc", "jgl", "measure-object"].includes(st.cmd)) return true;
  if (st.cmd === "select-object") {
    const n = st.args.findIndex((x) => /^-(first|last)$/i.test(x));
    return n >= 0 && num(st.args[n + 1] ?? "") <= LINES;
  }
  if (st.cmd === "head" || st.cmd === "tail") return headTailLines(st) <= LINES;
  if (GREPS.has(st.cmd)) return grepInfo(st).small;
  return st.out;
}

const FIX = {
  logcat: (ex) =>
    `\`${ex}\` dumps the whole log buffer into your context. Save it to a file and ask the local model:\n` +
    `  adb logcat -d > tmp/logcat.txt\n` +
    `  ${LM} choice "What is the root cause of the failure?" crash="app crash" anr="ANR" other="other" < tmp/logcat.txt\n` +
    `For exact lines: \`rg -c PATTERN tmp/logcat.txt\`, then \`rg -m 20 PATTERN tmp/logcat.txt\`, or \`adb logcat -d -t ${LINES}\`.`,
  read: (ex) =>
    `\`${ex}\` prints a log into your context. Ask the local model instead:\n` +
    `  ${LM} yesno "Did it succeed?" < FILE\n` +
    `  ${LM} choice "What is the root cause?" a="..." b="..." other="other" < FILE\n` +
    `For exact lines: \`rg -c PATTERN FILE\` first, then \`rg -m 20 PATTERN FILE\` or \`tail -n ${LINES} FILE\`.`,
  search: (ex) =>
    `\`${ex}\` prints every match in a log into your context. Count first (\`rg -c PATTERN FILE\`), then ` +
    `print a few (\`rg -m 20 PATTERN FILE\`, or pipe to \`head -n ${LINES}\`), or ask the local model: ` +
    `\`${LM} choice "<question>" a="..." other="other" < FILE\`.`,
};

// first dump that nothing after it cuts down; a wrapper or $(...) body counts unless its own
// stage or a later one cuts it (`bash -c "cat a.log" | head -5`, `lm-ask q <<< "$(cat a.log)"`)
function findDump(pipelines) {
  for (const pipe of pipelines)
    for (let i = 0; i < pipe.length; i++) {
      if (pipe.slice(i).some(cuts)) continue;
      if (pipe[i].inner.length) {
        const hit = findDump(pipe[i].inner);
        if (hit) return hit;
      }
      const kind = dump(pipe[i]);
      if (kind) return { kind, st: pipe[i] };
    }
  return null;
}

// lm-ask can answer: Ollama reachable, or a Jev key set
async function canAsk() {
  if (env.JEV_API_KEY) return true;
  if (env.LOCAL_MODEL_LOCAL === "0") return false;
  const url = (env.LOCAL_MODEL_URL || "http://127.0.0.1:11434").replace(/\/$/, "");
  try {
    await fetch(url, { signal: AbortSignal.timeout(800) });
    return true;
  } catch {
    return false;
  }
}

async function deny(kind, detail, reason) {
  if (!(await canAsk())) process.exit(0);
  await usage("lm-enforce", 0, 0, `denied ${kind}`, detail);
  emit({
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: `[lm-enforce] ${reason}`,
    },
  });
  process.exit(0);
}

const command = String(ti.command ?? "");
const short = (s) => (s.length > 100 ? s.slice(0, 97) + "..." : s);

if (event === "PreToolUse") {
  if (tool === "Bash") {
    if (!command || /\bLM_ENFORCE=0\b/.test(command)) process.exit(0);
    const hit = findDump(parse(command));
    if (hit) await deny(hit.kind, command, FIX[hit.kind](short([hit.st.cmd, ...hit.st.args].join(" "))));
  } else if (tool === "Read" && isLog(ti.file_path)) {
    if (ti.limit && ti.limit <= LINES) process.exit(0);
    await deny(
      "read",
      ti.file_path,
      `Reading ${path.basename(ti.file_path)} loads the whole log into your context. Ask the local model:\n` +
        `  ${LM} choice "What is the root cause?" a="..." other="other" < "${ti.file_path}"\n` +
        `For exact lines: Grep with output_mode "count" first, then Read with offset and limit <= ${LINES}.`,
    );
  } else if (tool === "Grep" && (isLog(ti.path) || isLog(ti.glob)) && ti.output_mode === "content") {
    const limit = ti.head_limit ?? 250;
    if (limit > 0 && limit <= LINES) process.exit(0);
    await deny(
      "search",
      `${ti.pattern} ${ti.path ?? ti.glob}`,
      `Grep content mode on a log prints every match. Use output_mode "count" first, then head_limit <= ${LINES}, ` +
        `or ask the local model: \`${LM} choice "<question>" a="..." other="other" < FILE\`.`,
    );
  }
  process.exit(0);
}

if (event !== "PostToolUse") process.exit(0);

const sid = String(input.session_id || "").replace(/[^\w-]/g, "");
if (!sid) process.exit(0);
const dir = path.join(os.homedir(), ".local-model", "state");
const file = path.join(dir, `enforce-${sid}.json`);
let state = { searches: 0 };
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
      if (f.startsWith("enforce-") && fs.statSync(path.join(dir, f)).mtimeMs < cut) fs.rmSync(path.join(dir, f), { force: true });
  } catch {}
};

if (EDIT_TOOLS.has(tool)) {
  if (state.searches) {
    state.searches = 0;
    save();
  }
  process.exit(0);
}

// A search for code: rg/grep over a folder, a glob or the working folder, as the first pipeline
// stage. Not `cmd | grep x` (a filter) and not a look inside files named explicitly.
const cwd = input.cwd || process.cwd();
const isDir = (p) => {
  try {
    return fs.statSync(path.resolve(cwd, p)).isDirectory();
  } catch {
    return false;
  }
};
function codeSearch(st) {
  if (!st || !GREPS.has(st.cmd)) return false;
  const { files } = grepInfo(st);
  if (files.length) return files.some((f) => isDir(f) || /[*?[]/.test(f));
  const plainGrep = ["grep", "egrep", "fgrep", "zgrep"].includes(st.cmd);
  return !plainGrep || st.args.some((x) => x === "--recursive" || /^-[a-zA-Z]*[rR]/.test(x));
}

const notes = [];
let searched = tool === "Grep" && ti.output_mode !== "count" && (!ti.path || isDir(ti.path));
if (tool === "Bash" && command && !/\bLM_ENFORCE=0\b/.test(command)) {
  const pipelines = parse(command);
  const stages = pipelines.flat();
  if (stages.some((st) => ["jgl", "jg", "lm-rank"].includes(st.cmd))) state.searches = 0;
  else if (pipelines.some((p) => codeSearch(p[0]))) searched = true;
  // Big output is no longer nudged here: lm-gate.mjs compresses it before the agent reads it.
}
if (searched) {
  state.searches = (state.searches || 0) + 1;
  if (state.searches >= SEARCHES) {
    notes.push(
      `[lm-enforce] ${state.searches} text searches in a row. If you do not know the exact name, ask by meaning: ` +
        `\`~/.local-model/jgl --files "<what the code does>"\`. If the hits are many, let \`~/.local-model/lm-rank\` ` +
        `pick the files worth opening. Keep rg for exact names and error strings.`,
    );
    await usage("lm-enforce", 0, 0, "search-nudge", tool === "Grep" ? `Grep ${ti.pattern}` : command);
    state.searches = 0;
  }
}
save();
if (notes.length) emit({ hookSpecificOutput: { hookEventName: "PostToolUse", additionalContext: notes.join("\n") } });
