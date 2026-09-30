---
name: workspace-cleanup
description: Organization of temporary test artifacts in /tmp and persistent project data in /memory, with strict user-command-only cleanup protocols.
version: 1.0.0
---

# Workspace Cleanup & Data Organization

This project enforces a strict separation between ephemeral AI-generated test artifacts and
persistent project lifecycle data to keep the repository lightweight and disk space controlled.

## Directory Separation

Both directories are located at the workspace root and are **strictly untracked** in `.gitignore`:

```text
<repo>/
├── tmp/                     # Ephemeral AI-generated artifacts (safe to delete anytime)
│   ├── artifacts/           # Test screenshots, recordings, dumps
│   ├── ui-dump.xml          # Live UI hierarchy inspection
│   └── scratch/             # Temporary debug scripts and test outputs
└── memory/                  # Long-term persistent project data (retained across tasks)
    ├── last-serial          # Resolved device serial
    └── devices/<serial>/    # Device geometry, display density, coords.tsv, device.env
```

### 1. `tmp/` (Ephemeral / Transient)
- **Contents**: Screenshots taken during testing/repro, `ui-dump.xml`, screen recordings,
  captured logcat streams, scratch scripts, interim test results, and test reports.
- **Lifecycle**: Temporary. May be wiped whenever a task is completed to prevent disk bloat.
- **Rule**: Never store files required across sessions or long-term device configurations here.

### 2. `memory/` (Persistent Project Lifecycle)
- **Contents**: Device serials (`last-serial`), resolved screen resolutions and densities,
  cached UI coordinates (`coords.tsv`), and device-specific environment variables (`device.env`).
- **Lifecycle**: Kept for the whole project lifecycle to avoid rediscovering devices or
  recalculating coordinates on each session.
- **Rule**: Never store heavy media, full logs, or transient screenshots here.

---

## Strict Rule: User-Command-Only Cleanup

> [!CAUTION]
> **NO AUTOMATIC CLEANUP**:
> Agents must **NEVER** automatically delete or clean up `tmp/` or `memory/` on their own.
> Cleanup must **ONLY** occur when the user explicitly gives a command or asks for cleanup.
> Never trigger background tasks, cron jobs, or automatic end-of-turn cleanup scripts.

---

## Cleanup Commands (On Explicit User Command Only)

When the user requests cleanup (e.g. "clean up tmp", "delete temporary test files"):

### Windows (PowerShell)
```powershell
# Remove all contents within tmp/ while keeping the directory intact
Get-ChildItem -Path tmp -Recurse | Remove-Item -Recurse -Force
```

### macOS / Linux (Bash)
```bash
# Remove all contents within tmp/ while keeping the directory intact
rm -rf tmp/*
```

### Resetting Persistent Memory (Only If User Explicitly Requests Memory Reset)
```powershell
# Only when user explicitly asks to reset device memory/coords:
Get-ChildItem -Path memory -Recurse | Remove-Item -Recurse -Force
```
