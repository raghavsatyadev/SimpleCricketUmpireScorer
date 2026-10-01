// Shared client for the Node hooks and tools (lm-rank, lm-diffcheck, lm-loop, lm-route): one
// /v1/systemone call to the LOCAL model on Ollama. Never uses Jev. Every failure returns null,
// so the caller does nothing; nothing here prints.
//
//   import { ask, yesno, choice } from "./lm-client.mjs";
//   await yesno(text, "Is this about X?")           -> 0.97 (P(yes)) | null
//   await choice(text, "Which?", { a: "...", b: "..." }) -> { choice: "a", p: 0.93 } | null
//       (p = probability of the chosen label)
//   await ask(text, { key: { type: "noul", instructions: "..." } }) -> answers object | null
//
// Env, as for lm-ask: LOCAL_MODEL_URL, LOCAL_MODEL_NAME, LOCAL_MODEL_MAX_BYTES, LOCAL_MODEL_KEEP_ALIVE,
// LOCAL_MODEL_HOOKS=0 (all off), LOCAL_MODEL_LOCAL=0 (no local model: every call returns null).
const env = process.env;
const URL_ = (env.LOCAL_MODEL_URL || "http://127.0.0.1:11434").replace(/\/$/, "");
const MODEL = env.LOCAL_MODEL_NAME || "nimble";
const BUDGET = Number(env.LOCAL_MODEL_MAX_BYTES || 16000);
const KEEP = env.LOCAL_MODEL_KEEP_ALIVE || "10m";

export const enabled = () => env.LOCAL_MODEL_HOOKS !== "0" && env.LOCAL_MODEL_LOCAL !== "0";

// ASCII only (as local-model.sh does), CR dropped, tabs to spaces; the TAIL is kept.
const clean = (s) =>
  String(s)
    .replace(/\r/g, "")
    .replace(/\t/g, " ")
    .replace(/[^\n\x20-\x7e]/g, "?");

export async function ask(text, questions, { timeoutMs = 10000 } = {}) {
  if (!enabled()) return null;
  let keep = BUDGET - JSON.stringify(questions).length;
  if (keep < 1000) return null;
  const all = clean(text);
  const deadline = Date.now() + timeoutMs;
  for (let attempt = 0; attempt < 3; attempt++) {
    const state = all.slice(-keep);
    let res;
    try {
      const r = await fetch(`${URL_}/v1/systemone`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ model: MODEL, keep_alive: KEEP, state, questions }),
        signal: AbortSignal.timeout(Math.max(500, deadline - Date.now())),
      });
      res = await r.text();
    } catch {
      return null;
    }
    try {
      const j = JSON.parse(res);
      if (j.answers) return j.answers;
    } catch {}
    // Over the context: "prompt 0 has N tokens; expected 1-M tokens" -> cut to ~85% and retry.
    const n = /has (\d+) tokens/.exec(res)?.[1];
    const m = /expected \d*\D*(\d+)/.exec(res)?.[1];
    if (!n || !m) return null;
    keep = Math.floor((keep * Number(m) * 0.85) / Number(n));
    if (keep < 500) return null;
  }
  return null;
}

export async function yesno(text, question, opts) {
  const a = await ask(text, { a: { type: "noul", instructions: question } }, opts);
  const v = a?.a?.noul;
  return typeof v === "number" ? v : null;
}

export async function choice(text, question, criteria, opts) {
  const a = await ask(text, { a: { type: "choice", instructions: question, criteria } }, opts);
  const c = a?.a?.choice;
  if (typeof c !== "string") return null;
  const p = a.a.probabilities?.[c];
  return { choice: c, p: typeof p === "number" ? p : null };
}

// Runs fn over items, `limit` at a time, keeping the order.
export async function pool(items, limit, fn) {
  const out = new Array(items.length);
  let next = 0;
  const worker = async () => {
    while (next < items.length) {
      const i = next++;
      out[i] = await fn(items[i], i);
    }
  };
  await Promise.all(Array.from({ length: Math.min(limit, items.length) }, worker));
  return out;
}

// One tab-separated line in ~/.local-model/usage.log, like lm-ask. Never throws.
export async function usage(tool, ms, bytesIn, result, detail) {
  if (env.LOCAL_MODEL_USAGE_LOG === "0") return;
  const { appendFileSync, mkdirSync } = await import("node:fs");
  const { join, dirname } = await import("node:path");
  const { homedir } = await import("node:os");
  const f = env.LOCAL_MODEL_USAGE_LOG_FILE || join(homedir(), ".local-model", "usage.log");
  const flat = (s) => String(s).replace(/[\t\r\n]+/g, " ");
  try {
    mkdirSync(dirname(f), { recursive: true });
    appendFileSync(f, [new Date().toISOString(), tool, ms, bytesIn, flat(result), process.cwd(), flat(detail)].join("\t") + "\n");
  } catch {}
}
