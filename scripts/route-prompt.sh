#!/usr/bin/env bash
# Claude Code UserPromptSubmit hook: point the agent at the .agents/rules files that fit the prompt.
#
# Wired in .claude/settings.json (hooks.UserPromptSubmit). Reads the hook JSON on stdin.
# Asks the optional local Nimble model (scripts/nimble.sh) to classify the prompt, then prints
# {"hookSpecificOutput":{"hookEventName":"UserPromptSubmit","additionalContext":"..."}}.
# Prints NOTHING when Nimble is off, slow (2 s cap), unsure (top choice < 0.6) or the class is
# "other". It never blocks: every path exits 0.

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
[ -f "$here/nimble.sh" ] || exit 0
# shellcheck source=nimble.sh
. "$here/nimble.sh"
NIMBLE_ALLOW_JEV=0   # hooks stay local and free; Jev is for nimble-ask only
nimble_enabled || exit 0

input=""
[ -t 0 ] || input=$(cat)
flat=$(printf '%s' "$input" | tr -d '\r' | tr '\n' ' ')
# The prompt text; the JSON escapes stay as they are, which is fine for classification.
prompt=$(printf '%s' "$flat" \
  | sed -n 's/.*"prompt"[[:space:]]*:[[:space:]]*"\(\([^"\]\|\.\)*\)".*/\1/p')
[ "${#prompt}" -ge 8 ] || exit 0

q='{"area":{"type":"choice","instructions":"Which area of this Android project does this request touch?","criteria":{"engine":"core business logic, domain code, native code (JNI/NDK), data layer","ui":"Compose or Material 3 user interface, screens, theme, keyboard layout","build":"Gradle build, dependencies, version catalog, CI, ktfmt, compile errors","device":"testing or reproducing a bug on an Android device or emulator, ARTEMIS, adb","docs":"documentation, AGENTS.md, agent rules or skills, branch or PR policy","other":"anything else, a question or chat"}}}'

resp=$(printf '%s' "$prompt" | nimble_ask 2 "$q") || exit 0
c=$(nimble_get "$resp" area choice) || exit 0
p=$(nimble_get "$resp" area p) || exit 0
nimble_ge "$p" 0.6 || exit 0

case "$c" in
  engine) r="android-build.md" ;;
  ui) r="format.md and android-build.md" ;;
  build) r="android-build.md and format.md" ;;
  device) r="artemis-mobile-testing.md and android-build.md" ;;
  docs) r="branch-pr-policy.md" ;;
  *) exit 0 ;;
esac
printf '{"hookSpecificOutput":{"hookEventName":"UserPromptSubmit","additionalContext":"Nimble routing (%s): read %s in .agents/rules/ before you start."}}\n' "$c" "$r"
exit 0
