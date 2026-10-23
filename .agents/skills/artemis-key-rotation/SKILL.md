---
name: artemis-key-rotation
description: Switch ARTEMIS to the other Gemini API key when runs fail on quota or rate limits (429, RESOURCE_EXHAUSTED, "quota exceeded").
---

# artemis-key-rotation

ARTEMIS reads one Gemini key, `GEMINI_API_KEY`, from the ARTEMIS clone's `.env` (`../artemis/.env` next to this repo). There are two keys, and both live only in this repo's gitignored `memory/`:

- `memory/artemis.env`: `GEMINI_API_KEY_1`, `GEMINI_API_KEY_2`, and the currently active `GEMINI_API_KEY`
- `memory/artemis-keys.md`: which key is active or on backup, and when a quota last ran out

Never write a key into this skill, a tracked file, a commit, a PR, chat output or Claude's memory. Don't search environment variables or other folders for keys; they are only in `memory/`.

## Rotate

Run from the repo root. It swaps to whichever key is not active, printing only the last 4 characters:

```bash
M=memory/artemis.env; E=../artemis/.env
CUR=$(grep '^GEMINI_API_KEY=' "$E" | cut -d= -f2-)
K1=$(grep '^GEMINI_API_KEY_1=' "$M" | cut -d= -f2-); K2=$(grep '^GEMINI_API_KEY_2=' "$M" | cut -d= -f2-)
[ "$CUR" = "$K1" ] && NEW=$K2 || NEW=$K1
cp "$E" "$E.bak-$(date +%Y%m%d%H%M)"
sed -i "s|^GEMINI_API_KEY=.*|GEMINI_API_KEY=$NEW|" "$E" "$M"
echo "ARTEMIS now on key ending ${NEW: -4}"
```

Then:
1. Update the headings in `memory/artemis-keys.md` to say which key is active and which ran out, with the date. Edit it with a script that writes a raw string (Python `r'...'`); sed and perl turn the `\a` in `..\artemis\.env` into a bell character.
2. Ask the user to restart the ARTEMIS MCP server (`/mcp`). ARTEMIS only reads `.env` at startup.
3. Delete old `.env.bak-*` copies in the ARTEMIS folder once the new key is confirmed working.

If both keys have run out, stop and tell the user. Fall back to plain adb for device checks (see [android-device-test](../android-device-test/SKILL.md)).
