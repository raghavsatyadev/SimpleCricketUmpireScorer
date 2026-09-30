---
name: fix-issue
description: Fix a GitHub issue end to end — read it, reproduce on a device with ARTEMIS, fix with a regression test, verify on the device, push a fix branch and open a PR against migration-cmp. Use when given a GitHub issue link or number.
argument-hint: <github-issue-url-or-number>
---

Fix GitHub issue: $ARGUMENTS

Follow Part 2 of [project-onboarding-setup](../project-onboarding-setup/SKILL.md)
exactly, reading the rules and skills it links as you reach each step.

Before step 1:

- `gh api user -q .login` must succeed. If not, stop and ask the user to run `gh auth login`.
- Call `mobile_diagnose`. If ARTEMIS tools are missing entirely, stop and point the user at Part 1
  of the onboarding skill (`scripts/setup-env.ps1` / `setup-env.sh`). If more than one device is
  connected, ask which one to use.

Stop and report back — don't push — if the bug won't reproduce, if the fix needs a product
decision, or if device verification still shows the bug.

End with the PR URL and a short summary: root cause, fix, test, device evidence. Never merge or
approve the PR.
