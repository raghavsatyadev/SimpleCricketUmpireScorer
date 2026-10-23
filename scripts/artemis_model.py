#!/usr/bin/env python3
"""Choose which model ARTEMIS drives the device with: Gemini, or a local Qwen3-VL via Ollama.

  python scripts/artemis_model.py status
  python scripts/artemis_model.py gemini   # Gemini; local Qwen takes over on quota errors
  python scripts/artemis_model.py qwen     # local Qwen only — no Gemini calls, no quota

Both modes are generated from the ARTEMIS clone's own config/artemis.jsonc, so upstream
config changes carry over; re-run after pulling ARTEMIS. The ARTEMIS clone is never edited
except for two lines in its .env (set once): ARTEMIS_ARTEMIS_JSONC points at an "active"
config file this script rewrites, and OPENAI_BASE_URL points at Ollama. The first run needs
one ARTEMIS restart (the script says so); after that a switch applies from the next task.

Options: --artemis-home PATH (default $ARTEMIS_HOME, then ../artemis next to this repo)
         --model NAME        (default: the qwen3-vl-artemis model found in Ollama)
"""

import argparse
import copy
import json
import os
import re
import sys
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
STATE_DIR = Path.home() / ".agent-kit-artemis"
ACTIVE = STATE_DIR / "artemis.active.jsonc"
OLLAMA = "http://localhost:11434"
LOCAL_FAMILY = "qwen3-vl-artemis"

# Every model field of ARTEMIS's LLMConfig (artemis/config/llm.py).
AGENT_NODES = (
    "planner", "summarizer", "operator", "operator_summarizer", "log_reader_sub_agent",
    "log_analyzer", "diagnoser", "checker", "planner_avatar", "history_analyzer_expert",
    "diagnoser_expert", "explorer", "history_analyzer", "validator_pixel_safety_net",
    "planner_validation", "output_analyzer",
)
UTILS_NODES = ("outputter", "hopper", "video_analyzer", "object_detector")


def strip_jsonc(text: str) -> str:
    """Remove // and /* */ comments and trailing commas, leaving strings untouched."""
    out, i, n, in_str = [], 0, len(text), False
    while i < n:
        c = text[i]
        if in_str:
            out.append(c)
            if c == "\\":
                out.append(text[i + 1])
                i += 1
            elif c == '"':
                in_str = False
        elif c == '"':
            in_str = True
            out.append(c)
        elif text.startswith("//", i):
            while i < n and text[i] != "\n":
                i += 1
            continue
        elif text.startswith("/*", i):
            i = text.index("*/", i) + 2
            continue
        else:
            out.append(c)
        i += 1
    return re.sub(r",(\s*[}\]])", r"\1", "".join(out))


def local_endpoint(model: str) -> dict:
    return {"provider": "ollama", "model": model, "temperature": 0.0}


def gemini_config(base: dict, model: str) -> dict:
    """Upstream config, with the local model as the default fallback."""
    cfg = copy.deepcopy(base)
    cfg["default"]["fallback"] = local_endpoint(model)
    return cfg


def qwen_config(base: dict, model: str) -> dict:
    """Every node on the local model; features that only work with Gemini are switched off."""
    cfg = copy.deepcopy(base)
    endpoint = {**local_endpoint(model), "fallback": local_endpoint(model)}
    # Write the full per-node schema (top-level "planner" + "utils") instead of
    # "default"/"nodes": the short form cannot configure the two lightweight judges, which
    # otherwise stay on a hardcoded Gemini model.
    for key in ("default", "nodes", "presets"):
        cfg.pop(key, None)
    for name in AGENT_NODES:
        cfg[name] = dict(endpoint)
    cfg["utils"] = {name: dict(endpoint) for name in UTILS_NODES}

    agent = cfg.setdefault("agent", {})
    flash = agent.setdefault("flash", {})
    # The one-shot explorer needs Gemini's embodied-reasoning model for coordinates;
    # the "pro" explorer grounds through the UI tree instead.
    flash["explorer_mode"] = "pro"
    agent.setdefault("pro", {}).setdefault("explorer", {})["mode"] = "pro"
    agent.setdefault("explorer", {})["default_version"] = "pro"
    # The step summarizer and the history-chunk capsule lens call Gemini directly, whatever
    # the config says. Turning the transcript ledger off drops chunk compression with it, so
    # keep local runs short (repro / verify flows), since history is no longer compressed.
    flash.setdefault("step_summarizer", {})["enabled"] = False
    agent.setdefault("memory", {}).setdefault("transcript", {})["enabled"] = False
    # Video analysis on a local model means shipping keyframes through an 8B VLM; skip it.
    agent["pro"].setdefault("video_analyzer", {})["enabled"] = False
    agent.setdefault("video_analyzer", {})["enabled"] = False
    return cfg


def find_artemis_home(arg: str | None) -> Path:
    home = Path(arg or os.environ.get("ARTEMIS_HOME") or REPO_ROOT.parent / "artemis")
    if not (home / "mcp_server").is_dir():
        sys.exit(f"ARTEMIS not found at {home} (pass --artemis-home or set ARTEMIS_HOME)")
    return home


def find_local_model(arg: str | None) -> str:
    if arg:
        return arg
    try:
        with urllib.request.urlopen(f"{OLLAMA}/api/tags", timeout=5) as r:
            names = [m["name"] for m in json.load(r)["models"]]
    except OSError:
        sys.exit("Ollama is not running on localhost:11434. Start it, then retry.")
    ours = [n for n in names if n.startswith(LOCAL_FAMILY + ":")]
    if not ours:
        sys.exit(
            f"No {LOCAL_FAMILY} model in Ollama. Run the setup doctor "
            "(scripts/setup-env.ps1 / setup-env.sh) to create one sized for your GPU."
        )
    return ours[0]


def read_env_value(env_file: Path, key: str) -> str:
    if env_file.exists():
        for line in env_file.read_text(encoding="utf-8").splitlines():
            m = re.match(rf"\s*{key}\s*=(.*)", line)
            if m:
                return m.group(1).strip().strip("\"'")
    return ""


def set_env_lines(env_file: Path, values: dict[str, str]) -> bool:
    """Set KEY=value lines in a dotenv file; return True if anything changed."""
    text = env_file.read_text(encoding="utf-8") if env_file.exists() else ""
    lines = text.splitlines()
    for key, value in values.items():
        entry = f"{key}={value}"
        for i, line in enumerate(lines):
            if re.match(rf"\s*{key}\s*=", line):
                lines[i] = entry
                break
        else:
            lines.append(entry)
    new_text = "\n".join(lines) + "\n"
    if new_text == text:
        return False
    env_file.write_text(new_text, encoding="utf-8")
    return True


def current_mode() -> str:
    if not ACTIVE.exists():
        return "upstream (ARTEMIS default config, no local fallback)"
    header = ACTIVE.read_text(encoding="utf-8").splitlines()[0]
    return header.removeprefix("// agent-kit-artemis mode: ")


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawTextHelpFormatter)
    ap.add_argument("mode", choices=("gemini", "qwen", "status"))
    ap.add_argument("--artemis-home")
    ap.add_argument("--model")
    args = ap.parse_args()

    home = find_artemis_home(args.artemis_home)
    if args.mode == "status":
        print(f"ARTEMIS model mode: {current_mode()}")
        return

    model = find_local_model(args.model)
    base = json.loads(strip_jsonc((home / "config" / "artemis.jsonc").read_text(encoding="utf-8")))
    cfg = gemini_config(base, model) if args.mode == "gemini" else qwen_config(base, model)
    label = (
        f"gemini (local fallback: {model})" if args.mode == "gemini" else f"qwen only ({model})"
    )

    STATE_DIR.mkdir(exist_ok=True)
    ACTIVE.write_text(
        f"// agent-kit-artemis mode: {label}\n"
        f"// Generated by {Path(__file__).name} from {home / 'config' / 'artemis.jsonc'}. "
        "Do not edit; re-run the script.\n" + json.dumps(cfg, indent=2) + "\n",
        encoding="utf-8",
    )
    env_values = {"ARTEMIS_ARTEMIS_JSONC": ACTIVE.as_posix(), "OPENAI_BASE_URL": f"{OLLAMA}/v1"}
    # ARTEMIS's Ollama client takes OPENAI_API_KEY when the variable exists — and its
    # .env.example ships it blank. A blank key fails client creation, and ARTEMIS then
    # silently falls back to a hardcoded Gemini model. Ollama ignores the value.
    if not read_env_value(home / ".env", "OPENAI_API_KEY"):
        env_values["OPENAI_API_KEY"] = "ollama"
    env_changed = set_env_lines(home / ".env", env_values)
    print(f"ARTEMIS model mode: {label}")
    if env_changed:
        # The MCP server loads .env once and hands its environment down to the daemon and
        # every task worker, so .env edits need one restart. Later switches only rewrite
        # the active config file, which each task reads fresh.
        print("First-time setup changed the ARTEMIS .env - restart ARTEMIS once:")
        print(f"  cd {home} && uv run artemis stop")
        print("  then reconnect the ARTEMIS MCP server (Claude Code: /mcp -> artemis -> Reconnect)")
    else:
        print("Applies from the next ARTEMIS task.")


if __name__ == "__main__":
    main()
