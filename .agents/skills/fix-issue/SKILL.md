---
name: fix-issue
description: Fix a GitHub issue end to end — device repro, fix with a test, device verification, PR. Use when given an issue link or number.
argument-hint: <github-issue-url-or-number>
---

Fix GitHub issue: $ARGUMENTS

## Before you start

- `gh api user -q .login` must succeed. If not, stop and ask the user to run `gh auth login`.
- Call `mobile_diagnose`. If ARTEMIS tools are missing, stop and point the user at
  [project-onboarding-setup](../project-onboarding-setup/SKILL.md). If more than one device is
  connected, ask which one to use.

## Steps

1. **Read the issue** — `gh issue view <number> --comments`. Note expected vs actual, repro steps,
   affected area.
2. **Branch** `fix/<number>-<slug>` per [branch-pr-policy](../../rules/branch-pr-policy.md).
3. **Reproduce on the device** before touching code — see
   [android-device-test](../android-device-test/SKILL.md). Build and install the current code,
   then reproduce with ARTEMIS. Keep notes and screenshots in `tmp/YYYY-MM-DD-issue-<number>.md`.
4. **Fix** — follow [cmp-best-practices](../cmp-best-practices/SKILL.md) and any project rule
   in `.agents/rules/` that covers the area. Add a unit test that fails against the old behaviour.
5. **Format, build, test** — `bash scripts/ci-local.sh --fix`, then the checks in
   [AGENTS.md](../../../AGENTS.md#commands) (unit tests; screenshot tests if UI changed).
6. **Verify on the device** — `./gradlew :androidCMP:installDebug`, then re-run the *same*
   ARTEMIS reproduction.
7. **Commit, push, open the PR** — commit `fix: <summary> (closes #<number>)`. PR body: `Closes
   #<number>`, root cause, what changed, the test added, device verification (device model,
   ARTEMIS steps, before/after). Don't commit screenshots or device serials.

Stop and report back — don't push — if the bug won't reproduce, if the fix needs a product
decision, or if device verification still shows the bug.

End with the PR URL and a short summary: root cause, fix, test, device evidence.

After a human merges: `git checkout migration-cmp && git pull && git branch -d fix/<number>-<slug> && git fetch --prune`.
