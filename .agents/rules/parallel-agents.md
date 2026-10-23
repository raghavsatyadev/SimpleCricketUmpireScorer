---
trigger: model_decision
description: When the user asks for two or more agents to work at the same time, each in its own git worktree (Claude Code `isolation: "worktree"`, or worktrees made by hand).
---

# RULE: PARALLEL AGENTS IN WORKTREES

Run parallel agents only when the user asks for them (see AGENTS.md, "How to work").

## Before the agents start

1. **Pin the base commit.** Worktrees have been created at a commit five commits behind
   `migration-cmp`, without the files the task needed. Put the base in every agent prompt:
   "Run `git log -1`. If HEAD is not `<sha>`, run `git reset --hard <sha>` before you start."
   Then check each worktree yourself. Do not trust what one agent says about the other agents:
   ```bash
   for d in .claude/worktrees/*/; do git -C "$d" merge-base --is-ancestor <sha> HEAD && echo "OK $d" || echo "STALE $d"; done
   ```
2. **Cap Gradle memory for three or more parallel builds.** The project `gradle.properties` asks
   for 6 GB Gradle + 6 GB Kotlin daemon per build. Four builds need ~48 GB. Create
   `~/.gradle/gradle.properties` (it overrides the project file, which stays unchanged):
   ```properties
   # TEMPORARY parallel-agent cap. DELETE THIS FILE when the wave ends.
   org.gradle.jvmargs=-Xmx3072M -Dkotlin.daemon.jvm.options="-Xmx3072M"
   org.gradle.parallel=false
   org.gradle.workers.max=3
   ```
   `GRADLE_OPTS=-Xmx…` does not work as a cap: `org.gradle.jvmargs` wins for the daemon, and the
   Kotlin daemon ignores it.
3. **Give one device to one agent.** Two agents that install to the same phone overwrite each
   other's APK and permission grants.

## When the wave ends

- Delete `~/.gradle/gradle.properties` if you made it. It is machine-wide and slows every
  later build, Android Studio too.
- Remove the worktrees of merged or stopped branches (`git worktree remove <path>`, then
  `git worktree prune`). Ask before removing a worktree that has uncommitted changes.
