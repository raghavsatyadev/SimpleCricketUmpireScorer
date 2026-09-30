#!/usr/bin/env bash
# Claude Code Stop hook: no "done" without evidence of a passing check.
#
# Wired in .claude/settings.json (hooks.Stop). Reads the hook JSON on stdin.
#
#   1. Kotlin/Gradle files (*.kt, *.kts, gradle/libs.versions.toml) differ from the
#      origin/$BASE_BRANCH merge-base, and tmp/.ci-ok is missing or does not match the current
#      evidence hash (scripts/evidence-hash.sh)  ->  exit 2, reason on stderr. Claude Code
#      feeds that back to the agent, which must run scripts/ci-local.sh before stopping.
#   2. On fix/* branches only: reminds (never blocks) when nothing in tmp/ is newer than the
#      newest debug APK (plus an optional Nimble check: "fix is done" with no device re-run in the
#      transcript). ARTEMIS keeps its traces in its own clone, so tmp/ has no artifact
#      that reliably proves a device run; this is a nudge, not a gate.
#
# stop_hook_active=true (the agent is already continuing because of this hook) -> exit 0,
# so the hook can never loop. Any unexpected state (no git, no scripts) also exits 0.
# No jq: the JSON is matched with grep/sed. Works in Git Bash, macOS and Linux.

input=""
[ -t 0 ] || input=$(cat)
# One line, no CRs, so the patterns below see the whole object.
flat=$(printf '%s' "$input" | tr -d '\r' | tr '\n' ' ')

if printf '%s' "$flat" | grep -Eq '"stop_hook_active"[[:space:]]*:[[:space:]]*true'; then
  exit 0
fi

# Work in the repo the session is in (hook JSON "cwd"), which may be a worktree rather than
# $CLAUDE_PROJECT_DIR.
cwd=$(printf '%s' "$flat" \
  | sed -n 's/.*"cwd"[[:space:]]*:[[:space:]]*"\(\([^"\\]\|\\.\)*\)".*/\1/p' \
  | sed 's/\\\\/\\/g; s/\\/\//g')
for d in "$cwd" "$PWD" "${CLAUDE_PROJECT_DIR:-}"; do
  [ -n "$d" ] || continue
  REPO=$(git -C "$d" rev-parse --show-toplevel 2>/dev/null) && break
done
[ -n "${REPO:-}" ] || exit 0

HASHER="$REPO/scripts/evidence-hash.sh"
[ -f "$HASHER" ] || HASHER="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/evidence-hash.sh"
[ -f "$HASHER" ] || exit 0
# shellcheck source=evidence-hash.sh
. "$HASHER"

. "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/kit-env.sh"

# ------------------------------------------------ any Kotlin/Gradle change at all?

base=$(git -C "$REPO" merge-base HEAD "origin/$BASE_BRANCH" 2>/dev/null) || base=HEAD
changed=0
git -C "$REPO" diff --quiet "$base" -- "${EVIDENCE_PATHSPEC[@]}" 2>/dev/null || changed=1
if [ "$changed" = 0 ] && [ -n "$(git -C "$REPO" ls-files --others --exclude-standard -- \
     "${EVIDENCE_PATHSPEC[@]}" 2>/dev/null | head -1)" ]; then
  changed=1
fi
[ "$changed" = 1 ] || exit 0

# ------------------------------------------------ rule 1: passing-check evidence

marker="$REPO/tmp/.ci-ok"
want=$(evidence_hash "$REPO")
have=""
[ -f "$marker" ] && have=$(tr -d '\r\n[:space:]' < "$marker")
if [ -z "$have" ] || [ "$have" != "$want" ]; then
  echo "Kotlin changed since the last passing check. Run scripts/ci-local.sh before finishing." >&2
  exit 2
fi

# ------------------------------------------------ rule 2: fix/* device reminder (soft)

branch=$(git -C "$REPO" symbolic-ref --short -q HEAD 2>/dev/null)
case "$branch" in
  fix/*) ;;
  *) exit 0 ;;
esac

apk_dir="$REPO/${APP_DIR}/build/outputs/apk"
newest_apk=""
if [ -d "$apk_dir" ]; then
  # ls -t sorts newest first on GNU and BSD alike.
  newest_apk=$(find "$apk_dir" -type f -name '*.apk' -exec ls -t {} + 2>/dev/null | head -1)
fi

evidence=""
if [ -n "$newest_apk" ] && [ -d "$REPO/tmp" ]; then
  # dev.sh screenshots/recordings/logcat (tmp/artifacts/), issue notes and other run logs.
  evidence=$(find "$REPO/tmp" -type f -newer "$newest_apk" \
    ! -name '.ci-ok' ! -name 'pr*body*' 2>/dev/null | head -1)
fi

msg=""
if [ -z "$evidence" ]; then
  msg="fix/* branch: no device evidence in tmp/ newer than the latest APK. Reinstall and re-run the ARTEMIS reproduction (save notes/screens to tmp/) before calling the fix verified."
fi

# Fuzzy check (optional, Nimble): the last assistant text claims the fix is done, but the
# transcript shows no device re-run. Reminder only; silent when Nimble is off, slow or unsure.
if [ -z "$msg" ] && [ -f "$REPO/scripts/nimble.sh" ]; then
  tpath=$(printf '%s' "$flat" \
    | sed -n 's/.*"transcript_path"[[:space:]]*:[[:space:]]*"\(\([^"\\]\|\\.\)*\)".*/\1/p' \
    | sed 's/\\\\/\\/g; s/\\/\//g')
  if [ -n "$tpath" ] && [ -f "$tpath" ]; then
    last=$(grep '"type":"assistant"' "$tpath" | grep '"type":"text"' | tail -n 1 \
      | sed -n 's/.*"type":"text","text":"\(\([^"\\]\|\\.\)*\)".*/\1/p')
    if printf '%s' "$last" | grep -Eiq '(fixed|done|resolved|works now|complete)'; then
      # shellcheck source=nimble.sh
      . "$REPO/scripts/nimble.sh"
      NIMBLE_ALLOW_JEV=0   # hooks stay local and free
      q='{"rerun":{"type":"noul","instructions":"Is there a device re-run in the text?"}}'
      if resp=$(tail -c 60000 "$tpath" | nimble_ask 2 "$q") \
         && n=$(nimble_get "$resp" rerun noul) && ! nimble_ge "$n" 0.3; then
        msg="fix/* branch: the last message says the fix is done, but the transcript shows no device re-run (Nimble, re-run score $n). Re-run the reproduction on the device before calling it verified."
      fi
    fi
  fi
fi

if [ -n "$msg" ]; then
  echo "$msg" >&2
  printf '{"systemMessage": "%s"}\n' "$msg"
fi
exit 0
