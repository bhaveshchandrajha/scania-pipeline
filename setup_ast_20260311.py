#!/usr/bin/env python3
"""
Extract 20260311.zip (PKS revised AST) to JSON_ast/JSON_20260311 if not already present.
Run this before using the pipeline with the new AST.
"""
from pathlib import Path
import zipfile
import sys

ROOT = Path(__file__).resolve().parent
ZIP_PATH = ROOT / "20260311.zip"
TARGET = ROOT / "JSON_ast" / "JSON_20260311"


def main():
    if not ZIP_PATH.exists():
        print(f"Not found: {ZIP_PATH}", file=sys.stderr)
        sys.exit(1)
    if TARGET.exists() and list(TARGET.glob("*-ast.json")):
        print(f"AST already extracted: {TARGET}")
        return
    TARGET.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(ZIP_PATH) as zf:
        for name in zf.namelist():
            if name.startswith("20260311/"):
                rel = name[len("20260311/"):]
                if not rel:
                    continue
                dest = TARGET / rel
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(zf.read(name))
                print(f"  {rel}")
    print(f"Extracted to {TARGET}")


if __name__ == "__main__":
    main()
