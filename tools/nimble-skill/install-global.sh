#!/usr/bin/env bash
# Installs the nimble skill for every project on this PC, for Claude Code and Gemini/Antigravity:
#   ~/.nimble/                     nimble.sh, nimble-ask, nimble-on, nimble-off, jgl, jg-nimble-proxy.mjs
#   ~/.claude/skills/nimble/       SKILL.md   (Claude Code)
#   ~/.gemini/skills/nimble/       SKILL.md   (Gemini CLI / Antigravity)
# Re-run to update. Removes the old ~/.claude/nimble/ copy.
set -eu
here="$(cd "$(dirname "$0")" && pwd)"
repo="$(cd "$here/../.." && pwd)"
bin="$HOME/.nimble"
mkdir -p "$bin" "$HOME/.claude/skills/nimble" "$HOME/.gemini/skills/nimble"
cp "$repo/scripts/nimble.sh" "$here/nimble-ask" "$here/nimble-off" "$here/nimble-on" "$here/jgl" "$here/jg-nimble-proxy.mjs" "$bin/"
chmod +x "$bin/nimble-ask" "$bin/nimble-off" "$bin/nimble-on" "$bin/jgl"
cp "$here/SKILL.md" "$HOME/.claude/skills/nimble/SKILL.md"
cp "$here/SKILL.md" "$HOME/.gemini/skills/nimble/SKILL.md"
rm -rf "$HOME/.claude/nimble"
echo "Installed: $bin, ~/.claude/skills/nimble, ~/.gemini/skills/nimble"
