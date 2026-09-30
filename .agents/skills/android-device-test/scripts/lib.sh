#!/usr/bin/env bash
# Shared discovery + cache layer for the android-device-test skill.
#
# Contains no device serials, package names, coordinates or absolute paths.
# Everything is discovered at runtime on first use and cached under
# $DT_CACHE (default: <repo>/.device-cache, which is git-ignored).
#
# Usage:  source "$(dirname "$0")/lib.sh"

set -uo pipefail

# On Windows/Git Bash, MSYS rewrites POSIX-looking arguments into Windows paths,
# so an on-device path like /sdcard/x.xml becomes C:/Program Files/Git/sdcard/x.xml.
# Device paths must reach adb untouched.
export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL='*'

# ---------------------------------------------------------------- repo + cache

dt_repo_root() {
  git rev-parse --show-toplevel 2>/dev/null || {
    # Fall back to four levels above .agents/skills/<skill>/scripts
    cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd
  }
}

DT_REPO="${DT_REPO:-$(dt_repo_root)}"
DT_MEMORY="${DT_MEMORY:-$DT_REPO/memory}"
DT_TMP="${DT_TMP:-$DT_REPO/tmp}"
DT_CACHE="$DT_TMP"
DT_ARTIFACTS="$DT_TMP/artifacts"
# Remembers the last device used, so a single-device run needs no serial.
DT_LAST_SERIAL_FILE="$DT_MEMORY/last-serial"

# Per-device files are set by dt_init once the serial is known: screen geometry and
# tap coordinates from one handset must never be reused for another.
DT_ENV_FILE=""
DT_COORDS_FILE=""

mkdir -p "$DT_MEMORY" "$DT_TMP" "$DT_ARTIFACTS"

dt_log()  { printf '%s\n' "$*" >&2; }
dt_die()  { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

# Host-native form of a path. Needed because MSYS_NO_PATHCONV stops the shell
# from translating paths for native (non-MSYS) programs such as Windows Python.
dt_winpath() {
  if command -v cygpath >/dev/null 2>&1; then cygpath -w "$1" 2>/dev/null || printf '%s' "$1"
  else printf '%s' "$1"; fi
}

# ------------------------------------------------------------------ adb lookup

# Resolve an adb binary without assuming it is on PATH.
dt_find_adb() {
  if [ -n "${DT_ADB:-}" ] && [ -x "$DT_ADB" ]; then printf '%s' "$DT_ADB"; return; fi
  if command -v adb >/dev/null 2>&1; then command -v adb; return; fi

  local sdk=""
  # local.properties (sdk.dir) is the project's own, untracked SDK pointer.
  if [ -f "$DT_REPO/local.properties" ]; then
    sdk=$(sed -n 's/^[[:space:]]*sdk\.dir[[:space:]]*=[[:space:]]*//p' "$DT_REPO/local.properties" \
          | head -1 | tr -d '\r' | sed 's/\\\\/\//g; s/\\/\//g')
  fi
  [ -z "$sdk" ] && sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"

  for cand in "$sdk/platform-tools/adb.exe" "$sdk/platform-tools/adb"; do
    [ -x "$cand" ] && { printf '%s' "$cand"; return; }
  done
  dt_die "adb not found. Put it on PATH, set DT_ADB, or set sdk.dir in local.properties."
}

# ------------------------------------------------------------- cache load/save

dt_cache_get() {
  [ -n "$DT_ENV_FILE" ] && [ -f "$DT_ENV_FILE" ] || return 1
  sed -n "s/^$1=//p" "$DT_ENV_FILE" | head -1
}

dt_cache_set() {
  local key="$1" val="$2"
  touch "$DT_ENV_FILE"
  local tmp="$DT_ENV_FILE.tmp"
  grep -v "^$key=" "$DT_ENV_FILE" 2>/dev/null > "$tmp" || true
  printf '%s=%s\n' "$key" "$val" >> "$tmp"
  mv "$tmp" "$DT_ENV_FILE"
}

# ---------------------------------------------------------------- device pick

# Every attached device, one serial per line.
dt_list_devices() {
  # Tab-separated: wireless serials can contain spaces, e.g. "adb-XXXX (2)._adb-tls-connect._tcp".
  "$DT_ADB_BIN" devices | awk -F'	' 'NR>1 && $2=="device" {print $1}'
}

# Choose a serial: explicit env > cached (if still attached) > sole attached device.
dt_resolve_serial() {
  local attached; attached=$(dt_list_devices)
  [ -z "$attached" ] && dt_die "No device attached. Connect one and enable USB/wireless debugging."

  # 1. Caller override wins and is never cached over a different explicit choice.
  if [ -n "${ADB_DEVICE_SERIAL:-}" ]; then
    printf '%s\n' "$attached" | grep -qxF "$ADB_DEVICE_SERIAL" \
      || dt_die "ADB_DEVICE_SERIAL is set but that device is not attached."
    printf '%s' "$ADB_DEVICE_SERIAL"; return
  fi

  # 2. Last device used, only if it is still present.
  local cached=""
  [ -f "$DT_LAST_SERIAL_FILE" ] && cached=$(head -1 "$DT_LAST_SERIAL_FILE")
  if [ -n "$cached" ] && printf '%s\n' "$attached" | grep -qxF "$cached"; then
    printf '%s' "$cached"; return
  fi

  # 3. Exactly one attached device -> unambiguous.
  if [ "$(printf '%s\n' "$attached" | wc -l)" -eq 1 ]; then
    printf '%s' "$attached"; return
  fi

  dt_log "Multiple devices attached:"; dt_log "$attached"
  dt_die "Set ADB_DEVICE_SERIAL to pick one."
}

# ------------------------------------------------------------- app id discovery

# Read the applicationId from agent-kit.env (APPLICATION_ID), else the Gradle version catalog.
dt_discover_app_id() {
  local cached; cached=$(dt_cache_get APP_ID || true)
  [ -n "$cached" ] && { printf '%s' "$cached"; return; }

  local catalog="$DT_REPO/gradle/libs.versions.toml" id=""
  [ -f "$DT_REPO/agent-kit.env" ] && id=$(sed -n 's/^APPLICATION_ID=//p' "$DT_REPO/agent-kit.env" | tr -d '"[:cntrl:]' | head -1)
  if [ -n "$id" ]; then printf '%s' "$id"; return; fi
  if [ -f "$catalog" ]; then
    id=$(sed -n 's/^[[:space:]]*appIdProd[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' "$catalog" | head -1)
    [ -z "$id" ] && id=$(sed -n 's/^[[:space:]]*nameSpace[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' "$catalog" | head -1)
  fi
  [ -z "$id" ] && dt_die "Could not discover applicationId. Set DT_APP_ID explicitly."
  printf '%s' "$id"
}

# ------------------------------------------------------------------ bootstrap

# Populate DT_ADB_BIN / DT_SERIAL / DT_APP_ID and the adb() wrapper.
# Re-running is cheap: values come from the cache after the first run.
dt_init() {
  DT_ADB_BIN="$(dt_find_adb)"
  export DT_ADB_BIN

  # $(...) runs in a subshell, so dt_die inside it cannot stop us; check the result instead.
  # An empty serial would make adb silently pick a default device.
  if [ -z "${DT_SERIAL:-}" ]; then
    DT_SERIAL="$(dt_resolve_serial)" || exit 1
  fi
  [ -n "$DT_SERIAL" ] || dt_die "could not resolve a device serial"
  export DT_SERIAL

  # Give this device its own slot in memory/, keyed by a filesystem-safe serial.
  local safe; safe=$(printf '%s' "$DT_SERIAL" | tr -c 'A-Za-z0-9._-' '_')
  local devdir="$DT_MEMORY/devices/$safe"
  mkdir -p "$devdir"
  DT_ENV_FILE="$devdir/device.env"
  DT_COORDS_FILE="$devdir/coords.tsv"
  printf '%s\n' "$DT_SERIAL" > "$DT_LAST_SERIAL_FILE"

  if [ -z "${DT_APP_ID:-}" ]; then
    DT_APP_ID="$(dt_discover_app_id)" || exit 1
  fi
  export DT_APP_ID

  # Screen geometry, used to keep taps and swipes resolution-independent.
  local size dens
  size=$(dt_cache_get SCREEN_SIZE || true)
  if [ -z "$size" ]; then
    size=$(dt_adb shell wm size 2>/dev/null | sed -n 's/.*Physical size:[[:space:]]*//p' | tr -d '\r' | head -1)
    dens=$(dt_adb shell wm density 2>/dev/null | sed -n 's/.*Physical density:[[:space:]]*//p' | tr -d '\r' | head -1)
    [ -n "$dens" ] && dt_cache_set DENSITY "$dens"
  fi
  DT_SCREEN_W="${size%x*}"; DT_SCREEN_H="${size#*x}"
  export DT_SCREEN_W DT_SCREEN_H

  # Persist whatever we just learned so later runs skip discovery entirely.
  dt_cache_set SERIAL      "$DT_SERIAL"
  dt_cache_set APP_ID      "$DT_APP_ID"
  dt_cache_set SCREEN_SIZE "${DT_SCREEN_W}x${DT_SCREEN_H}"
  dt_cache_set ADB_BIN     "$DT_ADB_BIN"
}

# Always talk to the resolved device explicitly, never the default one.
dt_adb() { "$DT_ADB_BIN" -s "$DT_SERIAL" "$@"; }

# Some Samsung/OEM shells emit CRLF; strip it so string compares work.
dt_adb_sh() { dt_adb shell "$@" 2>/dev/null | tr -d '\r'; }

dt_ts() { date +%Y%m%d-%H%M%S; }
