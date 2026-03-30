#!/usr/bin/env python3
"""
Standalone UI server for the Global Context / Knowledge Graph builder.

This is intentionally separate from ui_server.py so that the existing
Pure Java Pipeline UI (served at /pipeline on port 8002) remains
unchanged.

Usage:
  UI_PORT=8003 python3 ui_global_context_server.py

Then open:
  http://localhost:8003/

Endpoints:
  GET  /                       -> ui_global_context.html
  GET  /ui_global_context.html -> ui_global_context.html
  GET  /api/discover-directories
  GET  /api/list-programs
  POST /api/build-global-context
  POST /api/export-neo4j
  POST /api/migrate-feature      (generates Java + ui-schemas from displayFiles)
  POST /api/regenerate-ui-schema (regenerate ui-schemas without full migration)
  POST /api/build-application
"""

import json
import os
import subprocess
import sys
import threading
import time
from http.server import HTTPServer, BaseHTTPRequestHandler
from pathlib import Path
from urllib.parse import urlparse, parse_qs
from urllib.request import Request, urlopen
from urllib.error import URLError, HTTPError

ROOT_DIR = Path(__file__).resolve().parent


def _ensure_anthropic_key_from_workspace_env() -> None:
    """
    If ANTHROPIC_API_KEY is unset, try workspace .env (manual EC2/docker-compose or bind-mounted repo).
    Does not override a non-empty env var.
    """
    if (os.environ.get("ANTHROPIC_API_KEY") or "").strip():
        return
    paths: list[Path] = []
    extra = (os.environ.get("ANTHROPIC_KEY_FILE") or "").strip()
    if extra:
        paths.append(Path(extra))
    paths.extend([ROOT_DIR / ".env", Path("/workspace/.env")])
    for p in paths:
        if not p.is_file():
            continue
        try:
            text = p.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for line in text.splitlines():
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            if line.startswith("ANTHROPIC_API_KEY="):
                val = line.split("=", 1)[1].strip().strip('"').strip("'")
                if val:
                    os.environ["ANTHROPIC_API_KEY"] = val
                    return


BUILD_OUTPUT_MAX_CHARS = 80_000

# Module-level: running Spring Boot process (for /api/run-application)
_app_process = None

MATCH_PRIORITY = {"Chain": 1, "Read": 2, "Write": 3, "Update": 4, "SetLL": 5, "If": 6, "Eval": 7, "ExSr": 8}


def _truncate_build_output(raw: str) -> str:
    if not raw or len(raw) <= BUILD_OUTPUT_MAX_CHARS:
        return raw or ""
    tail = raw[-BUILD_OUTPUT_MAX_CHARS:]
    if not tail.startswith("\n"):
        tail = "\n" + tail
    return f"... (earlier output omitted, showing last {BUILD_OUTPUT_MAX_CHARS} chars)\n" + tail


def _run_maven_build_with_autofix(proj_path: Path, progress_callback=None, hitl_mode: bool = False) -> dict:
    """
    Run Maven build in proj_path with an agentic self-correction loop:

    - Run Maven once.
    - If there are filename/public-type mismatches, try to fix them and rebuild.
    - If still failing, call the LLM-based compile error fixer script and rebuild.
    - Repeat the LLM fixer + rebuild cycle up to MAX_LLM_PASSES times.

    progress_callback(msg: str) is called with status updates to keep connections alive.
    Returns a dict with buildSuccess, buildExitCode, buildOutput.
    """

    def _progress(msg: str) -> None:
        if progress_callback:
            try:
                progress_callback(msg)
            except Exception:
                pass

    MAX_LLM_PASSES = 4

    def _run_build_once(use_clean: bool = False, full_package: bool = False) -> tuple[int | None, str]:
        try:
            # test-compile includes compile; catches test source errors (e.g. int->BigDecimal) for fix loop
            if full_package:
                cmd = ["mvn", "-f", "pom.xml", "-T", "1C", "-q", "clean", "package", "-DskipTests"]
            elif use_clean:
                cmd = ["mvn", "-f", "pom.xml", "-T", "1C", "-q", "clean", "test-compile"]
            else:
                cmd = ["mvn", "-f", "pom.xml", "-T", "1C", "-q", "test-compile"]
            proc = subprocess.run(
                cmd,
                cwd=str(proj_path),
                capture_output=True,
                text=True,
                timeout=300,
            )
            output = (proc.stdout or "") + (proc.stderr or "")
            return proc.returncode, output
        except Exception as e:
            return None, f"Build failed to start: {e}"

    def _autofix_filename_issues(build_output: str) -> bool:
        """
        Look for errors like 'interface X is public, should be declared in a file named X.java'
        and either rename the file or delete the misnamed one if a correct file already exists.
        """
        changed = False
        for line in build_output.splitlines():
            if "should be declared in a file named " not in line:
                continue
            try:
                # Example line:
                # [ERROR] /path/HSAHWPF Repository.java:[21,8] interface HSAHWPFRepository is public, should be declared in a file named HSAHWPFRepository.java
                if "] " in line:
                    _, rest = line.split("] ", 1)
                else:
                    rest = line
                path_part = rest.split(":", 1)[0].strip()
                marker = "should be declared in a file named "
                idx = line.index(marker) + len(marker)
                recommended = line[idx:].strip()
                # Drop trailing punctuation if any
                if recommended.endswith("."):
                    recommended = recommended[:-1]
                if not recommended.endswith(".java"):
                    continue
                orig_path = Path(path_part)
                # If path was absolute in log, use as is; otherwise resolve from project root
                if not orig_path.is_absolute():
                    orig_path = proj_path / orig_path
                target_path = orig_path.with_name(recommended)

                if orig_path.exists():
                    if target_path.exists():
                        # Correctly named file already exists; delete the misnamed one
                        orig_path.unlink()
                        changed = True
                    else:
                        # Rename misnamed file to recommended Java filename
                        orig_path.rename(target_path)
                        changed = True
            except Exception:
                continue
        return changed

    # Pre-build: resilient pipeline fixers
    try:
        from fix_idclass import run_fix as run_idclass_fix
        count, _ = run_idclass_fix(proj_path)
        if count > 0:
            _progress(f"IdClass fixer: {count} entity(ies) fixed.")
    except Exception:
        pass
    try:
        from fix_test_alignment import run_fix as run_test_alignment
        count, _ = run_test_alignment(proj_path)
        if count > 0:
            _progress(f"Test alignment: {count} file(s) fixed.")
    except Exception:
        pass
    try:
        from fix_ambiguous_mapping import run_fix as run_ambiguous_mapping_fix
        count, _ = run_ambiguous_mapping_fix(proj_path)
        if count > 0:
            _progress(f"Ambiguous mapping fixer: {count} endpoint(s) fixed.")
    except Exception:
        pass
    try:
        from fix_int_bigdecimal import run_fix as run_int_bigdecimal_fix
        count, _ = run_int_bigdecimal_fix(proj_path)
        if count > 0:
            _progress(f"Int/BigDecimal fixer: {count} file(s) fixed.")
    except Exception:
        pass
    try:
        from fix_jpql_repository_mismatch import run_fix as run_jpql_mismatch_fix
        count, _ = run_jpql_mismatch_fix(proj_path)
        if count > 0:
            _progress(f"JPQL repository mismatch fixer: {count} file(s) fixed.")
    except Exception:
        pass

    # Initial build (clean compile for fresh state) + filename mismatch autofix
    _progress("Running Maven compile...")
    exit_code, output = _run_build_once(use_clean=True)
    combined_output = output

    if exit_code is not None and exit_code != 0:
        if _autofix_filename_issues(output):
            exit_code2, output2 = _run_build_once(use_clean=True)
            combined_output = (combined_output or "") + "\n\n=== Rebuild after filename autofix ===\n\n" + (output2 or "")
            exit_code = exit_code2

    # Iterative LLM-based correction loop (compile-only, no clean = faster)
    llm_pass = 0
    suggested_fixes = None
    while exit_code is not None and exit_code != 0 and llm_pass < MAX_LLM_PASSES:
        llm_pass += 1
        use_propose_only = hitl_mode  # always show HITL on every failure when in HITL mode
        _progress(f"Running LLM compile fixer (pass {llm_pass}/{MAX_LLM_PASSES}){' [propose-only for HITL]' if use_propose_only else ''}...")
        try:
            log_path = proj_path / "target" / f"last_compile_errors_pass{llm_pass}.txt"
            log_path.parent.mkdir(parents=True, exist_ok=True)
            log_path.write_text(combined_output or "", encoding="utf-8", errors="ignore")

            # Call the LLM-based fixer script (with --propose-only for HITL on first pass)
            cmd = [sys.executable, str(ROOT_DIR / "fix_compile_errors.py"), str(proj_path), str(log_path)]
            if use_propose_only:
                cmd.append("--propose-only")
            proc = subprocess.run(
                cmd,
                cwd=str(ROOT_DIR),
                capture_output=True,
                text=True,
                timeout=900,
            )
            combined_output = (
                (combined_output or "")
                + f"\n\n=== LLM compile error fixer output (pass {llm_pass}) ===\n\n"
                + (proc.stdout or "")
                + (proc.stderr or "")
            )

            if use_propose_only:
                # HITL: parse JSON from stdout, return suggested fixes without applying
                try:
                    for line in (proc.stdout or "").splitlines():
                        line = line.strip()
                        if line.startswith("{"):
                            data = json.loads(line)
                            if data.get("proposeOnly") and data.get("suggestedFixes"):
                                suggested_fixes = data["suggestedFixes"]
                                break
                except Exception:
                    pass
                if suggested_fixes:
                    return {
                        "buildSuccess": False,
                        "buildExitCode": exit_code,
                        "buildOutput": combined_output,
                        "testOutput": "",
                        "testSummary": None,
                        "needsReview": True,
                        "suggestedFixes": suggested_fixes,
                        "errorSummary": "Compilation failed. Review suggested fixes and apply to continue.",
                    }
        except Exception as e:
            combined_output = (combined_output or "") + f"\n\n(LLM compile error fixer failed on pass {llm_pass}: {e})"
            break

        # Rebuild after LLM corrections (compile only, incremental)
        _progress(f"Rebuilding after LLM pass {llm_pass}...")
        exit_code, output_after_llm = _run_build_once()
        combined_output = (
            (combined_output or "")
            + f"\n\n=== Rebuild after LLM corrections (pass {llm_pass}) ===\n\n"
            + (output_after_llm or "")
        )

        if exit_code == 0:
            break

    # After successful compile: package (jar) + run tests
    # Gate: build is only successful if compile, package, AND tests all pass.
    # SKIP_TESTS=1: skip test compile & run (use when tests are out of sync after migration).
    skip_tests = os.environ.get("SKIP_TESTS", "").lower() in ("1", "true", "yes")
    test_output = ""
    test_summary = None
    package_ok = False
    test_ok = False
    max_test_fix_passes = 2  # Retry once after ambiguous-mapping fix
    if exit_code == 0:
        for test_fix_pass in range(max_test_fix_passes):
            try:
                # -Dmaven.test.skip=true skips test-compile AND test execution (avoids test/domain mismatch)
                # When skip_tests=False, pass no flag so mvn package runs tests
                mvn_args = ["mvn", "-f", "pom.xml", "-T", "1C", "-q", "package"]
                if skip_tests:
                    mvn_args.append("-Dmaven.test.skip=true")
                proc = subprocess.run(
                    mvn_args,
                    cwd=str(proj_path),
                    capture_output=True,
                    text=True,
                    timeout=180,
                )
                pack_out = (proc.stdout or "") + (proc.stderr or "")
                if proc.returncode != 0:
                    test_output = pack_out
                    # If package/tests failed with Ambiguous mapping, try fix and retry
                    if "Ambiguous mapping" in (pack_out or "") and test_fix_pass < max_test_fix_passes - 1:
                        try:
                            from fix_ambiguous_mapping import run_fix as run_ambiguous_mapping_fix
                            count, _ = run_ambiguous_mapping_fix(proj_path, pack_out)
                            if count > 0:
                                _progress(f"Ambiguous mapping fixer: {count} endpoint(s) fixed. Retrying...")
                                continue
                        except Exception:
                            pass
                    break
                package_ok = True
                if skip_tests:
                    test_ok = True
                    test_summary = "Tests skipped (SKIP_TESTS=1)"
                    break
                # When skip_tests=False, mvn package already ran tests; use its output
                test_output = pack_out
                test_ok = True  # package succeeded so tests passed
                for line in (test_output or "").splitlines():
                    if "Tests run:" in line and "Failures:" in line:
                        test_summary = line.strip()
                        break
                break
                # If tests failed with Ambiguous mapping, try fix and retry
                if "Ambiguous mapping" in (test_output or "") and test_fix_pass < max_test_fix_passes - 1:
                    try:
                        from fix_ambiguous_mapping import run_fix as run_ambiguous_mapping_fix
                        count, _ = run_ambiguous_mapping_fix(proj_path, test_output)
                        if count > 0:
                            _progress(f"Ambiguous mapping fixer (post-test): {count} endpoint(s) fixed. Retrying tests...")
                            continue
                    except Exception:
                        pass
                break
            except Exception as e:
                test_output = f"Test run failed: {e}"
                break

    # Build succeeds only if compile, package, and tests all pass
    build_success = (
        exit_code == 0 if exit_code is not None else False
    ) and package_ok and test_ok

    return {
        "buildSuccess": build_success,
        "buildExitCode": exit_code,
        "buildOutput": combined_output,
        "testOutput": test_output,
        "testSummary": test_summary,
        "needsReview": False,
        "suggestedFixes": None,
    }


def _format_value_short(value) -> str:
    """Format an AST value/expression tree into a compact string."""
    if value is None:
        return "..."
    if isinstance(value, str):
        return value
    if isinstance(value, (int, float)):
        return str(value)
    if isinstance(value, dict):
        name = value.get("name", "")
        if name:
            return name.replace("sym.var.", "")
        val = value.get("value", "")
        if val:
            return str(val)
        return "..."
    if isinstance(value, list):
        if len(value) == 0:
            return "..."
        if len(value) >= 2 and isinstance(value[0], str) and value[0] == "ASSIGN":
            parts = value[1] if isinstance(value[1], list) else value[1:]
            return _format_value_short(parts)
        parts = []
        for item in value:
            if isinstance(item, str) and item in ("ADD", "SUB", "MULT", "DIV", "AND", "OR", "EQ", "NE", "LT", "GT", "LE", "GE"):
                ops = {"ADD": "+", "SUB": "-", "MULT": "*", "DIV": "/", "AND": "AND", "OR": "OR",
                       "EQ": "=", "NE": "<>", "LT": "<", "GT": ">", "LE": "<=", "GE": ">="}
                parts.append(ops.get(item, item))
            elif isinstance(item, list):
                parts.append(_format_value_short(item))
            elif isinstance(item, dict):
                parts.append(_format_value_short(item))
            elif isinstance(item, str):
                parts.append(item.replace("sym.var.", ""))
        return " ".join(parts)
    return "..."


class GlobalContextHandler(BaseHTTPRequestHandler):
    server_version = "GlobalContextServer/0.1"

    def log_message(self, format, *args):
        # Keep stdout clean; log to stderr
        sys.stderr.write("%s - - [%s] %s\n" % (self.address_string(), self.log_date_time_string(), format % args))

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(204)
        self.end_headers()

    def _send_json(self, obj, status=200):
        data = json.dumps(obj).encode("utf-8")
        try:
            self.send_response(status)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
        except (BrokenPipeError, ConnectionResetError):
            pass  # Client disconnected; avoid traceback

    def _write_chunked_line(self, line: str) -> None:
        """Write a single newline-delimited JSON line as a chunked chunk. Keeps connection alive."""
        data = (line + "\n").encode("utf-8")
        try:
            self.wfile.write(f"{len(data):x}\r\n".encode("ascii"))
            self.wfile.write(data)
            self.wfile.write(b"\r\n")
            self.wfile.flush()
        except (BrokenPipeError, ConnectionResetError):
            pass  # Client disconnected; avoid traceback

    def _start_chunked_response(self, content_type: str = "application/x-ndjson; charset=utf-8") -> None:
        """Start a chunked response. Call _write_chunked_line for each chunk, then _end_chunked_response."""
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Transfer-Encoding", "chunked")
        self.end_headers()

    def _end_chunked_response(self) -> None:
        """End chunked response with 0-length chunk."""
        try:
            self.wfile.write(b"0\r\n\r\n")
            self.wfile.flush()
        except (BrokenPipeError, ConnectionResetError):
            pass

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path in ("/", "/ui_global_context.html"):
            ui_path = ROOT_DIR / "ui_global_context.html"
            if not ui_path.exists():
                self.send_error(404, "ui_global_context.html not found")
                return
            content = ui_path.read_bytes()
            app_port = os.environ.get("APP_PORT", "8081")
            content = content.replace(b"__APP_PORT__", app_port.encode())
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.send_header("Cache-Control", "no-cache")
            self.end_headers()
            self.wfile.write(content)
            return

        if parsed.path == "/api/health":
            port = self.server.server_address[1] if self.server else 8003
            self._send_json({"ok": True, "server": "global-context", "port": port})
            return

        if parsed.path == "/api/discover-directories":
            # Discover AST directories under JSON_ast and suggest RPG dirs/zip files
            ast_root = ROOT_DIR / "JSON_ast"
            ast_dirs = []
            dds_ast_dirs = []
            rpg_dirs = []
            zip_files = []

            if ast_root.is_dir():
                for sub in sorted(ast_root.iterdir()):
                    if not sub.is_dir():
                        continue
                    ast_files = list(sub.glob("**/*-ast.json"))
                    if ast_files:
                        rel = f"JSON_ast/{sub.name}"
                        label = " (PKS revised, column redundancy fix)" if "20260311" in sub.name else None
                        ast_dirs.append({
                            "path": rel,
                            "label": label,
                            "fileCount": len(ast_files),
                        })

            # Look for RPG-like roots inside project (optional)
            for candidate in ROOT_DIR.iterdir():
                if not candidate.is_dir():
                    continue
                # Heuristic: directories that contain HSSRC/QRPGLESRC are likely RPG roots
                if (candidate / "HSSRC" / "QRPGLESRC").is_dir():
                    rpg_dirs.append(str(candidate))

            # Look for PoC and dated AST zip files (20260311, 20260227)
            for zip_path in ROOT_DIR.glob("PoC_*.zip"):
                zip_files.append({"path": str(zip_path)})
            for zip_path in ROOT_DIR.glob("202*.zip"):
                zip_files.append({"path": str(zip_path)})

            # Prefer JSON_20260311 (PKS revised AST) as default when present
            default_ast = "JSON_ast/JSON_20260227"
            if ast_dirs:
                json_20260311 = next((d["path"] for d in ast_dirs if "20260311" in d["path"]), None)
                default_ast = json_20260311 or ast_dirs[-1]["path"]

            result = {
                "astDirs": ast_dirs,
                "ddsAstDirs": dds_ast_dirs,
                "rpgDirs": rpg_dirs,
                "defaultAstDir": default_ast,
                "defaultRpgDir": rpg_dirs[0] if rpg_dirs else None,
            }
            if zip_files:
                result["zipFiles"] = zip_files
                if not rpg_dirs:
                    result["suggestion"] = f"Found {zip_files[0]['path']} - extract it to create an RPG directory"

            self._send_json(result)
            return

        if parsed.path == "/api/list-programs":
            # List programs and their procedures/subroutines for the Migrate Feature UI
            programs_dir = ROOT_DIR / "global_context" / "programs"
            result = {"programs": []}
            if programs_dir.is_dir():
                for ctx_path in sorted(programs_dir.glob("*.program.json")):
                    try:
                        ctx = json.loads(ctx_path.read_text(encoding="utf-8", errors="ignore"))
                    except Exception:
                        continue
                    program_id = ctx.get("programId") or ctx_path.stem.replace(".program", "")
                    unit_id = ctx.get("unitId")
                    nodes = []
                    for n in ctx.get("nodes", []):
                        if not isinstance(n, dict):
                            continue
                        kind = n.get("kind")
                        if kind not in ("Procedure", "Subroutine"):
                            continue
                        nodes.append({
                            "nodeId": n.get("id"),
                            "kind": kind,
                            "name": n.get("name"),
                            "procedureName": n.get("procedureName"),
                        })
                    result["programs"].append({
                        "programId": program_id,
                        "unitId": unit_id,
                        "nodes": nodes,
                    })
            self._send_json(result)
            return

        if parsed.path == "/api/check-models":
            try:
                from check_anthropic_models import check_models
                result = check_models()
                self._send_json(result)
            except Exception as e:
                self._send_json({"opus_available": False, "opus_model": None, "error": str(e)})
            return

        if parsed.path == "/api/build-context":
            project_dir = "warranty_demo"
            proj_path = ROOT_DIR / project_dir
            java_count = 0
            if proj_path.is_dir():
                java_root = proj_path / "src" / "main" / "java"
                if java_root.is_dir():
                    java_count = len(list(java_root.rglob("*.java")))
            last_migrated = None
            migrations_dir = ROOT_DIR / "global_context" / "migrations"
            if migrations_dir.is_dir():
                manifests = list(migrations_dir.glob("*.json"))
                if manifests:
                    latest = max(manifests, key=lambda p: p.stat().st_mtime)
                    try:
                        mf = json.loads(latest.read_text(encoding="utf-8", errors="ignore"))
                        last_migrated = {
                            "programId": mf.get("programId"),
                            "entryNodeId": mf.get("entryNodeId"),
                            "generatedCount": len(mf.get("generatedFiles") or []),
                            "uiSchemaCount": len(mf.get("uiSchemaGenerated") or []),
                            "timestamp": latest.stem.split("_")[-1] if "_" in latest.stem else latest.name,
                        }
                    except Exception:
                        pass
            self._send_json({
                "projectDir": project_dir,
                "javaFileCount": java_count,
                "lastMigrated": last_migrated,
            })
            return

        if parsed.path == "/api/customize-guide":
            customize_path = ROOT_DIR / "CUSTOMIZE.md"
            if customize_path.is_file():
                content = customize_path.read_text(encoding="utf-8", errors="replace")
                escaped = content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")
                html = f"""<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Customize Guide</title>
<style>body{{font-family:system-ui;max-width:720px;margin:24px auto;padding:0 16px;color:#e5e7eb;background:#0f172a;}}
pre{{background:#1e293b;padding:12px;border-radius:6px;overflow:auto;}} code{{background:#1e293b;padding:2px 6px;border-radius:4px;}}
a{{color:#60a5fa;}}</style></head><body><pre style="white-space:pre-wrap;">{escaped}</pre></body></html>"""
                self.send_response(200)
                self.send_header("Content-Type", "text/html; charset=utf-8")
                self.send_header("Content-Length", str(len(html.encode("utf-8"))))
                self.end_headers()
                self.wfile.write(html.encode("utf-8"))
            else:
                self.send_error(404, "CUSTOMIZE.md not found")
            return

        if parsed.path == "/traceability":
            ui_path = ROOT_DIR / "ui_traceability.html"
            if not ui_path.exists():
                self.send_error(404, "ui_traceability.html not found")
                return
            content = ui_path.read_bytes()
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
            return

        # /api/traceability-data/<programId>/<nodeId>
        trace_match = None
        if parsed.path.startswith("/api/traceability-data/"):
            parts = parsed.path.split("/")
            if len(parts) >= 5:
                trace_match = (parts[3], parts[4])

        if trace_match:
            program_id, entry_node_id = trace_match
            result = self._build_traceability_response(program_id, entry_node_id)
            if "error" in result:
                self._send_json(result, status=result.get("status", 404))
            else:
                self._send_json(result)
            return

        if parsed.path == "/api/run-application-log":
            proj_path = ROOT_DIR / "warranty_demo"
            log_path = proj_path / "target" / "spring-boot-run.log"
            qs = parse_qs(parsed.query or "")
            lines = 80
            if "lines" in qs and qs["lines"]:
                try:
                    lines = min(max(1, int(qs["lines"][0])), 200)
                except (ValueError, TypeError):
                    pass
            if not log_path.is_file():
                self._send_json({"logTail": "", "error": "Log file not found. Run the application first."})
                return
            try:
                with open(log_path, "r", encoding="utf-8", errors="ignore") as f:
                    all_lines = f.readlines()
                tail = "".join(all_lines[-lines:])
                self._send_json({"logTail": tail[-8000:] if len(tail) > 8000 else tail})
            except Exception as e:
                self._send_json({"logTail": "", "error": str(e)})
            return

        # Proxy to warranty app (avoids CORS when UI is on 8003 and app on 8081)
        app_port = os.environ.get("APP_PORT", "8081")
        app_base = f"http://127.0.0.1:{app_port}"
        if parsed.path == "/api/proxy/app-status":
            try:
                req = Request(app_base + "/demo.html", method="GET")
                with urlopen(req, timeout=5) as resp:
                    self._send_json({"ok": True, "status": resp.status})
            except (URLError, HTTPError, OSError) as e:
                self._send_json({"ok": False, "error": str(e)}, status=502)
            return

        if parsed.path == "/api/knowledge-graph-data":
            self._send_json(self._build_graph_data())
            return

        if parsed.path == "/code-origin":
            html_path = ROOT_DIR / "ui_code_origin.html"
            if html_path.is_file():
                content = html_path.read_bytes()
                self.send_response(200)
                self.send_header("Content-Type", "text/html; charset=utf-8")
                self.send_header("Content-Length", str(len(content)))
                self.end_headers()
                self.wfile.write(content)
            else:
                self.send_error(404, "ui_code_origin.html not found")
            return

        code_origin_match = None
        if parsed.path.startswith("/api/code-origin-data/"):
            parts = parsed.path.split("/")
            if len(parts) >= 5:
                code_origin_match = (parts[3], parts[4])
        if code_origin_match:
            result = self._build_code_origin_response(*code_origin_match)
            if result.get("status") == 404:
                self.send_error(404, result.get("error", "Not found"))
            else:
                self._send_json(result)
            return

        # Fallback 404 for unknown GET paths
        self.send_error(404, "Not Found")

    def _build_graph_data(self) -> dict:
        """Build a graph structure for interactive visualization from knowledge graph artifacts."""
        graph_nodes = []
        graph_edges = []
        node_ids = set()

        # 1. Programs from program contexts
        programs_dir = ROOT_DIR / "global_context" / "programs"
        program_data = {}
        if programs_dir.is_dir():
            for ctx_path in sorted(programs_dir.glob("*.program.json")):
                try:
                    ctx = json.loads(ctx_path.read_text(encoding="utf-8", errors="ignore"))
                except Exception:
                    continue
                pid = ctx.get("programId") or ctx_path.stem.replace(".program", "")
                program_data[pid] = ctx
                nid = f"prog:{pid}"
                node_ids.add(nid)
                ast_nodes = ctx.get("nodes", [])
                procs = [n for n in ast_nodes if isinstance(n, dict) and n.get("kind") in ("Procedure", "Subroutine")]
                graph_nodes.append({
                    "id": nid, "label": pid, "type": "program",
                    "title": f"Program {pid}\n{len(ast_nodes)} AST nodes\n{len(procs)} procedures/subroutines",
                })

                # Add procedure/subroutine nodes
                for n in procs:
                    n_id = n.get("id", "")
                    name = n.get("procedureName") or n.get("name") or n_id
                    sub_nid = f"node:{pid}:{n_id}"
                    if sub_nid in node_ids:
                        continue
                    node_ids.add(sub_nid)
                    graph_nodes.append({
                        "id": sub_nid, "label": name or n_id,
                        "type": "procedure" if n.get("kind") == "Procedure" else "subroutine",
                        "title": f"{n.get('kind')} {name}\nNode: {n_id}\nProgram: {pid}",
                    })
                    graph_edges.append({
                        "from": nid, "to": sub_nid, "type": "HAS_NODE",
                        "label": "has",
                    })

        # 2. DB files from db_registry
        db_path = ROOT_DIR / "global_context" / "db_registry.json"
        if db_path.is_file():
            try:
                db_data = json.loads(db_path.read_text(encoding="utf-8", errors="ignore"))
            except Exception:
                db_data = {}
            for f in db_data.get("files", []):
                name = f.get("name", "")
                lib = f.get("library", "")
                cols = f.get("columns", [])
                units = f.get("sourceUnits", [])
                fnid = f"db:{name}"
                if fnid not in node_ids:
                    node_ids.add(fnid)
                    key_cols = [c.get("name") for c in cols if c.get("key")]
                    graph_nodes.append({
                        "id": fnid, "label": name, "type": "dbfile",
                        "title": f"DB File: {name}\nLibrary: {lib}\n{len(cols)} columns\nKeys: {', '.join(key_cols) if key_cols else 'none'}",
                    })
                # Edges: program USES_FILE → dbfile
                for unit_id in units:
                    member = unit_id.split("/")[-1] if "/" in unit_id else unit_id
                    prog_nid = f"prog:{member}"
                    if prog_nid in node_ids:
                        edge_id = f"{prog_nid}->{fnid}"
                        graph_edges.append({
                            "from": prog_nid, "to": fnid, "type": "USES_FILE",
                            "label": "uses",
                        })

        # 3. Call graph edges
        for cg_name in ("call_graph_enriched.json", "call_graph.json"):
            cg_path = ROOT_DIR / "global_context" / cg_name
            if not cg_path.is_file():
                continue
            try:
                cg_data = json.loads(cg_path.read_text(encoding="utf-8", errors="ignore"))
            except Exception:
                continue
            seen_call_edges = set()
            for prog in cg_data.get("programs", []):
                pid = prog.get("programId", "")
                prog_nid = f"prog:{pid}"
                for call in prog.get("calls", []):
                    callee_name = call.get("calleeName", "") or ""
                    if not callee_name:
                        continue
                    callee_nid_by_id = None
                    callee_node_id = call.get("calleeNodeId")
                    if callee_node_id:
                        callee_nid_by_id = f"node:{pid}:{callee_node_id}"
                    callee_nid = callee_nid_by_id if (callee_nid_by_id and callee_nid_by_id in node_ids) else f"call:{pid}:{callee_name}"

                    if callee_nid not in node_ids:
                        node_ids.add(callee_nid)
                        is_ext = callee_name.startswith("CALL")
                        graph_nodes.append({
                            "id": callee_nid, "label": callee_name,
                            "type": "external" if is_ext else "subroutine",
                            "title": f"{'External call' if is_ext else 'Subroutine'}: {callee_name}\nProgram: {pid}",
                        })
                        graph_edges.append({"from": prog_nid, "to": callee_nid, "type": "HAS_NODE", "label": "has"})

                    caller_node_id = call.get("callerNodeId")
                    src_nid = f"node:{pid}:{caller_node_id}" if caller_node_id and f"node:{pid}:{caller_node_id}" in node_ids else prog_nid
                    edge_key = f"{src_nid}->{callee_nid}"
                    if edge_key not in seen_call_edges:
                        seen_call_edges.add(edge_key)
                        graph_edges.append({
                            "from": src_nid, "to": callee_nid, "type": "CALLS",
                            "label": callee_name,
                        })
            break

        return {"nodes": graph_nodes, "edges": graph_edges}

    def _build_code_origin_response(self, program_id: str, entry_node_id: str) -> dict:
        """Build code-origin data: generated Java files with RPG origin metadata per file and per block."""
        import re as _re
        context_dir = ROOT_DIR / "context_index"

        # Load node index
        node_index_path = context_dir / f"{program_id}_nodes.json"
        node_index = {}
        if node_index_path.is_file():
            try:
                ni_data = json.loads(node_index_path.read_text(encoding="utf-8", errors="ignore"))
                node_index = ni_data.get("nodes", {})
            except Exception:
                pass
        if not node_index:
            return {"error": f"Node index not found for {program_id}.", "status": 404}

        # Build RPG symbol lookup and statement lists
        sym_lookup: dict[str, list] = {}
        db_file_nodes: dict[str, list] = {}
        root_node = node_index.get(entry_node_id, {})
        root_range = root_node.get("range", {})
        root_start = root_range.get("startLine", 0)
        root_end = root_range.get("endLine", 999999)

        # Collect all statement-level nodes within the entry node's range, sorted by line
        rpg_stmts_by_kind: dict[str, list] = {}
        for nid, n in node_index.items():
            kind = n.get("kind", "")
            props = n.get("props", {})
            opcode = props.get("opcode", "")
            target = props.get("target", "")
            rpg_src = n.get("rpgSource", "")
            node_range = n.get("range") or {}
            sl = node_range.get("startLine", 0)

            # Synthesise RPG source from node properties when rpgSource is empty
            if not rpg_src:
                rpg_src = self._synthesize_node_rpg(n)

            if kind in ("Chain", "Read", "Write", "Update", "SetLL", "DclF") and target:
                db_file_nodes.setdefault(target.upper(), []).append({
                    "nodeId": nid, "kind": kind, "opcode": opcode,
                    "rpgSource": rpg_src,
                    "line": sl,
                })

            for sym_key in n.get("sem", {}):
                parts = sym_key.split(".")
                short_name = parts[-1].upper() if parts else ""
                if short_name and len(short_name) > 1:
                    sym_lookup.setdefault(short_name, []).append({
                        "nodeId": nid, "kind": kind, "opcode": opcode,
                        "rpgSource": rpg_src,
                    })

            # Collect statements within root range for ordinal matching
            if root_start <= sl <= root_end and kind in (
                "Chain", "Read", "Write", "Update", "SetLL",
                "Eval", "If", "DoW", "ExSr", "Select", "When",
            ):
                el = node_range.get("endLine", sl)
                rpg_stmts_by_kind.setdefault(kind, []).append({
                    "nodeId": nid, "kind": kind, "opcode": opcode,
                    "target": target, "rpgSource": rpg_src, "line": sl, "endLine": el,
                })

        for kind_list in rpg_stmts_by_kind.values():
            kind_list.sort(key=lambda x: x["line"])

        # Synthesise RPG pseudo-source for display
        rpg_source, rpg_start_line = self._synthesize_rpg_from_ast(
            entry_node_id, node_index, ni_data.get("lineToNodes", {}))

        # Find generated Java files
        java_root = None
        for candidate in [
            ROOT_DIR / f"{program_id}_{entry_node_id}_pure_java" / "src" / "main" / "java",
            ROOT_DIR / "warranty_demo" / "src" / "main" / "java",
        ]:
            if candidate.is_dir():
                java_root = candidate
                break
        if not java_root:
            return {"error": "No generated Java files found.", "status": 404}

        # Determine file list from manifest or scan
        file_paths = []
        migrations_dir = ROOT_DIR / "global_context" / "migrations"
        if migrations_dir.is_dir():
            pattern = f"{program_id}_{entry_node_id}_*.json"
            latest = None
            for p in migrations_dir.glob(pattern):
                if latest is None or p.stat().st_mtime > latest.stat().st_mtime:
                    latest = p
            if latest:
                try:
                    manifest = json.loads(latest.read_text(encoding="utf-8", errors="ignore"))
                    file_paths = manifest.get("generatedFiles", [])
                except Exception:
                    pass
        if not file_paths:
            pkg = java_root / "com" / "scania" / "warranty"
            if pkg.is_dir():
                for sub in ("domain", "service", "repository", "dto", "web", "config"):
                    sd = pkg / sub
                    if sd.is_dir():
                        for jf in sorted(sd.glob("*.java")):
                            file_paths.append(str(jf.relative_to(java_root)))

        # Categorise and analyse each file
        LAYER_ORDER = {"domain": 0, "repository": 1, "service": 2, "dto": 3, "web": 4, "config": 5}
        TABLE_RE = _re.compile(r'@Table\s*\(\s*name\s*=\s*"([^"]+)"')
        COLUMN_RE = _re.compile(r'@Column\s*\([^)]*name\s*=\s*"([^"]+)"')
        IDENT_RE = _re.compile(r'[A-Za-z_]\w*')

        files_out = []
        for fp in file_paths:
            full = java_root / fp
            if not full.is_file():
                continue
            try:
                content = full.read_text(encoding="utf-8", errors="replace")
            except Exception:
                continue
            lines = content.splitlines()
            parts = fp.replace("\\", "/").split("/")
            layer = "other"
            for p in parts:
                if p in LAYER_ORDER:
                    layer = p
                    break
            class_name = parts[-1].replace(".java", "")

            # File-level origin
            file_origin = None
            table_m = TABLE_RE.search(content)
            if table_m:
                rpg_file = table_m.group(1).upper()
                ops = db_file_nodes.get(rpg_file, [])
                op_kinds = sorted(set(o["kind"] for o in ops))
                file_origin = {
                    "type": "db_file",
                    "rpgFile": rpg_file,
                    "description": f"Entity mapped from RPG DB file {rpg_file}",
                    "operations": op_kinds,
                    "rpgSources": [o["rpgSource"] for o in ops if o["rpgSource"]][:5],
                }
            elif layer == "repository":
                repo_table = _re.search(r'extends\s+\w+Repository\s*<\s*(\w+)', content)
                if repo_table:
                    entity_name = repo_table.group(1)
                    for other_fp in file_paths:
                        other_full = java_root / other_fp
                        if other_full.is_file() and entity_name in other_fp:
                            try:
                                other_content = other_full.read_text(encoding="utf-8", errors="replace")
                                tm2 = TABLE_RE.search(other_content)
                                if tm2:
                                    rpg_file = tm2.group(1).upper()
                                    ops = db_file_nodes.get(rpg_file, [])
                                    file_origin = {
                                        "type": "db_access",
                                        "rpgFile": rpg_file,
                                        "description": f"Repository for RPG DB file {rpg_file} (entity {entity_name})",
                                        "operations": sorted(set(o["kind"] for o in ops)),
                                    }
                                    break
                            except Exception:
                                pass
                if not file_origin:
                    for rpg_file, ops in db_file_nodes.items():
                        if rpg_file.upper() in class_name.upper():
                            file_origin = {
                                "type": "db_access",
                                "rpgFile": rpg_file,
                                "description": f"Repository for RPG DB file {rpg_file}",
                                "operations": sorted(set(o["kind"] for o in ops)),
                            }
                            break
            elif layer == "service":
                file_origin = {
                    "type": "business_logic",
                    "rpgNode": entry_node_id,
                    "description": f"Business logic from RPG subroutine {root_node.get('name', entry_node_id)} (program {program_id})",
                }
            elif layer == "dto":
                file_origin = {
                    "type": "data_transfer",
                    "description": "Data transfer object derived from RPG data structures and service interfaces",
                }
            elif layer == "web":
                file_origin = {
                    "type": "api_layer",
                    "description": "REST API layer — new architecture (no direct RPG equivalent)",
                }

            # Block-level origin: scan lines for RPG origin signals
            blocks = []
            current_block = None
            FINDBY_RE = _re.compile(r'\.\s*find\w*By|\.findAll|\.findMax|\.count\w*By', _re.IGNORECASE)
            QUERY_RE = _re.compile(r'@Query\s*\(')
            REPO_METHOD_RE = _re.compile(
                r'\b(find\w*By|findAll\w*|findOne|findMax\w*|findFirst|count\w*By)\s*\(',
                _re.IGNORECASE,
            )
            SAVE_RE = _re.compile(r'\.\s*save\s*\(')
            SETTER_RE = _re.compile(r'\.\s*set[A-Z]\w*\s*\(')
            IF_RE = _re.compile(r'^\s*(?:if|else\s+if)\s*\(')
            FOR_RE = _re.compile(r'^\s*for\s*\(')
            THROW_RE = _re.compile(r'^\s*throw\s+')

            # Regex to parse injected @origin comments (from pipeline / inject_origin_annotations)
            ORIGIN_COMMENT_RE = _re.compile(
                r'//\s*@origin\s+(\w+)\s+L(\d+)-(\d+)\s*\((\w+)\)',
                _re.IGNORECASE,
            )

            # Ordinal counters for RPG statement matching
            ord_counters: dict[str, int] = {}

            def _parse_origin_from_prev_line(idx: int) -> dict | None:
                """If line idx-1 is // @origin, parse and return origin_tag. Aligns with injected annotations."""
                if idx <= 0:
                    return None
                prev = lines[idx - 1].strip()
                m = ORIGIN_COMMENT_RE.search(prev)
                if not m or m.group(1).upper() != program_id.upper():
                    return None
                rpg_line = int(m.group(2))
                rpg_end = int(m.group(3))
                kind = m.group(4).upper()
                return {
                    "kind": kind, "label": kind,
                    "detail": f"RPG L{rpg_line}-{rpg_end} ({kind})",
                    "rpgLine": rpg_line, "rpgEndLine": rpg_end,
                    "rpgLines": [f"L{rpg_line}-{rpg_end} ({kind})"],
                }

            def _next_rpg(rpg_kind: str) -> dict | None:
                """Get the next RPG statement of the given kind via ordinal matching."""
                idx = ord_counters.get(rpg_kind, 0)
                stmts = rpg_stmts_by_kind.get(rpg_kind, [])
                if idx < len(stmts):
                    ord_counters[rpg_kind] = idx + 1
                    return stmts[idx]
                return None

            for i, line in enumerate(lines):
                stripped = line.strip()
                if not stripped or stripped.startswith("//") or stripped.startswith("*"):
                    if current_block:
                        current_block["endLine"] = i
                    continue
                if stripped.startswith("package ") or stripped.startswith("import "):
                    continue

                origin_tag = None

                # @Table → DB file origin
                tm = TABLE_RE.search(line)
                if tm:
                    rpg_file = tm.group(1).upper()
                    ops = db_file_nodes.get(rpg_file, [])
                    rpg_lines = [o.get("rpgSource", "") for o in ops if o.get("rpgSource")][:5]
                    if not rpg_lines:
                        rpg_lines = [f"CHAIN {rpg_file}", f"READ {rpg_file}", f"WRITE {rpg_file}"]
                    origin_tag = {
                        "kind": "DB_FILE", "label": rpg_file,
                        "detail": f"Entity mapped from RPG DB file {rpg_file}",
                        "rpgLines": rpg_lines,
                    }

                # @Column → field origin
                if not origin_tag:
                    cm = COLUMN_RE.search(line)
                    if cm:
                        col_name = cm.group(1).upper()
                        origin_tag = {
                            "kind": "FIELD", "label": col_name,
                            "detail": f"RPG field {col_name} (column in DB file)",
                        }

                # Structural patterns: prefer @origin from previous line (injected by pipeline), else ordinal
                if not origin_tag and layer in ("service", "repository"):
                    injected = _parse_origin_from_prev_line(i)
                    if injected:
                        origin_tag = injected
                    else:
                        rpg_node = None
                        is_db_lookup = (
                            FINDBY_RE.search(line)
                            or QUERY_RE.search(line)
                            or REPO_METHOD_RE.search(line)
                        )
                        if is_db_lookup:
                            rpg_node = _next_rpg("Chain") or _next_rpg("Read") or _next_rpg("SetLL")
                            if rpg_node:
                                origin_tag = {
                                    "kind": "CHAIN", "label": rpg_node["opcode"] or "CHAIN",
                                    "detail": rpg_node["rpgSource"],
                                    "rpgLines": [rpg_node["rpgSource"]],
                                    "rpgLine": rpg_node.get("line"),
                                    "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                                }
                            else:
                                origin_tag = {"kind": "CHAIN", "label": "CHAIN/READ", "detail": "DB lookup — RPG CHAIN/READ"}
                        elif SAVE_RE.search(line):
                            rpg_node = _next_rpg("Write") or _next_rpg("Update")
                            if rpg_node:
                                origin_tag = {
                                    "kind": "WRITE", "label": rpg_node["opcode"] or "WRITE",
                                    "detail": rpg_node["rpgSource"],
                                    "rpgLines": [rpg_node["rpgSource"]],
                                    "rpgLine": rpg_node.get("line"),
                                    "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                                }
                            else:
                                origin_tag = {"kind": "WRITE", "label": "WRITE", "detail": "DB write — RPG WRITE/UPDATE"}
                        elif SETTER_RE.search(line):
                            rpg_node = _next_rpg("Eval")
                            if rpg_node:
                                origin_tag = {
                                    "kind": "EVAL", "label": "EVAL",
                                    "detail": rpg_node["rpgSource"],
                                    "rpgLines": [rpg_node["rpgSource"]],
                                    "rpgLine": rpg_node.get("line"),
                                    "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                                }
                            else:
                                origin_tag = {"kind": "EVAL", "label": "EVAL", "detail": "Field assignment — RPG EVAL"}
                        elif IF_RE.search(line):
                            rpg_node = _next_rpg("If")
                            if rpg_node:
                                origin_tag = {
                                    "kind": "IF", "label": "IF",
                                    "detail": rpg_node["rpgSource"],
                                    "rpgLines": [rpg_node["rpgSource"]],
                                    "rpgLine": rpg_node.get("line"),
                                    "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                                }
                            else:
                                origin_tag = {"kind": "IF", "label": "IF", "detail": "Condition — RPG IF"}
                        elif FOR_RE.search(line):
                            rpg_node = _next_rpg("DoW")
                            if rpg_node:
                                origin_tag = {
                                    "kind": "DOW", "label": "DOW",
                                    "detail": rpg_node["rpgSource"],
                                    "rpgLines": [rpg_node["rpgSource"]],
                                    "rpgLine": rpg_node.get("line"),
                                    "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                                }
                            else:
                                origin_tag = {"kind": "DOW", "label": "DOW", "detail": "Loop — RPG DOW"}
                        elif THROW_RE.search(line):
                            rpg_node = _next_rpg("ExSr")
                            if rpg_node:
                                origin_tag = {
                                    "kind": "EXSR", "label": "EXSR",
                                    "detail": rpg_node["rpgSource"],
                                    "rpgLines": [rpg_node["rpgSource"]],
                                    "rpgLine": rpg_node.get("line"),
                                    "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                                }
                            else:
                                origin_tag = {"kind": "EXSR", "label": "EXSR", "detail": "Error handling — RPG EXSR"}

                # Fall-through: RPG symbol matching
                if not origin_tag and layer in ("service", "repository"):
                    idents = set(w.upper() for w in IDENT_RE.findall(line) if len(w) > 2)
                    best_match = None
                    for ident in idents:
                        if ident in sym_lookup:
                            hits = sym_lookup[ident]
                            for h in hits:
                                if h["kind"] in ("Chain", "Read", "Write", "Update", "SetLL", "Eval", "If", "ExSr"):
                                    if best_match is None or MATCH_PRIORITY.get(h["kind"], 99) < MATCH_PRIORITY.get(best_match["kind"], 99):
                                        best_match = h
                    if best_match:
                        origin_tag = {
                            "kind": best_match["kind"].upper(),
                            "label": best_match["opcode"] or best_match["kind"],
                            "detail": best_match.get("rpgSource", ""),
                        }

                # Extend or start block
                if origin_tag:
                    tag_key = origin_tag["kind"] + ":" + origin_tag["label"]
                    if current_block and current_block.get("_key") == tag_key and i - current_block["endLine"] <= 3:
                        current_block["endLine"] = i
                    else:
                        if current_block:
                            blocks.append(current_block)
                        current_block = {
                            "startLine": i, "endLine": i,
                            "origin": origin_tag, "_key": tag_key,
                        }
                elif current_block and i - current_block["endLine"] <= 2:
                    current_block["endLine"] = i
                else:
                    if current_block:
                        blocks.append(current_block)
                        current_block = None

            if current_block:
                blocks.append(current_block)
            for b in blocks:
                b.pop("_key", None)

            files_out.append({
                "path": fp,
                "className": class_name,
                "layer": layer,
                "layerOrder": LAYER_ORDER.get(layer, 99),
                "content": content,
                "lineCount": len(lines),
                "fileOrigin": file_origin,
                "originBlocks": blocks,
            })

        files_out.sort(key=lambda f: (f["layerOrder"], f["className"]))

        rpg_file_id = root_range.get("sourceId", "")
        rpg_file_name = rpg_file_id.split("/")[-1] if "/" in rpg_file_id else (rpg_file_id or program_id)
        return {
            "programId": program_id,
            "nodeId": entry_node_id,
            "rpgProgram": program_id,
            "rpgNode": entry_node_id,
            "rpgNodeName": root_node.get("name", entry_node_id),
            "rpgFileId": rpg_file_id,
            "rpgFileName": rpg_file_name,
            "rpgSource": rpg_source,
            "rpgStartLine": rpg_start_line,
            "rpgSourceLines": len(rpg_source.splitlines()) if rpg_source else 0,
            "rpgLineRange": f"{root_range.get('startLine', '?')}–{root_range.get('endLine', '?')}",
            "files": files_out,
            "summary": {
                "totalFiles": len(files_out),
                "totalLines": sum(f["lineCount"] for f in files_out),
                "layers": sorted(set(f["layer"] for f in files_out)),
                "dbFiles": sorted(db_file_nodes.keys()),
            },
        }

    @staticmethod
    def _synthesize_node_rpg(node: dict) -> str:
        """Produce a short, readable RPG statement from a single AST node's properties."""
        kind = node.get("kind", "")
        props = node.get("props", {})
        opcode = props.get("opcode", kind.upper())
        target = props.get("target", "")
        keys = props.get("keys", [])
        value = props.get("value")
        condition = props.get("condition")

        def _val_str(v, depth=0) -> str:
            if depth > 5:
                return "..."
            if isinstance(v, str):
                return v
            if isinstance(v, (int, float)):
                return str(v)
            if isinstance(v, dict):
                if v.get("type") == "literal":
                    raw = v.get("value", "")
                    return repr(raw) if isinstance(raw, str) else str(raw)
                if v.get("type") == "builtin":
                    args = v.get("args", [])
                    a = ", ".join(_val_str(x, depth+1) for x in args) if args else ""
                    return f"%{v.get('name', '?')}({a})" if a else f"%{v.get('name', '?')}"
                return v.get("name", "") or v.get("value", "") or ""
            if isinstance(v, list):
                if len(v) == 0:
                    return ""
                if len(v) >= 2 and isinstance(v[0], str):
                    op = v[0]
                    rest = v[1] if isinstance(v[1], list) else v[1:]
                    parts = [_val_str(x, depth+1) for x in (rest if isinstance(rest, list) else [rest])]
                    parts = [p for p in parts if p]
                    if op == "ASSIGN":
                        return parts[0] if len(parts) == 1 else " = ".join(parts)
                    return f" {op} ".join(parts) if parts else op
                return " ".join(_val_str(x, depth+1) for x in v)
            return str(v) if v else ""

        tgt_short = target.split(".")[-1] if "." in target else target

        if kind in ("Chain", "Read", "SetLL"):
            k = " ".join(keys) if keys else ""
            return f"{opcode} {k} {tgt_short}".strip()
        if kind in ("Write", "Update"):
            return f"{opcode} {tgt_short}".strip()
        if kind == "Eval" and value:
            return f"{tgt_short} = {_val_str(value)}"
        if kind == "Eval":
            return f"EVAL {tgt_short}" if tgt_short else "EVAL"
        if kind == "If" and condition:
            return f"IF {_val_str(condition)}"
        if kind == "If":
            return "IF ..."
        if kind == "DoW" and condition:
            return f"DOW {_val_str(condition)}"
        if kind == "DoW":
            return "DOW ..."
        if kind == "ExSr":
            return f"EXSR {tgt_short}" if tgt_short else "EXSR"
        if kind == "Select":
            return "SELECT"
        if kind == "When" and condition:
            return f"WHEN {_val_str(condition)}"
        return f"{opcode} {tgt_short}".strip() or kind

    @staticmethod
    def _synthesize_rpg_from_ast(root_node_id: str, node_index: dict, line_to_nodes: dict) -> tuple:
        """Synthesize readable RPG pseudo-source from AST nodes when real source is unavailable."""
        root = node_index.get(root_node_id)
        if not root:
            return "", 1

        rng = root.get("range") or {}
        start_line = rng.get("startLine", 1)
        end_line = rng.get("endLine", start_line)

        # Collect all descendant nodes that belong to this root
        def collect(nid):
            node = node_index.get(nid)
            if not node:
                return []
            result = [node]
            for cid in (node.get("children") or []):
                result.extend(collect(cid))
            return result

        descendants = collect(root_node_id)
        # Filter to nodes with valid startLine and sort by line number
        with_lines = [(n, n.get("range", {}).get("startLine", 0)) for n in descendants if n.get("range", {}).get("startLine")]
        with_lines.sort(key=lambda x: x[1])

        # Build lines array for the range
        total_lines = max(1, end_line - start_line + 1)
        synth_lines = [""] * total_lines

        seen_lines = set()
        for node, sl in with_lines:
            idx = sl - start_line
            if idx < 0 or idx >= total_lines or idx in seen_lines:
                continue
            seen_lines.add(idx)

            kind = node.get("kind", "")
            props = node.get("props") or {}
            opcode = props.get("opcode", kind).upper()
            target = props.get("target", "")
            sem = node.get("sem") or {}

            # Build a readable pseudo-RPG line
            sym_names = [k.split(".")[-1] for k in sem if k.startswith("sym.var.")]
            file_names = [k.split(".")[-1] for k in sem if k.startswith("sym.file.")]

            if kind == "Comment":
                synth_lines[idx] = f"       // (comment)"
            elif kind in ("Subroutine", "EndSubroutine"):
                name = props.get("name", "")
                synth_lines[idx] = f"       BEGSR     {name}" if kind == "Subroutine" else "       ENDSR"
            elif kind == "Procedure":
                name = props.get("name", "")
                synth_lines[idx] = f"       DCL-PROC  {name}"
            elif opcode == "EVAL":
                tgt = target.replace("sym.var.", "") if target else ""
                val = _format_value_short(props.get("value"))
                synth_lines[idx] = f"       EVAL      {tgt} = {val}"
            elif opcode == "IF":
                cond = " AND ".join(sym_names[:3]) if sym_names else "..."
                synth_lines[idx] = f"       IF        {cond}"
            elif opcode in ("ELSE", "ENDIF", "ENDDO", "ENDSL", "ENDMON"):
                synth_lines[idx] = f"       {opcode}"
            elif opcode == "DOW":
                cond = " AND ".join(sym_names[:3]) if sym_names else "..."
                synth_lines[idx] = f"       DOW       {cond}"
            elif opcode in ("CHAIN", "READ", "READE", "READP", "READPE"):
                file_nm = target if target else (file_names[0] if file_names else "?")
                keys_list = props.get("keys", [])
                keys_str = " : ".join(str(k) for k in keys_list) if keys_list else ""
                detail = f"{keys_str}  {file_nm}" if keys_str else file_nm
                synth_lines[idx] = f"       {opcode:10s}{detail}"
            elif opcode in ("WRITE", "UPDATE"):
                file_nm = target if target else (file_names[0] if file_names else "?")
                synth_lines[idx] = f"       {opcode:10s}{file_nm}"
            elif opcode == "SETLL":
                file_nm = target if target else (file_names[0] if file_names else "?")
                keys_list = props.get("keys", [])
                keys_str = " : ".join(str(k) for k in keys_list) if keys_list else ""
                detail = f"{keys_str}  {file_nm}" if keys_str else file_nm
                synth_lines[idx] = f"       SETLL     {detail}"
            elif opcode == "EXSR":
                sub_name = target if target else "?"
                synth_lines[idx] = f"       EXSR      {sub_name}"
            elif opcode == "RETURN":
                synth_lines[idx] = f"       RETURN"
            elif opcode in ("SETON", "SETOFF"):
                inds = [k.split(".")[-1] for k in sem if k.startswith("sym.ind.")]
                synth_lines[idx] = f"       {opcode:10s}{', '.join(inds) if inds else '...'}"
            elif opcode == "SELECT":
                synth_lines[idx] = f"       SELECT"
            elif opcode == "WHEN":
                synth_lines[idx] = f"       WHEN      {' '.join(sym_names[:3]) if sym_names else '...'}"
            elif kind in ("DclF", "DclDS", "DclConst"):
                name = props.get("name", sym_names[0] if sym_names else "?")
                synth_lines[idx] = f"       {kind.upper():10s}{name}"
            elif kind == "Assign":
                tgt = target.replace("sym.var.", "") if target else ""
                synth_lines[idx] = f"       EVAL      {tgt} = ..."
            else:
                detail = target.replace("sym.var.", "") if target else (" ".join(sym_names[:2]) if sym_names else "")
                synth_lines[idx] = f"       {opcode:10s}{detail}"

        return "\n".join(synth_lines), start_line

    def _build_traceability_response(self, program_id: str, entry_node_id: str) -> dict:
        """Build the combined traceability response: RPG source + node index + generated Java."""
        context_dir = ROOT_DIR / "context_index"

        # Load node index (with lineToNodes mapping)
        node_index_path = context_dir / f"{program_id}_nodes.json"
        node_index = {}
        line_to_nodes = {}
        if node_index_path.is_file():
            try:
                ni_data = json.loads(node_index_path.read_text(encoding="utf-8", errors="ignore"))
                node_index = ni_data.get("nodes", {})
                line_to_nodes = ni_data.get("lineToNodes", {})
            except Exception:
                pass

        if not node_index:
            return {"error": f"Node index not found for {program_id}. Run Build Global Context first.", "status": 404}

        # Load RPG source and range from the context package
        rpg_source = ""
        rpg_start_line = 1
        rpg_file_id = ""
        ctx_path = context_dir / f"{program_id}_{entry_node_id}.json"
        if ctx_path.is_file():
            try:
                ctx = json.loads(ctx_path.read_text(encoding="utf-8", errors="ignore"))
                rpg_source = ctx.get("rpgSnippet", "")
                ast_node = ctx.get("astNode", {})
                r = ast_node.get("range", {})
                rpg_start_line = r.get("startLine", 1) or 1
                rpg_file_id = r.get("sourceId", "")
            except Exception:
                pass

        # If no actual RPG source, synthesize from AST nodes
        if not rpg_source and node_index:
            rpg_source, rpg_start_line = self._synthesize_rpg_from_ast(
                entry_node_id, node_index, line_to_nodes)

        # Find the latest migration manifest for this (program, node)
        migrations_dir = ROOT_DIR / "global_context" / "migrations"
        manifest_data = None
        latest = None
        if migrations_dir.is_dir():
            pattern = f"{program_id}_{entry_node_id}_*.json"
            for p in migrations_dir.glob(pattern):
                if latest is None or p.stat().st_mtime > latest.stat().st_mtime:
                    latest = p
            if latest:
                try:
                    manifest_data = json.loads(latest.read_text(encoding="utf-8", errors="ignore"))
                except Exception:
                    pass

        # Read generated Java files
        generated_files = []
        java_root = ROOT_DIR / "warranty_demo" / "src" / "main" / "java"
        file_paths = (manifest_data or {}).get("generatedFiles", [])

        if not file_paths:
            warranty_pkg = java_root / "com" / "scania" / "warranty"
            if warranty_pkg.is_dir():
                for sub in ("domain", "service", "repository", "dto", "web", "config"):
                    sub_dir = warranty_pkg / sub
                    if sub_dir.is_dir():
                        for jf in sorted(sub_dir.glob("*.java")):
                            rel = str(jf.relative_to(java_root))
                            if rel not in file_paths:
                                file_paths.append(rel)

        for fp in file_paths:
            full_path = java_root / fp
            if full_path.is_file():
                try:
                    content = full_path.read_text(encoding="utf-8", errors="replace")
                    generated_files.append({
                        "path": fp,
                        "content": content,
                    })
                except Exception:
                    pass

        return {
            "programId": program_id,
            "nodeId": entry_node_id,
            "rpgSource": rpg_source,
            "rpgStartLine": rpg_start_line,
            "rpgFileId": rpg_file_id,
            "nodeIndex": node_index,
            "lineToNodes": line_to_nodes,
            "generatedFiles": generated_files,
            "manifestPath": str(latest) if manifest_data else None,
        }

    def do_POST(self):
        parsed = urlparse(self.path)

        # Proxy to warranty app (avoids CORS when UI is on 8003 and app on 8081)
        app_port = os.environ.get("APP_PORT", "8081")
        app_base = f"http://127.0.0.1:{app_port}"
        if parsed.path == "/api/proxy/seed":
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length > 0:
                self.rfile.read(content_length)
            try:
                req = Request(app_base + "/api/seed", method="POST", data=b"")
                with urlopen(req, timeout=10) as resp:
                    body = resp.read().decode("utf-8", errors="replace")
                    try:
                        data = json.loads(body)
                    except json.JSONDecodeError:
                        data = {"status": "ok" if resp.status == 200 else "error", "message": body}
                    self._send_json(data)
            except HTTPError as e:
                err_body = e.read().decode("utf-8", errors="replace") if e.fp else str(e)
                try:
                    err_data = json.loads(err_body)
                except json.JSONDecodeError:
                    err_data = {"status": "error", "message": err_body}
                self._send_json(err_data, status=e.code)
            except (URLError, OSError) as e:
                self._send_json({"status": "error", "message": str(e)}, status=502)
            return

        if parsed.path == "/api/build-global-context":
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length > 0:
                body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                ast_dir = body.get("astDir", "JSON_ast/JSON_20260311")
                rpg_dir = body.get("rpgDir", "")
            else:
                params = parse_qs(parsed.query)
                ast_dir = params.get("astDir", ["JSON_ast/JSON_20260311"])[0]
                rpg_dir = params.get("rpgDir", [""])[0]

            # Resolve RPG directory (best-effort)
            rpg_path = None
            rpg_dir_clean = (rpg_dir or "").strip()
            original_path = rpg_dir_clean
            if rpg_dir_clean.startswith("Users/") or rpg_dir_clean.startswith("home/"):
                rpg_dir_clean = "/" + rpg_dir_clean
                print(f"[Global Context] Normalized path: '{original_path}' -> '{rpg_dir_clean}'", flush=True)

            if rpg_dir_clean:
                if Path(rpg_dir_clean).is_absolute():
                    cand = Path(rpg_dir_clean)
                else:
                    cand = ROOT_DIR / rpg_dir_clean
                if cand.exists() and cand.is_dir():
                    rpg_path = cand

            output = ""
            ast_dir_arg = ["--astDir", ast_dir]
            try:
                # 1) DB registry
                db_proc = subprocess.run(
                    [sys.executable, str(ROOT_DIR / "global_context" / "build_db_registry.py")] + ast_dir_arg,
                    capture_output=True,
                    text=True,
                    timeout=120,
                    cwd=str(ROOT_DIR),
                )
                output += (db_proc.stdout or "") + (db_proc.stderr or "")
                # 2) Program-level context
                prog_proc = subprocess.run(
                    [sys.executable, str(ROOT_DIR / "global_context" / "build_program_context.py")] + ast_dir_arg,
                    capture_output=True,
                    text=True,
                    timeout=120,
                    cwd=str(ROOT_DIR),
                )
                output += (prog_proc.stdout or "") + (prog_proc.stderr or "")
                # 3) AST-based call graph
                call_proc = subprocess.run(
                    [sys.executable, str(ROOT_DIR / "global_context" / "build_call_graph.py")] + ast_dir_arg,
                    capture_output=True,
                    text=True,
                    timeout=120,
                    cwd=str(ROOT_DIR),
                )
                output += (call_proc.stdout or "") + (call_proc.stderr or "")
                # 4) Enrich call graph with RPG callee names (only if RPG dir is known)
                if rpg_path and rpg_path.is_dir():
                    call_rpg_proc = subprocess.run(
                        [
                            sys.executable,
                            str(ROOT_DIR / "global_context" / "build_call_graph_with_rpg.py"),
                            "--rpgDir",
                            str(rpg_path),
                        ],
                        capture_output=True,
                        text=True,
                        timeout=180,
                        cwd=str(ROOT_DIR),
                    )
                    output += (call_rpg_proc.stdout or "") + (call_rpg_proc.stderr or "")
                else:
                    output += "\n(Global context: RPG directory not found or not provided; enriched call graph skipped.)"
                # 5) Build context index (Python replacement for Java IndexAll)
                ctx_idx_cmd = [
                    sys.executable,
                    str(ROOT_DIR / "build_context_index.py"),
                    "--astDir", ast_dir,
                ]
                if rpg_path and rpg_path.is_dir():
                    ctx_idx_cmd += ["--rpgDir", str(rpg_path)]
                ctx_idx_proc = subprocess.run(
                    ctx_idx_cmd,
                    capture_output=True,
                    text=True,
                    timeout=180,
                    cwd=str(ROOT_DIR),
                )
                output += (ctx_idx_proc.stdout or "") + (ctx_idx_proc.stderr or "")
                # 5b) Build fine-grained node index for traceability
                ni_cmd = [
                    sys.executable,
                    str(ROOT_DIR / "build_node_index.py"),
                    "--astDir", ast_dir,
                ]
                if rpg_path and rpg_path.is_dir():
                    ni_cmd += ["--rpgDir", str(rpg_path)]
                ni_proc = subprocess.run(
                    ni_cmd,
                    capture_output=True,
                    text=True,
                    timeout=120,
                    cwd=str(ROOT_DIR),
                )
                output += (ni_proc.stdout or "") + (ni_proc.stderr or "")
                # 6) Export knowledge graph
                neo4j_proc = subprocess.run(
                    [sys.executable, str(ROOT_DIR / "global_context" / "export_neo4j_cypher.py")],
                    capture_output=True,
                    text=True,
                    timeout=60,
                    cwd=str(ROOT_DIR),
                )
                output += (neo4j_proc.stdout or "") + (neo4j_proc.stderr or "")
            except Exception as e:
                output += f"\n(Global context build failed: {e})"

            self._send_json({
                "success": True,
                "message": "Global context built (DB registry, program context, call graph, context index, knowledge graph).",
                "output": output,
            })
            return

        if parsed.path == "/api/export-neo4j":
            from global_context import export_neo4j_cypher  # type: ignore[import]

            try:
                export_neo4j_cypher.main()
                msg = "Knowledge graph view written (neo4j_export.cypher)."
                self._send_json({
                    "success": True,
                    "message": msg,
                    "output": msg,
                })
            except Exception as e:
                self._send_json({
                    "success": False,
                    "error": f"Knowledge graph view failed: {e}",
                }, status=500)
            return

        if parsed.path == "/api/migrate-feature":
            from datetime import datetime

            content_length = int(self.headers.get("Content-Length", 0))
            rpg_dir: str | None = None
            if content_length > 0:
                body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                program_id = body.get("programId")
                entry_node_id = body.get("entryNodeId")
                rpg_dir = (body.get("rpgDir") or "").strip() or None
            else:
                params = parse_qs(parsed.query)
                program_id = params.get("programId", [""])[0]
                entry_node_id = params.get("entryNodeId", [""])[0]

            if not program_id or not entry_node_id:
                self._send_json({
                    "success": False,
                    "error": "programId and entryNodeId are required",
                }, status=400)
                return

            programs_dir = ROOT_DIR / "global_context" / "programs"
            ctx_path = programs_dir / f"{program_id}.program.json"
            if not ctx_path.exists():
                self._send_json({
                    "success": False,
                    "error": f"Program context not found for {program_id}. Run Build Global Context first.",
                }, status=400)
                return

            try:
                ctx = json.loads(ctx_path.read_text(encoding="utf-8", errors="ignore"))
            except Exception as e:
                self._send_json({
                    "success": False,
                    "error": f"Failed to read program context: {e}",
                }, status=500)
                return

            unit_id = ctx.get("unitId") or ""
            unit_short = unit_id.split("/")[-1] if "/" in unit_id else unit_id

            # Load call graph (enriched if available)
            cg_enriched = ROOT_DIR / "global_context" / "call_graph_enriched.json"
            cg_plain = ROOT_DIR / "global_context" / "call_graph.json"
            call_data = None
            for p in (cg_enriched, cg_plain):
                if p.exists():
                    try:
                        call_data = json.loads(p.read_text(encoding="utf-8", errors="ignore"))
                        break
                    except Exception:
                        continue

            # Build adjacency: caller -> [callee] and reverse: callee -> [caller]
            adjacency = {}
            reverse_adjacency = {}  # callee -> [caller]; for dependency order
            if call_data:
                for prog in call_data.get("programs", []):
                    if prog.get("programId") != program_id:
                        continue
                    for call in prog.get("calls", []):
                        caller = call.get("callerNodeId")
                        callee = call.get("calleeNodeId")
                        if caller and callee:
                            adjacency.setdefault(caller, set()).add(callee)
                            reverse_adjacency.setdefault(callee, set()).add(caller)

            # BFS from entry_node_id over CALLS edges to get the full slice
            visited = set()
            queue = [entry_node_id]
            while queue:
                nid = queue.pop(0)
                if nid in visited:
                    continue
                visited.add(nid)
                for succ in adjacency.get(nid, []):
                    if succ not in visited:
                        queue.append(succ)

            nodes_set = visited

            # Topological sort: migrate callees before callers (dependency order)
            # Graph: callee -> caller (reverse_adjacency). Topological order = callees first.
            # Kahn's algorithm: in_degree[caller] = number of callees that call it.
            in_degree = {n: 0 for n in nodes_set}
            for callee, callers in reverse_adjacency.items():
                for caller in callers:
                    if caller in nodes_set:
                        in_degree[caller] = in_degree.get(caller, 0) + 1
            topo_queue = sorted(n for n in nodes_set if in_degree.get(n, 0) == 0)
            topo_order = []
            while topo_queue:
                n = topo_queue.pop(0)
                topo_order.append(n)
                # In reverse graph, n points to its callers; reduce their in-degree
                for succ in reverse_adjacency.get(n, []):
                    if succ not in nodes_set:
                        continue
                    in_degree[succ] = in_degree.get(succ, 0) - 1
                    if in_degree[succ] == 0:
                        topo_queue.append(succ)
            # Any remaining (cycle) fall back to sorted
            remaining = [n for n in nodes_set if n not in topo_order]
            if remaining:
                topo_order.extend(sorted(remaining))
            nodes_in_slice = topo_order

            # Determine context files
            context_files = []
            for nid in nodes_in_slice:
                if not unit_short:
                    continue
                cf = ROOT_DIR / "context_index" / f"{unit_short}_{nid}.json"
                if cf.exists():
                    context_files.append(str(cf))

            # Very coarse DB files: any file whose sourceUnits include this unitId
            db_registry_path = ROOT_DIR / "global_context" / "db_registry.json"
            db_files = []
            if db_registry_path.exists():
                try:
                    reg = json.loads(db_registry_path.read_text(encoding="utf-8", errors="ignore"))
                    seen = set()
                    for f in reg.get("files", []):
                        units = f.get("sourceUnits", [])
                        if unit_id in units:
                            key = (f.get("library"), f.get("name"))
                            if key in seen:
                                continue
                            seen.add(key)
                            db_files.append({
                                "name": f.get("name"),
                                "library": f.get("library"),
                            })
                except Exception:
                    pass

            # Resolve RPG file path when rpgDir is provided (aligns with Global Context build)
            rpg_file_path: Path | None = None
            if rpg_dir and program_id:
                rpg_root = Path(rpg_dir)
                if not rpg_root.is_absolute():
                    rpg_root = ROOT_DIR / rpg_root
                for candidate in [
                    rpg_root / f"{program_id}.sqlrpgle",
                    rpg_root / f"{program_id}.rpgle",
                    rpg_root / "HSSRC" / "QRPGLESRC" / f"{program_id}.sqlrpgle",
                    rpg_root / "HSSRC" / "QRPGLESRC" / f"{program_id}.rpgle",
                ]:
                    if candidate.exists():
                        rpg_file_path = candidate
                        break
                if not rpg_file_path:
                    matches = list(rpg_root.rglob(f"{program_id}.sqlrpgle")) or list(rpg_root.rglob(f"{program_id}.rpgle"))
                    if matches:
                        rpg_file_path = matches[0]

            # Run migrate_to_pure_java.py for each context file, integrating into the main app project
            # Use chunked streaming to keep connection alive and show progress (avoids timeout after 18+ min)
            target_project = "warranty_demo"
            runs = []
            n_files = len(context_files)
            self._start_chunked_response()

            # Check for large context (CLI mode: extended timeout, Opus, prompt caching)
            LARGE_CONTEXT_KB = 400
            any_large = False
            for cf in context_files:
                try:
                    sz = Path(cf).stat().st_size
                    if sz > LARGE_CONTEXT_KB * 1024:
                        any_large = True
                        break
                except Exception:
                    pass

            def send_progress(msg: str) -> None:
                try:
                    self._write_chunked_line(json.dumps({"progress": msg}))
                except (BrokenPipeError, ConnectionResetError):
                    pass

            def send_log(line: str) -> None:
                """Stream a log line to the UI (append to live log area)."""
                if not line:
                    return
                try:
                    self._write_chunked_line(json.dumps({"log": line}))
                except (BrokenPipeError, ConnectionResetError):
                    pass

            if any_large:
                send_progress("Context size is large - running in CLI mode (extended timeout, Opus if available, prompt caching)")

            for i, cf in enumerate(context_files):
                cf_name = Path(cf).name
                cf_size_kb = 0
                try:
                    cf_size_kb = Path(cf).stat().st_size / 1024
                except Exception:
                    pass
                if cf_size_kb > LARGE_CONTEXT_KB:
                    send_progress(f"Migrating {i + 1}/{n_files}: {cf_name}... (large context {cf_size_kb:.0f} KB - CLI mode)")
                else:
                    send_progress(f"Migrating {i + 1}/{n_files}: {cf_name}...")
                cmd = [
                    sys.executable,
                    str(ROOT_DIR / "migrate_to_pure_java.py"),
                    cf,
                    "--target-project",
                    target_project,
                    "--no-inline-origin",  # Skip inject_origin (saves 1-3+ min as project grows)
                    "--validate",  # Call-graph validation (entity consolidation, subroutine coverage)
                ]
                if rpg_file_path:
                    cmd.extend(["--rpg-file", str(rpg_file_path)])
                try:
                    proc = subprocess.Popen(
                        cmd,
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE,
                        text=True,
                        bufsize=1,  # Line-buffered for real-time streaming
                        cwd=str(ROOT_DIR),
                        env=os.environ.copy(),
                    )
                    start = time.monotonic()
                    migrate_timeout = int(os.environ.get("MIGRATE_TIMEOUT", "3600"))  # default 60 min for large nodes like n404
                    stdout_lines: list[str] = []
                    stderr_lines: list[str] = []

                    def read_stream(pipe, lines: list[str], label: str) -> None:
                        try:
                            for line in iter(pipe.readline, ""):
                                if line:
                                    lines.append(line)
                                    send_log(line.rstrip())
                        except (BrokenPipeError, ConnectionResetError, ValueError):
                            pass
                        finally:
                            try:
                                pipe.close()
                            except Exception:
                                pass

                    t_out = threading.Thread(target=read_stream, args=(proc.stdout, stdout_lines, "out"))
                    t_err = threading.Thread(target=read_stream, args=(proc.stderr, stderr_lines, "err"))
                    t_out.daemon = True
                    t_err.daemon = True
                    t_out.start()
                    t_err.start()

                    last_heartbeat = start
                    while proc.poll() is None and (time.monotonic() - start) < migrate_timeout:
                        time.sleep(5)  # Poll every 5s
                        now = time.monotonic()
                        if now - last_heartbeat >= 30:  # Heartbeat every 30s so user knows it's alive
                            send_progress(f"Still migrating {cf_name}... (elapsed {now - start:.0f}s — LLM may take 5–30+ min for large context)")
                            last_heartbeat = now

                    t_out.join(timeout=2)
                    t_err.join(timeout=2)
                    t_out.join(timeout=1)
                    t_err.join(timeout=1)

                    if proc.poll() is None:
                        proc.kill()
                        proc.wait()
                        runs.append({"contextFile": cf, "error": f"Migration timeout ({migrate_timeout}s)"})
                    else:
                        stdout_text = "\n".join(stdout_lines)
                        stderr_text = "\n".join(stderr_lines)
                        runs.append({
                            "contextFile": cf,
                            "returnCode": proc.returncode,
                            "stdoutPreview": (stdout_text or "")[:4000],
                            "stderrPreview": (stderr_text or "")[:4000],
                        })
                except Exception as e:
                    runs.append({"contextFile": cf, "error": str(e)})

            # Build list of generated files from stdout/stderr previews
            generated_files = []
            ui_schema_generated = []
            for r in runs:
                for key in ("stdoutPreview", "stderrPreview"):
                    out = r.get(key) or ""
                    for line in out.splitlines():
                        line = line.strip()
                        if line.startswith("// ✅ Wrote "):
                            # Format examples:
                            #   // ✅ Wrote domain/Claim.java
                            #   // ✅ Wrote com/scania/warranty/domain/Claim.java
                            #   // ✅ Wrote HS1210_n404_pure_java/domain/Claim.java  (older standalone runs)
                            rel = line.replace("// ✅ Wrote ", "", 1).strip()
                            if " (" in rel:
                                rel = rel.split(" (", 1)[0].strip()
                            if rel and rel not in generated_files:
                                generated_files.append(rel)
                        elif line.startswith("// ✅ Generated UI schema: "):
                            # Migration pipeline: ui-schemas/<ScreenId>.json from displayFiles
                            rel = line.replace("// ✅ Generated UI schema: ", "", 1).strip()
                            if rel and rel not in ui_schema_generated:
                                ui_schema_generated.append(rel)
                                if rel not in generated_files:
                                    generated_files.append(rel)

            # Fallback: if no generated files were detected from this run (e.g., idempotent run
            # that didn't rewrite files), try to reuse the most recent manifest's generatedFiles
            # for the same (programId, entryNodeId) so that the UI can still show traceability.
            if not generated_files:
                try:
                    migrations_dir = ROOT_DIR / "global_context" / "migrations"
                    if migrations_dir.is_dir():
                        pattern = f"{program_id}_{entry_node_id}_*.json"
                        latest_manifest = None
                        for path in migrations_dir.glob(pattern):
                            if latest_manifest is None or path.stat().st_mtime > latest_manifest.stat().st_mtime:
                                latest_manifest = path
                        if latest_manifest is not None:
                            mf_data = json.loads(latest_manifest.read_text(encoding="utf-8", errors="ignore"))
                            prev_files = mf_data.get("generatedFiles") or []
                            for f in prev_files:
                                if f and f not in generated_files:
                                    generated_files.append(f)
                            prev_ui = mf_data.get("uiSchemaGenerated") or []
                            for f in prev_ui:
                                if f and f not in ui_schema_generated:
                                    ui_schema_generated.append(f)
                except Exception:
                    # Best-effort only; safe to ignore failures here.
                    pass

            # Optional: extract RPG snippet for traceability from the first context file
            rpg_snippet = None
            try:
                if context_files:
                    cf0 = Path(context_files[0])
                    if cf0.is_file():
                        cf_data = json.loads(cf0.read_text(encoding="utf-8", errors="ignore"))
                        rpg_snippet = cf_data.get("rpgSnippet") or cf_data.get("astNode", {}).get("rpgSnippet")
            except Exception:
                rpg_snippet = None

            # Optional: identify a primary generated Java file for side-by-side view
            primary_service_file: str | None = None
            for f in generated_files:
                # Prefer a generated service class for this feature
                if "/service/" in f:
                    primary_service_file = f
                    break
            if not primary_service_file:
                # Fallback: use a controller if no service was generated
                for f in generated_files:
                    if "/web/" in f:
                        primary_service_file = f
                        break
            if not primary_service_file and generated_files:
                # Last resort: use the first generated file so UI can still show something
                primary_service_file = generated_files[0]

            primary_service_source = None
            if primary_service_file:
                try:
                    java_root = ROOT_DIR / "warranty_demo" / "src" / "main" / "java"
                    java_path = java_root / primary_service_file
                    if java_path.is_file():
                        text = java_path.read_text(encoding="utf-8", errors="ignore")
                        # Keep response payload reasonable
                        primary_service_source = text[:12000]
                except Exception:
                    primary_service_source = None

            # Determine whether migration runs succeeded
            migration_ok = False
            if runs:
                migration_ok = True
                for r in runs:
                    if "error" in r:
                        migration_ok = False
                        break
                    rc = r.get("returnCode")
                    if rc not in (0, None):
                        migration_ok = False
                        break

            # Build manifest
            migrations_dir = ROOT_DIR / "global_context" / "migrations"
            migrations_dir.mkdir(parents=True, exist_ok=True)
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            manifest_path = migrations_dir / f"{program_id}_{entry_node_id}_{ts}.json"

            nodes_lookup = {n.get("id"): n for n in ctx.get("nodes", []) if isinstance(n, dict)}
            nodes_slice_info = []
            for nid in nodes_in_slice:
                n = nodes_lookup.get(nid) or {}
                nodes_slice_info.append({
                    "nodeId": nid,
                    "kind": n.get("kind"),
                    "name": n.get("name"),
                    "procedureName": n.get("procedureName"),
                    "range": n.get("range"),
                })

            if db_files:
                db_items = [f"{d.get('name')} ({d.get('library')})" for d in db_files]
                # Format as multiline: wrap every 8 items for readability
                wrap_at = 8
                db_lines = []
                for i in range(0, len(db_items), wrap_at):
                    chunk = db_items[i : i + wrap_at]
                    db_lines.append(", ".join(chunk))
                db_part = "\n  ".join(db_lines)
                summary = f"Migrated feature starting at {program_id} {entry_node_id} with {len(nodes_in_slice)} node(s); DB files involved:\n  {db_part}."
            else:
                summary = f"Migrated feature starting at {program_id} {entry_node_id} with {len(nodes_in_slice)} node(s)."

            # Write manifest
            manifest = {
                "programId": program_id,
                "unitId": unit_id,
                "entryNodeId": entry_node_id,
                "nodesInSlice": nodes_slice_info,
                "contextFiles": context_files,
                "dbFiles": db_files,
                "generatedFiles": generated_files,
                "uiSchemaGenerated": ui_schema_generated,
                "rpgSnippet": rpg_snippet,
                "primaryServiceFile": primary_service_file,
                "runs": runs,
                "summary": summary,
            }
            manifest_path.write_text(json.dumps(manifest, indent=2), encoding="utf-8")

            # If migration failed (e.g. LLM/API error), surface this to the client instead of
            # pretending success or running a build. Previously generated Java (from earlier
            # successful runs) remains in the project, but this specific migration attempt
            # did not produce new code.
            if not migration_ok:
                error_chunks = []
                for r in runs:
                    ctx_file = r.get("contextFile") or "<unknown context>"
                    if "error" in r:
                        error_chunks.append(f"{ctx_file}: {r['error']}")
                    rc = r.get("returnCode")
                    if rc not in (0, None):
                        stderr = (r.get("stderrPreview") or "").strip()
                        if stderr:
                            error_chunks.append(f"{ctx_file} stderr:\n{stderr}")
                error_text = "\n\n".join(error_chunks) or "Migration script failed; see server logs."
                send_progress("Migration failed.")
                self._write_chunked_line(json.dumps({"result": {
                    "success": False,
                    "summary": summary,
                    "manifestPath": str(manifest_path),
                    "generatedFiles": generated_files,
                    "uiSchemaGenerated": ui_schema_generated,
                    "rpgSnippet": rpg_snippet,
                    "primaryServiceFile": primary_service_file,
                    "error": error_text,
                    "cliMode": any_large,
                }}))
                self._end_chunked_response()
                return

            # Pipeline separation: Migrate Feature (tab 3) = code gen only.
            # Build Application (tab 5) handles compile/build error resolution.
            send_progress("Code generation complete. Run Build Application (tab 5) to compile.")

            # Respond with migration info only (no build - that's tab 5's responsibility)
            self._write_chunked_line(json.dumps({"result": {
                "success": True,
                "summary": summary,
                "manifestPath": str(manifest_path),
                "generatedFiles": generated_files,
                "uiSchemaGenerated": ui_schema_generated,
                "rpgSnippet": rpg_snippet,
                "primaryServiceFile": primary_service_file,
                "primaryServiceSource": primary_service_source,
                "buildSuccess": None,
                "buildExitCode": None,
                "buildOutput": None,
                "cliMode": any_large,
            }}))
            self._end_chunked_response()
            return

        if parsed.path == "/api/regenerate-ui-schema":
            content_length = int(self.headers.get("Content-Length", 0))
            program_id = None
            entry_node_id = None
            if content_length > 0:
                try:
                    body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                    program_id = body.get("programId")
                    entry_node_id = body.get("entryNodeId")
                except Exception:
                    pass
            if not program_id or not entry_node_id:
                self._send_json({
                    "success": False,
                    "error": "programId and entryNodeId are required",
                }, status=400)
                return
            unit_short = program_id.split("/")[-1] if "/" in program_id else program_id
            ctx_path = ROOT_DIR / "context_index" / f"{unit_short}_{entry_node_id}.json"
            if not ctx_path.exists():
                self._send_json({
                    "success": False,
                    "error": f"Context not found: {unit_short}_{entry_node_id}.json. Run Build Global Context and Migrate Feature first.",
                }, status=400)
                return
            try:
                ctx = json.loads(ctx_path.read_text(encoding="utf-8", errors="ignore"))
                from ui_schema_generator import generate_ui_schema, write_ui_schema
                schema = generate_ui_schema(ctx, unit_id=unit_short)
                if not schema:
                    self._send_json({
                        "success": False,
                        "error": "No display files in context; cannot generate UI schema.",
                    }, status=400)
                    return
                out_path = write_ui_schema(schema, ROOT_DIR / "warranty_demo")
                rel_path = str(out_path.relative_to(ROOT_DIR))
                self._send_json({
                    "success": True,
                    "message": f"UI schema generated: {rel_path}",
                    "uiSchemaPath": rel_path,
                    "screenId": schema.get("screenId"),
                })
            except ImportError as e:
                self._send_json({
                    "success": False,
                    "error": f"ui_schema_generator not found: {e}",
                }, status=500)
            except Exception as e:
                self._send_json({
                    "success": False,
                    "error": str(e),
                }, status=500)
            return

        if parsed.path == "/api/validate":
            content_length = int(self.headers.get("Content-Length", 0))
            project_dir = "warranty_demo"
            program_id = None
            entry_node_id = None
            if content_length > 0:
                try:
                    body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                    project_dir = (body.get("projectDir") or project_dir).strip() or project_dir
                    program_id = body.get("programId")
                    entry_node_id = body.get("entryNodeId")
                except Exception:
                    pass

            proj_path = ROOT_DIR / project_dir
            if not proj_path.is_dir():
                self._send_json({
                    "success": False,
                    "error": f"Project directory not found: {project_dir}",
                }, status=400)
                return

            context_path = None
            if program_id and entry_node_id:
                unit_short = program_id
                cf = ROOT_DIR / "context_index" / f"{unit_short}_{entry_node_id}.json"
                if cf.exists():
                    context_path = cf

            try:
                from validate_pure_java import PureJavaValidator
                validator = PureJavaValidator(proj_path, context_path)
                results = validator.validate_all()

                # Include generated file contents only for the migrated program/node
                generated_files_data = []
                rpg_snippet = None
                rpg_start_line = 1
                if program_id and entry_node_id:
                    try:
                        migrations_dir = ROOT_DIR / "global_context" / "migrations"
                        if migrations_dir.is_dir():
                            pattern = f"{program_id}_{entry_node_id}_*.json"
                            latest = None
                            for p in migrations_dir.glob(pattern):
                                if latest is None or p.stat().st_mtime > latest.stat().st_mtime:
                                    latest = p
                            if latest:
                                mf = json.loads(latest.read_text(encoding="utf-8", errors="ignore"))
                                rpg_snippet = mf.get("rpgSnippet")
                                nodes = mf.get("nodesInSlice") or []
                                if nodes and nodes[0].get("range"):
                                    rpg_start_line = nodes[0]["range"].get("startLine", 1)
                                # Fallback to context file when manifest has no rpgSnippet
                                if not rpg_snippet and context_path and context_path.is_file():
                                    try:
                                        cf_data = json.loads(context_path.read_text(encoding="utf-8", errors="ignore"))
                                        rpg_snippet = cf_data.get("rpgSnippet") or (cf_data.get("astNode") or {}).get("rpgSnippet")
                                        if not rpg_snippet and nodes and nodes[0].get("range"):
                                            rpg_start_line = nodes[0]["range"].get("startLine", 1)
                                    except Exception:
                                        pass
                                # Fallback: synthesize from AST when both manifest and context have no rpgSnippet
                                if not rpg_snippet:
                                    try:
                                        node_index_path = ROOT_DIR / "context_index" / f"{program_id}_nodes.json"
                                        if node_index_path.is_file():
                                            ni_data = json.loads(node_index_path.read_text(encoding="utf-8", errors="ignore"))
                                            node_index = ni_data.get("nodes", {})
                                            line_to_nodes = ni_data.get("lineToNodes", {})
                                            if node_index:
                                                rpg_snippet, rpg_start_line = self._synthesize_rpg_from_ast(
                                                    entry_node_id, node_index, line_to_nodes or {})
                                    except Exception:
                                        pass
                                # Final fallback: use full traceability builder (loads RPG from AST/context)
                                if not rpg_snippet:
                                    try:
                                        tb_result = self._build_traceability_response(program_id, entry_node_id)
                                        if tb_result and "error" not in tb_result:
                                            rpg_snippet = tb_result.get("rpgSource") or ""
                                            rpg_start_line = tb_result.get("rpgStartLine") or 1
                                    except Exception:
                                        pass
                                java_root = proj_path / "src" / "main" / "java"
                                for rel in mf.get("generatedFiles") or []:
                                    fp = java_root / rel
                                    if fp.is_file():
                                        try:
                                            content = fp.read_text(encoding="utf-8", errors="ignore")
                                            generated_files_data.append({"path": rel, "content": content[:15000]})
                                        except Exception:
                                            pass
                    except Exception:
                        pass

                resp_data = {
                    "success": True,
                    "validation": results,
                    "generatedFiles": generated_files_data,
                    "rpgSnippet": rpg_snippet,
                    "rpgStartLine": rpg_start_line,
                }
                # Include nodeIndex for @rpg-trace hover
                if program_id and entry_node_id and generated_files_data:
                    try:
                        tb_result = self._build_traceability_response(program_id, entry_node_id)
                        if tb_result and "error" not in tb_result and tb_result.get("nodeIndex"):
                            resp_data["nodeIndex"] = tb_result["nodeIndex"]
                    except Exception:
                        pass
                self._send_json(resp_data)
            except Exception as e:
                self._send_json({
                    "success": False,
                    "error": str(e),
                }, status=500)
            return

        if parsed.path == "/api/push-to-repo":
            content_length = int(self.headers.get("Content-Length", 0))
            project_dir = "warranty_demo"
            branch_name = None
            if content_length > 0:
                try:
                    body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                    project_dir = (body.get("projectDir") or project_dir).strip() or project_dir
                    branch_name = (body.get("branch") or "").strip() or None
                except Exception:
                    pass

            proj_path = ROOT_DIR / project_dir
            if not proj_path.is_dir():
                self._send_json({
                    "success": False,
                    "error": f"Project directory not found: {project_dir}",
                }, status=400)
                return

            try:
                from push_to_repo import push_to_repo
                result = push_to_repo(proj_path, branch_name=branch_name)
                if result.get("success"):
                    self._send_json({
                        "success": True,
                        "message": result.get("message", "Pushed successfully"),
                        "branch": result.get("branch"),
                        "repo": result.get("repo"),
                    })
                else:
                    self._send_json({
                        "success": False,
                        "error": result.get("error", "Push failed"),
                    }, status=500)
            except Exception as e:
                self._send_json({
                    "success": False,
                    "error": str(e),
                }, status=500)
            return

        if parsed.path == "/api/run-application":
            global _app_process
            proj_path = ROOT_DIR / "warranty_demo"
            if not proj_path.is_dir():
                self._send_json({"started": False, "error": "warranty_demo not found. Build the application first."}, status=400)
                return
            # Clear dead process so we can start fresh
            if _app_process is not None and _app_process.poll() is not None:
                _app_process = None
            if _app_process is not None and _app_process.poll() is None:
                self._send_json({"started": True, "message": "Application already running."})
                return
            # Consume any POST body (profile param no longer used)
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length > 0:
                self.rfile.read(content_length)
            log_path = proj_path / "target" / "spring-boot-run.log"
            log_path.parent.mkdir(parents=True, exist_ok=True)
            try:
                # Default: H2 (so H2 Console works). Set RUN_PROFILE=rds for PostgreSQL
                run_profile = os.environ.get("RUN_PROFILE", "").strip()
                # For H2: remove stale DB so schema + DataInitializer seed run fresh (fixes "No claims found")
                if not run_profile:
                    data_dir = proj_path / "data"
                    for f in ["warranty_db.mv.db", "warranty_db.trace.db"]:
                        p = data_dir / f
                        if p.exists():
                            try:
                                p.unlink()
                            except OSError:
                                pass
                cmd = ["mvn", "spring-boot:run", "-DskipTests"]
                if run_profile:
                    cmd.extend(["-Dspring-boot.run.profiles=" + run_profile])
                log_file = open(log_path, "w", encoding="utf-8")
                log_file.write(f"Starting: {' '.join(cmd)}\n")
                log_file.flush()
                _app_process = subprocess.Popen(
                    cmd,
                    cwd=str(proj_path),
                    stdout=log_file,
                    stderr=subprocess.STDOUT,
                    start_new_session=True,
                )
                time.sleep(3)
                if _app_process.poll() is not None:
                    with open(log_path, "r", encoding="utf-8", errors="ignore") as f:
                        log_content = f.read()
                    tail = "".join(log_content.splitlines()[-40:])
                    # Runtime fixer: try IdClass fix and retry once
                    try:
                        from fix_runtime_errors import apply_fixes
                        fixed, msg = apply_fixes(proj_path, log_content)
                        if fixed:
                            log_file2 = open(log_path, "a", encoding="utf-8")
                            log_file2.write(f"\n\n=== Runtime fix applied: {msg} ===\n")
                            log_file2.flush()
                            _app_process = subprocess.Popen(
                                cmd,
                                cwd=str(proj_path),
                                stdout=log_file2,
                                stderr=subprocess.STDOUT,
                                start_new_session=True,
                            )
                            time.sleep(5)
                            if _app_process.poll() is None:
                                self._send_json({
                                    "started": True,
                                    "message": f"Application started after runtime fix. {msg}",
                                })
                                return
                            with open(log_path, "r", encoding="utf-8", errors="ignore") as f2:
                                tail = "".join(f2.readlines()[-40:])
                    except Exception:
                        pass
                    self._send_json({
                        "started": False,
                        "error": f"Application exited immediately (exit {_app_process.returncode}). Check {log_path}",
                        "logTail": tail[-2000:] if len(tail) > 2000 else tail,
                    }, status=500)
                    _app_process = None
                    return
                self._send_json({
                    "started": True,
                    "message": "Application starting. Check status in 30-60 seconds. Log: " + str(log_path),
                })
            except Exception as e:
                self._send_json({"started": False, "error": str(e)}, status=500)
            return

        if parsed.path == "/api/build-application":
            # Run a Maven build for the main application (default: warranty_demo)
            content_length = int(self.headers.get("Content-Length", 0))
            project_dir = "warranty_demo"
            hitl_mode = True
            if content_length > 0:
                try:
                    body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                    project_dir = (body.get("projectDir") or project_dir).strip() or project_dir
                    hitl_mode = body.get("hitlMode", True)
                except Exception:
                    pass

            proj_path = ROOT_DIR / project_dir
            if not proj_path.is_dir():
                self._send_json({
                    "success": False,
                    "error": f"Project directory not found: {proj_path}",
                }, status=400)
                return

            java_count = 0
            java_root = proj_path / "src" / "main" / "java"
            if java_root.is_dir():
                java_count = len(list(java_root.rglob("*.java")))
            last_migrated = None
            migrations_dir = ROOT_DIR / "global_context" / "migrations"
            if migrations_dir.is_dir():
                manifests = list(migrations_dir.glob("*.json"))
                if manifests:
                    latest = max(manifests, key=lambda p: p.stat().st_mtime)
                    try:
                        mf = json.loads(latest.read_text(encoding="utf-8", errors="ignore"))
                        last_migrated = {
                            "programId": mf.get("programId"),
                            "entryNodeId": mf.get("entryNodeId"),
                            "generatedCount": len(mf.get("generatedFiles") or []),
                        }
                    except Exception:
                        pass

            build_result = _run_maven_build_with_autofix(proj_path, hitl_mode=hitl_mode)
            resp = {
                "success": build_result["buildSuccess"],
                "output": _truncate_build_output(build_result["buildOutput"] or ""),
                "exitCode": build_result["buildExitCode"],
                "buildContext": {
                    "projectDir": project_dir,
                    "javaFileCount": java_count,
                    "lastMigrated": last_migrated,
                },
            }
            if build_result.get("testSummary"):
                resp["testSummary"] = build_result["testSummary"]
            if build_result.get("testOutput"):
                resp["testOutput"] = _truncate_build_output(build_result["testOutput"])
            if build_result.get("needsReview"):
                resp["needsReview"] = True
                resp["suggestedFixes"] = build_result.get("suggestedFixes")
                resp["errorSummary"] = build_result.get("errorSummary", "Build failed. Review suggested fixes.")
                # Include old content for each file so UI can show diff
                suggested_fixes = build_result.get("suggestedFixes") or {}
                detail = {}
                for rel_path in suggested_fixes:
                    try:
                        fp = proj_path / rel_path.replace("\\", "/")
                        if fp.is_file():
                            detail[rel_path] = {
                                "old": fp.read_text(encoding="utf-8", errors="ignore"),
                                "new": suggested_fixes[rel_path],
                            }
                    except Exception:
                        pass
                if detail:
                    resp["suggestedFixesDetail"] = detail
            self._send_json(resp, status=200 if build_result["buildSuccess"] else 500)
            return

        if parsed.path == "/api/apply-approved-fixes":
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length == 0:
                self._send_json({"success": False, "error": "Request body required"}, status=400)
                return
            try:
                body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                project_dir = (body.get("projectDir") or "warranty_demo").strip()
                suggested_fixes = body.get("suggestedFixes")
                if not suggested_fixes or not isinstance(suggested_fixes, dict):
                    self._send_json({"success": False, "error": "suggestedFixes (object) required"}, status=400)
                    return
            except json.JSONDecodeError as e:
                self._send_json({"success": False, "error": f"Invalid JSON: {e}"}, status=400)
                return

            proj_path = ROOT_DIR / project_dir
            if not proj_path.is_dir():
                self._send_json({"success": False, "error": f"Project directory not found: {proj_path}"}, status=400)
                return

            written = []
            for rel_path, content in suggested_fixes.items():
                rel_path = rel_path.replace("\\", "/")
                target = proj_path / rel_path
                try:
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_text(content, encoding="utf-8")
                    written.append(rel_path)
                except Exception as e:
                    self._send_json({"success": False, "error": f"Failed to write {rel_path}: {e}"}, status=500)
                    return

            build_result = _run_maven_build_with_autofix(proj_path, hitl_mode=True)
            resp = {
                "success": build_result["buildSuccess"],
                "output": _truncate_build_output(build_result["buildOutput"] or ""),
                "exitCode": build_result["buildExitCode"],
                "filesApplied": written,
                "buildContext": {
                    "projectDir": project_dir,
                    "javaFileCount": len(list((proj_path / "src" / "main" / "java").rglob("*.java"))) if (proj_path / "src" / "main" / "java").is_dir() else 0,
                },
            }
            if build_result.get("testSummary"):
                resp["testSummary"] = build_result["testSummary"]
            if build_result.get("testOutput"):
                resp["testOutput"] = _truncate_build_output(build_result["testOutput"])
            if build_result.get("needsReview") and build_result.get("suggestedFixes"):
                resp["needsReview"] = True
                resp["suggestedFixes"] = build_result["suggestedFixes"]
                resp["errorSummary"] = build_result.get("errorSummary", "Build still failed. Review new suggested fixes.")
                suggested_fixes = build_result.get("suggestedFixes") or {}
                detail = {}
                for rel_path in suggested_fixes:
                    try:
                        fp = proj_path / rel_path.replace("\\", "/")
                        if fp.is_file():
                            detail[rel_path] = {"old": fp.read_text(encoding="utf-8", errors="ignore"), "new": suggested_fixes[rel_path]}
                    except Exception:
                        pass
                if detail:
                    resp["suggestedFixesDetail"] = detail
            self._send_json(resp, status=200 if build_result["buildSuccess"] else 500)
            return

        # Fallback 404 for unknown POST paths
        self.send_error(404, "Not Found")


def main():
    _ensure_anthropic_key_from_workspace_env()
    port = int(os.environ.get("UI_PORT", "8003"))
    bind_host = os.environ.get("BIND_HOST", "0.0.0.0")  # 0.0.0.0 for Docker; 127.0.0.1 for local-only
    addr = (bind_host, port)
    httpd = HTTPServer(addr, GlobalContextHandler)
    print()
    print(f"Global Context UI server running on http://localhost:{port}/")
    print()
    print("Endpoints:")
    print(f"  GET  /                    - Global Context UI (ui_global_context.html)")
    print(f"  GET  /traceability        - RPG→Java Traceability Viewer")
    print(f"  GET  /api/health              - Health check")
    print(f"  GET  /api/discover-directories")
    print(f"  GET  /api/list-programs")
    print(f"  GET  /api/build-context")
    print(f"  GET  /api/customize-guide")
    print(f"  POST /api/validate")
    print(f"  POST /api/push-to-repo")
    print(f"  GET  /api/run-application-log  - Last N lines of spring-boot-run.log")
    print(f"  POST /api/run-application")
    print(f"  POST /api/build-global-context")
    print(f"  POST /api/export-neo4j")
    print(f"  POST /api/migrate-feature")
    print(f"  POST /api/regenerate-ui-schema")
    print(f"  POST /api/build-application")
    print(f"  POST /api/apply-approved-fixes")
    print()
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down...")
        httpd.server_close()


if __name__ == "__main__":
    main()

