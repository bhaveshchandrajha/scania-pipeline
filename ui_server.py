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

ROOT_DIR = Path(__file__).resolve().parent
CONTEXT_DIR = ROOT_DIR / "context_index"
MANIFEST_PATH = CONTEXT_DIR / "manifest.json"

# Module-level: running Spring Boot process (for /api/run-application)
_app_process = None


def find_rpg_files(directory: Path):
    """
    Find RPG source files in a directory (case-insensitive).
    Returns a list of Path objects for .rpgle and .sqlrpgle files.
    """
    rpg_files = []
    if not directory.exists() or not directory.is_dir():
        return rpg_files
    
    # Search for both lowercase and uppercase extensions
    for pattern in ["*.rpgle", "*.RPGLE", "*.sqlrpgle", "*.SQLRPGLE"]:
        rpg_files.extend(directory.rglob(pattern))
    
    # Remove duplicates (in case filesystem is case-insensitive)
    seen = set()
    unique_files = []
    for f in rpg_files:
        if f not in seen:
            seen.add(f)
            unique_files.append(f)
    
    return unique_files


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
            # Redirect to Pure Java Pipeline UI (Track B) - the main workflow
            self.send_response(302)
            self.send_header("Location", "/pipeline")
            self.end_headers()
            return
        
        if parsed.path == "/track-a" or parsed.path == "/rpg-native":
            # Serve the Track A (RPG-native) UI file
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
        
        if parsed.path == "/ui_pure_java_pipeline.html" or parsed.path == "/pipeline":
            # Serve the Pure Java Pipeline UI (Track B) - DEFAULT
            ui_path = ROOT_DIR / "ui_pure_java_pipeline.html"
            if not ui_path.exists():
                self.send_error(404, "ui_pure_java_pipeline.html not found")
                return
            content = ui_path.read_bytes()
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
            return
        
        if parsed.path == "/ui_global_context.html" or parsed.path == "/global-context":
            # Serve the Global Context / Knowledge Graph builder UI
            ui_path = ROOT_DIR / "ui_global_context.html"
            if not ui_path.exists():
                self.send_error(404, "ui_global_context.html not found")
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

        # Proxy /api/demo/* to Spring Boot backend (must be before any fallback to file serving)
        if parsed.path.startswith("/api/demo") or parsed.path.startswith("/demo/"):
            from urllib.request import urlopen, Request
            from urllib.error import URLError, HTTPError
            demo_backend_port = int(os.environ.get("DEMO_BACKEND_PORT", "8081"))
            backend_path = parsed.path if parsed.path.startswith("/api/demo") else "/api/demo" + parsed.path
            backend_url = f"http://localhost:{demo_backend_port}{backend_path}"
            if parsed.query:
                backend_url += "?" + parsed.query
            try:
                req = Request(backend_url, method="GET")
                with urlopen(req, timeout=30) as resp:
                    body = resp.read()
                    self.send_response(resp.getcode())
                    self.send_header("Content-Type", resp.headers.get("Content-Type", "application/json"))
                    self.send_header("Content-Length", str(len(body)))
                    self.end_headers()
                    self.wfile.write(body)
            except HTTPError as e:
                body = e.read() if hasattr(e, "read") else b""
                self.send_response(e.code)
                self.send_header("Content-Type", "application/json")
                self.send_header("Content-Length", str(len(body)))
                self.end_headers()
                self.wfile.write(body)
            except URLError as e:
                err = str(e.reason) if getattr(e, "reason", None) else str(e)
                self._send_json({
                    "error": "Demo backend not reachable",
                    "detail": err,
                    "hint": f"Start the Spring Boot app that includes MigrationDemoController (e.g. on port {demo_backend_port}). Set DEMO_BACKEND_PORT if your app uses another port.",
                }, status=503)
            except Exception as e:
                self._send_json({"error": str(e)}, status=500)
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

        if parsed.path == "/api/pure-java-source":
            # Traceability endpoint: show generated Java for a migrated node (cause → effect)
            params = parse_qs(parsed.query)
            unit_id = params.get("unitId", [None])[0]
            node_id = params.get("nodeId", [None])[0]
            rel_path = params.get("path", [None])[0]

            if not unit_id or not node_id:
                self._send_json({"error": "unitId and nodeId are required"}, status=400)
                return

            output_path = ROOT_DIR / f"{unit_id}_{node_id}_pure_java"
            if not output_path.exists() or not output_path.is_dir():
                self._send_json(
                    {
                        "error": f"Pure Java output directory not found for {unit_id}_{node_id}. "
                                 "Run /api/migrate-pure-java first.",
                        "outputDir": str(output_path),
                    },
                    status=404,
                )
                return

            target_file = None
            if rel_path:
                candidate = (output_path / rel_path).resolve()
                try:
                    candidate.relative_to(output_path)
                except ValueError:
                    self._send_json({"error": "Requested path is outside of output directory"}, status=400)
                    return
                if candidate.is_file():
                    target_file = candidate
            else:
                # Heuristic: prefer service classes, then controllers, then any Java file
                java_files = sorted(output_path.rglob("*.java"))
                if not java_files:
                    self._send_json(
                        {
                            "error": "No Java files found in output directory",
                            "outputDir": str(output_path),
                        },
                        status=404,
                    )
                    return

                def score(p: Path) -> int:
                    s = 0
                    parts = {part.lower() for part in p.parts}
                    name = p.name.lower()
                    if "service" in parts or "service" in name:
                        s += 3
                    if "web" in parts or "controller" in name:
                        s += 2
                    if "domain" in parts or "repository" in parts:
                        s += 1
                    return s

                target_file = max(java_files, key=score)

            try:
                code = target_file.read_text(encoding="utf-8")
            except Exception as e:
                self._send_json({"error": f"Failed to read Java source: {e}"}, status=500)
                return

            self._send_json(
                {
                    "unitId": unit_id,
                    "nodeId": node_id,
                    "outputDir": str(output_path),
                    "path": str(target_file.relative_to(output_path)),
                    "javaSource": code,
                }
            )
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

        if parsed.path == "/api/test-api":
            # Test API endpoints on localhost:8081
            from urllib.request import urlopen, Request
            from urllib.error import URLError, HTTPError
            import urllib.parse
            
            params = parse_qs(parsed.query)
            endpoint = params.get("endpoint", ["/api/claims/search"])[0]
            method = params.get("method", ["GET"])[0]
            body_data = params.get("body", [None])[0]
            
            try:
                url = f"http://localhost:8081{endpoint}"
                req = Request(url, method=method)
                
                if body_data and method in ["POST", "PUT", "PATCH"]:
                    req.add_header("Content-Type", "application/json")
                    req.data = body_data.encode("utf-8")
                
                try:
                    with urlopen(req, timeout=5) as response:
                        response_data = response.read().decode("utf-8")
                        status_code = response.getcode()
                        
                        # Try to parse as JSON
                        try:
                            json_data = json.loads(response_data)
                            self._send_json({
                                "success": True,
                                "status": status_code,
                                "data": json_data,
                                "endpoint": endpoint,
                                "method": method,
                            })
                        except json.JSONDecodeError:
                            self._send_json({
                                "success": True,
                                "status": status_code,
                                "data": response_data,
                                "endpoint": endpoint,
                                "method": method,
                                "note": "Response is not JSON",
                            })
                except HTTPError as e:
                    # HTTP error (4xx, 5xx) - still got a response
                    error_body = e.read().decode("utf-8") if hasattr(e, 'read') else str(e)
                    try:
                        error_json = json.loads(error_body)
                        self._send_json({
                            "success": False,
                            "status": e.code,
                            "error": error_json,
                            "endpoint": endpoint,
                            "method": method,
                        }, status=e.code)
                    except:
                        self._send_json({
                            "success": False,
                            "status": e.code,
                            "error": error_body[:500],
                            "endpoint": endpoint,
                            "method": method,
                        }, status=e.code)
                except URLError as e:
                    # Connection error (server not running, network issue, etc.)
                    error_msg = str(e.reason) if hasattr(e, 'reason') else str(e)
                    if "Connection refused" in error_msg or "Errno 61" in str(e):
                        self._send_json({
                            "success": False,
                            "status": 0,
                            "error": "Connection refused - Spring Boot application is not running on localhost:8081",
                            "endpoint": endpoint,
                            "method": method,
                            "suggestion": "Start the application first: cd HS1210_n404_pure_java && mvn spring-boot:run",
                            "hint": "Make sure you've built the project (step 4) and started the Spring Boot server",
                        }, status=503)
                    else:
                        self._send_json({
                            "success": False,
                            "status": 0,
                            "error": f"Network error: {error_msg}",
                            "endpoint": endpoint,
                            "method": method,
                        }, status=503)
            except Exception as e:
                import traceback
                self._send_json({
                    "success": False,
                    "status": 0,
                    "error": f"Unexpected error: {str(e)}",
                    "endpoint": endpoint,
                    "method": method,
                    "traceback": traceback.format_exc(),
                }, status=500)
            return
        
        if parsed.path == "/api/test-api-old":
            # Test API endpoints on localhost:8081
            params = parse_qs(parsed.query)
            endpoint = params.get("endpoint", ["/api/claims"])[0]
            method = params.get("method", ["GET"])[0]
            
            import urllib.request
            import urllib.error
            
            try:
                url = f"http://localhost:8081{endpoint}"
                req = urllib.request.Request(url)
                req.get_method = lambda: method
                
                if method == "POST" and params.get("body"):
                    req.add_header("Content-Type", "application/json")
                    body = params.get("body", [""])[0].encode("utf-8")
                    response = urllib.request.urlopen(req, data=body, timeout=5)
                else:
                    response = urllib.request.urlopen(req, timeout=5)
                
                result = {
                    "status": response.getcode(),
                    "headers": dict(response.headers),
                    "body": response.read().decode("utf-8", errors="replace")
                }
                self._send_json(result)
            except urllib.error.HTTPError as e:
                self._send_json({
                    "status": e.code,
                    "error": str(e),
                    "body": e.read().decode("utf-8", errors="replace") if e.fp else ""
                }, status=e.code)
            except Exception as e:
                self._send_json({"error": str(e), "status": 0}, status=500)
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
        
        if parsed.path == "/api/discover-directories":
            # Discover available AST and RPG directories
            ast_dirs = []
            rpg_dirs = []
            seen_ast_paths = set()

            def add_ast_dir(item_path: Path, label: str | None = None, allow_empty: bool = False):
                try:
                    rel = str(item_path.relative_to(ROOT_DIR))
                except ValueError:
                    return
                if rel in seen_ast_paths:
                    return
                ast_files = list(item_path.glob("*-ast.json"))
                if ast_files or allow_empty:
                    seen_ast_paths.add(rel)
                    ast_dirs.append({
                        "path": rel,
                        "fileCount": len(ast_files),
                        "files": [f.name for f in ast_files[:5]],
                        "label": label,
                    })
            try:
                # 1) AST directories under JSON_ast (RPG program ASTs); include even if empty so user can add new ASTs
                ast_base = ROOT_DIR / "JSON_ast"
                if ast_base.exists():
                    for item in sorted(ast_base.iterdir(), key=lambda p: p.name):
                        if item.is_dir():
                            add_ast_dir(item, allow_empty=True)

                # 2) Root-level directories that contain *-ast.json (e.g. HS1210D_20260216 DDS AST)
                for item in sorted(ROOT_DIR.iterdir(), key=lambda p: p.name):
                    if item.is_dir() and not item.name.startswith("."):
                        ast_files = list(item.glob("*-ast.json"))
                        label = "DDS" if "D_" in item.name or any(f.name.endswith("D-ast.json") for f in ast_files) else None
                        add_ast_dir(item, label=label)
            except OSError:
                pass  # e.g. permission denied on iterdir

            # 3) Explicit fallback: ensure known DDS AST folder appears if it exists (avoids iterdir/order issues)
            dds_fallback_name = "HS1210D_20260216"
            dds_fallback_path = ROOT_DIR / dds_fallback_name
            if dds_fallback_name not in seen_ast_paths and dds_fallback_path.is_dir():
                if list(dds_fallback_path.glob("*-ast.json")):
                    add_ast_dir(dds_fallback_path, label="DDS")

            # Prefer JSON_ast subdirs first, then others (by path)
            ast_dirs.sort(key=lambda d: (0 if d["path"].startswith("JSON_ast/") else 1, d["path"]))

            # Build dedicated ddsAstDirs list so UI can show DDS options even if astDirs was filtered
            dds_ast_dirs = [d for d in ast_dirs if d.get("label") == "DDS"]
            if not dds_ast_dirs:
                # Explicit check: add known DDS folder by path so it always appears when present
                for candidate in ["HS1210D_20260216"]:
                    p = ROOT_DIR / candidate
                    if p.is_dir() and list(p.glob("*-ast.json")):
                        dds_ast_dirs.append({
                            "path": candidate,
                            "fileCount": len(list(p.glob("*-ast.json"))),
                            "files": [f.name for f in p.glob("*-ast.json")][:5],
                            "label": "DDS",
                        })
                        break

            # Find RPG directories
            for item in ROOT_DIR.iterdir():
                if item.is_dir() and (item.name.startswith("PoC_") or item.name.startswith("HS12")):
                    # Check if it looks like an RPG source directory (has .rpgle or .sqlrpgle files)
                    rpg_files = find_rpg_files(item)
                    if rpg_files or item.name.startswith("PoC_"):
                        # Use relative path from project root
                        rel_path = str(item.relative_to(ROOT_DIR))
                        rpg_dirs.append(rel_path)
            
            # Also check nested directories (e.g., PoC_HS1210/PoC_HS1210/)
            for item in ROOT_DIR.iterdir():
                if item.is_dir():
                    for subdir in item.iterdir():
                        if subdir.is_dir() and (subdir.name.startswith("PoC_") or subdir.name.startswith("HS12")):
                            rpg_files = find_rpg_files(subdir)
                            if rpg_files or subdir.name.startswith("PoC_"):
                                rel_path = str(subdir.relative_to(ROOT_DIR))
                                if rel_path not in rpg_dirs:
                                    rpg_dirs.append(rel_path)
            
            # Zip files that might need extraction
            zip_files = []
            for item in ROOT_DIR.iterdir():
                if item.is_file() and item.suffix == ".zip" and item.name.startswith("PoC_"):
                    zip_files.append({
                        "path": str(item.name),
                        "extracted": False,
                        "suggestion": f"Extract {item.name} to create RPG directory"
                    })

            result = {
                "astDirs": ast_dirs,
                "ddsAstDirs": dds_ast_dirs,
                "rpgDirs": rpg_dirs,
                "defaultAstDir": ast_dirs[0]["path"] if ast_dirs else "JSON_ast/JSON_20260211",
                "defaultRpgDir": rpg_dirs[0] if rpg_dirs else None
            }
            if zip_files:
                result["zipFiles"] = zip_files
                if not rpg_dirs:
                    result["suggestion"] = f"Found {zip_files[0]['path']} - extract it to create RPG directory"

            self._send_json(result)
            return
        
        if parsed.path == "/api/build-context":
            # Build context packages from ASTs
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length > 0:
                body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                ast_dir = body.get("astDir", "JSON_ast/JSON_20260211")
                rpg_dir = body.get("rpgDir", "PoC_HS1210")
                dds_ast_dir_param = body.get("ddsAstDir") or ""
            else:
                params = parse_qs(parsed.query)
                ast_dir = params.get("astDir", ["JSON_ast/JSON_20260211"])[0]
                rpg_dir = params.get("rpgDir", ["PoC_HS1210"])[0]
                dds_ast_dir_param = (params.get("ddsAstDir") or [""])[0]

            # Validate directories exist
            # Handle both relative and absolute paths
            if Path(ast_dir).is_absolute():
                ast_path = Path(ast_dir)
            else:
                ast_path = ROOT_DIR / ast_dir
            dds_ast_path = None
            if dds_ast_dir_param and str(dds_ast_dir_param).strip():
                if Path(dds_ast_dir_param).is_absolute():
                    dds_ast_path = Path(dds_ast_dir_param)
                else:
                    dds_ast_path = ROOT_DIR / dds_ast_dir_param
                if not dds_ast_path.is_dir():
                    dds_ast_path = None  # ignore invalid optional dir
            
            # Handle RPG directory - try multiple path resolutions
            rpg_path = None
            rpg_dir_clean = rpg_dir.strip()
            
            # Normalize path - add leading slash if it looks like an absolute path but missing slash
            original_path = rpg_dir_clean
            if rpg_dir_clean.startswith('Users/') or rpg_dir_clean.startswith('home/'):
                # Looks like absolute path missing leading slash
                rpg_dir_clean = '/' + rpg_dir_clean
                print(f"[Build Context] Normalized path: '{original_path}' -> '{rpg_dir_clean}'", flush=True)
            
            # Try 1: Absolute path (can be outside project root)
            if Path(rpg_dir_clean).is_absolute():
                rpg_path = Path(rpg_dir_clean)
                if rpg_path.exists() and rpg_path.is_dir():
                    print(f"[Build Context] ✓ Found RPG directory at absolute path: {rpg_path}", flush=True)
                else:
                    print(f"[Build Context] ✗ Path does not exist: {rpg_path}", flush=True)
                    rpg_path = None
            
            # Try 2: Relative to project root
            if not rpg_path:
                test_path = ROOT_DIR / rpg_dir_clean
                if test_path.exists():
                    rpg_path = test_path
            
            # Try 3: Check if it's a nested path (e.g., PoC_HS1210/PoC_HS1210)
            if not rpg_path:
                # Check if parent directory exists with this as subdirectory
                parts = rpg_dir_clean.split('/')
                if len(parts) > 1:
                    parent = ROOT_DIR / parts[0]
                    if parent.exists():
                        test_path = parent / parts[1]
                        if test_path.exists():
                            rpg_path = test_path
            
            # Try 4: Search recursively for directory name within project root
            if not rpg_path:
                dir_name = parts[-1] if '/' in rpg_dir_clean else rpg_dir_clean
                for item in ROOT_DIR.rglob(dir_name):
                    if item.is_dir() and item.name == dir_name:
                        rpg_path = item
                        print(f"[Build Context] Found RPG directory via search: {rpg_path}", flush=True)
                        break
            
            # Try 5: Search in sibling directories (e.g., ScaniaRPG2JavaAgentic)
            if not rpg_path:
                # Extract directory name from path
                if '/' in rpg_dir_clean:
                    parts = rpg_dir_clean.split('/')
                    dir_name = parts[-1]
                else:
                    dir_name = rpg_dir_clean
                # Check sibling directories of the project root
                project_parent = ROOT_DIR.parent
                if project_parent.exists():
                    for sibling_dir in project_parent.iterdir():
                        if sibling_dir.is_dir() and sibling_dir != ROOT_DIR:
                            # Search recursively in sibling directories
                            try:
                                for item in sibling_dir.rglob(dir_name):
                                    if item.is_dir() and item.name == dir_name:
                                        rpg_path = item
                                        print(f"[Build Context] Found RPG directory in sibling directory: {rpg_path}", flush=True)
                                        break
                                if rpg_path:
                                    break
                            except (PermissionError, OSError) as e:
                                # Skip directories we can't access
                                print(f"[Build Context] Skipping inaccessible sibling directory: {sibling_dir} ({e})", flush=True)
                                continue
            
            if not ast_path.exists():
                self._send_json({
                    "error": f"AST directory not found: {ast_dir}",
                    "suggestion": "Use /api/discover-directories to find available directories"
                }, status=404)
                return
            
            # Check if RPG directory exists, or if there's a zip file to extract
            if not rpg_path or not rpg_path.exists():
                # Determine where to look for zip file
                zip_path = None
                if rpg_path:
                    # Try zip in same location as expected directory
                    zip_path = rpg_path.with_suffix('.zip')
                    if not zip_path.exists() and rpg_path.parent.exists():
                        zip_path = rpg_path.parent / f"{rpg_path.name}.zip"
                else:
                    # Try to find zip file based on directory name
                    dir_name = rpg_dir_clean.split('/')[-1]
                    zip_path = ROOT_DIR / f"{dir_name}.zip"
                    if not zip_path.exists():
                        # Check nested locations
                        for item in ROOT_DIR.rglob(f"{dir_name}.zip"):
                            zip_path = item
                            break
                
                if zip_path and zip_path.exists():
                    # Try to extract the zip
                    try:
                        import zipfile
                        extract_to = ROOT_DIR / rpg_dir_clean.split('/')[0] if '/' in rpg_dir_clean else ROOT_DIR / rpg_dir_clean
                        print(f"[Build Context] Extracting {zip_path} to {extract_to}...", flush=True)
                        extract_to.mkdir(parents=True, exist_ok=True)
                        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                            zip_ref.extractall(extract_to)
                        print(f"[Build Context] ✓ Extracted {zip_path.name}", flush=True)
                        # Update rpg_path to point to extracted directory
                        # Check what was extracted
                        extracted_dirs = [d for d in extract_to.iterdir() if d.is_dir()]
                        if extracted_dirs:
                            # Use the first extracted directory that matches or contains RPG files
                            for d in extracted_dirs:
                                rpg_files_check = find_rpg_files(d)
                                if rpg_files_check:
                                    rpg_path = d
                                    break
                            if not rpg_path:
                                rpg_path = extracted_dirs[0]
                    except Exception as e:
                        import traceback
                        error_trace = traceback.format_exc()
                        print(f"[Build Context] Extraction error: {error_trace}", flush=True)
                        self._send_json({
                            "error": f"RPG directory not found: {rpg_dir}",
                            "zipFound": True,
                            "zipPath": str(zip_path),
                            "extractionError": str(e),
                            "suggestion": f"Found zip file {zip_path.name} but extraction failed: {str(e)}. Extract it manually."
                        }, status=404)
                        return
                else:
                    # Provide helpful debugging info
                    searched_paths = [
                        str(ROOT_DIR / rpg_dir_clean),
                        str(Path(rpg_dir_clean)) if Path(rpg_dir_clean).is_absolute() else None
                    ]
                    searched_paths = [p for p in searched_paths if p]
                    
                    # Provide helpful error with suggestions
                    error_msg = f"RPG directory not found: {rpg_dir}"
                    suggestion = f"Directory '{rpg_dir}' not found."
                    
                    # Check if a similar directory exists elsewhere
                    dir_name = rpg_dir_clean.split('/')[-1]
                    similar_dirs = []
                    if dir_name:
                        # Search in common locations and sibling directories
                        search_locations = [
                            Path.home() / "Documents",
                            Path("/Users") / Path.home().name / "Documents" if Path.home().name else None
                        ]
                        # Also search in sibling directories of project root
                        project_parent = ROOT_DIR.parent
                        if project_parent.exists():
                            for sibling_dir in project_parent.iterdir():
                                if sibling_dir.is_dir() and sibling_dir != ROOT_DIR:
                                    search_locations.append(sibling_dir)
                        
                        for loc in search_locations:
                            if loc and loc.exists():
                                try:
                                    for item in loc.rglob(dir_name):
                                        if item.is_dir() and item.name == dir_name:
                                            similar_dirs.append(str(item))
                                            if len(similar_dirs) >= 5:  # Increased limit
                                                break
                                    if len(similar_dirs) >= 5:
                                        break
                                except (PermissionError, OSError) as e:
                                    # Skip directories we can't access
                                    print(f"[Build Context] Skipping inaccessible location: {loc} ({e})", flush=True)
                                    continue
                    
                    if similar_dirs:
                        suggestion += f" Found similar directories: {', '.join(similar_dirs[:3])}"
                    
                    suggestion += f" Use Browse button to select the directory, or type the full absolute path (e.g., /Users/fkhan/Documents/ScaniaRPG2JavaAgentic/PoC_HS1210)"
                    
                    self._send_json({
                        "error": error_msg,
                        "searchedPaths": searched_paths,
                        "projectRoot": str(ROOT_DIR),
                        "similarDirs": similar_dirs[:3],
                        "suggestion": suggestion
                    }, status=404)
                    return
            
            # Verify RPG directory has source files
            rpg_files = find_rpg_files(rpg_path)
            if not rpg_files:
                self._send_json({
                    "error": f"RPG directory found but contains no .rpgle or .sqlrpgle files: {rpg_dir}",
                    "suggestion": f"Ensure the directory '{rpg_dir}' contains RPG source files (case-insensitive: .rpgle, .RPGLE, .sqlrpgle, .SQLRPGLE)."
                }, status=404)
                return
            
            # Check for AST files
            ast_files = list(ast_path.glob("*-ast.json"))
            if not ast_files:
                self._send_json({
                    "error": f"No AST files (*-ast.json) found in: {ast_dir}",
                    "suggestion": "Ensure AST JSON files are in the specified directory"
                }, status=404)
                return
            
            try:
                # Find Java executable
                java_home = os.environ.get("JAVA_HOME")
                if not java_home:
                    # Try to find Java 17+
                    import shutil
                    java_path = shutil.which("java")
                    if java_path:
                        java_home = str(Path(java_path).parent.parent)
                
                if not java_home or not Path(java_home).exists():
                    self._send_json({"error": "JAVA_HOME not set or invalid"}, status=400)
                    return
                
                java_exe = Path(java_home) / "bin" / "java"
                jar_file = ROOT_DIR / "target" / "pks-ast-migration-pipeline-0.1.0-SNAPSHOT.jar"
                
                if not jar_file.exists():
                    self._send_json({"error": f"JAR not found: {jar_file}. Run 'mvn clean package' first."}, status=404)
                    return
                
                cmd = [
                    str(java_exe),
                    "-cp", str(jar_file),
                    "com.pks.migration.IndexAll",
                    "--astDir", str(ast_path),
                    "--rpgDir", str(rpg_path),
                    "--outputDir", str(ROOT_DIR)
                ]
                
                proc = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=300,
                    cwd=str(ROOT_DIR)
                )
                
                if proc.returncode == 0:
                    # Optionally run enricher to add display files + DDS AST uiContracts
                    enricher_output = ""
                    try:
                        enricher_cmd = [
                            sys.executable,
                            str(ROOT_DIR / "enrich_context_with_display_files.py"),
                            "--astDir", str(ast_path),
                            "--contextDir", str(CONTEXT_DIR),
                            "--attachUnitDspf",
                        ]
                        if dds_ast_path and dds_ast_path.is_dir():
                            enricher_cmd.extend(["--ddsAstDir", str(dds_ast_path)])
                            # Same folder usually contains raw DDS (e.g. HS1210D.DSPF) → attach ddsSource too
                            enricher_cmd.extend(["--ddsDir", str(dds_ast_path)])
                        elif rpg_path and rpg_path.is_dir():
                            enricher_cmd.extend(["--ddsDir", str(rpg_path)])
                        enricher_proc = subprocess.run(
                            enricher_cmd,
                            capture_output=True,
                            text=True,
                            timeout=120,
                            cwd=str(ROOT_DIR),
                        )
                        enricher_output = (enricher_proc.stdout or "") + (enricher_proc.stderr or "")
                        if enricher_proc.returncode != 0:
                            enricher_output = f"(enricher warning: {enricher_proc.returncode})\n{enricher_output}"
                    except Exception as e:
                        enricher_output = f"(enricher skipped: {e})"

                    self._send_json({
                        "success": True,
                        "message": "Context packages built successfully" + (" (enricher run with DDS AST)" if dds_ast_path else ""),
                        "output": proc.stdout + ("\n" + enricher_output if enricher_output else ""),
                        "manifest": str(MANIFEST_PATH),
                    })
                else:
                    self._send_json({
                        "success": False,
                        "error": proc.stderr or "Unknown error",
                        "output": proc.stdout
                    }, status=500)
            except subprocess.TimeoutExpired:
                self._send_json({"error": "Context building timed out"}, status=500)
            except Exception as e:
                self._send_json({"error": str(e)}, status=500)
            return
        
        if parsed.path == "/api/build-global-context":
            # Build persistent global context (DB registry, program context, call graph) from ASTs and RPG dir
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length > 0:
                body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                ast_dir = body.get("astDir", "JSON_ast/JSON_20260211")
                rpg_dir = body.get("rpgDir", "")
            else:
                params = parse_qs(parsed.query)
                ast_dir = params.get("astDir", ["JSON_ast/JSON_20260211"])[0]
                rpg_dir = params.get("rpgDir", [""])[0]

            # Resolve RPG directory similarly to /api/build-context
            rpg_path = None
            rpg_dir_clean = (rpg_dir or "").strip()
            original_path = rpg_dir_clean
            if rpg_dir_clean.startswith('Users/') or rpg_dir_clean.startswith('home/'):
                rpg_dir_clean = '/' + rpg_dir_clean
                print(f"[Global Context] Normalized path: '{original_path}' -> '{rpg_dir_clean}'", flush=True)

            if Path(rpg_dir_clean).is_absolute():
                rpg_path = Path(rpg_dir_clean)
                if not (rpg_path.exists() and rpg_path.is_dir()):
                    print(f"[Global Context] ✗ Path does not exist: {rpg_path}", flush=True)
                    rpg_path = None
            if not rpg_path and rpg_dir_clean:
                test_path = ROOT_DIR / rpg_dir_clean
                if test_path.exists():
                    rpg_path = test_path

            global_ctx_output = ""
            try:
                # 1) DB registry
                db_proc = subprocess.run(
                    [sys.executable, str(ROOT_DIR / "global_context" / "build_db_registry.py")],
                    capture_output=True,
                    text=True,
                    timeout=120,
                    cwd=str(ROOT_DIR),
                )
                global_ctx_output += (db_proc.stdout or "") + (db_proc.stderr or "")
                # 2) Program-level context
                prog_proc = subprocess.run(
                    [sys.executable, str(ROOT_DIR / "global_context" / "build_program_context.py")],
                    capture_output=True,
                    text=True,
                    timeout=120,
                    cwd=str(ROOT_DIR),
                )
                global_ctx_output += (prog_proc.stdout or "") + (prog_proc.stderr or "")
                # 3) AST-based call graph
                call_proc = subprocess.run(
                    [sys.executable, str(ROOT_DIR / "global_context" / "build_call_graph.py")],
                    capture_output=True,
                    text=True,
                    timeout=120,
                    cwd=str(ROOT_DIR),
                )
                global_ctx_output += (call_proc.stdout or "") + (call_proc.stderr or "")
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
                    global_ctx_output += (call_rpg_proc.stdout or "") + (call_rpg_proc.stderr or "")
                else:
                    global_ctx_output += "\n(Global context: RPG directory not found or not provided; enriched call graph skipped.)"
            except Exception as e:
                global_ctx_output += f"\n(Global context build failed: {e})"

            self._send_json({
                "success": True,
                "message": "Global context built (DB registry, program context, call graph).",
                "output": global_ctx_output,
            })
            return
        
        if parsed.path == "/api/migrate-pure-java":
            # Run Pure Java migration (Track B)
            params = parse_qs(parsed.query)
            unit_id = params.get("unitId", [None])[0]
            node_id = params.get("nodeId", [None])[0]
            model = params.get("model", ["claude-sonnet-4-5"])[0]
            output_dir = params.get("outputDir", [None])[0] or str(ROOT_DIR)
            enforce_logic = (params.get("enforceLogic", ["0"])[0] or params.get("autoFixLogic", ["0"])[0] or "0").strip().lower() in ("1", "true", "yes")
            
            if not unit_id or not node_id:
                self._send_json({"error": "unitId and nodeId are required"}, status=400)
                return
            
            ctx_file = CONTEXT_DIR / f"{unit_id}_{node_id}.json"
            if not ctx_file.exists():
                self._send_json({"error": f"Context file not found: {ctx_file.name}"}, status=404)
                return
            
            api_key = os.environ.get("ANTHROPIC_API_KEY")
            if not api_key:
                self._send_json({"error": "ANTHROPIC_API_KEY env var is not set"}, status=400)
                return
            
            try:
                migrate_script = ROOT_DIR / "migrate_to_pure_java.py"
                cmd = [
                    sys.executable,
                    str(migrate_script),
                    str(ctx_file),
                    "--output-dir", output_dir,
                    "--model", model
                ]
                
                print(f"[Pure Java Migration] Starting {unit_id}_{node_id}...", flush=True)
                proc = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=1200,
                    cwd=str(ROOT_DIR),
                    env=os.environ.copy()
                )
                
                # Parse output directory to find generated files
                output_path = Path(output_dir) / f"{unit_id}_{node_id}_pure_java"
                files = []
                file_structure = {}
                
                if output_path.exists():
                    # Collect all files with their directory structure
                    for java_file in sorted(output_path.rglob("*.java")):
                        rel_path = java_file.relative_to(output_path)
                        path_str = str(rel_path)
                        try:
                            content = java_file.read_text(encoding="utf-8")
                            package = self._extract_package(content)
                        except:
                            package = None
                        
                        files.append({
                            "path": path_str,
                            "size": java_file.stat().st_size,
                            "package": package
                        })
                    
                    # Also check for other important files (application.properties, pom.xml, etc.)
                    for config_file in output_path.rglob("*"):
                        if config_file.is_file() and config_file.suffix in [".properties", ".xml", ".yml", ".yaml"]:
                            rel_path = config_file.relative_to(output_path)
                            files.append({
                                "path": str(rel_path),
                                "size": config_file.stat().st_size,
                                "package": None
                            })
                
                # Build directory tree structure
                dirs = set()
                for file_info in files:
                    path_parts = file_info["path"].split("/")
                    if len(path_parts) > 1:
                        # Add all parent directories
                        for i in range(1, len(path_parts)):
                            dirs.add("/".join(path_parts[:i]))
                
                is_maven_project = (output_path / "pom.xml").is_file() if output_path.exists() else False

                result = {
                    "success": proc.returncode == 0,
                    "output": proc.stdout,
                    "stderr": proc.stderr,
                    "error": proc.stderr if proc.returncode != 0 else None,
                    "outputDir": str(output_path),
                    "files": files,
                    "fileCount": len(files),
                    "directories": sorted(list(dirs)),
                    "exists": output_path.exists(),
                    "isMavenProject": is_maven_project,
                }
                
                # Enforce logic completeness after generation: run validator with context
                if proc.returncode == 0 and output_path.exists() and ctx_file.exists():
                    try:
                        validate_proc = subprocess.run(
                            [sys.executable, str(ROOT_DIR / "validate_pure_java.py"), str(output_path), str(ctx_file), "--json"],
                            capture_output=True,
                            text=True,
                            timeout=120,
                            cwd=str(ROOT_DIR),
                        )
                        stdout_text = (validate_proc.stdout or "").strip()
                        if stdout_text:
                            import re
                            json_match = re.search(r"\{[\s\S]*\}", stdout_text)
                            if json_match:
                                val = json.loads(json_match.group(0))
                                lc = val.get("results", {}).get("logic_completeness", {})
                                if lc and not lc.get("skipped"):
                                    score = lc.get("score", 0)
                                    result["logic_completeness_enforced"] = True
                                    result["logic_completeness_score"] = score
                                    if score < 100:
                                        result["logic_completeness_failed"] = True
                                        result["logic_completeness_gaps"] = lc.get("potential_gaps", [])
                                        result["logic_completeness_gap_details"] = lc.get("gap_details", [])
                                        result["logic_completeness_action_required"] = lc.get("action_required", "")
                                        print(f"[Pure Java Migration] ⚠ Logic completeness {score}% (required 100%)", flush=True)
                                    else:
                                        result["logic_completeness_failed"] = False
                                        print(f"[Pure Java Migration] ✓ Logic completeness 100%", flush=True)
                    except Exception as e:
                        print(f"[Pure Java Migration] Logic completeness check skipped: {e}", flush=True)
                
                # Enforce 100% logic: if requested and score < 100%, run fix-logic-gaps then re-validate (once)
                if (proc.returncode == 0 and result.get("logic_completeness_failed") and enforce_logic
                        and output_path.exists() and ctx_file.exists() and api_key):
                    max_fix_attempts = 2
                    for fix_attempt in range(max_fix_attempts):
                        try:
                            print(f"[Pure Java Migration] Enforce logic: running fix-logic-gaps (attempt {fix_attempt + 1})...", flush=True)
                            fix_proc = subprocess.run(
                                [sys.executable, str(ROOT_DIR / "fix_logic_gaps.py"), str(output_path), str(ctx_file)],
                                capture_output=True,
                                text=True,
                                timeout=600,
                                cwd=str(ROOT_DIR),
                                env=os.environ.copy(),
                            )
                            if fix_proc.returncode != 0:
                                result["logic_enforce_fix_stderr"] = fix_proc.stderr or ""
                                result["logic_enforce_fix_stdout"] = fix_proc.stdout or ""
                                print(f"[Pure Java Migration] Fix-logic-gaps exited with code {fix_proc.returncode}", flush=True)
                                break
                            result["logic_enforce_fix_attempted"] = True
                            result["logic_enforce_fix_files"] = []
                            if fix_proc.stdout:
                                try:
                                    fix_out = json.loads(fix_proc.stdout.strip())
                                    result["logic_enforce_fix_files"] = fix_out.get("files_fixed", [])
                                except json.JSONDecodeError:
                                    pass
                            # Re-run validator
                            validate_proc2 = subprocess.run(
                                [sys.executable, str(ROOT_DIR / "validate_pure_java.py"), str(output_path), str(ctx_file), "--json"],
                                capture_output=True,
                                text=True,
                                timeout=120,
                                cwd=str(ROOT_DIR),
                            )
                            stdout_text2 = (validate_proc2.stdout or "").strip()
                            if stdout_text2:
                                json_match2 = re.search(r"\{[\s\S]*\}", stdout_text2)
                                if json_match2:
                                    val2 = json.loads(json_match2.group(0))
                                    lc2 = val2.get("results", {}).get("logic_completeness", {})
                                    if lc2 and not lc2.get("skipped"):
                                        score2 = lc2.get("score", 0)
                                        result["logic_completeness_score"] = score2
                                        result["logic_completeness_failed"] = score2 < 100
                                        result["logic_completeness_gaps"] = lc2.get("potential_gaps", [])
                                        result["logic_completeness_gap_details"] = lc2.get("gap_details", [])
                                        result["logic_completeness_action_required"] = lc2.get("action_required", "")
                                        if score2 >= 100:
                                            print(f"[Pure Java Migration] ✓ Logic completeness 100% after fix", flush=True)
                                            break
                                        print(f"[Pure Java Migration] ⚠ Logic completeness {score2}% after fix (attempt {fix_attempt + 1})", flush=True)
                            if result.get("logic_completeness_failed") is False:
                                break
                        except subprocess.TimeoutExpired:
                            result["logic_enforce_fix_timeout"] = True
                            break
                        except Exception as e2:
                            result["logic_enforce_fix_error"] = str(e2)
                            break
                
                if proc.returncode != 0:
                    self._send_json(result, status=500)
                else:
                    print(f"[Pure Java Migration] ✅ Completed {unit_id}_{node_id}: {len(files)} files generated", flush=True)
                    self._send_json(result)
            except subprocess.TimeoutExpired:
                self._send_json({"error": "Migration timed out (20 minutes)"}, status=500)
            except Exception as e:
                import traceback
                error_trace = traceback.format_exc()
                print(f"[Pure Java Migration] ❌ Error: {error_trace}", flush=True)
                self._send_json({"error": str(e), "traceback": error_trace}, status=500)
            return
        
        if parsed.path == "/api/fix-logic-gaps":
            # Fix logic completeness gaps: run validator, send gap details + files to LLM, overwrite fixed files
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length == 0:
                self._send_json({"error": "Request body required"}, status=400)
                return
            try:
                body = json.loads(self.rfile.read(content_length).decode("utf-8"))
            except json.JSONDecodeError as e:
                self._send_json({"error": f"Invalid JSON: {e}"}, status=400)
                return
            app_dir = body.get("appDir")
            context_file = body.get("contextFile")
            if not app_dir or not context_file:
                self._send_json({"error": "appDir and contextFile are required"}, status=400)
                return
            if not os.path.isabs(app_dir):
                app_dir = os.path.normpath(os.path.join(str(ROOT_DIR), app_dir))
            if not os.path.isabs(context_file):
                context_file = os.path.normpath(os.path.join(str(ROOT_DIR), context_file))
            if not os.path.isdir(app_dir):
                self._send_json({"error": f"appDir not found: {app_dir}"}, status=404)
                return
            if not os.path.isfile(context_file):
                self._send_json({"error": f"contextFile not found: {context_file}"}, status=404)
                return
            api_key = os.environ.get("ANTHROPIC_API_KEY")
            if not api_key:
                self._send_json({"error": "ANTHROPIC_API_KEY env var is not set"}, status=400)
                return
            try:
                fix_script = ROOT_DIR / "fix_logic_gaps.py"
                cmd = [sys.executable, str(fix_script), app_dir, context_file]
                print(f"[Fix logic gaps] Running: {' '.join(cmd)}", flush=True)
                proc = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=600,
                    cwd=str(ROOT_DIR),
                    env=os.environ.copy(),
                )
                stderr_text = (proc.stderr or "").strip()
                stdout_text = (proc.stdout or "").strip()
                if proc.returncode != 0:
                    self._send_json({
                        "success": False,
                        "error": stderr_text or f"Script exited with code {proc.returncode}",
                        "stdout": stdout_text,
                        "stderr": stderr_text,
                    }, status=500)
                    return
                result = {"success": True, "stderr": stderr_text}
                if stdout_text:
                    try:
                        data = json.loads(stdout_text)
                        result.update(data)
                    except json.JSONDecodeError:
                        result["raw_stdout"] = stdout_text
                print(f"[Fix logic gaps] OK: {result.get('files_fixed', [])}", flush=True)
                self._send_json(result)
            except subprocess.TimeoutExpired:
                self._send_json({"error": "Fix logic gaps timed out (10 minutes)", "success": False}, status=500)
            except Exception as e:
                import traceback
                self._send_json({"error": str(e), "traceback": traceback.format_exc(), "success": False}, status=500)
            return
        
        if parsed.path == "/api/run-application":
            global _app_process
            proj_path = ROOT_DIR / "warranty_demo"
            if not proj_path.is_dir():
                self._send_json({"started": False, "error": "warranty_demo not found. Build the application first."}, status=400)
                return
            if _app_process is not None and _app_process.poll() is None:
                self._send_json({"started": True, "message": "Application already running."})
                return
            try:
                _app_process = subprocess.Popen(
                    ["mvn", "spring-boot:run", "-DskipTests"],
                    cwd=str(proj_path),
                    stdout=subprocess.DEVNULL,
                    stderr=subprocess.DEVNULL,
                    start_new_session=True,
                )
                self._send_json({"started": True, "message": "Application starting. Check status in 30-60 seconds."})
            except Exception as e:
                self._send_json({"started": False, "error": str(e)}, status=500)
            return

        if parsed.path == "/api/validate":
            # Global Context Validation API (projectDir + programId + entryNodeId)
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
                self._send_json({"success": False, "error": f"Project directory not found: {project_dir}"}, status=400)
                return

            context_path = None
            if program_id and entry_node_id:
                cf = ROOT_DIR / "context_index" / f"{program_id}_{entry_node_id}.json"
                if cf.exists():
                    context_path = cf

            try:
                from validate_pure_java import PureJavaValidator
                validator = PureJavaValidator(proj_path, context_path)
                results = validator.validate_all()

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

                self._send_json({
                    "success": True,
                    "validation": results,
                    "generatedFiles": generated_files_data,
                    "rpgSnippet": rpg_snippet,
                    "rpgStartLine": rpg_start_line,
                })
            except Exception as e:
                self._send_json({"success": False, "error": str(e)}, status=500)
            return

        if parsed.path == "/api/validate-pure-java":
            # Validate Pure Java application
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length > 0:
                body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                app_dir = body.get("appDir")
                context_file = body.get("contextFile")
            else:
                params = parse_qs(parsed.query)
                app_dir = params.get("appDir", [None])[0]
                context_file = params.get("contextFile", [None])[0]
            
            if not app_dir:
                self._send_json({"error": "appDir is required"}, status=400)
                return
            
            try:
                validate_script = ROOT_DIR / "validate_pure_java.py"
                cmd = [sys.executable, str(validate_script), app_dir]
                
                # Add context_file if provided (it's an optional positional argument)
                if context_file:
                    cmd.append(context_file)
                
                # Add --json flag last
                cmd.append("--json")
                
                print(f"[Validate] Running: {' '.join(cmd)}", flush=True)
                print(f"[Validate] Working directory: {os.getcwd()}", flush=True)
                print(f"[Validate] App dir exists: {os.path.exists(app_dir)}", flush=True)
                if context_file:
                    print(f"[Validate] Context file exists: {os.path.exists(context_file)}", flush=True)
                
                proc = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=300,
                    cwd=str(ROOT_DIR)
                )
                
                # Extract JSON from output
                # The --json flag should output only JSON, but handle mixed output gracefully
                stdout_text = proc.stdout.strip()
                stderr_text = proc.stderr.strip() if proc.stderr else ""
                
                validation_result = None
                json_error = None
                
                # Try parsing stdout as JSON directly
                if stdout_text:
                    try:
                        # Direct JSON parse
                        validation_result = json.loads(stdout_text)
                        print(f"[Validate] ✓ Successfully parsed JSON from stdout ({len(stdout_text)} chars)", flush=True)
                    except json.JSONDecodeError as e:
                        json_error = str(e)
                        print(f"[Validate] JSON parse error: {e}", flush=True)
                        print(f"[Validate] stdout preview: {stdout_text[:200]}...", flush=True)
                        
                        # Try to find JSON object in stdout (in case there's extra output)
                        stdout_lines = stdout_text.split('\n')
                        json_start = None
                        json_end = None
                        
                        # Find first line with {
                        for i, line in enumerate(stdout_lines):
                            stripped = line.strip()
                            if stripped.startswith('{'):
                                json_start = i
                                break
                        
                        # Find last line with }
                        for i in range(len(stdout_lines) - 1, -1, -1):
                            stripped = stdout_lines[i].strip()
                            if stripped.endswith('}'):
                                json_end = i + 1
                                break
                        
                        if json_start is not None and json_end is not None:
                            json_text = '\n'.join(stdout_lines[json_start:json_end])
                            print(f"[Validate] Extracted JSON from lines {json_start} to {json_end}", flush=True)
                            try:
                                validation_result = json.loads(json_text)
                                print(f"[Validate] ✓ Successfully parsed extracted JSON", flush=True)
                            except json.JSONDecodeError as e2:
                                json_error = str(e2)
                                print(f"[Validate] Extracted JSON also failed: {e2}", flush=True)
                
                # If still no result, try stderr
                if not validation_result and stderr_text:
                    try:
                        validation_result = json.loads(stderr_text)
                    except json.JSONDecodeError:
                        # Try to find JSON in stderr lines
                        stderr_lines = stderr_text.split('\n')
                        for line in stderr_lines:
                            stripped = line.strip()
                            if stripped.startswith('{'):
                                try:
                                    validation_result = json.loads(stripped)
                                    break
                                except:
                                    pass
                
                # If still no result, return error with details
                if not validation_result:
                    print(f"[Validate] ❌ Failed to parse JSON. stdout length: {len(stdout_text)}, stderr length: {len(stderr_text)}", flush=True)
                    print(f"[Validate] Return code: {proc.returncode}", flush=True)
                    print(f"[Validate] Command: {' '.join(cmd)}", flush=True)
                    if stdout_text:
                        print(f"[Validate] First 500 chars of stdout:\n{stdout_text[:500]}", flush=True)
                    if stderr_text:
                        print(f"[Validate] First 500 chars of stderr:\n{stderr_text[:500]}", flush=True)
                    
                    validation_result = {
                        "error": "Failed to parse validation output as JSON",
                        "jsonError": json_error or "No JSON found in output",
                        "stdoutLength": len(stdout_text),
                        "stderrLength": len(stderr_text),
                        "stdoutPreview": stdout_text[:2000] if stdout_text else "(empty)",
                        "stderrPreview": stderr_text[:2000] if stderr_text else "(empty)",
                        "returnCode": proc.returncode,
                        "command": ' '.join(cmd),
                        "hint": "Check if validate_pure_java.py is outputting valid JSON with --json flag"
                    }
                
                # Note: Return code 2 means WARNING status, which is acceptable
                # Only return code 1 means FAILED
                if proc.returncode == 2 and validation_result and not validation_result.get("error"):
                    # WARNING status is fine - validation passed with warnings
                    pass
                elif proc.returncode == 1:
                    # FAILED status
                    if validation_result and not validation_result.get("error"):
                        validation_result["status"] = "FAILED"
                    elif not validation_result:
                        validation_result = {
                            "error": "Validation failed",
                            "returnCode": proc.returncode,
                            "stdout": stdout_text[:500] if stdout_text else "",
                            "stderr": stderr_text[:500] if stderr_text else ""
                        }
                
                # Add debugging info if validation_result has error
                if validation_result and validation_result.get("error"):
                    validation_result["debug"] = {
                        "stdoutLength": len(stdout_text),
                        "stderrLength": len(stderr_text),
                        "returnCode": proc.returncode,
                        "stdoutSample": stdout_text[:200] if stdout_text else "",
                        "stderrSample": stderr_text[:200] if stderr_text else ""
                    }
                
                self._send_json(validation_result)
            except subprocess.TimeoutExpired:
                self._send_json({"error": "Validation timed out"}, status=500)
            except Exception as e:
                import traceback
                error_trace = traceback.format_exc()
                print(f"[Validate] Error: {error_trace}", flush=True)
                self._send_json({"error": str(e), "traceback": error_trace}, status=500)
            return
        
        if parsed.path == "/api/build-project":
            # Build Maven project
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length > 0:
                body = json.loads(self.rfile.read(content_length).decode("utf-8"))
                project_dir = body.get("projectDir", "warranty_demo")
            else:
                params = parse_qs(parsed.query)
                project_dir = params.get("projectDir", ["warranty_demo"])[0]
            
            try:
                project_path = ROOT_DIR / project_dir
                if not project_path.exists():
                    self._send_json({"error": f"Project directory not found: {project_dir}"}, status=404)
                    return
                
                # Set JAVA_HOME if available
                env = os.environ.copy()
                java_home = os.environ.get("JAVA_HOME")
                if java_home:
                    env["JAVA_HOME"] = java_home
                
                cmd = ["mvn", "clean", "package", "-DskipTests"]
                proc = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=600,
                    cwd=str(project_path),
                    env=env
                )
                
                result = {
                    "success": proc.returncode == 0,
                    "output": proc.stdout,
                    "error": proc.stderr if proc.returncode != 0 else None,
                    "projectDir": str(project_path)
                }
                
                if proc.returncode != 0:
                    self._send_json(result, status=500)
                else:
                    self._send_json(result)
            except subprocess.TimeoutExpired:
                self._send_json({"error": "Build timed out"}, status=500)
            except FileNotFoundError:
                self._send_json({"error": "Maven not found. Please install Maven."}, status=500)
            except Exception as e:
                self._send_json({"error": str(e)}, status=500)
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
    
    def _extract_package(self, java_code: str) -> Optional[str]:
        """Extract package declaration from Java code."""
        import re
        match = re.search(r'package\s+([\w.]+);', java_code)
        return match.group(1) if match else None


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
    print("  GET  /                    - Pure Java Pipeline UI (Track B) - DEFAULT")
    print("  GET  /pipeline            - Pure Java Pipeline UI (Track B)")
    print("  GET  /track-a             - Track A UI (RPG-native migration)")
    print("  GET  /api/ping            - Health check")
    print("  GET  /api/discover-directories - Discover available AST/RPG directories")
    print("  GET  /api/manifest        - List indexed nodes")
    print("  GET  /api/rpg-snippet     - RPG source snippet (traceability)")
    print("  GET  /api/test-api        - Test API endpoints on localhost:8081")
    print("  POST /api/compile         - Compile Java code (body=source)")
    print("  POST /api/migrate         - Run Track A migration (RPG-native)")
    print("  POST /api/migrate-pure-java - Run Track B migration (Pure Java)")
    print("  POST /api/fix-logic-gaps   - Fix logic completeness gaps with LLM (appDir + contextFile)")
    print("  POST /api/build-context   - Build context packages from ASTs")
    print("  POST /api/validate          - Global Context validation (projectDir, programId, entryNodeId)")
    print("  POST /api/validate-pure-java - Validate Pure Java application")
    print("  POST /api/build-project   - Build Maven project")
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

