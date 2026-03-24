#!/usr/bin/env python3
"""
Check which Anthropic models the API key has access to.
Used to auto-select Claude Opus when available for highest-accuracy migration.

Returns: JSON with { "opus_available": bool, "opus_model": str|None, "models": [...] }
"""

import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent

# Opus model IDs (Anthropic may add new versions)
OPUS_MODELS = ("claude-opus-4-6", "claude-opus-4-5", "claude-opus-4", "claude-3-5-opus-20241022")


def check_models() -> dict:
    """Fetch available models and check for Opus access."""
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        return {"opus_available": False, "opus_model": None, "models": [], "error": "ANTHROPIC_API_KEY not set"}

    try:
        import urllib.request
        req = urllib.request.Request(
            "https://api.anthropic.com/v1/models",
            headers={
                "anthropic-version": "2023-06-01",
                "x-api-key": api_key,
            },
        )
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode())
    except Exception as e:
        return {"opus_available": False, "opus_model": None, "models": [], "error": str(e)}

    models = data.get("data", [])
    model_ids = {m.get("id", "").lower() for m in models if m.get("id")}

    opus_model = None
    for candidate in OPUS_MODELS:
        if candidate.lower() in model_ids:
            opus_model = candidate
            break

    return {
        "opus_available": opus_model is not None,
        "opus_model": opus_model,
        "models": [m.get("id", "") for m in models[:20]],
        "error": None,
    }


def main() -> None:
    result = check_models()
    print(json.dumps(result, indent=2))
    sys.exit(0 if result.get("opus_available") else 1)


if __name__ == "__main__":
    main()
