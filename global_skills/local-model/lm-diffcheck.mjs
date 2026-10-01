#!/usr/bin/env node
// Cheap rule checks over a diff before a big review: the project's yes/no questions, asked of
// each changed file's diff by the local model.
//
//   lm-diffcheck                # this branch (and uncommitted changes) against its base branch
//   lm-diffcheck origin/main    # against another base
//   git diff | lm-diffcheck -   # a diff on stdin
//
// Questions: .agents/diff-checks.txt in the repo, one per line, "<glob> | <question>" phrased so
// that "yes" is the problem ("Does this change ... ?"). # starts a comment. No file -> exit 0.
// "<glob> | !<message>" is a path rule: any change to a matching file is flagged, without the model.
// Base: the argument, else BASE_BRANCH from agent-kit.env, else origin/HEAD, as origin/<branch>.
// Output: one "<P(yes)> <file>: <question>" line per flag (yes >= LM_DIFFCHECK_MIN, default 0.7),
// or "lm-diffcheck: no flags (N files, M checks)". A flag is a lead to look at, not a finding.
// The UserPromptExpansion hook runs it before /code-review, /review, /security-review and
// /simplify and adds the flags to the prompt (--hook mode). Never uses Jev.
import fs from "node:fs";
import path from "node:path";
import { execFileSync } from "node:child_process";
import { ask, pool, usage } from "./lm-client.mjs";

const hook = process.argv[2] === "--hook";
const arg = hook ? null : process.argv[2];
const MIN = Number(process.env.LM_DIFFCHECK_MIN || 0.7);
let cwd = process.cwd();
if (hook) {
  try {
    const input = JSON.parse(fs.readFileSync(0, "utf8"));
    if (input.cwd) cwd = input.cwd;
  } catch {
    process.exit(0);
  }
  if (process.env.LOCAL_MODEL_HOOKS === "0") process.exit(0);
}
const git = (...a) => execFileSync("git", a, { cwd, encoding: "utf8", maxBuffer: 64 << 20, stdio: ["ignore", "pipe", "ignore"] });

let root;
try {
  root = git("rev-parse", "--show-toplevel").trim();
} catch {
  process.exit(0);
}
const checksFile = path.join(root, ".agents", "diff-checks.txt");
if (!fs.existsSync(checksFile)) {
  if (!hook) console.log("lm-diffcheck: no .agents/diff-checks.txt in this repo");
  process.exit(0);
}
const globRe = (g) => new RegExp("^" + g.trim().replace(/[.+^${}()|[\]\\]/g, "\\$&").replace(/\*/g, ".*").replace(/\?/g, ".") + "$");
const checks = fs
  .readFileSync(checksFile, "utf8")
  .split(/\r?\n/)
  .map((l) => l.trim())
  .filter((l) => l && !l.startsWith("#") && l.includes("|"))
  .map((l) => {
    const i = l.indexOf("|");
    return { glob: l.slice(0, i).trim(), re: globRe(l.slice(0, i)), q: l.slice(i + 1).trim() };
  });
if (!checks.length) process.exit(0);

let diff;
try {
  if (arg === "-") diff = fs.readFileSync(0, "utf8");
  else {
    let base = arg;
    if (!base) {
      const env = path.join(root, "agent-kit.env");
      const b = fs.existsSync(env) && /^BASE_BRANCH=(\S+)/m.exec(fs.readFileSync(env, "utf8"))?.[1];
      if (b) base = `origin/${b}`;
      else {
        try {
          base = git("symbolic-ref", "--short", "refs/remotes/origin/HEAD").trim();
        } catch {
          base = "origin/main";
        }
      }
    }
    const mb = git("merge-base", "HEAD", base).trim();
    diff = git("diff", "--no-color", "-U3", mb); // committed + uncommitted, against the base
    // New files not yet added are not in git diff: add them as all-added diffs.
    for (const f of git("ls-files", "--others", "--exclude-standard").split("\n").filter(Boolean)) {
      try {
        const body = fs.readFileSync(path.join(root, f), "utf8");
        if (body.includes("\0")) continue;
        diff += `\ndiff --git a/${f} b/${f}\n@@ new file @@\n${body.split("\n").map((l) => "+" + l).join("\n")}\n`;
      } catch {}
    }
  }
} catch {
  if (!hook) console.log("lm-diffcheck: could not get the diff (is the base fetched?)");
  process.exit(0);
}

// Split per file; keep only the hunks (added and removed lines with context).
const files = [];
for (const part of diff.split(/^diff --git /m).slice(1)) {
  const name = /^a\/\S+ b\/(\S+)/.exec(part)?.[1];
  if (!name || /^Binary files/m.test(part)) continue;
  const hunks = part.slice(part.search(/^@@/m));
  if (hunks.startsWith("@@")) files.push({ name, hunks });
}

const t0 = Date.now();
let bytes = 0, asked = 0;
const flags = [];
await pool(files, 3, async (f) => {
  const matching = checks.filter((c) => c.re.test(f.name) || c.re.test(path.basename(f.name)));
  // "!" rules depend only on the path: any change to a matching file is flagged, no model.
  for (const c of matching) if (c.q.startsWith("!")) flags.push({ p: 1, file: f.name, q: c.q.slice(1).trim() });
  const qs = matching.filter((c) => !c.q.startsWith("!"));
  if (!qs.length) return;
  const questions = Object.fromEntries(qs.map((c, i) => [`q${i}`, { type: "noul", instructions: c.q }]));
  const text = `File: ${f.name}\nDiff (lines starting with + are added, - are removed):\n${f.hunks.slice(0, 12000)}`;
  bytes += Buffer.byteLength(text);
  asked += qs.length;
  const a = await ask(text, questions, { timeoutMs: 15000 });
  if (!a) return;
  qs.forEach((c, i) => {
    const p = a[`q${i}`]?.noul;
    if (typeof p === "number" && p >= MIN) flags.push({ p, file: f.name, q: c.q });
  });
});
flags.sort((a, b) => b.p - a.p);
await usage("lm-diffcheck", Date.now() - t0, bytes, `${flags.length} flags`, `${files.length} files`);

const lines = flags.map((x) => `${x.p.toFixed(2)} ${x.file}: ${x.q}`);
if (hook) {
  if (!lines.length) process.exit(0);
  process.stdout.write(
    JSON.stringify({
      hookSpecificOutput: {
        hookEventName: "UserPromptExpansion",
        additionalContext: `[lm-diffcheck] The local model flagged these changed files against .agents/diff-checks.txt (P(yes), file, rule). Leads to check first, not findings:\n${lines.join("\n")}`,
      },
    }),
  );
} else {
  console.log(lines.length ? lines.join("\n") : `lm-diffcheck: no flags (${files.length} files, ${asked} checks)`);
}
