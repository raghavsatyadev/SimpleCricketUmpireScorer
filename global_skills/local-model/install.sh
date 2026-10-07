#!/usr/bin/env bash
# Extra setup for the local-model skill; global_skills/install.sh runs it after it copies SKILL.md.
#   ~/.local-model/                local-model.sh, lm-ask, lm-on, lm-off, jgl, lm-rank, lm-diffcheck and
#                                  the Node hooks (lm-client, lm-gate, lm-loop, lm-route, ...)
#   ~/.claude/settings.json        Claude Code hooks: SessionStart lm-on, SessionEnd lm-off,
#                                  PostToolUse lm-gate (long build/test output), Pre/PostToolUse(Failure)
#                                  lm-loop (repeated failures), UserPromptSubmit lm-route (request
#                                  size hint), UserPromptExpansion lm-diffcheck (before /code-review)
#   ~/.local-model/mod/            Claude Code mods, as the `local-model` plugin marketplace:
#                                  lm-savings (/lm-savings sums usage.log). Needs Claude Code 2.1.287+.
# Re-run to update. Moves an install from before the rename (the `nimble` skill, ~/.nimble/,
# nimble-on/off hooks) over: keeps its usage.log, removes the rest.
set -eu
here="$(cd "$(dirname "$0")" && pwd)"
repo="$(cd "$here/../.." && pwd)"
bin="$HOME/.local-model"
mkdir -p "$bin"
cp "$repo/scripts/local-model.sh" "$here"/lm-ask "$here"/lm-off "$here"/lm-on "$here"/jgl "$here"/lm-rank   "$here"/lm-diffcheck "$here"/*.mjs "$bin/"
chmod +x "$bin/lm-ask" "$bin/lm-off" "$bin/lm-on" "$bin/jgl" "$bin/lm-rank" "$bin/lm-diffcheck"
old="$HOME/.nimble"
if [ -d "$old" ]; then
  [ -f "$old/usage.log" ] && cat "$old/usage.log" >>"$bin/usage.log"
  rm -rf "$old"
fi
rm -rf "$HOME/.claude/nimble" "$HOME/.claude/skills/nimble" "$HOME/.gemini/skills/nimble" "$HOME/.agents/skills/nimble"
# Global Claude Code hooks, so every project turns the model on at start and off at end.
# Drops the old nimble-on/off hooks; skipped when an lm-on / lm-off hook is already there; the
# Node hooks are replaced each run.
node - "$HOME/.claude/settings.json" <<'JS'
const fs = require("fs"), f = process.argv[2];
const s = fs.existsSync(f) ? JSON.parse(fs.readFileSync(f, "utf8")) : {};
s.hooks ??= {};
const add = (event, oldScript, script, command, extra = {}) => {
  let list = (s.hooks[event] ??= []).filter((h) => !JSON.stringify(h).includes(oldScript));
  if (!JSON.stringify(list).includes(script)) list.push({ ...extra, hooks: [{ type: "command", command }] });
  s.hooks[event] = list;
};
add("SessionStart", "nimble-on", "lm-on", 'bash "$HOME/.local-model/lm-on"');
add("SessionEnd", "nimble-off", "lm-off", 'bash "$HOME/.local-model/lm-off" "${LOCAL_MODEL_NAME:-nimble}"');
const node = (script, args = "") => `node "$HOME/.local-model/${script}"${args}`;
add("PostToolUse", "lm-gate.mjs", "lm-gate.mjs", node("lm-gate.mjs"), { matcher: "Bash" });
add("PreToolUse", "lm-loop.mjs", "lm-loop.mjs", node("lm-loop.mjs"), { matcher: "Bash" });
add("PostToolUse", "lm-loop.mjs", "lm-loop.mjs", node("lm-loop.mjs"), { matcher: "Bash|Edit|Write|MultiEdit|NotebookEdit" });
add("PostToolUseFailure", "lm-loop.mjs", "lm-loop.mjs", node("lm-loop.mjs"), { matcher: "Bash" });
add("UserPromptSubmit", "lm-route.mjs", "lm-route.mjs", node("lm-route.mjs"));
add("UserPromptExpansion", "lm-diffcheck.mjs", "lm-diffcheck.mjs", node("lm-diffcheck.mjs", " --hook"), {
  matcher: "code-review|review|security-review|simplify",
});
fs.writeFileSync(f, JSON.stringify(s, null, 2) + "\n");
JS
# Mods: a local marketplace; an installed plugin is cached by version, so bump plugin.json to update.
rm -rf "$bin/mod" && cp -r "$here/mod" "$bin/mod"
if command -v claude >/dev/null 2>&1; then
  mp="$bin/mod"; command -v cygpath >/dev/null 2>&1 && mp="$(cygpath -w "$mp")"
  claude plugin marketplace add "$mp" >/dev/null 2>&1 || claude plugin marketplace update local-model >/dev/null 2>&1 || true
  claude plugin install lm-savings@local-model >/dev/null 2>&1 || claude plugin update lm-savings@local-model >/dev/null 2>&1 || true
fi
echo "Installed: $bin, hooks in ~/.claude/settings.json, mods from $bin/mod"
