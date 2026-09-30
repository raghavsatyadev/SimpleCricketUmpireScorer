---
trigger: model_decision
description: When exploring, testing or writing tests against the app on a device with ARTEMIS MCP tools (mobile_run_task, mobile_diagnose, mobile_get_device_state).
---

# ARTEMIS mobile-testing rules

The full rules ship with ARTEMIS as `mcp_server/rules.md` in the ARTEMIS clone. Do not copy them
into this repo — they change upstream and a second copy wastes context.

- **Claude Code**: `uv run artemis mcp --install claude` (run inside the ARTEMIS clone) installs
  them to `~/.claude/rules/artemis.md`, which loads every session. Nothing more to read.
- **Other agents** (Gemini, Codex, Cursor): read `$ARTEMIS_HOME/mcp_server/rules.md` before
  driving a device, or install them with `uv run artemis mcp --install <client>`.

Project essentials, in case the rules are not loaded:

- Call `mobile_diagnose` first whenever an ARTEMIS tool errors or no device is found;
  `attempt_fix=true` before any manual ADB fix. Follow its `next_steps` in order.
- More than one device attached → ask the user which serial to use, then pass `device_serial`.
- `Flash` for short, deterministic flows; `Pro` for multi-step exploration, checkpoints, or reports.
- Pull device artifacts to the host with `adb -s <serial> pull …` — ARTEMIS stays on the device.
- API keys go in the ARTEMIS `.env`, never through the chat.
