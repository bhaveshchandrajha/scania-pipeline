"""
Resolve GitHub HTTPS credentials for non-interactive git (CI, Docker, servers).

Order: GIT_USE_TOKEN=0 skips PAT embedding; then GITHUB_TOKEN / GIT_PUSH_TOKEN;
then GITHUB_TOKEN_FILE / GIT_PUSH_TOKEN_FILE (first line = token); then `gh auth token`.
"""

from __future__ import annotations

import os
import subprocess
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parent


def read_token_file(path: str | None) -> str | None:
    if not path or not str(path).strip():
        return None
    p = Path(path).expanduser()
    if not p.is_file():
        return None
    try:
        line = p.read_text(encoding="utf-8", errors="ignore").splitlines()[0].strip()
        return line or None
    except OSError:
        return None


def get_github_push_token(root_dir: Path | None = None) -> str | None:
    """Return a PAT for https://github.com/ when embedding in the remote URL is allowed."""
    if os.environ.get("GIT_USE_TOKEN", "").lower() in ("0", "false", "no"):
        return None
    t = (os.environ.get("GITHUB_TOKEN") or os.environ.get("GIT_PUSH_TOKEN") or "").strip()
    if t:
        return t
    for key in ("GITHUB_TOKEN_FILE", "GIT_PUSH_TOKEN_FILE"):
        t = read_token_file(os.environ.get(key))
        if t:
            return t
    root = root_dir or ROOT_DIR
    try:
        proc = subprocess.run(
            ["gh", "auth", "token"],
            capture_output=True,
            text=True,
            timeout=8,
            cwd=str(root),
        )
        if proc.returncode == 0 and (proc.stdout or "").strip():
            return proc.stdout.strip()
    except (FileNotFoundError, subprocess.TimeoutExpired, OSError):
        pass
    return None


def server_credential_hint() -> str:
    """Extra line for errors when CI/servers have no Keychain."""
    if os.environ.get("CI") or os.environ.get("CI_SERVER") or os.environ.get("CONTINUOUS_INTEGRATION"):
        return (
            " On CI/servers, export GITHUB_TOKEN (or GIT_PUSH_TOKEN) from your secret store, "
            "or set GITHUB_TOKEN_FILE to a file containing the PAT (e.g. Docker/Kubernetes secret mount)."
        )
    return ""
