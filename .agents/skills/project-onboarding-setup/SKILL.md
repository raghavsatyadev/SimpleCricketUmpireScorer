---
name: project-onboarding-setup
description: Developer environment setup (toolchain, ARTEMIS, GitHub CLI, local LLM fallback, Nimble/Laya/Jev decision model) and the end-to-end GitHub bug → fix → device verification → PR workflow.
version: 3.1.0
---

# Onboarding & GitHub Bug Resolution

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
| GitHub CLI | `gh`, logged in (`gh auth login`) with push access to the repo |
| Python / uv | Python 3.10+, `uv` |
| ARTEMIS | cloned, MCP registered with your agent, Gemini key in its `.env` |
| Ollama *(optional)* | local Qwen3-VL for ARTEMIS — quota fallback, or the only model |
| Decision model *(optional)* | Nimble (Ollama ≥ 0.35.0) or Laya (pip), plus optional hosted Jev — recommended by hardware, user's choice |
| Device | Android phone with USB debugging authorised (`adb devices -l` shows `device`) |

ktfmt is **not** a prerequisite — `scripts/ci-local.sh` downloads the version CI uses.

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

Qwen-only mode, verified end to end on a Galaxy S25 Ultra:

- **Works for short, explicit repro/verify flows.** Tell it which screen it is on and list the
  steps; it drove a text-entry flow (open field, replace text, wait, report) in 4 turns.
- **Weak at open-ended navigation.** Told to "switch to the X tab" while already
  there (keyboard hiding the tab bar), it swiped up for 12+ turns. Use Gemini (or `Pro`) for
  exploration.
- Some ARTEMIS features only exist on Gemini, so qwen mode switches them off: the one-shot
  object detector (explorer runs in `pro` mode instead), the Flash step summarizer, history
  compression, and video analysis. Long sessions therefore aren't compressed — keep them short.
- A blank `OPENAI_API_KEY=` in the ARTEMIS `.env` (as `.env.example` ships) makes the Ollama client
  fail and ARTEMIS **silently** falls back to hardcoded Gemini — the script sets it to `ollama`.
  Check `stdout.log` for `Failed to get operator LLM` if a local run seems to use Gemini.

## Part 1b — Decision model for agents: Nimble / Laya (local) and Jev (hosted)

A System One model answers quick yes/no and pick-one questions so agents need not read long text:
the hooks in `scripts/` (`gradle-agent.sh` build hint, `route-prompt.sh`, `check-done.sh`, all
through `scripts/nimble.sh`) and the `nimble` skill (`nimble-ask`, `jgl`, `nimble-off`). All of
it is optional: without a model every hook stays silent. It works the same for Claude Code and
Gemini/Antigravity; only the automatic hooks are Claude Code only.

The doctor (step 8b; `-DecisionModel` / `DECISION_MODEL=` for no prompt) checks the PC,
**recommends**, and lets the user **choose**. If you set it up by hand as an agent, check the PC
first, recommend, then ask the user:

```powershell
nvidia-smi --query-gpu=name,memory.total --format=csv,noheader   # GPU and VRAM (Windows/Linux)
(Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB # RAM (Windows)
ollama --version                                                 # needs 0.35.0+ for Nimble
```

| Hardware | Recommend | Local install |
| --- | --- | --- |
| GPU ≥ 12 GB VRAM (Apple Silicon ≥ 18 GB unified) | **Nimble + Jev** | `ollama pull nimble` (~9 GB VRAM loaded; reads ~6K tokens; 56/60 tone, every log question right, 50–110 ms warm) |
| GPU 4–12 GB, or no GPU but ≥ 16 GB RAM | **Laya + Jev** | `~/laya-env` (`pip install torch` + `pip install laya`) on `127.0.0.1:8000`; ~1.3 GB, ~30 ms, last 512 tokens only; sets `NIMBLE_URL`/`NIMBLE_MODEL=laya`/`NIMBLE_MAX_BYTES=1800` |
| Less than that | **Jev only** | none; sets `NIMBLE_LOCAL=0` |

**Jev is optional and paid** (TypeSafe, `api.typesafe.ai`, `jev-latest`, charged per input token,
32K-token state). Rules, enforced in `scripts/nimble.sh`:

- It is used only when a TypeSafe key is set (`JEV_API_KEY` or `~/.config/typesafe/api_key`, never
  in the repo) **and** the text is longer than the local model's budget, or no local model answers.
- The hooks never use it (`NIMBLE_ALLOW_JEV=0`); only `nimble-ask`, which an agent runs on purpose
  for a very long file it would otherwise read whole. Cut the text first when a slice is enough.
- Every Jev call is logged with its input tokens in `~/.config/typesafe/usage.log`.
- The doctor asks for the key (Enter skips) and offers the TypeSafe skill
  (`claude plugin install typesafe@typesafe-ai`; Gemini: `npx skills add typesafe-ai/skills --skill typesafe-ai`).

**Search by meaning:** with Nimble or Laya, `~/.nimble/jgl` runs `jg` (`npm install -g
@remotehost/jg`, needs ripgrep) with the local model judging snippets; nothing leaves the PC. On
the repo it came from it put the right file in the top 3 for 9 of 10 questions (rg keyword search: 2 of 10),
at ~9 s per query. Hosted `jg` (jevgrep.com login) is for PCs without a local model.

**Free the GPU:** Ollama unloads the model 10 min after the last call (`NIMBLE_KEEP_ALIVE`).
Claude Code also runs `tools/nimble-skill/nimble-off nimble` on session end (`.claude/settings.json`).
Gemini/Antigravity has no hook: run `~/.nimble/nimble-off nimble` when the task is done. It
unloads only the decision model; Ollama keeps serving other models and other machines.

Nimble and a local ARTEMIS `qwen3-vl` model do not both fit under ~18 GB VRAM; Ollama swaps them.
Laya on CPU or Apple `mps` is untested. Turn every hook off with `NIMBLE_HOOKS=0`.

**Global skill: ask first.** After the model works, ask the user whether to install the `nimble`
skill globally, so agents in *every* project (Claude Code and Gemini/Antigravity) can use it. Only
on a yes, run `bash tools/nimble-skill/install-global.sh` (the doctor offers the same). It puts the
commands in `~/.nimble/` and `SKILL.md` in `~/.claude/skills/nimble/` and `~/.gemini/skills/nimble/`.
Check it with `echo hi | bash ~/.nimble/nimble-ask yesno "Is this a greeting?"`.

## Part 2 — GitHub bug → PR

> [!IMPORTANT]
> **STRICT RULE: NEVER PUSH DIRECTLY TO the base branch OR `master`.**
> Direct pushes to the base branch (`BASE_BRANCH`) or `master` are strictly prohibited for all changes (fixes,
> features, refactors, docs, or chores). Always branch off `origin/$BASE_BRANCH`, commit, push the
> branch to origin, and open a PR with base the base branch (`BASE_BRANCH`).

Input: an issue link, e.g. `https://github.com/<owner>/<repo>/issues/123`.

1. **Read the issue** — `gh issue view <number> --comments`. Note expected vs actual, repro steps,
   affected area.
2. **Branch** off an up-to-date the base branch (`BASE_BRANCH`) (the base is always the base branch (`BASE_BRANCH`)):
   ```bash
   git fetch origin
   git checkout -b fix/<number>-<slug> origin/$BASE_BRANCH
   ```
3. **Reproduce on the device** before touching code — see
   [android-device-test](../android-device-test/SKILL.md). Build and install the current code,
   then reproduce with ARTEMIS (`mobile_get_device_state`, `mobile_run_task`). Keep screenshots
   and notes in `tmp/YYYY-MM-DD-issue-<number>.md` (git-ignored, see [workspace-cleanup](../workspace-cleanup/SKILL.md)).
   If it won't reproduce, stop and report back instead of guessing.
4. **Fix** — follow the project rules in `.agents/rules/` and [cmp-best-practices](../cmp-best-practices/SKILL.md).
   Add a unit test that fails against the old behaviour.
5. **Format, build, test**:
   ```bash
   bash scripts/ci-local.sh --fix
   ./gradlew :androidCMP:testDebugUnitTest --no-daemon --console=plain
   ./gradlew :androidCMP:validateScreenshotTest --no-daemon --console=plain
   ```
6. **Verify on the device** — `./gradlew :androidCMP:installDebug`, then re-run the *same*
   ARTEMIS reproduction. A green unit test is not device verification.
7. **Commit, push, open the PR** (the pre-push hook re-runs the CI checks):
   ```bash
   git add <files>
   git commit -m "fix: <summary> (closes #<number>)"
   git push -u origin fix/<number>-<slug>
   # Always write PR description to a git-ignored file (e.g. in tmp/)
   gh pr create --base $BASE_BRANCH --title "fix: <summary>" --body-file tmp/pr_body.md
   ```
   Body: `Closes #<number>`, root cause, what changed, the test added, and the device
   verification (device model, ARTEMIS steps, before/after result). Don't commit screenshots or
   device serials.

   **CRITICAL — Never use inline `--body "..."` (Shell Escaping & Corrupted Fonts):**
   NEVER pass multiline or formatted markdown inline via `--body "..."`. Shells (especially
   Windows PowerShell) interpolate backslashes before backticks and word boundaries as control
   characters (`\t` becomes a literal tab, `\b` a backspace, `\a` a bell character), stripping
   monospace backticks and littering PR descriptions and comments with raw backslashes and weird
   fonts. Always write the markdown to a file first and pass `--body-file <file>` to
   `gh pr create`, `gh pr edit`, and `gh pr comment`.
8. **Stop.** Give the user the PR URL. **Never** run `gh pr merge` or `gh pr review --approve` —
   only human maintainers approve and merge.

After a human merges (the remote branch auto-deletes):
`git checkout $BASE_BRANCH && git pull && git branch -d fix/<number>-<slug> && git fetch --prune`.
