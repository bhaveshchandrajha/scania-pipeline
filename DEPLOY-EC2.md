# Deploy the migration pipeline on AWS EC2

This guide assumes you push this repository to GitHub and clone it on an EC2 instance (Ubuntu 22.04 LTS recommended).

## What gets exposed

| Port | Purpose |
|------|---------|
| **8003** | Global Context / migration pipeline UI (`ui_global_context_server.py`) |
| **8082** | Migrated Spring Boot app (host maps to container **8081**; `APP_PORT` in the UI) |
| 22 | SSH (admin only) |

Open **8003** and **8082** in the instance security group only to trusted IPs or your VPN. Do not expose them to `0.0.0.0/0` on the public internet without additional protection (HTTPS reverse proxy, auth, or IP allowlist).

## One-time: install Docker on the instance

On the EC2 host (Ubuntu):

```bash
sudo bash deploy/ec2/bootstrap-docker.sh
```

Log out and back in if your user was added to the `docker` group, or prefix Docker commands with `sudo`.

## Deploy the pipeline

```bash
sudo mkdir -p /opt/migration-pipeline
sudo chown "$USER:$USER" /opt/migration-pipeline
cd /opt/migration-pipeline

git clone <YOUR_GITHUB_REPO_URL> .
cp .env.example .env
nano .env   # set ANTHROPIC_API_KEY (required for migrate / LLM steps)
```

**Important:** `docker-compose-pipeline.yml` used to pass empty `ANTHROPIC_API_KEY` / `GITHUB_TOKEN` from the host and override `.env`. For EC2, use **`docker-compose.ec2.yml`**, which relies on `env_file: .env` only for secrets.

Start (build image and run detached):

```bash
docker compose -f docker-compose.ec2.yml up -d --build
```

Open in a browser:

- **Pipeline UI:** `http://<EC2_PUBLIC_DNS_OR_IP>:8003/`
- **Migrated app (after you run it in the UI):** `http://<EC2_PUBLIC_DNS_OR_IP>:8082/`

## Updates after you push to GitHub

```bash
cd /opt/migration-pipeline
git pull
docker compose -f docker-compose.ec2.yml up -d --build
```

The compose file mounts the repo at `/workspace`, so Python/UI changes apply after restart. Rebuild the image when `Dockerfile.pipeline`, `requirements.txt`, or OS-level dependencies change.

## Operations

- **Logs:** `docker logs -f migration-pipeline`
- **Health:** `curl -s http://127.0.0.1:8003/api/health`
- **Stop:** `docker compose -f docker-compose.ec2.yml down`

## GitHub Actions

`.github/workflows/pipeline-docker.yml` builds `Dockerfile.pipeline` on pushes to `main`/`master` so broken images are caught before deployment.

## Optional: HTTPS

Terminate TLS with a reverse proxy (nginx, Caddy, ALB) in front of ports 8003/8082, or use SSH port forwarding for demos:

```bash
ssh -L 8003:127.0.0.1:8003 -L 8082:127.0.0.1:8082 ec2-user@<host>
```

Then open `http://localhost:8003/` locally.
