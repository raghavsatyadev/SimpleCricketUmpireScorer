#!/usr/bin/env bash
# Extra setup for the nimble skill; global_skills/install.sh runs it after it copies SKILL.md.
#   ~/.nimble/                     nimble.sh, nimble-ask, nimble-on, nimble-off, jgl, jg-nimble-proxy.mjs
#   ~/.claude/settings.json        SessionStart hook nimble-on, SessionEnd hook nimble-off (Claude Code)
# Re-run to update. Removes the old ~/.claude/nimble/ copy.
set -eu
here="$(cd "$(dirname "$0")" && pwd)"
repo="$(cd "$here/../.." && pwd)"
bin="$HOME/.nimble"
mkdir -p "$bin"
cp "$repo/scripts/nimble.sh" "$here/nimble-ask" "$here/nimble-off" "$here/nimble-on" "$here/jgl" "$here/jg-nimble-proxy.mjs" "$bin/"
chmod +x "$bin/nimble-ask" "$bin/nimble-off" "$bin/nimble-on" "$bin/jgl"
rm -rf "$HOME/.claude/nimble"
# Global Claude Code hooks, so every project turns the model on at start and off at end.
# Skipped when a hook already runs nimble-on / nimble-off.
node - "$HOME/.claude/settings.json" <<'JS'
const fs = require("fs"), f = process.argv[2];
const s = fs.existsSync(f) ? JSON.parse(fs.readFileSync(f, "utf8")) : {};
s.hooks ??= {};
const add = (event, script, command) => {
  const list = (s.hooks[event] ??= []);
  if (!JSON.stringify(list).includes(script)) list.push({ hooks: [{ type: "command", command }] });
};
add("SessionStart", "nimble-on", 'bash "$HOME/.nimble/nimble-on"');
add("SessionEnd", "nimble-off", 'bash "$HOME/.nimble/nimble-off" "${NIMBLE_MODEL:-nimble}"');
fs.writeFileSync(f, JSON.stringify(s, null, 2) + "\n");
JS
echo "Installed: $bin, hooks in ~/.claude/settings.json"
