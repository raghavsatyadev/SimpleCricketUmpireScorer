#!/usr/bin/env bash
# Run the GitHub CI checks locally, before committing or pushing.
#
# Mirrors .github/workflows/ci.yml:
#   job 1  ktfmt-check  — ktfmt --google-style --dry-run --set-exit-if-changed
#   job 2  build-app    — ./gradlew :${APP_MODULE#:}:compile${APP_VARIANT}Kotlin
#
#   scripts/ci-local.sh                  check working tree + commits vs the base branch
#   scripts/ci-local.sh --fix            reformat offending files instead of failing
#   scripts/ci-local.sh --committed-only exactly what CI sees (ignores uncommitted work)
#   scripts/ci-local.sh --all            every Kotlin file in the repo
#   scripts/ci-local.sh --base master    compare against a different base branch
#   scripts/ci-local.sh --format-only    skip the Gradle compile (fast)
#
# Exit code is 0 only when every check CI runs would pass.
# A passing run that compiled (not --format-only) writes tmp/.ci-ok: the evidence hash from
# scripts/evidence-hash.sh, which the Claude Code Stop hook (scripts/check-done.sh) checks.

set -uo pipefail

REPO="$(git rev-parse --show-toplevel 2>/dev/null)" || {
  echo "ERROR: not inside a git repository" >&2; exit 1
}
cd "$REPO" || exit 1

# CI caches ktfmt at this path; reuse it so both see the same jars.
CACHE_DIR="$REPO/.cache/ktfmt"
. "$REPO/scripts/kit-env.sh"
BASE_BRANCH="${CI_LOCAL_BASE:-$BASE_BRANCH}"
MODE="working"      # working | committed | all
FIX=0
RUN_COMPILE=1

while [ $# -gt 0 ]; do
  case "$1" in
    --fix)            FIX=1 ;;
    --committed-only) MODE="committed" ;;
    --all)            MODE="all" ;;
    --format-only)    RUN_COMPILE=0 ;;
    --base)           BASE_BRANCH="${2:?--base needs a branch}"; shift ;;
    -h|--help)        sed -n '2,20p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "unknown option: $1" >&2; exit 2 ;;
  esac
  shift
done

RED=$'\033[31m'; GREEN=$'\033[32m'; YEL=$'\033[33m'; BLD=$'\033[1m'; RST=$'\033[0m'
say()  { printf '%s\n' "$*"; }
head2(){ printf '\n%s%s%s\n' "$BLD" "$*" "$RST"; }
fail=0

# --------------------------------------------------------------- ktfmt binary

# CI always resolves the LATEST release from Maven Central. Matching that matters:
# different ktfmt versions disagree on formatting, so a jar that is merely "already
# on disk" can pass locally and still fail CI.
resolve_ktfmt() {
  mkdir -p "$CACHE_DIR"
  local latest jar tmp
  latest="${KTFMT_VERSION:-}"
  [ -n "$latest" ] || latest=$(curl -fsSL --connect-timeout 5 \
    "https://repo1.maven.org/maven2/com/facebook/ktfmt/maven-metadata.xml" 2>/dev/null \
    | sed -n 's/.*<release>\([^<]*\)<\/release>.*/\1/p' | head -1)

  if [ -n "$latest" ]; then
    jar="$CACHE_DIR/ktfmt-${latest}-with-dependencies.jar"
    if [ ! -f "$jar" ]; then
      say "  downloading ktfmt ${latest} (matching CI)..." >&2
      tmp="${jar}.tmp.$$"
      if curl -fsSL --connect-timeout 5 \
        "https://repo1.maven.org/maven2/com/facebook/ktfmt/${latest}/ktfmt-${latest}-with-dependencies.jar" \
        -o "$tmp" 2>/dev/null; then
        rm -f "$CACHE_DIR"/ktfmt-*-with-dependencies.jar
        mv "$tmp" "$jar"
      else
        rm -f "$tmp"
        say "  ${YEL}download failed; falling back to a cached jar${RST}" >&2
        jar=""
      fi
    fi
  fi

  if [ -z "${jar:-}" ] || [ ! -f "$jar" ]; then
    jar=$(ls -1 "$CACHE_DIR"/ktfmt-*-with-dependencies.jar 2>/dev/null | sort -V | tail -1)
    [ -n "$jar" ] && say "  ${YEL}offline: using ${jar##*/} — CI uses the latest release${RST}" >&2
  fi

  [ -n "$jar" ] && printf '%s' "$jar"
}

# ------------------------------------------------------------- file selection

kotlin_files() {
  case "$MODE" in
    all) git ls-files '*.kt' '*.kts' ;;

    committed)
      # What CI compares on a pull request: base...HEAD.
      if git rev-parse --verify --quiet "origin/$BASE_BRANCH" >/dev/null; then
        git diff --name-only --diff-filter=ACMR "origin/$BASE_BRANCH...HEAD" -- '*.kt' '*.kts'
      elif git rev-parse --verify --quiet "$BASE_BRANCH" >/dev/null; then
        git diff --name-only --diff-filter=ACMR "$BASE_BRANCH...HEAD" -- '*.kt' '*.kts'
      else
        git diff --name-only --diff-filter=ACMR HEAD~1..HEAD -- '*.kt' '*.kts'
      fi
      ;;

    *)
      # Default: everything CI would see, plus work not yet committed, so problems
      # surface before the push rather than after it.
      {
        if git rev-parse --verify --quiet "origin/$BASE_BRANCH" >/dev/null; then
          git diff --name-only --diff-filter=ACMR "origin/$BASE_BRANCH...HEAD" -- '*.kt' '*.kts'
        elif git rev-parse --verify --quiet "$BASE_BRANCH" >/dev/null; then
          git diff --name-only --diff-filter=ACMR "$BASE_BRANCH...HEAD" -- '*.kt' '*.kts'
        fi
        git diff --name-only --diff-filter=ACMR -- '*.kt' '*.kts'          # unstaged
        git diff --name-only --diff-filter=ACMR --cached -- '*.kt' '*.kts' # staged
        git ls-files --others --exclude-standard -- '*.kt' '*.kts'         # untracked
      } | sort -u
      ;;
  esac
}

# ------------------------------------------------------------ job 1: ktfmt

head2 "[1/2] ktfmt lint check   (base: $BASE_BRANCH, mode: $MODE)"

KTFMT_JAR=$(resolve_ktfmt)
if [ -z "$KTFMT_JAR" ]; then
  say "  ${RED}FAIL${RST}  no ktfmt jar available and Maven Central unreachable"
  fail=1
else
  say "  using ${KTFMT_JAR##*/}"

  # CI skips files that no longer exist on disk; do the same.
  FILES=()
  while IFS= read -r f; do [ -n "$f" ] && [ -f "$f" ] && FILES+=("$f"); done < <(kotlin_files)

  if [ "${#FILES[@]}" -eq 0 ]; then
    say "  ${GREEN}PASS${RST}  no Kotlin files to check"
  else
    say "  checking ${#FILES[@]} file(s)"
    if [ "$FIX" = "1" ]; then
      if java -jar "$KTFMT_JAR" --google-style "${FILES[@]}" >/dev/null 2>&1; then
        if git diff --quiet -- "${FILES[@]}" 2>/dev/null; then
          say "  ${GREEN}PASS${RST}  already formatted"
        else
          say "  ${YEL}FIXED${RST} reformatted; review and stage:"
          git diff --name-only -- "${FILES[@]}" | sed 's/^/           /'
        fi
      else
        say "  ${RED}FAIL${RST}  ktfmt could not format these files"; fail=1
      fi
    else
      if out=$(java -jar "$KTFMT_JAR" --google-style --dry-run --set-exit-if-changed \
               "${FILES[@]}" 2>&1); then
        say "  ${GREEN}PASS${RST}  formatting clean"
      else
        say "  ${RED}FAIL${RST}  these files are not ktfmt-clean:"
        printf '%s\n' "$out" | sed 's/^/           /'
        say "           fix with: scripts/ci-local.sh --fix"
        fail=1
      fi
    fi
  fi
fi

# ---------------------------------------------------- job 2: compile Kotlin

# Evidence of what the compile below actually saw (after any --fix reformat).
EVIDENCE=""
if [ "$RUN_COMPILE" = "1" ] && [ -f "$REPO/scripts/evidence-hash.sh" ]; then
  # shellcheck source=evidence-hash.sh
  . "$REPO/scripts/evidence-hash.sh"
  EVIDENCE=$(evidence_hash "$REPO")
fi

if [ "$RUN_COMPILE" = "1" ]; then
  head2 "[2/2] Build & verify compilation"
  say "  ./gradlew :${APP_MODULE#:}:compile${APP_VARIANT}Kotlin"

  GRADLE="./gradlew"
  # On Windows the POSIX wrapper is not executable; use the batch one. It must be
  # path-qualified, or cmd looks it up on PATH and does not find it.
  case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) GRADLE="./gradlew.bat" ;;
  esac

  if out=$("$GRADLE" ":${APP_MODULE#:}:compile${APP_VARIANT}Kotlin" --console=plain 2>&1); then
    say "  ${GREEN}PASS${RST}  compiles"
  else
    say "  ${RED}FAIL${RST}  compilation failed:"
    printf '%s\n' "$out" | grep -E "^e:|error:|FAILURE|Caused by|Execution failed" \
      | head -30 | sed 's/^/           /'
    fail=1
  fi
else
  head2 "[2/2] Build & verify compilation — skipped (--format-only)"
fi

# ------------------------------------------------------------------ verdict

printf '\n'
if [ "$fail" = "0" ]; then
  if [ -n "$EVIDENCE" ]; then
    mkdir -p "$REPO/tmp" && printf '%s\n' "$EVIDENCE" > "$REPO/tmp/.ci-ok"
  fi
  say "${GREEN}${BLD}CI checks passed.${RST} Safe to commit and push."
else
  [ "$RUN_COMPILE" = "1" ] && rm -f "$REPO/tmp/.ci-ok"
  say "${RED}${BLD}CI checks failed.${RST} Fix the above before pushing — GitHub will reject it otherwise."
fi
exit "$fail"
