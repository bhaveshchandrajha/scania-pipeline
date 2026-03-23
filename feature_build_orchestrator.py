#!/usr/bin/env python3
"""
Feature build orchestrator for the warranty_demo application.

This script wraps Maven and the existing LLM-based compile error fixer in an
iterative loop so you can move from "code that looks correct" to "code that
builds" with minimal manual intervention.

Behavior:
- Runs `mvn clean compile -DskipTests` in the `warranty_demo` project.
- On failure, writes the full build log to
  `warranty_demo/target/last_compile_errors.txt`.
- Invokes `fix_compile_errors.py` with the project directory and log file.
- Repeats the build + fix cycle up to MAX_RETRIES times (default: 3).

Usage:
  export ANTHROPIC_API_KEY=...   # required by fix_compile_errors.py
  python3 feature_build_orchestrator.py
"""

import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parent
PROJECT_DIR = ROOT / "warranty_demo"
BUILD_LOG = PROJECT_DIR / "target" / "last_compile_errors.txt"
FIX_SCRIPT = ROOT / "fix_compile_errors.py"
MAX_RETRIES = 4


def run_maven_build(clean: bool = True) -> int:
    """Run a Maven build and persist the full log to BUILD_LOG."""
    cmd = ["mvn", "-T", "1C", "-q", "clean", "compile"] if clean else ["mvn", "-T", "1C", "-q", "compile"]
    print(f"[build] Running: {' '.join(cmd)} (cwd={PROJECT_DIR})")
    proc = subprocess.run(
        cmd,
        cwd=str(PROJECT_DIR),
        text=True,
        capture_output=True,
    )
    output = (proc.stdout or "") + (proc.stderr or "")
    BUILD_LOG.parent.mkdir(parents=True, exist_ok=True)
    BUILD_LOG.write_text(output, encoding="utf-8")
    print(f"[build] Exit code: {proc.returncode}")
    return proc.returncode


def run_llm_fixer() -> int:
    """
    Invoke the existing LLM-based compile error fixer on the last build log.

    This uses the same CLI that ui_global_context_server.py calls:
      python fix_compile_errors.py <project_dir> <build_log_file>
    """
    if not FIX_SCRIPT.is_file():
        print(f"[fixer] ERROR: fix_compile_errors.py not found at {FIX_SCRIPT}")
        return 1
    if not BUILD_LOG.is_file():
        print(f"[fixer] ERROR: build log not found at {BUILD_LOG}")
        return 1

    cmd = [sys.executable, str(FIX_SCRIPT), str(PROJECT_DIR), str(BUILD_LOG)]
    print(f"[fixer] Running: {' '.join(cmd)} (cwd={ROOT})")
    proc = subprocess.run(cmd, cwd=str(ROOT))
    print(f"[fixer] Exit code: {proc.returncode}")
    return proc.returncode


def run_pre_build_fixers() -> None:
    """Run deterministic fixers before first build (same as ui_global_context_server pre-build)."""
    try:
        sys.path.insert(0, str(ROOT))
        from fix_jpql_repository_mismatch import run_fix
        count, _ = run_fix(PROJECT_DIR)
        if count > 0:
            print(f"[fixer] JPQL repository mismatch: {count} file(s) fixed.")
    except Exception:
        pass


def main() -> None:
    if not PROJECT_DIR.is_dir():
        print(f"[orchestrator] ERROR: Project directory not found: {PROJECT_DIR}")
        sys.exit(1)

    run_pre_build_fixers()

    for attempt in range(1, MAX_RETRIES + 1):
        print(f"\n=== Build attempt {attempt}/{MAX_RETRIES} ===")
        code = run_maven_build(clean=(attempt == 1))
        if code == 0:
            print("[orchestrator] Build succeeded.")
            return

        print("[orchestrator] Build failed; invoking LLM fixer...")
        fixer_code = run_llm_fixer()
        if fixer_code != 0:
            print(f"[orchestrator] Fixer failed with exit code {fixer_code}, stopping.")
            sys.exit(code or fixer_code)

    print(f"[orchestrator] Exhausted {MAX_RETRIES} attempts; build still failing.")
    sys.exit(1)


if __name__ == "__main__":
    main()

