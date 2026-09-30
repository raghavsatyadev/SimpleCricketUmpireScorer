#!/usr/bin/env bash
# Run Gradle with short output for coding agents.
# Success: one line. Failure: the "What went wrong" block, the Kotlin errors and the full log path.
# Usage: scripts/gradle-agent.sh :app:compileDebugKotlin [more tasks or flags]
set -u

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root" || exit 1

mkdir -p tmp
log="tmp/gradle-agent-$(date +%Y%m%d-%H%M%S)-$$.log"

./gradlew --console=plain -q --warning-mode=summary "$@" >"$log" 2>&1
code=$?

if [ "$code" -eq 0 ]; then
  echo "BUILD OK: $*"
  exit 0
fi

echo "BUILD FAILED ($code): $*"
awk '/^\* What went wrong:/ { p = 1 } /^\* Try:/ { p = 0 } p' "$log" | head -n 40
grep -E '^e: file://' "$log" | awk '!seen[$0]++' | head -n 40

# Optional one-line hint from the local Nimble model (scripts/nimble.sh). Silent when Nimble is
# off, slow or unsure (top choice under 0.6).
if [ -f "$root/scripts/nimble.sh" ]; then
  # shellcheck source=nimble.sh
  . "$root/scripts/nimble.sh"
  NIMBLE_ALLOW_JEV=0   # hooks stay local and free
  q='{"class":{"type":"choice","instructions":"Classify why this Gradle build failed.","criteria":{"kotlin_compile":"Kotlin compile error (e: file://)","ktfmt":"ktfmt or spotless format check failed","dependency":"dependency resolution or download failure","jdk_jlink":"JDK, toolchain or jlink failure","submodule_native":"git submodule missing or native CMake build failure","gradle_oom":"Gradle or Kotlin daemon out of memory","other":"none of the above"}}}'
  if resp=$(tail -n 200 "$log" | nimble_ask 2 "$q")      && c=$(nimble_get "$resp" class choice) && p=$(nimble_get "$resp" class p)      && [ "$c" != "other" ] && nimble_ge "$p" 0.6; then
    case "$c" in
      kotlin_compile) h="Kotlin compile error: fix the e: lines above." ;;
      ktfmt) h="Format check failed: run scripts/ci-local.sh --fix." ;;
      dependency) h="Dependency resolution failed: check network, repositories and gradle/libs.versions.toml." ;;
      jdk_jlink) h="JDK or jlink problem: check JAVA_HOME and the toolchain version." ;;
      submodule_native) h="Submodule or native build problem: run git submodule update --init, then rebuild." ;;
      gradle_oom) h="Gradle ran out of memory: raise org.gradle.jvmargs or stop other daemons." ;;
      *) h="" ;;
    esac
    [ -n "$h" ] && echo "Hint (Nimble, $p): $h"
  fi
fi
echo "Full log: $root/$log"
exit "$code"
