#!/usr/bin/env bash
# Evidence hash of the current Kotlin/Gradle state of a working tree.
#
# Sourced by scripts/ci-local.sh (writes it to tmp/.ci-ok after a passing run) and by
# scripts/check-done.sh (the Claude Code Stop hook, which compares against it). Both must
# compute it the same way, so the logic lives only here.
#
#   evidence_hash [repo-dir]   prints one hash on stdout
#
# The hash covers the path and working-tree content of every tracked or untracked (not
# ignored) *.kt / *.kts file plus gradle/libs.versions.toml. It is content-based, not tied to
# HEAD: committing code that already passed does not invalidate the evidence, but editing,
# adding, deleting or renaming any of those files does. `git hash-object` applies the same
# clean filters (core.autocrlf) every time, so CRLF checkouts hash consistently.
#
# Run directly (`bash scripts/evidence-hash.sh`) it prints the hash for the current repo.

EVIDENCE_PATHSPEC=('*.kt' '*.kts' 'gradle/libs.versions.toml')

evidence_hash() {
  local dir="${1:-.}" f
  local -a files=() blobs=()
  while IFS= read -r -d '' f; do
    [ -f "$dir/$f" ] && files+=("$f")
  done < <(git -C "$dir" ls-files -z --cached --others --exclude-standard -- \
             "${EVIDENCE_PATHSPEC[@]}" 2>/dev/null)

  if [ "${#files[@]}" -gt 0 ]; then
    # One git process for all files; output order matches input order.
    while IFS= read -r f; do blobs+=("$f"); done < <(
      printf '%s\n' "${files[@]}" | git -C "$dir" hash-object --stdin-paths 2>/dev/null)
  fi

  local i
  {
    for i in "${!files[@]}"; do printf '%s %s\n' "${blobs[$i]:-missing}" "${files[$i]}"; done
  } | LC_ALL=C sort | git -C "$dir" hash-object --stdin
}

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  evidence_hash "${1:-$(git rev-parse --show-toplevel)}"
fi
