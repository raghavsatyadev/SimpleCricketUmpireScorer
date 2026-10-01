#!/usr/bin/env node
// Local stand-in for jevgrep.com's /api/v1/grep, answered by the local model on Ollama. Lets `jg` judge
// snippets on this PC: nothing leaves the machine.
//
//   node ~/.local-model/jg-local-proxy.mjs          # listens on 127.0.0.1:11435
//   JEVGREP_ENDPOINT=http://127.0.0.1:11435 JEVGREP_TOKEN=local jg "search intent" [paths]
//
// Each snippet is one Noul ("could this snippet be what the query looks for?") with the query and
// snippet as named state fields, as in TypeSafe's re-ranking cookbook. The noul is the score.
// Env: LOCAL_MODEL_URL (default http://127.0.0.1:11434), LOCAL_MODEL_NAME (nimble), LOCAL_MODEL_KEEP_ALIVE (2m),
// LOCAL_MODEL_MAX_BYTES (16000; the snippet gets 3/4 of it, at most 12000),
// JG_PROXY_PORT (11435), JG_PROXY_CONCURRENCY (4).
import http from 'node:http';

const BASE = (process.env.LOCAL_MODEL_URL || 'http://127.0.0.1:11434').replace(/\/$/, '');
const MODEL = process.env.LOCAL_MODEL_NAME || 'nimble';
const KEEP_ALIVE = process.env.LOCAL_MODEL_KEEP_ALIVE || '2m';
const PORT = Number(process.env.JG_PROXY_PORT || 11435);
const CONCURRENCY = Number(process.env.JG_PROXY_CONCURRENCY || 4);
// Characters per snippet: keeps one request inside the model's window (Nimble ~8K tokens, Tev1 ~2K).
const MAX_SNIPPET = Math.min(12000, Math.floor(Number(process.env.LOCAL_MODEL_MAX_BYTES || 16000) * 0.75));

async function judge(query, snippet) {
  const state = { query, snippet: snippet.text.slice(0, MAX_SNIPPET) };
  if (snippet.context) state.context = String(snippet.context).slice(0, 2000);
  const res = await fetch(`${BASE}/v1/systemone`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    signal: AbortSignal.timeout(25000),
    body: JSON.stringify({
      model: MODEL, keep_alive: KEEP_ALIVE, state,
      questions: { match: { type: 'noul', instructions:
        'Is `snippet` (source code, with its file path in `context` when given) a place a developer ' +
        'searching for `query` would want to open: it implements, defines or directly handles what ' +
        '`query` describes? Merely mentioning a related word is not enough.' } },
    }),
  });
  if (!res.ok) throw new Error(`Local model HTTP ${res.status}: ${(await res.text()).slice(0, 200)}`);
  const p = (await res.json())?.answers?.match?.noul;
  if (typeof p !== 'number' || !Number.isFinite(p)) throw new Error('Local model gave no noul');
  return Math.min(1, Math.max(0, p));
}

async function grep(body) {
  const { query, snippets } = body;
  if (typeof query !== 'string' || !Array.isArray(snippets)) throw Object.assign(new Error('bad request'), { status: 400 });
  const out = new Array(snippets.length);
  let next = 0;
  await Promise.all(Array.from({ length: Math.min(CONCURRENCY, snippets.length) }, async () => {
    while (next < snippets.length) { const i = next++; out[i] = await judge(query, snippets[i]); }
  }));
  return { probabilities: out };
}

http.createServer(async (req, res) => {
  const send = (status, obj) => { res.writeHead(status, { 'content-type': 'application/json' }); res.end(JSON.stringify(obj)); };
  try {
    if (req.method === 'GET' && req.url === '/api/v1/me') return send(200, { email: 'local-model' });
    if (req.method !== 'POST' || req.url !== '/api/v1/grep') return send(404, { error: 'not found' });
    let raw = ''; for await (const c of req) raw += c;
    send(200, await grep(JSON.parse(raw)));
  } catch (e) {
    send(e.status || 503, { error: String(e.message || e) });
  }
}).listen(PORT, '127.0.0.1', () => console.error(`jg-local-proxy on http://127.0.0.1:${PORT} -> ${BASE} (${MODEL})`));
