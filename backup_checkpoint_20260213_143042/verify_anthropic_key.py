#!/usr/bin/env python3
"""
Quick verification that ANTHROPIC_API_KEY is set and accepted by the API.
Run in the same terminal where you've set the key, or after: export ANTHROPIC_API_KEY='sk-ant-...'
"""
import os
import sys

def main():
    key = os.environ.get("ANTHROPIC_API_KEY")
    if not key:
        print("ANTHROPIC_API_KEY is not set.")
        print("Export it in this shell: export ANTHROPIC_API_KEY='sk-ant-...'")
        sys.exit(1)

    masked = key[:12] + "..." + key[-4:] if len(key) > 20 else "***"
    print(f"Key found (masked): {masked}")

    try:
        import anthropic
    except ImportError:
        print("anthropic package not installed. Run: pip install anthropic")
        sys.exit(1)

    client = anthropic.Anthropic()
    try:
        r = client.messages.create(
            model="claude-sonnet-4-5",
            max_tokens=50,
            messages=[{"role": "user", "content": "Reply with exactly: OK"}],
        )
        text = r.content[0].text.strip() if r.content else ""
        print(f"API response: {repr(text)}")
        print("Anthropic key is working.")
        sys.exit(0)
    except anthropic.AuthenticationError as e:
        print("Authentication failed (invalid or expired key):", e)
        sys.exit(1)
    except Exception as e:
        print(f"Error ({type(e).__name__}):", e)
        sys.exit(1)


if __name__ == "__main__":
    main()
