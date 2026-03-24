# Migration pipeline – runs ui_global_context_server.py (Global Context UI)
#
# Build:
#   docker build -t migration-pipeline:1.0 .
#
# Run:
#   docker run -d --name migration-pipeline \
#     -p 8003:8003 -p 8081:8081 \
#     -e ANTHROPIC_API_KEY=... \
#     -e BIND_HOST=0.0.0.0 -e UI_PORT=8003 \
#     migration-pipeline:1.0
#
# Optional: mount repo for live edits
#   docker run ... -v $(pwd):/workspace migration-pipeline:1.0

FROM python:3.11-slim-bookworm

# Node.js 20, Maven, Java 17, Git (used by pipeline UI: Maven builds, git push, etc.)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    gnupg \
    ca-certificates \
    git \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz | tar xz -C /opt \
    && ln -sf /opt/apache-maven-3.9.6/bin/mvn /usr/local/bin/mvn \
    && apt-get install -y --no-install-recommends openjdk-17-jdk \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
    PIP_ROOT_USER_ACTION=ignore \
    UI_PORT=8003 \
    BIND_HOST=0.0.0.0

RUN useradd -m -u 1000 -s /bin/bash appuser

WORKDIR /workspace

COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip setuptools wheel \
    && pip install --no-cache-dir -r requirements.txt

COPY --chown=appuser:appuser . .

USER appuser

EXPOSE 8003 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fsS http://127.0.0.1:8003/api/health >/dev/null || exit 1

CMD ["python3", "ui_global_context_server.py"]
