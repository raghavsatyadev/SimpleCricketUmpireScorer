---
name: local-model
description: Turn on the local decision model first (bash ~/.local-model/lm-on), then rely on it. lm-ask gives a yes/no or pick-one verdict on a long log, test output or file instead of reading it; jgl finds code when you do not know its name, rg when you do. Unload the model when done.
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
`JEV_API_KEY`, `JEV_MODEL` (default `jev-latest`), `JEV_MAX_BYTES` (default 100000).

## Usage log

`lm-ask` and `jgl` add one tab-separated line per call to `~/.local-model/usage.log`: time, tool,
milliseconds, bytes (the input for `lm-ask`, the output for `jgl`), answer or result count,
working directory, question or arguments. `LOCAL_MODEL_USAGE_LOG=0` turns it off;
`LOCAL_MODEL_USAGE_LOG_FILE` moves it.
