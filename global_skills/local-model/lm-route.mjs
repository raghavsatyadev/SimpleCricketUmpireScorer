#!/usr/bin/env node
// UserPromptSubmit hook: the local model sizes the request, and a confident answer becomes a
// one-line hint on how much machinery to use. The agent's own model and effort cannot be changed
// from a hook; what the hint changes is how much it reads and which model its subagents get.
//   easy (p >= 0.9)  answer or act directly: no subagents, no plan, read only what is needed
//   hard (p >= 0.9)  subagents that only search or read can run on a cheaper model
// Anything less sure, a short prompt (< 12 chars) or a slash command -> nothing.
// On 34 real prompts: 29/34 right overall, 14/14 right at p >= 0.9.
// Off: LOCAL_MODEL_HOOKS=0 or LOCAL_MODEL_ROUTE=0.
import fs from "node:fs";
import { choice, usage } from "./lm-client.mjs";

const env = process.env;
if (env.LOCAL_MODEL_HOOKS === "0" || env.LOCAL_MODEL_ROUTE === "0") process.exit(0);
let input;
try {
  input = JSON.parse(fs.readFileSync(0, "utf8"));
} catch {
  process.exit(0);
}
const prompt = String(input.prompt ?? "").trim();
if (prompt.length < 12 || prompt.startsWith("/")) process.exit(0);

const t0 = Date.now();
const r = await choice(
  prompt.slice(-4000),
  "How much work does this request to a coding agent need?",
  {
    easy: "a question, a status check or one small action that needs no planning",
    hard: "multi-step work: code changes, a fix, a feature, an investigation or testing",
  },
  { timeoutMs: 3000 },
);
if (!r || r.p === null) process.exit(0);
await usage("lm-route", Date.now() - t0, Buffer.byteLength(prompt), `${r.choice} ${r.p.toFixed(3)}`, prompt.slice(0, 80));
if (r.p < 0.9) process.exit(0);

const hint =
  r.choice === "easy"
    ? `Local-model hint (quick request, ${r.p.toFixed(2)}): answer or act directly. No subagents, no plan, read only what the answer needs.`
    : `Local-model hint (multi-step request, ${r.p.toFixed(2)}): subagents that only search or read (Explore, research) can use model "haiku" or "sonnet"; keep edits and decisions in this session.`;
process.stdout.write(JSON.stringify({ hookSpecificOutput: { hookEventName: "UserPromptSubmit", additionalContext: hint } }));
