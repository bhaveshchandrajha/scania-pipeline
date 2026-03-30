#!/bin/sh
# Ensure ANTHROPIC_API_KEY is in the environment before Python starts (so all child processes inherit it).
export PYTHONPATH="/workspace${PYTHONPATH:+:$PYTHONPATH}"
eval "$(python3 -c "
import os
import sys
from pathlib import Path
sys.path.insert(0, '/workspace')
os.chdir('/workspace')
from anthropic_env import load_anthropic_from_env_files
load_anthropic_from_env_files(Path('/workspace'))
k = (os.environ.get('ANTHROPIC_API_KEY') or '').strip()
if k:
    print('export ANTHROPIC_API_KEY=' + repr(k))
m = (os.environ.get('ANTHROPIC_MODEL') or '').strip()
if m:
    print('export ANTHROPIC_MODEL=' + repr(m))
")"
exec "$@"
