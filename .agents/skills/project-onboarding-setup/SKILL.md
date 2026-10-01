---
name: project-onboarding-setup
description: Set up a developer machine — toolchain, global agent skills, ARTEMIS and its model, GitHub CLI, the Nimble/Tev1/Laya/Jev decision model. Use when setup-env reports a failure or a tool is missing.
version: 3.3.0
---

# Onboarding

## Part 1 — Environment setup

Run the doctor. It checks everything below, offers fixes, and prints what it can't do for you.

```powershell
powershell -ExecutionPolicy ByPass -File scripts/setup-env.ps1   # Windows
```
```bash
bash scripts/setup-env.sh                                         # macOS / Linux
```

| Component | Requirement |
| --- | --- |
| JDK | 21, `JAVA_HOME` set |
| Android SDK | API 37, Build-Tools 36.1.0, `ANDROID_HOME` set, `platform-tools` on `PATH` |
| NDK / CMake *(if used)* | set `NDK_VERSION` in `agent-kit.env` |
| Submodules *(if any)* | `git submodule update --init` |
| Git hooks | `git config core.hooksPath .githooks` |
| Global skills | `global_skills/*` copied to the user's global skill folders (see below) |
| GitHub CLI | `gh`, logged in (`gh auth login`) with push access to the repo |
| Python / uv | Python 3.10+, `uv` |
| ARTEMIS | cloned, MCP registered with your agent, Gemini key in its `.env` |
| Ollama *(optional)* | local Qwen3-VL for ARTEMIS — quota fallback, or the only model |
| Decision model *(optional)* | Nimble or Tev1 (Ollama ≥ 0.35.0) or Laya (pip), plus optional hosted Jev — recommended by hardware, user's choice |
| Device | Android phone with USB debugging authorised (`adb devices -l` shows `device`) |

### Global skills — ask the user to install them

Some skills are global: they live in each developer's own skill folders and work in every
project. The repo keeps the shared copy in `global_skills/` (now: `nimble`). No agent reads that
folder directly, so a new machine has none of them until they are copied.

Ask the user to install them, then on a yes run (Git Bash on Windows; the doctor offers the same):

```bash
bash global_skills/install.sh   # re-run after a pull that changes global_skills/
```

It copies each skill folder to `~/.claude/skills/` (Claude Code), `~/.gemini/skills/`
(Gemini CLI / Antigravity) and `~/.agents/skills/` (Codex and other agents), and runs the skill's
own `install.sh` (`nimble`: `~/.nimble/` commands and Claude Code hooks). On a no, tell the user
which skills are missing. Restart the agent afterwards so it loads them.

### ARTEMIS

ARTEMIS lives in its own clone, not in this repo. The doctor looks for it at `$ARTEMIS_HOME`, then
at `../artemis` next to this repo.

```bash
git clone https://github.com/google/artemis.git ../artemis
cd ../artemis
./start.sh                               # Windows: .\start.bat — installs uv deps, adb, scrcpy
uv run artemis mcp --install claude      # registers the MCP server + rules with Claude Code
```

Put `GEMINI_API_KEY=` (from https://aistudio.google.com/apikey) in `../artemis/.env`, then
restart your agent so it picks up the MCP server. Verify with `mobile_diagnose` — its `verdict`
must be `ready` or `degraded`. For other agents use `--install <client>` (`all`, `codex`,
`antigravity`, …).

### ARTEMIS model: Gemini or local Qwen

ARTEMIS reasons over screenshots, so a local model must be **vision + tool-calling** — text-only
models (e.g. `qwen2.5-coder`) cannot drive it at all. The doctor sizes a Qwen3-VL model to your
GPU and creates `qwen3-vl-artemis:<tier>` in Ollama:

| Usable VRAM (Apple Silicon: ~⅔ of RAM) | Base model |
| --- | --- |
| ≥ 24 GB | `qwen3-vl:30b-a3b-instruct` |
| 10–24 GB | `qwen3-vl:8b-instruct` |
| 6–10 GB | `qwen3-vl:4b-instruct` (context 16K) |
| < 6 GB | none — stay on Gemini |

`qwen3-vl-artemis:<tier>` is that base model with `num_ctx 32768` and `temperature 0`. Both
matter: at Ollama's default 256K context the 8B model spilled to CPU (46 GB, ~77 s/step on a
16 GB GPU); capped, it runs fully on GPU at 0.5–6 s/step. Use `-instruct`, not the default
thinking build, which emits thousands of reasoning tokens per step.

Pick the mode — the doctor asks, or switch any time:

```bash
python scripts/artemis_model.py gemini   # Gemini; local Qwen takes over on quota / 429 errors
python scripts/artemis_model.py qwen     # local only — no Gemini calls, no quota
python scripts/artemis_model.py status
```

Non-interactive: `setup-env.ps1 -ArtemisModel gemini|qwen` / `ARTEMIS_MODEL=… setup-env.sh`.
The script never edits the ARTEMIS repo: it writes `~/.agent-kit-artemis/artemis.active.jsonc`
(generated from ARTEMIS's own config) and sets `ARTEMIS_ARTEMIS_JSONC`, `OPENAI_BASE_URL` and
`OPENAI_API_KEY` in the ARTEMIS `.env`. The first run changes `.env`, so restart ARTEMIS once
(`uv run artemis stop`, then reconnect the MCP server); later switches apply to the next task.

Qwen-only mode works for short, explicit repro/verify flows (say which screen the app is on and
list the steps) but loops on open-ended navigation — use Gemini for exploration. It also turns off
ARTEMIS's Gemini-only features (object detector, step summarizer, history compression), so keep
sessions short. If a local run seems to use Gemini, check `stdout.log` for
`Failed to get operator LLM`: a blank `OPENAI_API_KEY=` makes ARTEMIS silently fall back.

## Part 1b — Decision model for agents: Nimble / Tev1 / Laya (local) and Jev (hosted)

Optional. A System One model answers yes/no and pick-one questions so agents need not read long
text: the hooks in `scripts/` (through `scripts/nimble.sh`) and the `nimble` skill
(`global_skills/nimble/SKILL.md` — how to use it, Jev rules, `jgl`, unloading). Without a model every
hook stays silent.

The doctor (step 8b; `-DecisionModel` / `DECISION_MODEL=` for no prompt) checks the PC,
**recommends**, and lets the user **choose**. By hand: check the PC, recommend, then ask the user:

```powershell
nvidia-smi --query-gpu=name,memory.total --format=csv,noheader   # GPU and VRAM (Windows/Linux)
(Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB # RAM (Windows)
ollama --version                                                 # needs 0.35.0+ for Nimble and Tev1
```

| Hardware | Recommend | Local install |
| --- | --- | --- |
| GPU ≥ 12 GB VRAM (Apple Silicon ≥ 18 GB unified) | **Nimble + Jev** | `ollama pull nimble` (~9 GB VRAM loaded; reads ~6K tokens; 55/60 tone, 10/10 log questions, ~80 ms warm) |
| GPU 6–12 GB | **Tev1 4B + Jev** | `ollama pull tev1:4b` (~4.7 GB loaded; reads ~1.5K tokens; 55/60 tone, 10/10 logs, ~85 ms); sets `NIMBLE_MODEL=tev1:4b`/`NIMBLE_MAX_BYTES=3600` |
| GPU 2–6 GB, or no GPU but ≥ 8 GB RAM | **Tev1 0.8B + Jev** | `ollama pull tev1:0.8b` (~0.9 GB; reads ~1.5K tokens; 46/60 tone, 10/10 logs, ~45 ms on GPU; CPU untested); sets `NIMBLE_MODEL=tev1:0.8b`/`NIMBLE_MAX_BYTES=3600` |
| Less than that | **Jev only** | none; sets `NIMBLE_LOCAL=0` |

Laya (`~/laya-env`, pip, `127.0.0.1:8000`, last ~512 tokens, 46/60 tone) is still offered by hand
(`l`), but Tev1 0.8B matches its tone score with 4x the window and needs only Ollama.
Numbers: RTX 5080, 2026-09-30, on the app this kit came from: 60 tone samples as one choice question plus 10
Gradle/test-log yes/no questions. Tev1 weights: Together AI, licence not final.

**Jev is optional and paid** (TypeSafe, per input token). The doctor asks for the key (Enter
skips; stored in `~/.config/typesafe/api_key`, never in the repo) and offers the TypeSafe skill
(`claude plugin install typesafe@typesafe-ai`; Gemini: `npx skills add typesafe-ai/skills --skill typesafe-ai`).

Nimble and a local ARTEMIS `qwen3-vl` model do not both fit under ~18 GB VRAM; Ollama swaps them.
Tev1 4B and `qwen3-vl:4b` fit together in ~10 GB.

The `nimble` skill comes from the global skills step above (`bash global_skills/install.sh`).
After the model works, check it with
`echo hi | bash ~/.nimble/nimble-ask yesno "Is this a greeting?"`.
