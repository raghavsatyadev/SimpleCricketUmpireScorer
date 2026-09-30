# Known Flows (per app)

Fill this in for your app. Keep each flow proven: an ARTEMIS prompt that worked, plus an ADB
fallback. Helper script: `D=.agents/skills/android-device-test/scripts/dev.sh`

Write prompts as explicit steps: name the screen the app is on, name each element to tap, and end
with what to report.

## 1. <Flow name>

* **Primary (ARTEMIS):**
  ```text
  "In SCUS, open the <X> tab. Tap '<Button>'. Report the text shown in '<Label>'."
  ```
* **ADB / dev.sh fallback:**
  ```bash
  bash $D tap "desc:<Content description>"
  bash $D find "<text>"
  ```
* **Verify:** what on screen or in logcat proves the flow passed.

## 2. <Next flow>

Once a flow is stable, record it with Maestro (see `.maestro/README.md`) so it replays without an LLM.
