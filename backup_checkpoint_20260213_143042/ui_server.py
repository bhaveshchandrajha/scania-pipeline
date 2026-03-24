#!/usr/bin/env python3
"""
Simple web UI server to demonstrate the full migration pipeline:

- Lists all indexed AST nodes from context_index/manifest.json
- Lets you trigger LLM migration for a given node
- Shows the generated Java code in the browser

Usage:
  export ANTHROPIC_API_KEY=sk-ant-...
  python ui_server.py

Then open: http://localhost:8001
"""

import json
import os
import subprocess
import sys
import tempfile
from getpass import getpass
from pathlib import Path

from http.server import HTTPServer, SimpleHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
from typing import Optional

ROOT_DIR = Path(__file__).parent
CONTEXT_DIR = ROOT_DIR / "context_index"
MANIFEST_PATH = CONTEXT_DIR / "manifest.json"


class MigrationHandler(SimpleHTTPRequestHandler):
    """
    HTTP handler serving:
    - GET /            -> static index.html UI
    - GET /api/manifest -> manifest.json (list of nodes)
    - GET /api/rpg-snippet?unitId=...&nodeId=... -> RPG snippet for traceability
    - POST /api/compile -> Compile Java code (body=Java source), returns compile result
    - POST /api/migrate?unitId=...&nodeId=...&model=... -> run migrate_with_claude.py (non-streaming)
    - GET /api/migrate-stream?unitId=...&nodeId=...&model=... -> SSE streaming migration
    """

    def _send_json(self, obj, status=200):
        """Send JSON response, gracefully handling client disconnections."""
        try:
            data = json.dumps(obj).encode("utf-8")
            self.send_response(status)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            self.wfile.flush()
        except (BrokenPipeError, ConnectionResetError, OSError) as e:
            # Client disconnected before response was sent - this is normal and harmless
            # Common when user refreshes, navigates away, or request times out
            pass
        except Exception as e:
            # Log unexpected errors but don't crash
            print(f"[Error] Failed to send JSON response: {e}", flush=True)

    def _clean_java_code(self, java_code: str) -> str:
        """Remove markdown code fences and other non-Java content from generated code."""
        code = java_code.strip()
        # Remove markdown code fences (```java ... ``` or ``` ... ```)
        lines = code.split("\n")
        # Remove first line if it starts with ```
        if lines and lines[0].strip().startswith("```"):
            lines = lines[1:]
        # Remove last line if it's just ```
        if lines and lines[-1].strip() == "```":
            lines = lines[:-1]
        code = "\n".join(lines).strip()
        # Also remove any trailing ``` that might be on the same line
        if code.endswith("```"):
            code = code[:-3].strip()
        return code

    def _extract_public_class_name(self, java_code: str) -> Optional[str]:
        """Extract the first public class name from Java code. Returns None if no public class found."""
        import re
        # Match: public class ClassName or public final class ClassName, etc.
        match = re.search(r'public\s+(?:final\s+)?(?:abstract\s+)?class\s+(\w+)', java_code)
        if match:
            return match.group(1)
        return None

    def _run_compile_check(self, java_code: str) -> dict:
        """Run javac on Java code and return compile result (reused from validate_java.py logic)."""
        # Clean markdown fences before compiling
        java_code = self._clean_java_code(java_code)
        result = {"success": False, "errors": [], "message": "", "skipped": True}
        javac_path = None
        if os.environ.get("JAVA_HOME"):
            jc = Path(os.environ["JAVA_HOME"]) / "bin" / "javac"
            if jc.exists():
                javac_path = str(jc)
        if not javac_path:
            import shutil
            javac_path = shutil.which("javac")
        if not javac_path:
            result["message"] = "javac not found (set JAVA_HOME or add Java to PATH)"
            return result

        # Resolve classpath: env COMPILE_CLASSPATH, or ROOT_DIR/lib/*.jar
        cp = os.environ.get("COMPILE_CLASSPATH")
        if not cp:
            lib = ROOT_DIR / "lib"
            if lib.is_dir():
                jars = list(lib.glob("*.jar"))
                if jars:
                    cp = os.pathsep.join(str(p) for p in jars)
        if not cp:
            result["message"] = "No classpath (set COMPILE_CLASSPATH or run: mvn dependency:copy-dependencies -DoutputDirectory=lib)"
            return result

        result["skipped"] = False
        try:
            # Extract public class name to use as filename (Java requires public classes match filename)
            public_class_name = self._extract_public_class_name(java_code)
            if public_class_name:
                # Use the public class name as the filename
                temp_java = ROOT_DIR / f"{public_class_name}.java"
                with open(temp_java, "w", encoding="utf-8") as f:
                    f.write(java_code)
            else:
                # No public class, use temp file
                with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False, dir=str(ROOT_DIR), encoding="utf-8") as f:
                    f.write(java_code)
                    temp_java = Path(f.name)
            
            try:
                # Verify Spring Data JPA is on classpath
                cp_jars = cp.split(os.pathsep)
                has_spring_data_jpa = any("spring-data-jpa" in jar for jar in cp_jars)
                if not has_spring_data_jpa:
                    result["message"] = "Spring Data JPA not found on classpath. Run: mvn dependency:copy-dependencies -DoutputDirectory=lib"
                    result["errors"] = [f"Missing spring-data-jpa jar. Classpath has {len(cp_jars)} jars: {', '.join([Path(j).name for j in cp_jars[:5]])}..."]
                    return result
                
                # Use the filename (not full path) for javac since we're compiling from ROOT_DIR
                java_filename = temp_java.name if isinstance(temp_java, Path) else Path(temp_java).name
                proc = subprocess.run(
                    [javac_path, "-cp", cp, "-Xlint:none", "-encoding", "UTF-8", java_filename],
                    cwd=str(ROOT_DIR),
                    capture_output=True,
                    text=True,
                    timeout=30,
                )
                if proc.returncode == 0:
                    result["success"] = True
                    result["message"] = "Compiles successfully"
                else:
                    err_text = (proc.stderr or proc.stdout or "").strip()
                    error_lines = [line for line in err_text.split("\n") if line.strip()]
                    # Filter: only report syntax errors, not missing custom types (entities, repositories, DTOs)
                    syntax_errors = []
                    missing_types = []
                    # Known Spring/JPA types that should be on classpath
                    spring_jpa_types = ["JpaRepository", "Repository", "CrudRepository", "PagingAndSortingRepository",
                                       "Entity", "Service", "Autowired", "Transactional", "Column", "EntityManager",
                                       "Query", "Modifying", "Component", "Controller", "RestController"]
                    for i, line in enumerate(error_lines):
                        if "error:" in line.lower():
                            # Syntax errors: illegal character, unexpected token, etc.
                            if any(keyword in line.lower() for keyword in ["illegal character", "unexpected", "';' expected", "'(' expected", "')' expected", "not a statement", "invalid method declaration"]):
                                syntax_errors.append(line)
                            # Missing types: "cannot find symbol", "package ... does not exist"
                            elif "cannot find symbol" in line.lower() or "does not exist" in line.lower():
                                # Check if it's a standard library/Spring type by looking at the error context
                                is_spring_jpa = any(pkg in line for pkg in ["java.", "jakarta.", "org.springframework."])
                                # Also check the next line(s) for the symbol name if it's a "cannot find symbol" error
                                symbol_name = None
                                if "cannot find symbol" in line.lower() and i + 1 < len(error_lines):
                                    next_line = error_lines[i + 1]
                                    if "symbol:" in next_line.lower():
                                        # Extract symbol name (e.g., "symbol: class JpaRepository")
                                        parts = next_line.split(":")
                                        if len(parts) > 1:
                                            symbol_part = parts[-1].strip()
                                            # Extract class/interface name
                                            words = symbol_part.split()
                                            if len(words) > 1:
                                                symbol_name = words[-1]
                                if is_spring_jpa or (symbol_name and symbol_name in spring_jpa_types):
                                    syntax_errors.append(line)
                                    if i + 1 < len(error_lines):
                                        syntax_errors.append(error_lines[i + 1])  # Include symbol line
                                else:
                                    missing_types.append(line)
                            else:
                                syntax_errors.append(line)
                    
                    if syntax_errors:
                        result["errors"] = syntax_errors[:20]  # Limit to first 20
                        result["message"] = f"Compilation failed: {len(syntax_errors)} syntax error(s)"
                        if missing_types:
                            result["message"] += f" (and {len(missing_types)} missing custom type(s) - expected)"
                    elif missing_types:
                        # Only missing custom types - this is expected, code is syntactically valid
                        result["success"] = True
                        result["message"] = f"Syntax OK (missing {len(missing_types)} custom types - entities/repositories/DTOs expected to be generated separately)"
                        result["errors"] = []  # Don't show missing types as errors
                    else:
                        result["errors"] = error_lines[:20]
                        result["message"] = "Compilation failed"
            finally:
                try:
                    # Clean up temp file and .class files
                    if isinstance(temp_java, Path):
                        temp_java.unlink()
                        class_file = temp_java.with_suffix(".class")
                        if class_file.exists():
                            class_file.unlink()
                    else:
                        os.unlink(temp_java)
                        class_file = Path(temp_java).with_suffix(".class")
                        if class_file.exists():
                            class_file.unlink()
                except OSError:
                    pass
        except subprocess.TimeoutExpired:
            result["message"] = "Compilation timed out"
            result["errors"] = ["javac timed out after 30s"]
        except Exception as e:
            result["message"] = str(e)
            result["errors"] = [str(e)]

        return result

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path == "/":
            # Serve the UI file
            ui_path = ROOT_DIR / "ui_index.html"
            if not ui_path.exists():
                self.send_error(404, "ui_index.html not found")
                return
            content = ui_path.read_bytes()
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
            return

        if parsed.path == "/api/ping":
            self._send_json({"status": "ok", "message": "UI server is running"})
            return

        if parsed.path == "/api/manifest":
            if not MANIFEST_PATH.exists():
                self._send_json({"error": "manifest.json not found. Run indexing first."}, status=404)
                return
            with MANIFEST_PATH.open("r", encoding="utf-8") as f:
                manifest = json.load(f)
            self._send_json(manifest)
            return

        if parsed.path == "/api/rpg-snippet":
            params = parse_qs(parsed.query)
            unit_id = params.get("unitId", [None])[0]
            node_id = params.get("nodeId", [None])[0]
            if not unit_id or not node_id:
                self._send_json({"error": "unitId and nodeId are required"}, status=400)
                return
            ctx_file = CONTEXT_DIR / f"{unit_id}_{node_id}.json"
            if not ctx_file.exists():
                self._send_json({"error": f"Context file not found: {ctx_file.name}"}, status=404)
                return
            with ctx_file.open("r", encoding="utf-8") as f:
                ctx = json.load(f)
            ast_node = ctx.get("astNode", {})
            range_node = ast_node.get("range") or {}
            source_id = range_node.get("sourceId") or range_node.get("fileId") or ""
            self._send_json({
                "unitId": unit_id,
                "nodeId": node_id,
                "kind": ast_node.get("kind"),
                "sourceFileId": source_id,
                "rpgSnippet": ctx.get("rpgSnippet", ""),
            })
            return

        if parsed.path == "/api/migrate-stream":
            params = parse_qs(parsed.query)
            unit_id = params.get("unitId", [None])[0]
            node_id = params.get("nodeId", [None])[0]
            model = params.get("model", ["claude-sonnet-4-5"])[0]

            if not unit_id or not node_id:
                self._send_json({"error": "unitId and nodeId are required"}, status=400)
                return

            ctx_file = CONTEXT_DIR / f"{unit_id}_{node_id}.json"
            if not ctx_file.exists():
                self._send_json(
                    {"error": f"Context file not found: {ctx_file.name}"},
                    status=404,
                )
                return

            api_key = os.environ.get("ANTHROPIC_API_KEY")
            if not api_key:
                self._send_json(
                    {"error": "ANTHROPIC_API_KEY env var is not set."},
                    status=400,
                )
                return

            # Stream migration using SSE
            self._stream_migration(unit_id, node_id, ctx_file, model)
            return

        return super().do_GET()

    def do_POST(self):
        parsed = urlparse(self.path)
        
        if parsed.path == "/api/compile":
            # POST endpoint: compile Java code from request body
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length == 0:
                self._send_json({"error": "Java code required in request body"}, status=400)
                return
            java_code = self.rfile.read(content_length).decode("utf-8")
            if not java_code or not java_code.strip():
                self._send_json({"error": "Java code is empty"}, status=400)
                return
            
            # Reuse compile logic
            result = self._run_compile_check(java_code)
            self._send_json(result)
            return
        
        if parsed.path == "/api/migrate":
            params = parse_qs(parsed.query)
            unit_id = params.get("unitId", [None])[0]
            node_id = params.get("nodeId", [None])[0]
            model = params.get("model", ["claude-sonnet-4-5"])[0]

            if not unit_id or not node_id:
                self._send_json({"error": "unitId and nodeId are required"}, status=400)
                return

            ctx_file = CONTEXT_DIR / f"{unit_id}_{node_id}.json"
            if not ctx_file.exists():
                self._send_json(
                    {"error": f"Context file not found: {ctx_file.name}. Make sure indexing has run."},
                    status=404,
                )
                return

            api_key = os.environ.get("ANTHROPIC_API_KEY")
            if not api_key:
                self._send_json(
                    {
                        "error": "ANTHROPIC_API_KEY env var is not set. "
                                 "Export it before calling /api/migrate."
                    },
                    status=400,
                )
                return

            # Call migrate_with_claude.py as a subprocess and capture the Java output.
            ctx_size_kb = ctx_file.stat().st_size / 1024
            ui_timeout = 1200  # 20 minutes for very large context (e.g. n404) + 64k output tokens
            print(f"[Migration] Starting {unit_id}_{node_id} (context {ctx_size_kb:.0f} KB, timeout {ui_timeout}s)...", flush=True)
            sys.stderr.flush()
            try:
                proc = subprocess.run(
                    [
                        "python3",
                        "-u",
                        str(ROOT_DIR / "migrate_with_claude.py"),
                        str(ctx_file),
                        "--model",
                        model,
                        "--max-tokens",
                        "64000",
                    ],
                    cwd=str(ROOT_DIR),
                    env=os.environ.copy(),
                    check=False,
                    capture_output=True,
                    text=True,
                    timeout=ui_timeout,
                )
                stderr_out = proc.stderr or ""
                # Rate limit (429): script exits with 129 or stderr contains rate_limit/429
                if proc.returncode == 129 or "429" in stderr_out or "rate_limit" in stderr_out.lower():
                    print(f"[Migration] Rate limit (429) for {unit_id}_{node_id}", flush=True)
                    self._send_json(
                        {
                            "error": "API rate limit exceeded (429)",
                            "details": "Wait one minute and try again, or run from CLI: python3 migrate_with_claude.py context_index/" + unit_id + "_" + node_id + ".json --stream",
                        },
                        status=429,
                    )
                    return
                if proc.returncode != 0:
                    raise subprocess.CalledProcessError(proc.returncode, proc.args, proc.stdout, proc.stderr)
                java_code = proc.stdout
                print(f"[Migration] Subprocess completed for {unit_id}_{node_id}", flush=True)
                # Log stderr messages (progress info) for debugging
                if proc.stderr:
                    stderr_lines = proc.stderr.strip().split('\n')
                    # Log key progress messages
                    for line in stderr_lines:
                        if 'Context package size' in line or 'LLM response received' in line:
                            print(f"[Migration] {line}", file=sys.stderr)
                
                if not java_code or len(java_code.strip()) < 100:
                    self._send_json(
                        {
                            "error": "LLM returned very short or empty code",
                            "details": "The generated Java code is suspiciously short. Check API response.",
                        },
                        status=500,
                    )
                    return

                # Run validation
                validation_result = None
                try:
                    # Write Java code to temp file for validation
                    import tempfile
                    with tempfile.NamedTemporaryFile(mode='w', suffix='.java', delete=False) as tmp_java:
                        tmp_java.write(java_code)
                        tmp_java_path = tmp_java.name

                    val_proc = subprocess.run(
                        [
                            "python3",
                            str(ROOT_DIR / "validate_java.py"),
                            str(ctx_file),
                            tmp_java_path,
                            "--unit-id", unit_id,
                            "--node-id", node_id,
                        ],
                        cwd=str(ROOT_DIR),
                        check=True,
                        capture_output=True,
                        text=True,
                    )
                    validation_result = json.loads(val_proc.stdout)

                    # Clean up temp file
                    os.unlink(tmp_java_path)
                except subprocess.CalledProcessError as e:
                    # Validation script failed - try to parse error or return details
                    error_msg = e.stderr if e.stderr else str(e)
                    print(f"[Validation] Failed for {unit_id}_{node_id}: {error_msg}", flush=True)
                    validation_result = {"error": f"Validation script failed: {error_msg[:200]}"}
                except json.JSONDecodeError as e:
                    # Validation script returned invalid JSON
                    error_output = val_proc.stdout if 'val_proc' in locals() else "No output"
                    print(f"[Validation] Invalid JSON from validation script: {error_output[:500]}", flush=True)
                    validation_result = {"error": f"Validation script returned invalid JSON: {str(e)[:200]}"}
                except Exception as e:
                    # Validation failed, but migration succeeded
                    print(f"[Validation] Exception during validation for {unit_id}_{node_id}: {e}", flush=True)
                    validation_result = {"error": str(e)[:200]}

                self._send_json(
                    {
                        "unitId": unit_id,
                        "nodeId": node_id,
                        "model": model,
                        "javaCode": java_code,
                        "validation": validation_result,
                    }
                )
            except subprocess.TimeoutExpired:
                print(f"[Migration] TIMEOUT after {ui_timeout}s for {unit_id}_{node_id}", flush=True)
                self._send_json(
                    {
                        "error": "Migration timeout",
                        "details": f"The migration took longer than {ui_timeout} seconds and was stopped.",
                        "suggestion": "Run from terminal for long jobs: python3 migrate_with_claude.py context_index/" + unit_id + "_" + node_id + ".json --stream",
                    },
                    status=504,
                )
                return
            except subprocess.CalledProcessError as e:
                print(f"[Migration] FAILED {unit_id}_{node_id}: returncode={e.returncode}", flush=True)
                if e.stderr:
                    print(e.stderr[:500], flush=True)
                error_msg = e.stderr if e.stderr else "Unknown error"
                # Check if it's an API error
                if "401" in error_msg or "authentication" in error_msg.lower():
                    self._send_json(
                        {
                            "error": "API authentication failed",
                            "details": "Check your ANTHROPIC_API_KEY environment variable.",
                        },
                        status=401,
                    )
                elif "404" in error_msg or "not_found" in error_msg.lower():
                    self._send_json(
                        {
                            "error": "Model not found",
                            "details": "The specified model may not be available. Try 'claude-sonnet-4-5'.",
                        },
                        status=404,
                    )
                else:
                    self._send_json(
                        {
                            "error": "Migration failed",
                            "details": error_msg[:500],  # Limit error message length
                            "returncode": e.returncode,
                        },
                        status=500,
                    )
                return
            except Exception as e:
                self._send_json(
                    {
                        "error": "Unexpected error",
                        "details": str(e)[:500],
                    },
                    status=500,
                )
                return
            return

    def _stream_migration(self, unit_id, node_id, ctx_file, model):
        """Stream migration using Server-Sent Events (SSE)."""
        # Set up SSE headers
        self.send_response(200)
        self.send_header("Content-Type", "text/event-stream")
        self.send_header("Cache-Control", "no-cache")
        self.send_header("Connection", "keep-alive")
        self.send_header("X-Accel-Buffering", "no")  # Disable nginx buffering
        self.end_headers()

        try:
            # Send initial status
            try:
                self._send_sse("status", {"message": "Starting migration...", "unitId": unit_id, "nodeId": node_id})
            except (BrokenPipeError, ConnectionResetError, OSError):
                return  # Client disconnected before migration started

            # Start migration subprocess with streaming
            # Use unbuffered output for real-time streaming
            import sys as sys_module
            proc = subprocess.Popen(
                [
                    "python3",
                    "-u",  # Unbuffered stdout/stderr
                    str(ROOT_DIR / "migrate_with_claude.py"),
                    str(ctx_file),
                    "--model",
                    model,
                    "--max-tokens",
                    "64000",
                    "--stream",
                ],
                cwd=str(ROOT_DIR),
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                bufsize=0,  # Unbuffered
            )

            # Stream stdout (Java code) and stderr (progress messages) line by line
            java_code = ""
            import threading
            
            # Read stdout (Java code) line by line
            def read_output():
                nonlocal java_code
                try:
                    for line in iter(proc.stdout.readline, ''):
                        if line:
                            java_code += line
                            # Send each line as a code chunk
                            try:
                                self._send_sse("code", {"chunk": line})
                            except (BrokenPipeError, ConnectionResetError, OSError):
                                # Client disconnected, stop reading
                                break
                except (BrokenPipeError, ConnectionResetError, OSError):
                    # Client disconnected, stop reading (exception occurred outside loop)
                    return
                except Exception as e:
                    try:
                        self._send_sse("error", {"error": "Error reading output", "details": str(e)})
                    except (BrokenPipeError, ConnectionResetError, OSError):
                        # Client already disconnected, ignore
                        return
            
            # Read stderr (progress messages) line by line
            def read_stderr():
                try:
                    for line in iter(proc.stderr.readline, ''):
                        if line:
                            line = line.strip()
                            # Send progress messages to client
                            try:
                                if "Context package size" in line or "Prompt size" in line or "tokens" in line.lower() or "seconds" in line.lower():
                                    self._send_sse("status", {"message": line})
                            except (BrokenPipeError, ConnectionResetError, OSError):
                                # Client disconnected, stop reading
                                break
                except (BrokenPipeError, ConnectionResetError, OSError):
                    return
                except Exception:
                    pass  # Ignore errors in stderr reading
            
            # Start reading stdout and stderr in separate threads to avoid blocking
            read_thread = threading.Thread(target=read_output, daemon=True)
            stderr_thread = threading.Thread(target=read_stderr, daemon=True)
            read_thread.start()
            stderr_thread.start()

            # Monitor process health and send periodic updates
            import time
            start_time = time.time()
            last_update_time = start_time
            while proc.poll() is None:  # Process still running
                elapsed = time.time() - start_time
                # Send status update every 30 seconds
                if time.time() - last_update_time >= 30:
                    try:
                        self._send_sse("status", {"message": f"Migration in progress... ({int(elapsed)}s elapsed)"})
                        last_update_time = time.time()
                    except (BrokenPipeError, ConnectionResetError, OSError):
                        break  # Client disconnected
                time.sleep(1)  # Check every second
            
            # Wait for reading threads to finish
            read_thread.join(timeout=30)  # Give threads time to finish reading
            stderr_thread.join(timeout=30)
            
            # Wait for process to finish if still running
            if proc.poll() is None:
                proc.wait(timeout=5)

            if proc.returncode != 0:
                # Read remaining stderr if any
                remaining_stderr = ""
                try:
                    if proc.stderr:
                        remaining_stderr = proc.stderr.read()
                except:
                    pass
                error_msg = remaining_stderr[:500] if remaining_stderr else "Migration process failed"
                try:
                    self._send_sse("error", {"error": "Migration failed", "details": error_msg, "returncode": proc.returncode})
                except (BrokenPipeError, ConnectionResetError, OSError):
                    pass  # Client disconnected, ignore
                return
            
            # Check if we got any code
            if not java_code.strip():
                try:
                    self._send_sse("error", {"error": "No code generated", "details": "Migration completed but no Java code was produced. Check stderr for details."})
                except (BrokenPipeError, ConnectionResetError, OSError):
                    pass
                return

            # Send completion status
            try:
                self._send_sse("complete", {
                    "unitId": unit_id,
                    "nodeId": node_id,
                    "model": model,
                    "totalLength": len(java_code)
                })
            except (BrokenPipeError, ConnectionResetError, OSError):
                pass  # Client disconnected, ignore

            # Run validation in background (non-blocking)
            try:
                self._send_sse("status", {"message": "Running validation..."})
            except (BrokenPipeError, ConnectionResetError, OSError):
                pass  # Client disconnected, ignore
            try:
                import tempfile
                with tempfile.NamedTemporaryFile(mode='w', suffix='.java', delete=False) as tmp_java:
                    tmp_java.write(java_code)
                    tmp_java_path = tmp_java.name

                val_proc = subprocess.run(
                    [
                        "python3",
                        str(ROOT_DIR / "validate_java.py"),
                        str(ctx_file),
                        tmp_java_path,
                        "--unit-id", unit_id,
                        "--node-id", node_id,
                    ],
                    cwd=str(ROOT_DIR),
                    check=True,
                    capture_output=True,
                    text=True,
                    timeout=30,
                )
                validation_result = json.loads(val_proc.stdout)
                try:
                    self._send_sse("validation", validation_result)
                except (BrokenPipeError, ConnectionResetError, OSError):
                    pass  # Client disconnected, ignore

                os.unlink(tmp_java_path)
            except Exception as e:
                try:
                    self._send_sse("validation", {"error": str(e)})
                except (BrokenPipeError, ConnectionResetError, OSError):
                    pass  # Client disconnected, ignore

        except subprocess.TimeoutExpired:
            try:
                self._send_sse("error", {"error": "Migration timeout", "details": "Took longer than 10 minutes"})
            except (BrokenPipeError, ConnectionResetError, OSError):
                pass  # Client disconnected, ignore
        except Exception as e:
            try:
                self._send_sse("error", {"error": "Unexpected error", "details": str(e)[:500]})
            except (BrokenPipeError, ConnectionResetError, OSError):
                pass  # Client disconnected, ignore

    def _send_sse(self, event_type, data):
        """Send Server-Sent Event, gracefully handling client disconnections."""
        try:
            message = json.dumps(data, ensure_ascii=False)
            self.wfile.write(f"event: {event_type}\n".encode('utf-8'))
            self.wfile.write(f"data: {message}\n\n".encode('utf-8'))
            self.wfile.flush()
        except (BrokenPipeError, ConnectionResetError, OSError):
            # Client disconnected during streaming - this is normal and harmless
            # Common when user refreshes, navigates away, or request times out
            # Silently ignore - caller can check if needed
            raise  # Re-raise only for loop control (e.g., in read_output)
        except Exception as e:
            # Log unexpected errors but don't crash
            print(f"[Error] Failed to send SSE event: {e}", flush=True)
            # Don't re-raise for unexpected errors - let caller decide


def main():
    # Check for ANTHROPIC_API_KEY, prompt if missing
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        print("ANTHROPIC_API_KEY not set in environment.")
        print("Please enter your Anthropic API key (it will be set for this session only):")
        api_key = getpass("API Key: ").strip()
        if not api_key:
            print("Error: API key is required. Exiting.")
            sys.exit(1)
        if not api_key.startswith("sk-ant-"):
            print("Warning: API key doesn't start with 'sk-ant-'. This might be invalid.")
        os.environ["ANTHROPIC_API_KEY"] = api_key
        print("✓ API key set for this session.\n")
    else:
        print(f"✓ Using ANTHROPIC_API_KEY from environment (starts with {api_key[:10]}...)\n")
    
    port = int(os.environ.get("UI_PORT", "8001"))
    server = HTTPServer(("0.0.0.0", port), MigrationHandler)
    print(f"UI server running on http://localhost:{port}")
    print("Endpoints:")
    print("  GET  /              - Web UI")
    print("  GET  /api/ping      - Health check")
    print("  GET  /api/manifest  - List indexed nodes")
    print("  GET  /api/rpg-snippet?unitId=&nodeId= - RPG source snippet (traceability)")
    print("  POST /api/compile   - Compile Java code (body=source), returns compile result")
    print("  POST /api/migrate   - Run migration (returns complete code + validation)")
    print("")
    print("Note: For real-time streaming, use CLI:")
    print("  python3 migrate_with_claude.py context_index/<unit>_<node>.json --stream")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped (Ctrl+C).")
        raise SystemExit(0)


if __name__ == "__main__":
    main()

