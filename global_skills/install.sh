#!/usr/bin/env bash
# Copies the skills in this folder to the global skill folders of this PC, so every agent in
# every project can use them:
#   ~/.claude/skills/<skill>/   Claude Code
#   ~/.gemini/skills/<skill>/   Gemini CLI / Antigravity
#   ~/.agents/skills/<skill>/   Codex (ChatGPT) and other agents that read the shared folder
# A skill with its own install.sh (for example nimble: commands in ~/.nimble, Claude Code hooks)
# runs it after the copy. Re-run to update.
# Usage: bash global_skills/install.sh [skill ...]     (no names = all skills)
set -eu
here="$(cd "$(dirname "$0")" && pwd)"
targets=("$HOME/.claude/skills" "$HOME/.gemini/skills" "$HOME/.agents/skills")
if [ "$#" -gt 0 ]; then skills=("$@"); else
  skills=(); for d in "$here"/*/; do [ -f "$d/SKILL.md" ] && skills+=("$(basename "$d")"); done
fi
for s in "${skills[@]}"; do
  src="$here/$s"
  [ -f "$src/SKILL.md" ] || { echo "No skill '$s' in $here" >&2; exit 1; }
  for t in "${targets[@]}"; do
    rm -rf "${t:?}/$s"; mkdir -p "$t/$s"
    (cd "$src" && find . -type f ! -name install.sh -exec cp --parents {} "$t/$s/" \;)
  done
  echo "Installed skill $s: ${targets[*]/%//$s}"
  [ -f "$src/install.sh" ] && bash "$src/install.sh"
done
echo "Restart your agents so they load the skills."
