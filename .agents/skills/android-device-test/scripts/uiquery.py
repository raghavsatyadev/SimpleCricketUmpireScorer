#!/usr/bin/env python3
"""Query a uiautomator XML hierarchy dump and report element centres.

Reads a dump from a file (or stdin) and prints matching nodes as TSV:

    x<TAB>y<TAB>bounds<TAB>class<TAB>text<TAB>resource-id<TAB>content-desc

Selectors are prefixed:  text:, textc: (contains), id:, desc:, class:
An unprefixed selector is treated as `textc:` (case-insensitive contains),
which is what most on-screen assertions actually want.

No project-specific values live here; everything comes from argv.
"""

from __future__ import annotations

import argparse
import re
import sys
import xml.etree.ElementTree as ET

BOUNDS_RE = re.compile(r"\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]")


def centre(bounds: str):
    """Return (cx, cy, w, h) for a uiautomator bounds string, or None."""
    m = BOUNDS_RE.match(bounds or "")
    if not m:
        return None
    x1, y1, x2, y2 = (int(g) for g in m.groups())
    if x2 <= x1 or y2 <= y1:  # zero-area nodes are not tappable
        return None
    return (x1 + x2) // 2, (y1 + y2) // 2, x2 - x1, y2 - y1


def parse_selector(raw: str):
    for prefix, field in (
        ("text:", "text"),
        ("textc:", "text"),
        ("id:", "resource-id"),
        ("desc:", "content-desc"),
        ("class:", "class"),
    ):
        if raw.startswith(prefix):
            exact = prefix in ("text:", "id:", "class:")
            return field, raw[len(prefix):], exact
    return "text", raw, False


def matches(node, field, needle, exact) -> bool:
    val = node.get(field) or ""
    if exact:
        # resource-id may be given in short form ("btn_go" for "pkg:id/btn_go").
        return val == needle or (field == "resource-id" and val.endswith("/" + needle))
    return needle.lower() in val.lower()


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("selector")
    ap.add_argument("--dump", default="-", help="XML dump path, or - for stdin")
    ap.add_argument("--index", type=int, default=0, help="which match to report")
    ap.add_argument("--clickable", action="store_true",
                    help="only nodes that are clickable themselves")
    ap.add_argument("--clickable-ancestor", action="store_true",
                    help="report the nearest clickable ancestor's centre instead")
    ap.add_argument("--all", action="store_true", help="print every match")
    args = ap.parse_args()

    raw = sys.stdin.read() if args.dump == "-" else open(
        args.dump, "r", encoding="utf-8", errors="replace").read()

    # uiautomator sometimes prefixes status text before the XML declaration.
    start = raw.find("<?xml")
    if start > 0:
        raw = raw[start:]
    if not raw.strip():
        print("EMPTY_DUMP", file=sys.stderr)
        return 3

    try:
        root = ET.fromstring(raw)
    except ET.ParseError as e:
        print(f"PARSE_ERROR {e}", file=sys.stderr)
        return 3

    # Parent links, so --clickable-ancestor can walk upward.
    parent = {c: p for p in root.iter() for c in p}
    field, needle, exact = parse_selector(args.selector)

    out = []
    for node in root.iter("node"):
        if not matches(node, field, needle, exact):
            continue
        target = node
        if args.clickable_ancestor and node.get("clickable") != "true":
            cur = node
            while cur is not None and cur.get("clickable") != "true":
                cur = parent.get(cur)
            if cur is not None:
                target = cur
        if args.clickable and target.get("clickable") != "true":
            continue
        c = centre(target.get("bounds", ""))
        if not c:
            continue
        cx, cy, _, _ = c
        out.append((cx, cy, target.get("bounds", ""), node.get("class", ""),
                    node.get("text", ""), node.get("resource-id", ""),
                    node.get("content-desc", "")))

    if not out:
        print("NOT_FOUND", file=sys.stderr)
        return 1

    rows = out if args.all else out[args.index: args.index + 1]
    if not rows:
        print(f"INDEX_OUT_OF_RANGE (matches={len(out)})", file=sys.stderr)
        return 1
    for r in rows:
        print("\t".join(str(x).replace("\t", " ") for x in r))
    return 0


if __name__ == "__main__":
    sys.exit(main())
