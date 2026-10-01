---
name: local-model
description: Turn on the local decision model first (bash ~/.local-model/lm-on), then rely on it. lm-ask gives a yes/no or pick-one verdict on a long log, test output or file instead of reading it; jgl finds code when you do not know its name, rg when you do; lm-rank picks the files worth opening from many rg hits; lm-diffcheck runs the project's rule checks over a diff. Unload the model when done.
---

# Local model: cheap verdicts instead of long reads

Commands live in `~/.local-model/` (Windows: `%USERPROFILE%\.local-model\`); run them with bash. They work
the same for Claude Code and Gemini/Antigravity agents.

## Turn on the local decision model first, then rely on it

1. **Before any lm-ask or jgl call**, turn on the local model in Ollama:
   `bash ~/.local-model/lm-on`. It returns at once; it starts Ollama if it is not running, and loads the model and the jgl proxy in the
   background. In Claude Code the global SessionStart hook (added by `global_skills/install.sh`) does
   this for you; run it anyway if `curl -s 127.0.0.1:11434/api/ps` does not list the model.
   Gemini/Antigravity has no such hook: always run it.
2. **Then use these tools before the usual ones:**
   - Looking for code and you do not know the exact name → `jgl` first, then `rg` to confirm.
   - Build log, test output, logcat or a file over ~200 lines, and you need a verdict →
     `lm-ask` first. Read the text only when it answers below 0.9 or exits 2.
   - Exact identifier or error string → `rg` directly (faster than jgl, same result).
   - `rg` hits in more than ~5 files → pipe them to `lm-rank "<task>"` and open its top 2-3.
   - Before a review of your own branch → `lm-diffcheck` (the `/code-review` hook runs it too).
3. At the end of the task: `~/.local-model/lm-off "${LOCAL_MODEL_NAME:-nimble}"`.

| Backend | Where | Cost | Reads |
| --- | --- | --- | --- |
| Nimble | Ollama on this PC (`LOCAL_MODEL_URL`, default `127.0.0.1:11434`) | free | last ~6K tokens |
| Tev1 | Ollama on this PC (`LOCAL_MODEL_NAME=tev1:4b` or `tev1:0.8b`, `LOCAL_MODEL_MAX_BYTES=3600`) | free | last ~1.5K tokens |
| Laya | pip server on this PC (`LOCAL_MODEL_URL=http://127.0.0.1:8000`, `LOCAL_MODEL_NAME=laya`) | free | last ~400 tokens |
| Jev | TypeSafe hosted (`api.typesafe.ai`), optional | paid per input token | last ~28K tokens |

## Ask about text

```bash
~/.local-model/lm-ask yesno  "Does this output show the build passed?"            < log.txt
~/.local-model/lm-ask choice "What kind of failure is this?" \
    kotlin="Kotlin compiler errors (e: lines)" deps="Could not resolve a dependency" \
    oom="OutOfMemoryError or daemon died" other="none of these"                  < log.txt
some-command 2>&1 | ~/.local-model/lm-ask yesno "Did any test fail?"
```

- Output: `<answer> <probability> <backend>`, for example `yes 0.97 local` or `kotlin 0.99 jev`.
- Exit 2: no backend answered. Read the text the normal way.
- Only the **tail** is sent. Pipe `grep`/`tail` first so the part that matters is at the end.

## When Jev is used (it costs money)

`lm-ask` goes to Jev **only** when all of these hold; otherwise it stays local:

1. A TypeSafe key is set (`JEV_API_KEY`, or `~/.config/typesafe/api_key`). No key, no Jev.
2. The input is longer than the local budget (`LOCAL_MODEL_MAX_BYTES`), or no local model answered.
3. The call allows it (`LOCAL_MODEL_ALLOW_JEV`, default 1 for `lm-ask`). Hooks never use Jev.

So only ask about a **very long** file when the other choice is reading all of it yourself. Cut
it first (`grep -n`, `tail -n 400`) when a slice is enough: then it stays local and free. Force
local for one call with `LOCAL_MODEL_ALLOW_JEV=0`. Every Jev call is logged with its input tokens in
`~/.config/typesafe/usage.log`.

## Long build and test output is shortened for you (Claude Code)

The global PostToolUse hook `lm-gate.mjs` runs after a Bash build, test or lint command (`gradlew`,
`gradle-agent.sh`, `ci-local.sh`, `npm test`, `pytest`, `cargo test`, `am instrument` ...) whose
output is over 200 lines (`LOCAL_MODEL_GATE_LINES`). It saves the full output in
`~/.local-model/gate/` and asks the local model whether the run passed:

- Passed (yes ≥ 0.9 and a result line such as `BUILD SUCCESSFUL`): you see `[lm-gate] PASSED`, the
  result lines and the last 5 lines.
- Otherwise, with failure lines found: you see `[lm-gate] Not a clean pass`, each failure line with
  the lines after it (assertion message, first stack frames) and the last 20 lines.
- No model, or nothing it can shorten: the output is left as it was.

Every shortened output ends with `Full output (N lines): <path>`: read that file when the short
view is not enough. Turn it off for one command with `LM_GATE=0 <command>`, or for the session
with `LOCAL_MODEL_GATE=0`. Quote the real result line it shows when you report a pass.

## Pick the files worth opening from many hits

```bash
rg -n "Prompt" app/src | ~/.local-model/lm-rank "where is the tone prompt built"     # top 3 files
~/.local-model/lm-rank "which note covers the jlink failure" .remember/*.md memory/*.md   # whole files
```

Prints `<P(relevant)> <path> (<hits> hits)` for the best 3 (`--top N`, `--all`). For each file the
model reads the hit lines with a few lines around them (a whole file: its best 3 KB chunk). On 8
code questions in a test app the right file was in its top 3 every time; `rg` ordered by hit count
got 3 of 8. About 4-8 s for 30-40 files. Exit 2: no score, read the hits yourself. Do not pass
files that hold secrets.

## Rule checks over a diff before a review

```bash
~/.local-model/lm-diffcheck              # this branch + uncommitted + new files, against its base
git diff | ~/.local-model/lm-diffcheck - # any diff
```

Asks each changed file's diff the project's yes/no questions in `.agents/diff-checks.txt` (one per
line, `<glob> | <question>`, phrased so that yes is the problem). Prints
`<P(yes)> <file>: <question>` for every yes ≥ 0.7, or `no flags`. A rule that depends only on the
path (a module that must not change) is written `<glob> | !<message>`: any change there is
flagged without asking the model, which is unreliable for such rules. The base is `BASE_BRANCH` from
`agent-kit.env`, else `origin/HEAD`. In Claude Code the global UserPromptExpansion hook runs it
when the user types `/code-review`, `/review`, `/security-review` or `/simplify`, and adds the
flags to the prompt. A flag is a lead to check, not a finding. No checks file: it does nothing.

## Hooks that run on their own (Claude Code)

- **Loop stop** (`lm-loop.mjs`). The same Bash command failing 3 times in a row with the same error
  and nothing changed in between (no file edit, no other command) → a warning; the next identical
  run is blocked once. With edits in between, the model is asked whether the 3 errors are the
  same; yes ≥ 0.9 → a warning that the edits are not reaching the cause. When you see
  `[lm-loop]`: stop retrying, read the error, change the approach or ask the user.
- **Request size hint** (`lm-route.mjs`). The model sizes each prompt; only a sure answer (≥ 0.9)
  adds a line. Quick request → answer directly, no subagents. Multi-step → search-only subagents
  can use `haiku` or `sonnet`. On 34 real prompts its sure answers were right 14 of 14.

## Find code by meaning

```bash
~/.local-model/jgl --files "where is the database migration defined"     # file names only
~/.local-model/jgl "which code parses the login response" app/src/        # snippets, one folder
```

`jgl` is `jg` (jevgrep) with the snippet judging done by the local model; nothing leaves the PC.
About 9 s per query. Use it when you do not know the right keyword; use `rg` when you do (exact
names, error strings). Hosted `jg` (jevgrep.com) is only for PCs with no local model.

## When to trust an answer

- Act on it alone only when the probability is ≥ 0.9 (yesno ≥ 0.9 or ≤ 0.1). Between those,
  read the relevant part yourself.
- Always offer an `other` label in `choice`, so it is not forced into a wrong class.
- A "passed" verdict is not evidence for the user. Quote the real line (`BUILD SUCCESSFUL`, the
  test count) before claiming done.
- Never send secrets, tokens or `.env` content, least of all to Jev.

## Free the GPU when done

Ollama unloads the model 10 minutes after the last call (`LOCAL_MODEL_KEEP_ALIVE`). When the task
that used it is finished, unload it now so the GPU frees ~9 GB and cools down:

```bash
~/.local-model/lm-off "${LOCAL_MODEL_NAME:-nimble}"  # just the decision model; Ollama keeps serving other models
```

Claude Code runs this on session end through the global hook. Gemini/Antigravity has no such hook:
run it yourself at the end of the task.

## Settings

`LOCAL_MODEL_URL`, `LOCAL_MODEL_NAME`, `LOCAL_MODEL_MAX_BYTES`, `LOCAL_MODEL_KEEP_ALIVE` (default `10m`),
`LOCAL_MODEL_TIMEOUT` (default 30 s), `LOCAL_MODEL_LOCAL=0` (Jev only), `LOCAL_MODEL_HOOKS=0` (all off),
`LOCAL_MODEL_GATE=0` (output gate off), `LOCAL_MODEL_GATE_LINES` (default 200), `LOCAL_MODEL_LOOP=0`
(loop stop off), `LOCAL_MODEL_LOOP_MAX` (default 3), `LOCAL_MODEL_ROUTE=0` (size hint off),
`LM_DIFFCHECK_MIN` (default 0.7),
`JEV_API_KEY`, `JEV_MODEL` (default `jev-latest`), `JEV_MAX_BYTES` (default 100000).

## Usage log

`lm-ask` and `jgl` add one tab-separated line per call to `~/.local-model/usage.log`: time, tool,
milliseconds, bytes (the input for `lm-ask`, the output for `jgl`), answer or result count,
working directory, question or arguments. `LOCAL_MODEL_USAGE_LOG=0` turns it off;
`LOCAL_MODEL_USAGE_LOG_FILE` moves it. `lm-gate` adds a line for each output it shortens: bytes
in, then `passed|failed <P(yes)> <bytes out>`, so the savings can be summed. `lm-rank`, `lm-diffcheck`,
`lm-loop` and `lm-route` add their own lines (tool name in the second column).
