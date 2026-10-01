# Global skills

Skills that belong in each developer's **global** skill folders, not in this repo's
`.agents/skills/`: they work in every project (for example `nimble`). No agent loads this folder
directly. It is the copy that new team members install from.

Install or update all of them (Git Bash on Windows):

```bash
bash global_skills/install.sh            # all skills
bash global_skills/install.sh nimble     # one skill
```

It copies each skill folder to:

| Agent | Global folder |
| --- | --- |
| Claude Code | `~/.claude/skills/<skill>/` |
| Gemini CLI / Antigravity | `~/.gemini/skills/<skill>/` |
| Codex (ChatGPT) and other agents | `~/.agents/skills/<skill>/` |

A skill with its own `install.sh` runs it after the copy (`nimble`: commands in `~/.nimble/`,
SessionStart/SessionEnd hooks in `~/.claude/settings.json`). Restart the agents afterwards.

To add a skill: put its folder here (`<skill>/SKILL.md`, plus an optional `install.sh`), run the
installer, and do not also keep a copy in `.agents/skills/`.
