#!/usr/bin/env python3
"""
Push Maven Surefire test results to a remote Git repository.

Collects JUnit XML from target/surefire-reports/ and optionally a JSON summary,
then commits and pushes to a configurable test-results repo.

Usage:
  python push_test_results.py [--project-dir warranty_demo]

Environment:
  GITHUB_TOKEN or GIT_PUSH_TOKEN - Token with push access (GIT_USE_TOKEN=0 to disable embedding)
  GITHUB_TOKEN_FILE or GIT_PUSH_TOKEN_FILE - First line = PAT (CI/Docker/Kubernetes secrets)
  TEST_RESULTS_REPO - Override default repo URL
  TEST_RESULTS_BRANCH - Branch name (default: results)
  Loads from .env in project root if present.
"""

import json
import os
import shutil
import subprocess
import sys
from datetime import datetime
from pathlib import Path

from git_auth import get_github_push_token, server_credential_hint

ROOT_DIR = Path(__file__).resolve().parent


def _load_env():
    """Load .env from project root if present."""
    env_path = ROOT_DIR / ".env"
    if env_path.exists():
        for line in env_path.read_text(encoding="utf-8", errors="ignore").splitlines():
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                k, v = line.split("=", 1)
                v = v.strip().strip('"').strip("'")
                if k.strip() and v:
                    os.environ.setdefault(k.strip(), v)


_load_env()

DEFAULT_REPO = "https://github.com/bhaveshchandrajha/scania-generated-java.git"
DEFAULT_BRANCH_PREFIX = "migration/"


def get_repo_url(repo_override: str | None) -> str:
    url = (repo_override or os.environ.get("TEST_RESULTS_REPO") or DEFAULT_REPO).strip()
    token = get_github_push_token(ROOT_DIR)
    if (
        token
        and url.startswith("https://github.com/")
        and os.environ.get("GIT_USE_TOKEN", "").lower() not in ("0", "false", "no")
    ):
        token = token.strip()
        url = url.replace("https://github.com/", f"https://x-access-token:{token}@github.com/", 1)
    return url


def collect_surefire_reports(project_dir: Path) -> tuple[list[Path], dict | None]:
    """Collect XML reports and build a JSON summary."""
    surefire_dir = project_dir / "target" / "surefire-reports"
    if not surefire_dir.is_dir():
        return [], None

    xml_files = list(surefire_dir.glob("*.xml"))
    if not xml_files:
        return [], None

    import re
    total = failed = skipped = errors = 0
    for f in xml_files:
        try:
            text = f.read_text(encoding="utf-8", errors="ignore")
            for m in re.finditer(r'tests="(\d+)"', text):
                total += int(m.group(1))
            for m in re.finditer(r'failures="(\d+)"', text):
                failed += int(m.group(1))
            for m in re.finditer(r'skipped="(\d+)"', text):
                skipped += int(m.group(1))
            for m in re.finditer(r'errors="(\d+)"', text):
                errors += int(m.group(1))
        except Exception:
            pass
    passed = max(0, total - failed - errors - skipped)

    summary = {
        "passed": passed,
        "failed": failed,
        "skipped": skipped,
        "errors": errors,
        "total": total,
        "timestamp": datetime.now().isoformat(),
        "project": project_dir.name,
    }
    return xml_files, summary


def push_test_results(
    project_dir: Path,
    repo_url: str | None = None,
    branch: str | None = None,
) -> dict:
    """
    Collect Surefire reports, clone test-results repo, add reports, commit, push.
    Returns dict with success, message, branch, error.
    """
    if not project_dir.is_dir():
        return {"success": False, "error": f"Project directory not found: {project_dir}"}

    xml_files, summary = collect_surefire_reports(project_dir)
    if not xml_files:
        return {
            "success": False,
            "error": "No Surefire reports found. Run 'mvn test' first.",
        }

    url = get_repo_url(repo_url)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    branch_name = branch or os.environ.get("TEST_RESULTS_BRANCH") or f"{DEFAULT_BRANCH_PREFIX}{timestamp}"
    subdir = f"{project_dir.name}_{timestamp}"

    root = project_dir.parent
    tmp_dir = root / ".push_test_tmp"
    clone_dir = tmp_dir / "scania-test-results"

    try:
        tmp_dir.mkdir(exist_ok=True)
        if clone_dir.exists():
            shutil.rmtree(clone_dir)

        # Clone default branch (branch_name is new, won't exist yet)
        proc = subprocess.run(
            ["git", "clone", "--depth", "1", url, str(clone_dir)],
            capture_output=True,
            text=True,
            timeout=120,
            cwd=str(root),
            env={**os.environ, "GIT_TERMINAL_PROMPT": "0"},
        )
        if proc.returncode != 0:
            err = (proc.stderr or proc.stdout or "").strip()
            hint = ""
            if "terminal prompts disabled" in err.lower() or "could not read Username" in err:
                hint = (
                    " Set GITHUB_TOKEN, GIT_PUSH_TOKEN, or GITHUB_TOKEN_FILE. "
                    + server_credential_hint()
                )
            return {"success": False, "error": f"Git clone failed: {err[:300]}{hint}"}

        subprocess.run(
            ["git", "checkout", "-b", branch_name],
            capture_output=True,
            text=True,
            timeout=30,
            cwd=str(clone_dir),
        )

        out_dir = clone_dir / subdir
        out_dir.mkdir(parents=True, exist_ok=True)

        for src in xml_files:
            shutil.copy2(src, out_dir / src.name)

        if summary:
            (out_dir / "summary.json").write_text(
                json.dumps(summary, indent=2),
                encoding="utf-8",
            )

        subprocess.run(["git", "add", "-A"], capture_output=True, timeout=30, cwd=str(clone_dir))
        proc = subprocess.run(
            ["git", "commit", "-m", f"Test results: {project_dir.name} @ {timestamp}"],
            capture_output=True,
            text=True,
            timeout=30,
            cwd=str(clone_dir),
        )
        if proc.returncode != 0 and "nothing to commit" not in (proc.stdout or "").lower():
            return {"success": False, "error": f"Git commit failed: {(proc.stderr or proc.stdout or '')[:200]}"}

        proc = subprocess.run(
            ["git", "push", "origin", branch_name],
            capture_output=True,
            text=True,
            timeout=120,
            cwd=str(clone_dir),
            env={**os.environ, "GIT_TERMINAL_PROMPT": "0"},
        )
        if proc.returncode != 0:
            err = (proc.stderr or proc.stdout or "").strip()
            hint = ""
            if "terminal prompts disabled" in err.lower() or "could not read Username" in err:
                hint = (
                    " Set GITHUB_TOKEN, GIT_PUSH_TOKEN, or GITHUB_TOKEN_FILE. "
                    + server_credential_hint()
                )
            return {"success": False, "error": f"Git push failed: {err[:300]}{hint}"}

        return {
            "success": True,
            "message": f"Pushed test results to {branch_name}/{subdir}",
            "branch": branch_name,
            "subdir": subdir,
            "summary": summary,
        }
    except subprocess.TimeoutExpired:
        return {"success": False, "error": "Git operation timed out"}
    except Exception as e:
        return {"success": False, "error": str(e)[:300]}
    finally:
        if tmp_dir.exists():
            try:
                shutil.rmtree(tmp_dir)
            except Exception:
                pass


def main():
    parser = __import__("argparse").ArgumentParser(description="Push warranty_demo test results to remote Git")
    parser.add_argument("--project-dir", default="warranty_demo", help="Project directory")
    parser.add_argument("--repo", default=None, help="Override repo URL")
    parser.add_argument("--branch", default=None, help="Branch name")
    args = parser.parse_args()

    root = Path(__file__).resolve().parent
    project_dir = root / args.project_dir

    result = push_test_results(project_dir, repo_url=args.repo, branch=args.branch)
    if result["success"]:
        print(f"✓ {result['message']}")
        if result.get("summary"):
            s = result["summary"]
            print(f"  Tests: {s.get('passed', 0)} passed, {s.get('failed', 0)} failed, {s.get('skipped', 0)} skipped")
        sys.exit(0)
    else:
        print(f"✗ Push failed: {result['error']}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
