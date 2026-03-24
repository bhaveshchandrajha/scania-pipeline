#!/bin/bash
# Start the Global Context UI server and open in browser.
# Use this to ensure you're on the correct URL (http://127.0.0.1:8003/)

cd "$(dirname "$0")"

# Extract 20260311.zip (PKS revised AST) to JSON_ast/JSON_20260311 if not present
if [ -f 20260311.zip ] && [ ! -d "JSON_ast/JSON_20260311" ]; then
  python3 setup_ast_20260311.py 2>/dev/null || true
fi

# Load .env so GITHUB_TOKEN and GIT_USE_TOKEN are available for Phase 4 (Push to repo)
if [ -f .env ]; then
  set -a
  source .env
  set +a
fi

# Ports: Global Context UI (8003), Warranty Demo (8081)
export UI_PORT=${UI_PORT:-8003}
export APP_PORT=${APP_PORT:-8081}
PORT=$UI_PORT
URL="http://0.0.0.0:${PORT}/"

echo "Starting Global Context UI server on port $PORT..."
echo "URL: $URL"
echo ""
echo "Warranty Demo: Tab 5 → Run Application → Open Demo (port $APP_PORT)"
echo "  - Demo UIs: demo.html, angular/, index"
echo "  - If claim creation fails: Tab 5 → Seed Demo Data (POST /api/seed)"
echo ""
echo "Opening in browser in 2 seconds..."
(sleep 2 && open "$URL" 2>/dev/null || xdg-open "$URL" 2>/dev/null || echo "Open manually: $URL") &
python3 ui_global_context_server.py
