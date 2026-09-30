#!/usr/bin/env node
// Local stand-in for jevgrep.com's /api/v1/grep, answered by Nimble on Ollama. Lets `jg` judge
// snippets on this PC: nothing leaves the machine.
//
//   node ~/.claude/nimble/jg-nimble-proxy.mjs            # listens on 127.0.0.1:11435
//   JEVGREP_ENDPOINT=http://127.0.0.1:11435 JEVGREP_TOKEN=local jg "search intent" [paths]
//
// Each snippet is one Noul ("could this snippet be what the query looks for?") with the query and
// snippet as named state fields, as in TypeSafe's re-ranking cookbook. The noul is the score.
// Env: NIMBLE_URL (default http://127.0.0.1:11434), NIMBLE_MODEL (nimble), NIMBLE_KEEP_ALIVE (2m),
// JG_PROXY_PORT (11435), JG_PROXY_CONCURRENCY (4).
import http from 'node:http';

const NIMBLE = (process.env.NIMBLE_URL || 'http://127.0.0.1:11434').replace(/\/$/, '');
const MODEL = process.env.NIMBLE_MODEL || 'nimble';
const KEEP_ALIVE = process.env.NIMBLE_KEEP_ALIVE || '2m';
const PORT = Number(process.env.JG_PROXY_PORT || 11435);
const CONCURRENCY = Number(process.env.JG_PROXY_CONCURRENCY || 4);
const MAX_SNIPPET = 12000; // characters; keeps one request inside Nimble's ~8K-token window

async function judge(query, snippet) {
  const state = { query, snippet: snippet.text.slice(0, MAX_SNIPPET) };
  if (snippet.context) state.context = String(snippet.context).slice(0, 2000);
  const res = await fetch(`${NIMBLE}/v1/systemone`, {
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
  if (!res.ok) throw new Error(`Nimble HTTP ${res.status}: ${(await res.text()).slice(0, 200)}`);
  const p = (await res.json())?.answers?.match?.noul;
  if (typeof p !== 'number' || !Number.isFinite(p)) throw new Error('Nimble gave no noul');
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
    if (req.method === 'GET' && req.url === '/api/v1/me') return send(200, { email: 'local-nimble' });
    if (req.method !== 'POST' || req.url !== '/api/v1/grep') return send(404, { error: 'not found' });
    let raw = ''; for await (const c of req) raw += c;
    send(200, await grep(JSON.parse(raw)));
  } catch (e) {
    send(e.status || 503, { error: String(e.message || e) });
  }
}).listen(PORT, '127.0.0.1', () => console.error(`jg-nimble-proxy on http://127.0.0.1:${PORT} -> ${NIMBLE} (${MODEL})`));
