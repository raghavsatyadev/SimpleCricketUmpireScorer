#!/usr/bin/env bash
# Optional helper: ask a "System One" decision model one small question about some text.
#
# Sourced by the agent hooks (gradle-agent.sh, route-prompt.sh, check-done.sh) and by nimble-ask.
# Every backend is OPTIONAL: when none is set up, reachable or sure, every function here fails
# quietly and the caller does nothing. Nothing here prints except answers.
#
# Backends, all on the same /v1/systemone API:
#   local  Nimble or Tev1 on Ollama (default Nimble) or Laya: free, on this PC.
#   jev    TypeSafe's hosted Jev: paid per input token, 32K-token state. Used ONLY when the caller
#          allows it (NIMBLE_ALLOW_JEV=1; the hooks never do) AND the text is too long for the
#          local model, or no local model answers. Short text never goes to Jev.
#
#   NIMBLE_HOOKS=0      turn every backend off
#   NIMBLE_URL          local backend, default http://127.0.0.1:11434 (Laya: http://127.0.0.1:8000)
#   NIMBLE_MODEL        default nimble (tev1:4b, tev1:0.8b; Laya: laya)
#   NIMBLE_MAX_BYTES    local input budget, default 16000 (Tev1: 3600, Laya: 1800). Nimble refuses
#                       more than 8194 tokens and dense Gradle logs run ~2.4 bytes a token.
#   NIMBLE_KEEP_ALIVE   how long Ollama keeps the model loaded after a call, default 10m
#   NIMBLE_LOCAL=0      skip the local backend (Jev only)
#   JEV_API_KEY         TypeSafe API key; else read from ~/.config/typesafe/api_key. None = no Jev.
#   JEV_URL / JEV_MODEL default https://api.typesafe.ai / jev-latest
#   JEV_MAX_BYTES       Jev input budget, default 100000 (~28K tokens, under the 32K state limit)
#   NIMBLE_ALLOW_JEV    1 lets this call use Jev (nimble-ask sets it); default 0
#
#   nimble_ask <max_seconds> <questions_json>  < state_text
#       Prints the response JSON on one line and sets NIMBLE_BACKEND (local|jev; only seen when
#       not called inside $(...), e.g. `nimble_ask ... >file`). Returns 1
#       on any failure. The state is made ASCII-safe, JSON-escaped and cut to its TAIL (the end of
#       a log is where the failure is).
#   nimble_get <response_json> <question_key> <field>
#       field = choice | confidence | noul | p   (p = probability of the chosen label)
#   nimble_ge <number> <threshold>   true when number >= threshold
#
# Bash + curl + sed + awk + tr only (no jq, no python). CRLF-safe.

NIMBLE_URL="${NIMBLE_URL:-http://127.0.0.1:11434}"
NIMBLE_MODEL="${NIMBLE_MODEL:-nimble}"
# Local budget in bytes: Nimble's context (8194 tokens) binds first, ~4 bytes/token for Gradle
# logs, ~2.5 for path-heavy text. _nimble_post retries smaller when the server says too many tokens.
NIMBLE_MAX_BYTES="${NIMBLE_MAX_BYTES:-16000}"
NIMBLE_KEEP_ALIVE="${NIMBLE_KEEP_ALIVE:-10m}"
JEV_URL="${JEV_URL:-https://api.typesafe.ai}"
JEV_MODEL="${JEV_MODEL:-jev-latest}"
JEV_MAX_BYTES="${JEV_MAX_BYTES:-100000}"
NIMBLE_BACKEND=""

nimble_enabled() {
  [ "${NIMBLE_HOOKS:-1}" != "0" ] && command -v curl >/dev/null 2>&1
}

_jev_key() {
  if [ -n "${JEV_API_KEY:-}" ]; then printf '%s' "$JEV_API_KEY"; return 0; fi
  local f="${HOME}/.config/typesafe/api_key"
  [ -s "$f" ] && tr -d ' \r\n' <"$f"
}

# stdin -> JSON string body (no surrounding quotes). CR is dropped, tabs become spaces, every
# other control byte and every non-ASCII byte becomes '?', then \ and " are escaped and lines
# are joined with \n.
_nimble_escape() {
  LC_ALL=C tr -d '\r' | LC_ALL=C tr '\11' ' ' | LC_ALL=C tr -c '\12\40-\176' '?' \
    | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' \
    | awk 'NR > 1 { printf "%s", "\\" "n" } { printf "%s", $0 }'
}

# _nimble_post <url> <model> <key or ""> <budget_bytes> <max_seconds> <questions> <raw_text>
_nimble_post() {
  local url="$1" model="$2" key="$3" budget="$4" max_time="$5" questions="$6" raw="$7"
  local esc keep resp try n m ka=""
  budget=$((budget - ${#questions}))
  [ "$budget" -gt 1000 ] || return 1
  [ -z "$key" ] && ka=",\"keep_alive\":\"$NIMBLE_KEEP_ALIVE\""
  keep=$budget
  for try in 1 2 3; do
    # Keep the tail; shrink until the escaped text fits (escaping can only grow it).
    while :; do
      esc=$(printf '%s' "$raw" | tail -c "$keep" | _nimble_escape)
      [ "${#esc}" -le "$budget" ] && break
      keep=$((keep * 3 / 4))
      [ "$keep" -gt 500 ] || return 1
    done
    resp=$(printf '{"model":"%s"%s,"state":"%s","questions":%s}' "$model" "$ka" "$esc" "$questions" \
      | curl -s --connect-timeout "${NIMBLE_CONNECT_TIMEOUT:-0.5}" -m "$max_time" \
          -H 'Content-Type: application/json' ${key:+-H "Authorization: Bearer $key"} \
          --data-binary @- "${url%/}/v1/systemone" 2>/dev/null) || return 1
    resp=$(printf '%s' "$resp" | tr -d '\r\n')
    case "$resp" in *'"answers"'*) printf '%s\n' "$resp"; return 0 ;; esac
    # Over the context: "prompt 0 has N tokens; expected 1-M tokens". Dense text (paths,
    # stack traces) can pass the byte budget; cut to ~85% of M/N and retry.
    n=$(printf '%s' "$resp" | sed -n 's/.* has \([0-9][0-9]*\) tokens.*/\1/p')
    m=$(printf '%s' "$resp" | sed -n 's/.*expected [0-9]*[^0-9]*\([0-9][0-9]*\).*/\1/p')
    [ -n "$n" ] && [ -n "$m" ] && [ "$n" -gt 0 ] || return 1
    keep=$((keep * m * 85 / (n * 100)))
    [ "$keep" -gt 500 ] || return 1
    budget=$keep
  done
  return 1
}

# Appends one line per Jev call to ~/.config/typesafe/usage.log (time, input tokens), so the
# spend is visible. Never fails the call.
_jev_log() {
  local t
  t=$(printf '%s' "$1" | sed -n 's/.*"input_tokens":\([0-9][0-9]*\).*/\1/p')
  mkdir -p "${HOME}/.config/typesafe" 2>/dev/null \
    && printf '%s\t%s\t%s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "${t:-?}" "${NIMBLE_CALLER:-hook}" \
      >>"${HOME}/.config/typesafe/usage.log" 2>/dev/null
  return 0
}

# A short hook timeout aborts the request, and Ollama then stops loading the model, so after an
# unload (nimble-off, keep_alive expiry) every hook would time out again. Load it in the
# background instead, so the next call is warm. Ollama only; returns at once.
_nimble_warm() {
  case "$NIMBLE_URL" in *:11434*) ;; *) return 0 ;; esac
  ( curl -s -m 120 "${NIMBLE_URL%/}/api/generate" \
      -d "{\"model\":\"$NIMBLE_MODEL\",\"keep_alive\":\"$NIMBLE_KEEP_ALIVE\"}" >/dev/null 2>&1 & ) \
    >/dev/null 2>&1
  return 0
}

nimble_ask() {
  nimble_enabled || return 1
  local max_time="$1" questions="$2" raw size key="" resp
  NIMBLE_BACKEND=""
  # NUL bytes cannot live in a shell variable; drop them. 1 MB is far past any budget.
  raw=$(tail -c 1000000 | tr -d '\000')
  size=${#raw}
  [ "${NIMBLE_ALLOW_JEV:-0}" = "1" ] && key=$(_jev_key)
  # Short enough for the local model: local only; Jev only when no local model answers.
  if [ "$size" -le "$NIMBLE_MAX_BYTES" ] || [ -z "$key" ]; then
    if [ "${NIMBLE_LOCAL:-1}" != "0" ] \
      && resp=$(_nimble_post "$NIMBLE_URL" "$NIMBLE_MODEL" "" "$NIMBLE_MAX_BYTES" "$max_time" "$questions" "$raw"); then
      NIMBLE_BACKEND=local; printf '%s\n' "$resp"; return 0
    fi
    _nimble_warm
    [ -n "$key" ] || return 1
  fi
  # Too long for the local model (or no local answer), and the caller allows Jev.
  if resp=$(_nimble_post "$JEV_URL" "$JEV_MODEL" "$key" "$JEV_MAX_BYTES" "$((max_time > 20 ? max_time : 20))" "$questions" "$raw"); then
    _jev_log "$resp"
    NIMBLE_BACKEND=jev; printf '%s\n' "$resp"; return 0
  fi
  # Jev failed (bad key, quota, offline): the local model on the tail beats nothing.
  [ "$size" -gt "$NIMBLE_MAX_BYTES" ] && [ "${NIMBLE_LOCAL:-1}" != "0" ] || return 1
  resp=$(_nimble_post "$NIMBLE_URL" "$NIMBLE_MODEL" "" "$NIMBLE_MAX_BYTES" "$max_time" "$questions" "$raw") || return 1
  NIMBLE_BACKEND=local; printf '%s\n' "$resp"
}

nimble_get() {
  local out
  out=$(printf '%s' "$1" | awk -v key="$2" -v field="$3" '
    {
      s = $0
      a = index(s, "\"answers\""); if (!a) exit 1
      s = substr(s, a)
      k = index(s, "\"" key "\":{"); if (!k) exit 1
      s = substr(s, k + length(key) + 3)
      if (field == "choice" || field == "p") {
        if (!match(s, /"choice":"[^"]*"/)) exit 1
        c = substr(s, RSTART + 10, RLENGTH - 11)
        if (field == "choice") { print c; exit 0 }
        if (!match(s, "\"" c "\":[0-9.eE+-]+")) exit 1
        print substr(s, RSTART + length(c) + 3, RLENGTH - length(c) - 3); exit 0
      }
      if (!match(s, "\"" field "\":[0-9.eE+-]+")) exit 1
      print substr(s, RSTART + length(field) + 3, RLENGTH - length(field) - 3)
    }') || return 1
  [ -n "$out" ] || return 1
  printf '%s\n' "$out"
}

nimble_ge() {
  awk -v a="$1" -v b="$2" 'BEGIN { exit !(a + 0 >= b + 0) }'
}
