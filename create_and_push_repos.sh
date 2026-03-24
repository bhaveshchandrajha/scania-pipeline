#!/bin/bash
#
# Create two GitHub repos (farcaz account) and push code:
#   1. Scania-frontend     <- warranty-ui (Angular frontend)
#   2. Scania-Migration-Pipeline <- migration pipeline (project minus warranty-ui)
#
# Prerequisites:
#   - GITHUB_TOKEN env var with repo scope (for creating repos)
#   - git configured with push access to github.com/farcaz
#
# Usage:
#   export GITHUB_TOKEN=ghp_...
#   ./create_and_push_repos.sh

set -e

GITHUB_USER="farcaz"
REPO_FRONTEND="Scania-frontend"
REPO_PIPELINE="Scania-Migration-Pipeline"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PUSH_TMP="${ROOT_DIR}/.push_tmp"

echo "=== Scania Repo Setup ==="
echo "User: $GITHUB_USER"
echo "Repos: $REPO_FRONTEND, $REPO_PIPELINE"
echo ""

# --- Create repos via GitHub API ---
if [ -z "$GITHUB_TOKEN" ]; then
  echo "WARNING: GITHUB_TOKEN not set. Skipping repo creation."
  echo "Create repos manually at https://github.com/new"
  echo "  - $REPO_FRONTEND (empty, no README)"
  echo "  - $REPO_PIPELINE (empty, no README)"
  echo ""
  CREATED=false
else
  echo "Creating repos on GitHub..."
  for REPO in "$REPO_FRONTEND" "$REPO_PIPELINE"; do
    if curl -s -o /dev/null -w "%{http_code}" -X POST \
      -H "Authorization: token $GITHUB_TOKEN" \
      -H "Accept: application/vnd.github.v3+json" \
      "https://api.github.com/user/repos" \
      -d "{\"name\":\"$REPO\",\"private\":false,\"description\":\"Scania $REPO\"}" | grep -qE "201|422"; then
      echo "  $REPO: OK (exists or created)"
    else
      echo "  $REPO: Failed (may already exist)"
    fi
  done
  CREATED=true
fi

# --- Push Scania-frontend (warranty-ui) ---
echo ""
echo "=== Pushing $REPO_FRONTEND (warranty-ui) ==="
mkdir -p "$PUSH_TMP"
FRONTEND_DIR="$PUSH_TMP/frontend"
rm -rf "$FRONTEND_DIR"
mkdir -p "$FRONTEND_DIR"

# Copy warranty-ui contents (exclude build artifacts)
(cd "$ROOT_DIR/warranty-ui" && tar cf - --exclude='node_modules' --exclude='dist' --exclude='.angular' --exclude='.vscode' .) | (cd "$FRONTEND_DIR" && tar xf -)

cd "$FRONTEND_DIR"
git init
git add -A
git commit -m "Initial commit: Angular warranty UI" || true
git branch -M main
git remote add origin "https://github.com/${GITHUB_USER}/${REPO_FRONTEND}.git"
FRONTEND_PUSH_OK=false
git push -u origin main 2>/dev/null && FRONTEND_PUSH_OK=true || {
  echo "Push failed. After creating the repo, run:"
  echo "  cd $FRONTEND_DIR && git push -u origin main"
}

# --- Push Scania-Migration-Pipeline ---
echo ""
echo "=== Pushing $REPO_PIPELINE ==="
cd "$ROOT_DIR"

# Use git archive + filter to exclude warranty-ui
PIPELINE_DIR="$PUSH_TMP/pipeline"
rm -rf "$PIPELINE_DIR"
mkdir -p "$PIPELINE_DIR"

# Export current tree excluding warranty-ui
git archive HEAD | tar -x -C "$PIPELINE_DIR"
# Remove warranty-ui from the export
rm -rf "$PIPELINE_DIR/warranty-ui" 2>/dev/null || true

cd "$PIPELINE_DIR"
git init
git add -A
git commit -m "Initial commit: Scania RPG-to-Java migration pipeline" || true
git branch -M main
git remote add origin "https://github.com/${GITHUB_USER}/${REPO_PIPELINE}.git"
PIPELINE_PUSH_OK=false
git push -u origin main 2>/dev/null && PIPELINE_PUSH_OK=true || {
  echo "Push failed. After creating the repo, run:"
  echo "  cd $PIPELINE_DIR && git push -u origin main"
}

# Cleanup temp dir only if both pushes succeeded
if [ "$FRONTEND_PUSH_OK" = true ] && [ "$PIPELINE_PUSH_OK" = true ]; then
  rm -rf "$PUSH_TMP"
fi
echo ""
echo "=== Done ==="
echo "  Frontend:  https://github.com/${GITHUB_USER}/${REPO_FRONTEND}"
echo "  Pipeline:  https://github.com/${GITHUB_USER}/${REPO_PIPELINE}"
