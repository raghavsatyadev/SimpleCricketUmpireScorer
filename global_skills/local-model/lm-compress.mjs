// Shrinks a long tool output before the agent reads it, the way Headroom does, with the local model
// only choosing what to keep. lm-gate.mjs calls compress() for any long Bash output that is not a
// build/test run (those get the pass/fail verdict instead).
//
//   import { compress } from "./lm-compress.mjs";
//   const r = await compress(text, { purpose: "Fetch the README", budget: 6000 });
//   -> { text, steps: ["ansi", "repeats", "json", "model 12/40"], bytesIn, bytesOut }
//
// Steps, cheapest first; it stops as soon as the text fits the budget:
//   1. ANSI colour codes and carriage-return progress bars dropped (only the last frame kept).
//   2. JSON (the whole output parses): arrays over 4 items keep the first 3 and a count, long
//      strings are cut. The structure and the keys stay.
//   3. Repeats: a run of 4+ lines that differ only in numbers, hashes or timestamps keeps its first
//      2 and last line with a "[... N similar lines ...]" marker; a long line seen 3+ times anywhere
//      keeps its first copy, marked "[xN]". Lines over 400 chars are cut in the middle.
//   4. Still over budget: the text is cut into blocks (blank lines, at most 30 lines each). The
//      first 12 and last 25 lines and every block with an error line are always kept; the local
//      model scores the other blocks against the purpose and the best ones fill the budget. With
//      no model, blocks that share words with the purpose are kept first.
// Nothing here prints or throws; the caller decides whether the result is worth using.
import { yesno, pool, enabled } from "./lm-client.mjs";

export const ERROR_LINE =
  /^e: |\bFAILED\b|What went wrong|\berror(\[\w+\])?:|\bERROR\b|^FAIL\b|fatal:|FATAL|panicked|Traceback|AssertionError|Caused by:|Exception\b|\bwarning:/;

const bytes = (s) => Buffer.byteLength(s);

export function stripAnsi(text) {
  return text
    .replace(/\x1b\[[0-9;?]*[ -/]*[@-~]/g, "")
    .replace(/\x1b\][^\x07]*(\x07|\x1b\\)/g, "")
    .replace(/\r\n/g, "\n")
    .split("\n")
    .map((l) => (l.includes("\r") ? l.split("\r").filter(Boolean).pop() ?? "" : l))
    .join("\n");
}

// Arrays keep their first `keep` items and a count; strings over `maxStr` chars are cut.
export function crushJson(text, { keep = 3, maxStr = 300 } = {}) {
  const t = text.trim();
  if (!/^[[{]/.test(t)) return null;
  let v;
  try {
    v = JSON.parse(t);
  } catch {
    return null;
  }
  const walk = (x) => {
    if (Array.isArray(x)) {
      const head = x.slice(0, x.length > keep + 1 ? keep : x.length).map(walk);
      if (x.length > keep + 1) head.push(`[... ${x.length - keep} more items, ${x.length} in all]`);
      return head;
    }
    if (x && typeof x === "object") return Object.fromEntries(Object.entries(x).map(([k, y]) => [k, walk(y)]));
    if (typeof x === "string" && x.length > maxStr) return `${x.slice(0, maxStr)}[... +${x.length - maxStr} chars]`;
    return x;
  };
  return JSON.stringify(walk(v), null, 1);
}

const shape = (l) =>
  l
    .replace(/\b[0-9a-f]{7,}\b/gi, "#")
    .replace(/\d+/g, "0")
    .replace(/\s+/g, " ")
    .trim();

export function collapseRepeats(text, { run = 4, seen = 3, minLen = 24, maxLine = 400 } = {}) {
  let lines = text.split("\n").map((l) =>
    l.length > maxLine ? `${l.slice(0, 300)}[... +${l.length - 360} chars ...]${l.slice(-60)}` : l,
  );
  // Runs of lines with the same shape.
  const out = [];
  for (let i = 0; i < lines.length; ) {
    const s = shape(lines[i]);
    let j = i + 1;
    while (j < lines.length && s && shape(lines[j]) === s) j++;
    const n = j - i;
    if (n >= run) out.push(lines[i], lines[i + 1], `[... ${n - 3} similar lines ...]`, lines[j - 1]);
    else out.push(...lines.slice(i, j));
    i = j;
  }
  // The same long line again and again across the output: keep the first copy, with its count.
  const count = new Map();
  for (const l of out) if (l.trim().length >= minLen) count.set(l, (count.get(l) || 0) + 1);
  const done = new Set();
  lines = [];
  for (const l of out) {
    const c = count.get(l) || 0;
    if (c < seen) lines.push(l);
    else if (!done.has(l)) done.add(l), lines.push(`${l}  [x${c}]`);
  }
  return lines.join("\n");
}

// Blocks: split at a blank line once the block has `min` bytes, and always at `max` lines or
// 2000 bytes, so short paragraphs merge and the model scores fewer, fuller pieces.
export function blocks(lines, max = 30, min = 600) {
  const out = [];
  let cur = [];
  let size = 0;
  let start = 0;
  lines.forEach((l, i) => {
    if (!cur.length) (start = i), (size = 0);
    cur.push(l);
    size += bytes(l) + 1;
    if ((!l.trim() && size >= min) || cur.length >= max || size >= 2000) out.push({ start, lines: cur }), (cur = []);
  });
  if (cur.length) out.push({ start, lines: cur });
  return out;
}

const words = (s) => new Set((String(s).toLowerCase().match(/[a-z][a-z0-9_]{3,}/g) || []).filter((w) => !STOP.has(w)));
const STOP = new Set("this that with from what which then than they them have into only show list file files read output command".split(" "));

export async function compress(text, { purpose = "", budget = 6000, head = 12, tail = 25, model = true, timeoutMs = 15000 } = {}) {
  const bytesIn = bytes(text);
  const steps = [];
  let t = stripAnsi(text);
  if (bytes(t) < bytesIn - 64) steps.push("ansi");
  const j = crushJson(t);
  if (j && bytes(j) < bytes(t)) (t = j), steps.push("json");
  const c = collapseRepeats(t);
  if (bytes(c) < bytes(t) - 64) (t = c), steps.push("repeats");
  if (bytes(t) <= budget) return { text: t, steps, bytesIn, bytesOut: bytes(t) };

  const lines = t.split("\n");
  const keep = new Set();
  const add = (b) => b.lines.forEach((_, k) => keep.add(b.start + k));
  lines.forEach((_, i) => (i < head || i >= lines.length - tail) && keep.add(i));
  const bl = blocks(lines);
  const fixed = bl.filter((b) => b.lines.some((l) => ERROR_LINE.test(l)));
  // Each error line keeps the line before it and up to 6 after it (the message, the first stack
  // frames), not its whole block, so a long trace cannot eat the budget.
  for (const b of fixed)
    b.lines.forEach((l, k) => {
      if (!ERROR_LINE.test(l)) return;
      for (let x = Math.max(0, k - 1); x <= Math.min(b.lines.length - 1, k + 6); x++) keep.add(b.start + x);
    });
  const used = () => [...keep].reduce((n, i) => n + bytes(lines[i]) + 1, 0);
  const rest = bl.filter((b) => !fixed.includes(b) && b.lines.some((_, k) => !keep.has(b.start + k)));

  let scored = null;
  if (model && enabled() && rest.length) {
    const deadline = Date.now() + timeoutMs;
    const ask = rest.slice(0, 40);
    scored = await pool(ask, 4, async (b) => {
      if (Date.now() > deadline - 500) return null;
      const p = await yesno(
        `Purpose of the command: ${purpose}\n--- part of its output ---\n${b.lines.join("\n")}`,
        "Is this part of the output needed for the purpose of the command?",
        { timeoutMs: Math.max(500, Math.min(8000, deadline - Date.now())) },
      );
      return p;
    });
    if (scored.every((p) => p === null)) scored = null;
  }
  const want = words(purpose);
  // A block the model did not score (past the first 40, or out of time) falls back to word overlap.
  const rank = rest.map((b, i) => {
    const p = scored?.[i];
    const overlap = [...words(b.lines.join(" "))].filter((w) => want.has(w)).length;
    const byWords = scored ? (overlap >= 2 ? 0.5 + overlap / 100 : -1) : overlap ? overlap / 100 : -1;
    return { b, score: typeof p === "number" ? p : byWords };
  });
  rank.sort((a, b) => b.score - a.score);
  let kept = 0;
  for (const { b, score } of rank) {
    if (score < (scored ? 0.5 : 0)) break;
    const size = b.lines.reduce((n, l) => n + bytes(l) + 1, 0);
    if (used() + size > budget) continue;
    add(b);
    kept++;
  }
  steps.push(scored ? `model ${kept}/${rest.length}` : `words ${kept}/${rest.length}`);

  const out = [];
  let gap = 0;
  lines.forEach((l, i) => {
    if (keep.has(i)) {
      if (gap) out.push(`[... ${gap} lines omitted ...]`), (gap = 0);
      out.push(l);
    } else gap++;
  });
  if (gap) out.push(`[... ${gap} lines omitted ...]`);
  t = out.join("\n");
  return { text: t, steps, bytesIn, bytesOut: bytes(t) };
}
