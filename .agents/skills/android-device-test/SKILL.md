---
name: android-device-test
description: On-device testing and bug reproduction for an Android app. ARTEMIS MCP tools are the primary driver; ADB and dev.sh cover install, permissions, logcat and file access.
version: 3.0.0
---

# android-device-test

ARTEMIS drives the UI; ADB handles everything below the UI. Proven prompts and commands for the
app's core flows are in [flows.md](flows.md) — start there.

## ARTEMIS (primary)

Tool basics (`mobile_diagnose` first, Flash vs Pro, device serials) are in
[artemis-mobile-testing](../../rules/artemis-mobile-testing.md). Use `mobile_get_device_state` for
ground truth and `mobile_inspect_trace` step screenshots as PR evidence. Never guess tap
coordinates — let ARTEMIS resolve targets.

Write task prompts as explicit steps: say which screen the app is already on, name the element to
tap (`'Text Input Field'`), and end with what to report. This is required when ARTEMIS runs on the
local Ollama model (it loops on vague "find the X tab" goals) and makes Gemini runs faster too.
Check which model a run used in its `stdout.log` (`ollama/…` vs `gemini-…`).

## ADB & dev.sh (supplementary)

`D=.agents/skills/android-device-test/scripts/dev.sh` · package = `APPLICATION_ID` in `agent-kit.env`

Device cache in `memory/devices/`, dumps and screenshots in `tmp/` — see
[workspace-cleanup](../workspace-cleanup/SKILL.md).

| Task | Command |
| --- | --- |
| List devices | `adb devices -l` |
| Install debug build | `./gradlew :androidCMP:installDebug` |
| Grant overlay | `bash $D grant overlay` |
| Grant accessibility | `bash $D grant a11y` |
| App logs | `adb logcat -s <Tag>` (or `bash $D log <Tag>`) |
| App files (downloaded models, seeded data) | `bash $D files` |
| Keep screen awake | `adb shell "svc power stayon true; settings put system screen_off_timeout 600000"` |
| Reset app (deletes all app data, downloads too) | `adb shell pm clear io.github.raghavsatyadev.scus` |

## Gotchas

1. **`uiautomator dump` resets accessibility services.** It destroys and rebinds every service,
   including your accessibility service, which hides the bug you're chasing. `dev.sh`
   `dump/find/tap/scroll/wait/gone` all dump — fine for setup before the assistant is running,
   never while testing it. ARTEMIS reads the tree via `com.artemis.helper` without the side effect.
2. **Windows PowerShell corrupts `adb exec-out screencap -p > file.png`** (UTF-16 redirection).
   Use `adb shell screencap -p /sdcard/s.png; adb pull /sdcard/s.png` or ARTEMIS instead.
3. **Samsung devices dim / palm-reject when idle** — set `svc power stayon true` before long runs.
4. **Wireless debugging can list one phone twice** (`adb-XXXX…` and `adb-XXXX (2)…`). ARTEMIS
   rejects the serial with a space in it. Keep one entry: `adb disconnect "<the (2) serial>"`; if
   none is left, `adb mdns services` shows the ports — `adb connect <ip>:<port>` and pass that
   `ip:port` as `device_serial`.
5. **Git Bash rewrites `/sdcard/…` paths** into Windows paths. Prefix adb commands with
   `MSYS_NO_PATHCONV=1`.
6. **Other apps can draw lookalike overlays** (keyboards, assistants). Before crediting a UI
   element to your app, check whose window it is:
   `adb shell dumpsys window windows | grep -E "Window #|package="`.
7. **`connectedDebugAndroidTest` uninstalls the app afterwards**, deleting app data.
   Run UI tests without Gradle's uninstall:
   `./gradlew :androidCMP:installDebug :androidCMP:installDebugAndroidTest`, then
   `adb shell am instrument -w -e class <TestClass> io.github.raghavsatyadev.scus.test/androidx.test.runner.AndroidJUnitRunner`
   (look for `OK (n tests)`). The Gradle task also reports FAILED even when every test passes.
8. **Reinstalling stops the overlay service.** After `installDebug`, start any overlay/accessibility
   service again and `bash $D grant a11y` again. A silent run (no rows, no log lines) looks like a
   broken fix. Before you conclude anything, check that the service is bound:
   `adb shell dumpsys accessibility | grep "Bound services"` must list your app. Some devices keep
   the service in `enabled_accessibility_services` while it is not bound.
9. **Leave auto-rotate as the user set it — off unless they ask.** `adb shell monkey … LAUNCHER 1`
   switches it on, and ARTEMIS launches apps with monkey. Launch apps with `am start` (see
   flows.md). After an ARTEMIS run, reset it. Check before and after a run:
   `adb shell settings get system accelerometer_rotation` (0 = off), restore with
   `adb shell settings put system accelerometer_rotation 0`. Who changed it:
   `adb shell dumpsys settings | grep -A8 "History (accelerometer_rotation)"`.
10. **A Room schema older than the device's database can stop writes silently** (e.g. after a
    branch switch). Stop the app, then delete the database with its WAL files:
    `adb shell "run-as io.github.raghavsatyadev.scus sh -c 'rm -f databases/<db-name>*'"`.
