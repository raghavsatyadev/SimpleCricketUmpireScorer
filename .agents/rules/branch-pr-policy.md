---
trigger: always_on
description: Never push to migration-cmp or master; branch, push and open a PR against migration-cmp.
---

# RULE: BRANCH & PR POLICY (STRICT)

**Direct pushes to `migration-cmp` or `master` are strictly prohibited.**

Every change — whether feature, bug fix, refactoring, documentation, skill update, or configuration — MUST follow the branch-and-PR workflow.

## Strict Rules

1. **Never Push Directly to Mainline Branches**:
   - `git push origin migration-cmp` and `git push origin master` are **strictly forbidden** for agents.
   - Any attempt to push directly to `migration-cmp` or `master` violates repository integrity policy.

2. **Always Create a Dedicated Branch**:
   - Every unit of work must start on a feature/fix/chore branch cut from an up-to-date `origin/migration-cmp`:
     ```bash
     git fetch origin
     git checkout -b <type>/<slug> origin/migration-cmp
     ```
   - Standard branch prefixes:
     - `feat/<slug>` — new features or functional enhancements
     - `fix/<number>-<slug>` — bug fixes (linked to an issue)
     - `refactor/<slug>` — code restructuring or cleanup
     - `docs/<slug>` — documentation, rules, or skill updates
     - `chore/<slug>` — build, dependencies, or maintenance tasks

3. **Open a Pull Request for Merging**:
   - Push your branch to origin: `git push -u origin <branch-name>`.
   - Open a PR targeting the base branch `migration-cmp`:
     ```bash
     gh pr create --base migration-cmp --title "<type>: <summary>" --body-file tmp/pr_body.md
     ```
   - Always use `--body-file` (never inline `--body`) to prevent shell escape font corruption.

4. **Human Review Only**:
   - **Never auto-approve or auto-merge PRs.**
   - Commands like `gh pr merge` and `gh pr review --approve` are prohibited for agents.
   - Stop and hand off the PR URL to the user for human review and merge.
