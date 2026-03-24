#!/usr/bin/env python3
"""
Push the generated warranty_demo Spring Boot application to a remote Git repository.

Usage:
  python push_to_repo.py [--project-dir warranty_demo] [--branch migration/HS1210_20260206]

Environment:
  GITHUB_TOKEN or GIT_PUSH_TOKEN - Token with push access to the target repo
  GIT_USE_TOKEN=1 - Use token for auth (required when no credential helper)
  GIT_USER_EMAIL, GIT_USER_NAME - Git identity for commits (default: pipeline@scania.local)
  PUSH_TARGET_REPO - Override default repo URL
  CLONE_BRANCH - Branch to clone from (default: migration/HS1210_20260313_145001)
  Loads from .env in project root if present.
"""

import argparse
import os
import shutil
import subprocess
import sys
from datetime import datetime
from pathlib import Path

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

DEFAULT_REPO = "https://github.com/farcaz/scania-springboot-app.git"
BRANCH_PREFIX = "migration/"
# Branch to clone from (repo default). Use CLONE_BRANCH env to override.
DEFAULT_CLONE_BRANCH = "migration/HS1210_20260313_145001"

# Paths to exclude when copying (like .gitignore)
EXCLUDE_DIRS = {"target", ".git", ".idea", ".vscode", "node_modules", "__pycache__", ".push_tmp", ".venv", "venv"}
EXCLUDE_SUFFIXES = (".class", ".jar", ".log", ".tmp", ".swp", ".swo")


def get_token() -> str | None:
    return os.environ.get("GITHUB_TOKEN") or os.environ.get("GIT_PUSH_TOKEN")


def get_repo_url(repo_override: str | None) -> str:
    url = (repo_override or os.environ.get("PUSH_TARGET_REPO") or DEFAULT_REPO).strip()
    use_token = os.environ.get("GIT_USE_TOKEN", "").lower() in ("1", "true", "yes")
    token = get_token() if use_token else None
    if token and url.startswith("https://github.com/"):
        # x-access-token format is more reliable for GitHub PAT auth
        token = token.strip()
        url = url.replace("https://github.com/", f"https://x-access-token:{token}@github.com/", 1)
    return url


def should_exclude(path: Path, base: Path) -> bool:
    rel = path.relative_to(base)
    parts = rel.parts
    if any(p in EXCLUDE_DIRS for p in parts):
        return True
    if path.suffix.lower() in EXCLUDE_SUFFIXES:
        return True
    return False


def copy_project(src: Path, dst: Path) -> None:
    """Copy warranty_demo contents to dst, excluding build artifacts."""
    for item in src.rglob("*"):
        if item.is_file() and not should_exclude(item, src):
            rel = item.relative_to(src)
            dest_file = dst / rel
            dest_file.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(item, dest_file)


def push_to_repo(
    project_dir: Path,
    repo_url: str | None = None,
    branch_name: str | None = None,
) -> dict:
    """
    Clone target repo, copy project, commit, push.
    Returns dict with success, message, branch, error.
    """
    if not project_dir.is_dir():
        return {"success": False, "error": f"Project directory not found: {project_dir}"}

    url = get_repo_url(repo_url)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    branch = branch_name or f"{BRANCH_PREFIX}HS1210_{timestamp}"

    root = project_dir.parent
    tmp_dir = root / ".push_tmp"
    clone_dir = tmp_dir / "scania-java-v2"

    try:
        tmp_dir.mkdir(exist_ok=True)
        if clone_dir.exists():
            shutil.rmtree(clone_dir)

        # Clone from configured branch (or repo default)
        clone_branch = os.environ.get("CLONE_BRANCH", "").strip() or DEFAULT_CLONE_BRANCH
        proc = subprocess.run(
            ["git", "clone", "-b", clone_branch, "--depth", "1", url, str(clone_dir)],
            capture_output=True,
            text=True,
            timeout=180,
            cwd=str(root),
        )
        if proc.returncode != 0:
            err = (proc.stderr or proc.stdout or "").strip()
            return {"success": False, "error": f"Git clone failed: {err[:300]}"}

        # Clear clone, copy warranty_demo to root, create branch (matches manual_push.sh flow)
        for item in clone_dir.iterdir():
            if item.name == ".git":
                continue
            if item.is_dir():
                shutil.rmtree(item)
            else:
                item.unlink()

        copy_project(project_dir, clone_dir)

        # Set git identity so commit succeeds (required in Docker / fresh envs)
        git_email = os.environ.get("GIT_USER_EMAIL") or "pipeline@scania.local"
        git_name = os.environ.get("GIT_USER_NAME") or "Scania Migration Pipeline"
        subprocess.run(["git", "config", "user.email", git_email], capture_output=True, timeout=10, cwd=str(clone_dir))
        subprocess.run(["git", "config", "user.name", git_name], capture_output=True, timeout=10, cwd=str(clone_dir))

        proc = subprocess.run(
            ["git", "checkout", "-b", branch],
            capture_output=True,
            text=True,
            timeout=30,
            cwd=str(clone_dir),
        )
        if proc.returncode != 0:
            return {"success": False, "error": f"Git checkout -b failed: {(proc.stderr or proc.stdout or '')[:200]}"}

        subprocess.run(["git", "add", "-A"], capture_output=True, timeout=30, cwd=str(clone_dir))
        commit_msg = f"Full repo push @ {timestamp}" if project_dir == root else f"Migration push: warranty_demo @ {timestamp}"
        proc = subprocess.run(
            ["git", "commit", "-m", commit_msg],
            capture_output=True,
            text=True,
            timeout=30,
            cwd=str(clone_dir),
        )
        if proc.returncode != 0 and "nothing to commit" not in (proc.stdout or "").lower():
            return {"success": False, "error": f"Git commit failed: {(proc.stderr or proc.stdout or '')[:200]}"}

        # Ensure remote uses token URL and bypass credential helper (use URL auth only)
        subprocess.run(["git", "remote", "set-url", "origin", url], capture_output=True, timeout=10, cwd=str(clone_dir))
        force = os.environ.get("PUSH_FORCE", "").lower() in ("1", "true", "yes")
        push_cmd = ["git", "-c", "credential.helper=", "push", "-u", "origin", branch]
        if force:
            push_cmd.insert(-1, "--force")
        env = os.environ.copy()
        env["GIT_TERMINAL_PROMPT"] = "0"
        proc = subprocess.run(
            push_cmd,
            capture_output=True,
            text=True,
            timeout=180,
            cwd=str(clone_dir),
            env=env,
        )
        if proc.returncode != 0:
            err = (proc.stderr or proc.stdout or "").strip()
            hint = ""
            if "403" in err or "Permission" in err or "denied" in err:
                hint = " Token tip: Use a Classic PAT (repo scope) or ensure Fine-grained token has 'farcaz/scania-springboot-app' in Repository access + Contents: Read and Write."
            return {"success": False, "error": f"Git push failed: {err[:300]}{hint}"}

        return {
            "success": True,
            "message": f"Pushed to {branch}",
            "branch": branch,
            "repo": DEFAULT_REPO,
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


def verify_token() -> bool:
    """Test if GITHUB_TOKEN has access to the target repo."""
    url = get_repo_url(None)
    proc = subprocess.run(
        ["git", "ls-remote", url],
        capture_output=True,
        text=True,
        timeout=30,
        cwd=str(ROOT_DIR),
        env={**os.environ, "GIT_TERMINAL_PROMPT": "0"},
    )
    if proc.returncode == 0:
        print("✓ Token verified: can access repo")
        return True
    err = (proc.stderr or proc.stdout or "").strip()
    print(f"✗ Token verification failed: {err[:200]}", file=sys.stderr)
    return False


def main():
    parser = argparse.ArgumentParser(description="Push warranty_demo or full project to remote Git repository")
    parser.add_argument("--project-dir", default=None, help="Project directory (default: warranty_demo). Use . for full repo.")
    parser.add_argument("--full-repo", action="store_true", help="Push entire ScaniaRPG2JavaRevisedDesign project")
    parser.add_argument("--repo", default=None, help="Override repo URL")
    parser.add_argument("--branch", default=None, help="Branch name (default: migration/HS1210_<timestamp>)")
    parser.add_argument("--verify-token", action="store_true", help="Test token access and exit")
    args = parser.parse_args()

    if args.verify_token:
        sys.exit(0 if verify_token() else 1)

    root = Path(__file__).resolve().parent
    if args.full_repo or (args.project_dir and args.project_dir.strip() in (".", "")):
        project_dir = root
    else:
        project_dir = root / (args.project_dir or "warranty_demo")

    result = push_to_repo(project_dir, repo_url=args.repo, branch_name=args.branch)
    if result["success"]:
        print(f"✓ {result['message']}")
        print(f"  Branch: {result['branch']}")
        sys.exit(0)
    else:
        print(f"✗ Push failed: {result['error']}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
