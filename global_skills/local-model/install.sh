#!/usr/bin/env bash
# Extra setup for the local-model skill; global_skills/install.sh runs it after it copies SKILL.md.
#   ~/.local-model/                local-model.sh, lm-ask, lm-on, lm-off, jgl, jg-local-proxy.mjs
#   ~/.claude/settings.json        SessionStart hook lm-on, SessionEnd hook lm-off (Claude Code)
# Re-run to update. Moves an install from before the rename (the `nimble` skill, ~/.nimble/,
# nimble-on/off hooks) over: keeps its usage.log, removes the rest.
set -eu
here="$(cd "$(dirname "$0")" && pwd)"
repo="$(cd "$here/../.." && pwd)"
bin="$HOME/.local-model"
mkdir -p "$bin"
cp "$repo/scripts/local-model.sh" "$here/lm-ask" "$here/lm-off" "$here/lm-on" "$here/jgl" "$here/jg-local-proxy.mjs" "$bin/"
chmod +x "$bin/lm-ask" "$bin/lm-off" "$bin/lm-on" "$bin/jgl"
old="$HOME/.nimble"
if [ -d "$old" ]; then
  [ -f "$old/usage.log" ] && cat "$old/usage.log" >>"$bin/usage.log"
  rm -rf "$old"
fi
rm -rf "$HOME/.claude/nimble" "$HOME/.claude/skills/nimble" "$HOME/.gemini/skills/nimble" "$HOME/.agents/skills/nimble"
# Global Claude Code hooks, so every project turns the model on at start and off at end.
# Drops the old nimble-on/off hooks; skipped when an lm-on / lm-off hook is already there.
node - "$HOME/.claude/settings.json" <<'JS'
const fs = require("fs"), f = process.argv[2];
const s = fs.existsSync(f) ? JSON.parse(fs.readFileSync(f, "utf8")) : {};
s.hooks ??= {};
const add = (event, oldScript, script, command) => {
  let list = (s.hooks[event] ??= []).filter((h) => !JSON.stringify(h).includes(oldScript));
  if (!JSON.stringify(list).includes(script)) list.push({ hooks: [{ type: "command", command }] });
  s.hooks[event] = list;
};
add("SessionStart", "nimble-on", "lm-on", 'bash "$HOME/.local-model/lm-on"');
add("SessionEnd", "nimble-off", "lm-off", 'bash "$HOME/.local-model/lm-off" "${LOCAL_MODEL_NAME:-nimble}"');
fs.writeFileSync(f, JSON.stringify(s, null, 2) + "\n");
JS
echo "Installed: $bin, hooks in ~/.claude/settings.json"
