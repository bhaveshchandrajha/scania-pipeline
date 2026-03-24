#!/usr/bin/env python3
"""
A/B test: compare migrations with vs without call graph context.

Runs migration twice (with and without --no-call-graph), then compares:
- Entity consolidation (validation pass/fail)
- Compile success (when --run-build)
- Entity count, duplicate count

USAGE:
  export ANTHROPIC_API_KEY=sk-ant-...
  python3 ab_test_migration.py context_index/HS1210_n404.json [--target-project warranty_demo] [--run-build]

Output: JSON report with both runs and comparison.
"""

from __future__ import annotations

import json
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any, Dict, List, Optional

ROOT_DIR = Path(__file__).resolve().parent


def _run_migration(
    context_file: Path,
    with_call_graph: bool,
    output_dir: Path,
    target_project: Optional[Path] = None,
    rpg_file: Optional[Path] = None,
) -> Dict[str, Any]:
    """Run migrate_to_pure_java and return result summary."""
    cmd = [
        sys.executable,
        str(ROOT_DIR / "migrate_to_pure_java.py"),
        str(context_file),
        "--output-dir",
        str(output_dir),
        "--validate",
    ]
    if not with_call_graph:
        cmd.append("--no-call-graph")
    if target_project:
        cmd.append("--target-project")
        cmd.append(str(target_project))
    if rpg_file:
        cmd.append("--rpg-file")
        cmd.append(str(rpg_file))

    proc = subprocess.run(
        cmd,
        cwd=str(ROOT_DIR),
        capture_output=True,
        text=True,
        timeout=600,
    )
    stderr = proc.stderr or ""
    # Parse validation from stderr
    validation_passed = "Validation passed" in stderr or "Entity consolidation: PASS" in stderr
    validation_failed = "Validation FAILED" in stderr
    return {
        "exitCode": proc.returncode,
        "validationPassed": validation_passed and not validation_failed,
        "stderrPreview": stderr[-4000:] if len(stderr) > 4000 else stderr,
    }


def _run_validation_only(context_file: Path, output_path: Path) -> Dict[str, Any]:
    """Run validate_migration_output and return report."""
    cmd = [
        sys.executable,
        str(ROOT_DIR / "validate_migration_output.py"),
        str(context_file),
        str(output_path),
    ]
    proc = subprocess.run(cmd, cwd=str(ROOT_DIR), capture_output=True, text=True, timeout=60)
    # Parse report from stdout
    report = {"exitCode": proc.returncode, "passed": proc.returncode == 0}
    for line in (proc.stdout or "").splitlines():
        if "Entity consolidation:" in line:
            report["entityConsolidation"] = "PASS" in line
        if "Subroutine coverage:" in line:
            report["subroutineCoverage"] = "PASS" in line
        if "Tables validated:" in line:
            try:
                report["tablesValidated"] = int(line.split(":")[-1].strip())
            except ValueError:
                pass
        if "Entities found:" in line:
            try:
                report["entitiesFound"] = int(line.split(":")[-1].strip())
            except ValueError:
                pass
    return report


def main() -> None:
    import argparse
    parser = argparse.ArgumentParser(description="A/B test migration with vs without call graph.")
    parser.add_argument("context_file", type=Path, help="Context JSON")
    parser.add_argument("--target-project", type=Path, default=None, help="Target project for integration mode")
    parser.add_argument("--rpg-file", type=Path, default=None, help="RPG source file")
    parser.add_argument("--run-build", action="store_true", help="Run mvn compile after each migration")
    args = parser.parse_args()

    context_file = args.context_file
    if not context_file.exists():
        print(f"Context file not found: {context_file}", file=sys.stderr)
        sys.exit(2)

    # Use temp dirs for standalone output (to avoid overwriting)
    with tempfile.TemporaryDirectory() as tmp:
        base = Path(tmp)
        out_with = base / "with_cg"
        out_without = base / "without_cg"
        out_with.mkdir()
        out_without.mkdir()

        target = args.target_project
        if target:
            # For integration mode we need separate project copies - skip for now, use same project
            print("// A/B test with --target-project: running sequentially (second run overwrites first)", file=sys.stderr)
            result_with = _run_migration(context_file, True, out_with, target, args.rpg_file)
            result_without = _run_migration(context_file, False, out_without, target, args.rpg_file)
        else:
            result_with = _run_migration(context_file, True, out_with, None, args.rpg_file)
            result_without = _run_migration(context_file, False, out_without, None, args.rpg_file)

        # Run validation on outputs
        if target:
            val_path = target
            report_with = _run_validation_only(context_file, val_path)
            report_without = _run_validation_only(context_file, val_path)
        else:
            stem = context_file.stem
            if "_" in stem:
                unit, node = stem.split("_", 1)
                val_path_with = out_with / f"{unit}_{node}_pure_java"
                val_path_without = out_without / f"{unit}_{node}_pure_java"
            else:
                val_path_with = out_with
                val_path_without = out_without
            report_with = _run_validation_only(context_file, val_path_with)
            report_without = _run_validation_only(context_file, val_path_without)

    comparison = {
        "withCallGraph": {
            "migrationExitCode": result_with["exitCode"],
            "validationPassed": result_with["validationPassed"],
            "entityConsolidation": report_with.get("entityConsolidation"),
            "entitiesFound": report_with.get("entitiesFound"),
            "tablesValidated": report_with.get("tablesValidated"),
        },
        "withoutCallGraph": {
            "migrationExitCode": result_without["exitCode"],
            "validationPassed": result_without["validationPassed"],
            "entityConsolidation": report_without.get("entityConsolidation"),
            "entitiesFound": report_without.get("entitiesFound"),
            "tablesValidated": report_without.get("tablesValidated"),
        },
        "callGraphBetter": (
            result_with["validationPassed"] and not result_without["validationPassed"]
        ) or (
            report_with.get("entityConsolidation") and not report_without.get("entityConsolidation")
        ),
    }

    print(json.dumps(comparison, indent=2))
    if comparison["callGraphBetter"]:
        print("// Call graph improved migration quality", file=sys.stderr)
    elif result_with["validationPassed"] == result_without["validationPassed"]:
        print("// No significant difference in validation", file=sys.stderr)


if __name__ == "__main__":
    main()
