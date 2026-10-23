---
name: workspace-cleanup
description: Where agent files go (tmp/ vs memory/) and how to clean them. Read before deleting anything in either — only when the user asks.
version: 2.0.0
---

# Workspace cleanup

Both folders are at the repo root and git-ignored.

| Folder | Holds | Lifetime |
| --- | --- | --- |
| `tmp/` | screenshots, recordings, `ui-dump.xml`, logcat captures, PR bodies, scratch scripts (`artifacts/` is written by `dev.sh`) | disposable |
| `memory/` | `last-serial`, `devices/<serial>/` (`device.env`, `coords.tsv`), **ARTEMIS keys** (`artemis.env`, `artemis-keys.md`), local plans and scripts | kept for the project's life |

Never store session-spanning data in `tmp/`, or media and full logs in `memory/`.

## Clean only on the user's command

Never delete from `tmp/` or `memory/` on your own — no end-of-task cleanup, no scheduled jobs.

- "Clean tmp": `rm -rf tmp/*` (PowerShell: `Get-ChildItem tmp | Remove-Item -Recurse -Force`).
  Keep `tmp/.ci-ok` if a PR still needs the local-CI marker.
- "Reset device memory": delete `memory/devices/` and `memory/last-serial` only. **Never** wipe all
  of `memory/` — it may hold the only copy of the ARTEMIS Gemini keys (see
  [artemis-key-rotation](../artemis-key-rotation/SKILL.md)). Delete other files there only when the
  user names them.
