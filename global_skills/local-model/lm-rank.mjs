#!/usr/bin/env node
// Rank search hits before reading them: the local model scores each file against the task, and
// only the best few are printed.
//
//   rg -n "Prompt" app/src | lm-rank "where is the tone prompt built"      # rg -n output on stdin
//   lm-rank "which note covers the jlink failure" .remember/*.md memory/reports/*.md   # whole files
//   options: --top N (default 3), --all (print every score)
//
// Per file the model reads the hit lines with 4 lines around each, at most ~3 KB; a whole file is
// read in 3 KB chunks (at most 8) and scores as its best chunk. Output: one
// "<P(relevant)> <path> (<hits> hits)" line per top file, best first. Exit 2 when the model gave
// no score: read the hits the normal way.
import fs from "node:fs";
import { yesno, pool, usage } from "./lm-client.mjs";

const args = process.argv.slice(2);
let top = 3, all = false;
const rest = [];
for (let i = 0; i < args.length; i++) {
  if (args[i] === "--top") top = Number(args[++i]) || 3;
  else if (args[i] === "--all") all = true;
  else rest.push(args[i]);
}
const [task, ...files] = rest;
if (!task) {
  console.error('usage: rg -n PATTERN | lm-rank "<task>" [--top N] [--all]\n       lm-rank "<task>" FILE...');
  process.exit(64);
}

const t0 = Date.now();
const groups = new Map(); // path -> line numbers (empty = whole file)
if (files.length) {
  for (const f of files) groups.set(f, []);
} else {
  const stdin = fs.readFileSync(0, "utf8");
  for (const l of stdin.replace(/\r/g, "").split("\n")) {
    // path:line:text (rg -n); a Windows drive letter is part of the path.
    const m = /^((?:[A-Za-z]:)?[^:]+):(\d+)[:-]/.exec(l);
    if (!m) continue;
    if (!groups.has(m[1])) groups.set(m[1], []);
    groups.get(m[1]).push(Number(m[2]));
  }
}
if (!groups.size) {
  console.error("lm-rank: no rg -n hits or files");
  process.exit(2);
}

const snippet = (file, hits) => {
  let lines;
  try {
    lines = fs.readFileSync(file, "utf8").replace(/\r/g, "").split("\n");
  } catch {
    return null;
  }
  if (!hits.length) {
    // Whole file: 3 KB chunks on line boundaries, at most 8; the file scores as its best chunk.
    const chunks = [];
    let cur = "";
    for (const l of lines) {
      if (cur && cur.length + l.length > 3000) chunks.push(cur), (cur = "");
      cur += l + "\n";
    }
    if (cur.trim()) chunks.push(cur);
    return chunks.slice(0, 8);
  }
  const keep = new Set();
  for (const h of hits) for (let i = h - 5; i <= h + 3; i++) if (i >= 0 && i < lines.length) keep.add(i);
  let out = "", prev = -2;
  for (const i of [...keep].sort((a, b) => a - b)) {
    if (i !== prev + 1) out += "...\n";
    out += `${i + 1}: ${lines[i]}\n`;
    prev = i;
    if (out.length > 3000) break;
  }
  return [out];
};

const items = [...groups.entries()].slice(0, 60);
let bytes = 0;
const scored = await pool(items, 4, async ([file, hits]) => {
  const parts = snippet(file, hits);
  if (!parts) return { file, hits, p: null };
  let p = null;
  for (const s of parts) {
    bytes += Buffer.byteLength(s);
    const v = await yesno(`File: ${file}\n${s}`, `Is this file relevant to the task: ${task}?`, { timeoutMs: 15000 });
    if (v !== null && (p === null || v > p)) p = v;
  }
  return { file, hits, p };
});
const ok = scored.filter((x) => x.p !== null).sort((a, b) => b.p - a.p);
if (!ok.length) process.exit(2);

const shown = all ? ok : ok.slice(0, top);
for (const x of shown) console.log(`${x.p.toFixed(2)} ${x.file} (${x.hits.length || "whole file"}${x.hits.length ? " hits" : ""})`);
if (ok.length > shown.length) console.log(`... ${ok.length - shown.length} more below ${shown[shown.length - 1].p.toFixed(2)} (--all)`);
if (scored.length > ok.length) console.log(`(${scored.length - ok.length} not scored)`);
if (groups.size > items.length) console.log(`(only the first ${items.length} of ${groups.size} files were scored)`);
await usage("lm-rank", Date.now() - t0, bytes, `${ok.length} files, top ${ok[0].p.toFixed(2)}`, task);
