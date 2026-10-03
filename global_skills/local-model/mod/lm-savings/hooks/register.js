// /lm-savings: sums ~/.local-model/usage.log, one tab-separated row per local-model call:
// time, tool, ms, bytes in, result, cwd, detail. Answers without a Claude turn.

const DAY = 86400000

// Local YYYY-MM-DD.
function day(t) {
  const d = new Date(t)
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

// Bytes Claude did not read because the local model read them instead.
// lm-gate: the output shrank from `bytes in` to the byte count at the end of its result.
// lm-ask / nimble-ask: Claude got a verdict instead of the input.
function saved(tool, bytesIn, result) {
  if (tool === 'lm-gate') return Math.max(0, bytesIn - (Number(result.split(' ').pop()) || 0))
  if (tool === 'lm-ask' || tool === 'nimble-ask') return bytesIn
  return 0
}

// '' -> last 7 days, 'today', 'all', or a number of days.
function since(arg, now) {
  const a = arg.trim().toLowerCase()
  if (a === 'all') return { from: 0, label: 'all time' }
  if (a === 'today') {
    const d = new Date(now)
    d.setHours(0, 0, 0, 0)
    return { from: d.getTime(), label: 'today' }
  }
  const n = Number(a) > 0 ? Number(a) : 7
  return { from: now - n * DAY, label: 'last ' + n + ' day' + (n === 1 ? '' : 's') }
}

function summarize(text, arg, now) {
  const { from, label } = since(arg, now)
  const rows = {}
  let first = null
  for (const line of text.split('\n')) {
    const f = line.split('\t')
    if (f.length < 5) continue
    const t = Date.parse(f[0])
    if (!(t >= from)) continue
    if (first === null || t < first) first = t
    const tool = f[1]
    const bytesIn = Number(f[3]) || 0
    const r = rows[tool] || (rows[tool] = { calls: 0, ms: 0, bytes: 0, saved: 0, notes: {} })
    r.calls += 1
    r.ms += Number(f[2]) || 0
    r.bytes += bytesIn
    r.saved += saved(tool, bytesIn, f[4])
    // lm-route easy/hard, lm-loop denied/warned/same, diffcheck flags
    const word = f[4].split(' ')[0]
    if (tool === 'lm-route' || tool === 'lm-loop') r.notes[word] = (r.notes[word] || 0) + 1
    if (tool === 'lm-diffcheck') r.notes.flags = (r.notes.flags || 0) + (Number(word) || 0)
  }
  const tools = Object.keys(rows).sort((a, b) => rows[b].saved - rows[a].saved || rows[b].calls - rows[a].calls)
  if (tools.length === 0) return label + ': no local-model calls logged.'
  // A Markdown table, padded so it also lines up where it is shown as plain text.
  const kb = (n) => (n / 1024).toFixed(1) + ' KB'
  const tok = (n) => '~' + (n / 4 >= 1000 ? (n / 4000).toFixed(1) + 'K' : Math.round(n / 4)) + ' tok'
  let total = 0
  const body = tools.map((k) => {
    const r = rows[k]
    total += r.saved
    return [
      k,
      String(r.calls),
      Math.round(r.ms / r.calls) + ' ms',
      r.saved >= 1024 ? kb(r.saved) + ' (' + tok(r.saved) + ')' : '-',
      Object.entries(r.notes).map(([n, c]) => c + ' ' + n).join(', ') || '-',
    ]
  })
  const head = ['Tool', 'Calls', 'Avg time', 'Saved', 'Notes']
  const right = [false, true, true, false, false]
  const w = head.map((h, i) => Math.max(h.length, ...body.map((b) => b[i].length)))
  const row = (cells) => '| ' + cells.map((c, i) => (right[i] ? c.padStart(w[i]) : c.padEnd(w[i]))).join(' | ') + ' |'
  const rule = '|' + w.map((n, i) => (right[i] ? '-'.repeat(n + 1) + ':' : '-'.repeat(n + 2))).join('|') + '|'
  return [
    '**' + label + '** (since ' + day(first) + ')',
    '',
    row(head),
    rule,
    ...body.map(row),
    '',
    '**Total saved:** ' + kb(total) + ' (' + tok(total) + ', at 4 bytes per token)',
  ].join('\n')
}

export function register(on) {
  on('session.start', async ($, e, next) => {
    try {
      await $.command.register({
        name: 'lm-savings',
        description: 'What the local model saved (from ~/.local-model/usage.log)',
        argumentHint: '[days|today|all]',
        immediate: true,
      })
    } catch {}
    return next(e)
  })

  on('command.run', { command: 'lm-savings' }, async ($, e) => {
    const home = (await $.env.get('USERPROFILE')) || (await $.env.get('HOME'))
    const file = (await $.env.get('LOCAL_MODEL_USAGE_LOG_FILE')) || home + '/.local-model/usage.log'
    if (!(await $.fs.exists(file))) return { text: 'No usage log at ' + file }
    const text = await $.fs.read(file)
    return { text: summarize(text, e.args || '', await $.clock.now()) }
  })
}
