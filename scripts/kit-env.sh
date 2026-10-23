#!/usr/bin/env bash
# Sourced by the kit scripts. Reads agent-kit.env from the repo root (the one config file).
# Sets APP_MODULE, APP_VARIANT, APP_DIR, APPLICATION_ID, BASE_BRANCH, KTFMT_VERSION, NDK_VERSION.
_kit_root="$(git rev-parse --show-toplevel 2>/dev/null || (cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd))"
APP_MODULE=":app"; APP_VARIANT="Debug"; APPLICATION_ID=""; BASE_BRANCH="development"; KTFMT_VERSION=""; NDK_VERSION=""
if [ -f "$_kit_root/agent-kit.env" ]; then
  # shellcheck disable=SC1090
  . <(tr -d '\r' < "$_kit_root/agent-kit.env")
fi
APP_DIR="${APP_MODULE#:}"; APP_DIR="${APP_DIR//://}"
export APP_MODULE APP_VARIANT APP_DIR APPLICATION_ID BASE_BRANCH KTFMT_VERSION NDK_VERSION
