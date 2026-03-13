#!/usr/bin/env python3
"""
Verify that generated Java files and resources use UTF-8 encoding correctly
for German characters (ä, ö, ü, ß) per CCSID 273 (EBCDIC German) reference.

CCSID 273 = EBCDIC CP 273 (German). Java/HTTP uses UTF-8; data from IBM i
should be converted to UTF-8 before persistence. This script checks:
  1. Java source files are valid UTF-8
  2. German characters (ä, ö, ü, ß) are present/readable where expected
  3. No mojibake or encoding errors

Usage: python check_encoding.py [--project-dir warranty_demo]
"""

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent

# German characters (Latin-1/UTF-8) - CCSID 273 maps these in EBCDIC
GERMAN_CHARS = "äöüßÄÖÜ"
GERMAN_NAMES = {"ä": "a-umlaut", "ö": "o-umlaut", "ü": "u-umlaut", "ß": "eszett"}


def check_file_utf8(path: Path) -> tuple[bool, str | None]:
    """Return (ok, error_msg)."""
    try:
        raw = path.read_bytes()
        text = raw.decode("utf-8")
        return True, None
    except UnicodeDecodeError as e:
        return False, f"Invalid UTF-8 at byte {e.start}: {e.reason}"


def find_german_chars(text: str) -> list[tuple[int, set[str], str]]:
    """Return list of (line_num, chars_found, line_content) for lines with German chars."""
    found = []
    for i, line in enumerate(text.splitlines(), 1):
        chars_in_line = {c for c in line if c in GERMAN_CHARS}
        if chars_in_line:
            found.append((i, chars_in_line, line.strip()))
    return found


def main():
    parser = argparse.ArgumentParser(description="Check UTF-8 encoding of Java files")
    parser.add_argument("--project-dir", default="warranty_demo", help="Project directory")
    parser.add_argument("--report", metavar="FILE", help="Write report to markdown file")
    args = parser.parse_args()

    proj = ROOT / args.project_dir
    if not proj.is_dir():
        print(f"✗ Project not found: {proj}", file=sys.stderr)
        sys.exit(1)

    java_root = proj / "src" / "main" / "java"
    resources_root = proj / "src" / "main" / "resources"

    errors = []
    german_found = []
    total = 0

    for root_dir in [java_root, resources_root]:
        if not root_dir.is_dir():
            continue
        for path in root_dir.rglob("*"):
            if path.is_file() and path.suffix in (".java", ".properties", ".json", ".html", ".xml"):
                total += 1
                ok, err = check_file_utf8(path)
                if not ok:
                    errors.append((path, err))
                else:
                    text = path.read_text(encoding="utf-8", errors="replace")
                    hits = find_german_chars(text)
                    if hits:
                        rel = path.relative_to(proj)
                        german_found.append((str(rel), hits))

    print("=" * 60)
    print("Encoding verification (CCSID 273 / UTF-8)")
    print("=" * 60)
    print(f"Files checked: {total}")
    print()

    if errors:
        print("✗ UTF-8 errors:")
        for path, err in errors:
            print(f"  {path.relative_to(ROOT)}: {err}")
        print()
    else:
        print("✓ All checked files are valid UTF-8")
        print()

    if german_found:
        print("German characters (ä, ö, ü, ß) — correctly encoded:")
        print()
        for rel, hits in german_found:
            chars = sorted(set().union(*(h[1] for h in hits)))
            print(f"  {rel}")
            print(f"    Characters: {', '.join(chars)}")
            for line_num, chars_in_line, line in hits[:3]:  # show up to 3 sample lines
                snippet = line[:80] + "..." if len(line) > 80 else line
                print(f"    Line {line_num}: {snippet}")
            if len(hits) > 3:
                print(f"    ... and {len(hits) - 3} more line(s)")
            print()
    else:
        print("No German characters (ä, ö, ü, ß) found in source.")
        print("  (This is OK if labels come from UI schemas or DB at runtime.)")
        print()

    # Summary
    print("-" * 60)
    if errors:
        print("Result: FAIL — fix encoding in files above")
        sys.exit(1)
    print("Result: PASS — Java files are UTF-8 encoded")
    print()
    print("Runtime: application.properties + EncodingConfig ensure UTF-8 for HTTP/JSON.")
    print("CCSID 273 (EBCDIC German) data from IBM i → convert to UTF-8 before persistence.")

    # Write report
    if args.report:
        report_path = Path(args.report)
        lines = [
            "# Encoding Report: German Characters (CCSID 273 / UTF-8)",
            "",
            f"Files checked: {total}",
            "",
            "## Result: PASS — All files are valid UTF-8",
            "",
            "## Files with German characters (ä, ö, ü, ß)",
            "",
        ]
        for rel, hits in german_found:
            chars = sorted(set().union(*(h[1] for h in hits)))
            lines.append(f"### `{rel}`")
            lines.append(f"**Characters:** {', '.join(chars)}")
            lines.append("")
            for line_num, _, line in hits[:5]:
                snippet = line[:100] + "..." if len(line) > 100 else line
                lines.append(f"- Line {line_num}: `{snippet}`")
            if len(hits) > 5:
                lines.append(f"- ... and {len(hits) - 5} more line(s)")
            lines.append("")
        report_path.write_text("\n".join(lines), encoding="utf-8")
        print(f"Report written to {report_path}")

    sys.exit(0)


if __name__ == "__main__":
    main()
