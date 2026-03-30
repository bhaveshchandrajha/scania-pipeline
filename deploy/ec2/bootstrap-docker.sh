#!/usr/bin/env bash
# Install Docker Engine + Compose plugin on Ubuntu (EC2).
# Run as root: sudo bash deploy/ec2/bootstrap-docker.sh
set -euo pipefail

if [[ "${EUID:-}" -ne 0 ]]; then
  echo "Run with sudo: sudo bash $0"
  exit 1
fi

apt-get update -y
apt-get install -y ca-certificates curl gnupg

install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${VERSION_CODENAME:-jammy}") stable" \
  >/etc/apt/sources.list.d/docker.list

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

systemctl enable --now docker

if id ubuntu &>/dev/null; then
  usermod -aG docker ubuntu
  echo "Added user 'ubuntu' to group docker. Re-login for it to take effect."
fi

docker --version
docker compose version
