#!/bin/bash
# Manual push to scania-java-v2 - uses your git credentials (gh auth, credential helper, etc.)
# Run from project root: ./manual_push.sh

set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
REPO="https://github.com/bhaveshchandrajha/scania-java-v2.git"
BRANCH="migration/HS1210_$(date +%Y%m%d_%H%M)"
TMP=.push_tmp/scania-java-v2
SRC=warranty_demo

echo "Cloning $REPO ..."
rm -rf "$TMP"
git clone --depth 1 "$REPO" "$TMP"
cd "$TMP"

# Replace clone content with warranty_demo (keep .git)
find . -mindepth 1 -maxdepth 1 ! -name .git -exec rm -rf {} +
cp -r "$ROOT/$SRC"/* .
rm -rf target 2>/dev/null || true

git checkout -b "$BRANCH"
git add -A
git status
git commit -m "Migration push: warranty_demo @ $(date +%Y%m%d_%H%M)" || echo "(nothing to commit)"
echo "Pushing to $BRANCH ..."
git push -u origin "$BRANCH"
cd ../..
rm -rf "$TMP"
echo "Done. Pushed to $BRANCH"
