"""
Load ANTHROPIC_API_KEY from files when not already set in the process environment.

Used by the UI server and CLIs (migrate_to_pure_java, fix_compile_errors, etc.) so Docker,
compose, and manual runs all see the same resolution order.
"""

from __future__ import annotations

import os
from pathlib import Path

_PIPELINE_ROOT = Path(__file__).resolve().parent


def _parse_env_line(line: str) -> tuple[str, str] | None:
    line = line.replace("\r", "").strip()
    if not line or line.startswith("#"):
        return None
    if line.startswith("export "):
        line = line[7:].lstrip()
    if "=" not in line:
        return None
    key, _, val = line.partition("=")
    key = key.strip()
    val = val.strip().strip('"').strip("'")
    return (key, val) if key else None


def _normalize_api_key(raw: str) -> str:
    s = raw.replace("\ufeff", "").replace("\r\n", "\n").replace("\r", "\n").strip()
    if "\n" in s:
        s = s.split("\n", 1)[0].strip()
    return s


def _try_read_secret_file(path: Path) -> bool:
    try:
        raw = path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return False
    val = _normalize_api_key(raw)
    if val:
        os.environ["ANTHROPIC_API_KEY"] = val
        return True
    return False


def _try_read_dotenv(path: Path) -> bool:
    try:
        text = path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return False
    for line in text.splitlines():
        parsed = _parse_env_line(line)
        if not parsed:
            continue
        k, v = parsed
        if k == "ANTHROPIC_API_KEY":
            v = _normalize_api_key(v)
            if v:
                os.environ["ANTHROPIC_API_KEY"] = v
                return True
    return False


def load_anthropic_from_env_files(repo_root: Path | None = None) -> None:
    """
    If ANTHROPIC_API_KEY is unset or whitespace-only, try (in order):

    - ANTHROPIC_KEY_FILE (path to a file whose contents are the key)
    - /run/secrets/anthropic_api_key (Docker ``secrets:/run/secrets/...``)
    - ``<repo_root>/.env`` when ``repo_root`` is provided
    - /workspace/.env
    - ``<pipeline repo>/.env`` (directory containing this module)
    """
    if (os.environ.get("ANTHROPIC_API_KEY") or "").strip():
        return

    key_file = (os.environ.get("ANTHROPIC_KEY_FILE") or "").strip()
    if key_file and _try_read_secret_file(Path(key_file)):
        return
    if _try_read_secret_file(Path("/run/secrets/anthropic_api_key")):
        return
    if _try_read_secret_file(Path("/etc/scania/anthropic_api_key")):
        return

    seen: set[Path] = set()
    candidates: list[Path] = []
    if repo_root is not None:
        candidates.append((repo_root.resolve() / ".env"))
    candidates.append(Path("/workspace/.env"))
    candidates.append(_PIPELINE_ROOT / ".env")

    for p in candidates:
        try:
            rp = p.resolve()
        except OSError:
            rp = p
        if rp in seen:
            continue
        seen.add(rp)
        if _try_read_dotenv(rp):
            return
