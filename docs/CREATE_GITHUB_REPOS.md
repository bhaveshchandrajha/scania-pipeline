# Create and Push to GitHub Repos (farcaz)

This guide creates two new repositories and pushes code:

1. **Scania-frontend** — Angular warranty UI (`warranty-ui/`)
2. **Scania-Migration-Pipeline** — Migration pipeline (project minus `warranty-ui/`)

---

## Option A: Automated (with GitHub token)

1. Create a [GitHub Personal Access Token](https://github.com/settings/tokens) with `repo` scope.

2. Run:
   ```bash
   export GITHUB_TOKEN=ghp_your_token_here
   ./create_and_push_repos.sh
   ```

   The script will:
   - Create both repos on GitHub (if they don't exist)
   - Push `warranty-ui` to `Scania-frontend`
   - Push the migration pipeline to `Scania-Migration-Pipeline`

---

## Option B: Manual

### Step 1: Create repos on GitHub

1. Go to https://github.com/new
2. Create **Scania-frontend**:
   - Name: `Scania-frontend`
   - Description: `Scania warranty Angular frontend`
   - **Do not** add README, .gitignore, or license (empty repo)
3. Create **Scania-Migration-Pipeline**:
   - Name: `Scania-Migration-Pipeline`
   - Description: `Scania RPG-to-Java migration pipeline`
   - **Do not** add README, .gitignore, or license (empty repo)

### Step 2: Push Scania-frontend

```bash
cd .push_tmp/frontend   # or re-run script to regenerate
git remote add origin https://github.com/farcaz/Scania-frontend.git
git push -u origin main
```

If `.push_tmp` was cleaned up, run `./create_and_push_repos.sh` first (it creates the temp dirs).

### Step 3: Push Scania-Migration-Pipeline

```bash
cd .push_tmp/pipeline
git remote add origin https://github.com/farcaz/Scania-Migration-Pipeline.git
git push -u origin main
```

---

## Repo contents

| Repo | Contents |
|------|----------|
| **Scania-frontend** | `warranty-ui/` — Angular app (claims list, create claim, claim detail) |
| **Scania-Migration-Pipeline** | Python build scripts, `global_context/`, `context_index/`, `warranty_demo/`, `docs/`, `migrate_to_pure_java.py`, etc. (everything except `warranty-ui/`) |

---

## Troubleshooting

- **Push rejected (repo doesn't exist):** Create the repos first (Option B Step 1).
- **Authentication failed:** Use HTTPS with a token, or SSH: `git@github.com:farcaz/Scania-frontend.git`
- **Permission denied:** Ensure your GitHub user `farcaz` has push access to these repos.
