#!/bin/bash
# Start the Global Context UI server and open in browser.
# Use this to ensure you're on the correct URL (http://127.0.0.1:8003/)

cd "$(dirname "$0")"

# Load .env so GITHUB_TOKEN and GIT_USE_TOKEN are available for Phase 4 (Push to repo)
if [ -f .env ]; then
  set -a
  source .env
  set +a
fi

PORT=${UI_PORT:-8003}
URL="http://127.0.0.1:${PORT}/"

echo "Starting Global Context UI server on port $PORT..."
echo "URL: $URL"
echo ""
echo "Opening in browser in 2 seconds..."
(sleep 2 && open "$URL" 2>/dev/null || xdg-open "$URL" 2>/dev/null || echo "Open manually: $URL") &
python3 ui_global_context_server.py
