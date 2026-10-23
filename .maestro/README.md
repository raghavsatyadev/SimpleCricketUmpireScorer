# .maestro — replayable UI flows

Maestro (2.x) replays a recorded flow with no LLM, so a proven flow costs no tokens.

Record a flow once an ARTEMIS run has proven it:

1. Connect the device and open the app on the start screen.
2. `maestro studio` and click through the flow (or write YAML by hand).
3. Save it as `.maestro/<flow-name>.yaml`, starting with `appId: io.github.raghavsatyadev.scus`.
4. Replay: `maestro test .maestro/<flow-name>.yaml` (whole folder: `maestro test .maestro`).

Tips: prefer `tapOn: "<text or id>"` over coordinates; add `assertVisible` at the end so the flow
fails loudly; use `clearState` first for a clean start. Record the flow's purpose in
`.agents/skills/android-device-test/flows.md`.
