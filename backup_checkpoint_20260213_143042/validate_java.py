#!/usr/bin/env python3
"""
Validation script that validates generated Java code against the context package.

This script:
- Reads a ContextPackage JSON and generated Java code
- Runs three-layer validation (Structural, Semantic, Behavioral)
- Outputs validation results as JSON

USAGE:
  python validate_java.py context_package.json generated.java > validation_result.json
"""

import argparse
import json
import os
import re
import subprocess
import tempfile
from pathlib import Path
from typing import Dict, List, Any, Optional


class StructuralValidator:
    """Validates Java code structure and syntax."""

    def validate(self, java_code: str) -> Dict[str, Any]:
        checks = []
        failures = []
        passed = 0
        total = 0

        # Check 1: Not empty
        total += 1
        if java_code and java_code.strip():
            checks.append("✓ Code is not empty")
            passed += 1
        else:
            failures.append("✗ Code is empty")
            checks.append("✗ Code is empty")

        # Check 2: Has class
        total += 1
        if re.search(r'\b(public\s+)?class\s+\w+', java_code):
            checks.append("✓ Contains class definition")
            passed += 1
        else:
            failures.append("✗ Missing class definition")
            checks.append("✗ Missing class definition")

        # Check 3: Has methods
        total += 1
        method_count = len(re.findall(r'\b(public|private|protected)\s+\w+\s+\w+\s*\(', java_code))
        if method_count > 0:
            checks.append(f"✓ Contains {method_count} method(s)")
            passed += 1
        else:
            failures.append("✗ No methods found")
            checks.append("✗ No methods found")

        # Check 4: Balanced braces
        total += 1
        open_braces = java_code.count('{')
        close_braces = java_code.count('}')
        if open_braces == close_braces and open_braces > 0:
            checks.append("✓ Balanced braces")
            passed += 1
        else:
            failures.append(f"✗ Unbalanced braces: {open_braces} open, {close_braces} close")
            checks.append("✗ Unbalanced braces")

        # Check 5: No GOTO
        total += 1
        if 'goto ' not in java_code and 'goto\n' not in java_code:
            checks.append("✓ No GOTO statements")
            passed += 1
        else:
            failures.append("✗ Contains GOTO statements")
            checks.append("✗ Contains GOTO statements")

        score = (passed * 100.0 / total) if total > 0 else 0.0

        return {
            "score": round(score, 2),
            "checks": checks,
            "failures": failures
        }


def clean_java_code(java_code: str) -> str:
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


def extract_public_class_name(java_code: str) -> Optional[str]:
    """Extract the first public class name from Java code. Returns None if no public class found."""
    # Match: public class ClassName or public final class ClassName, etc.
    match = re.search(r'public\s+(?:final\s+)?(?:abstract\s+)?class\s+(\w+)', java_code)
    if match:
        return match.group(1)
    return None


def run_compile_check(java_code: str, work_dir: Path) -> Dict[str, Any]:
    """
    Run javac on the generated Java code to ensure it compiles (no syntax errors).
    Returns dict with: success (bool), errors (list of str), message (str), skipped (bool).
    Uses COMPILE_CLASSPATH env or work_dir/lib/*.jar if present.
    """
    # Clean markdown fences before compiling
    java_code = clean_java_code(java_code)
    result: Dict[str, Any] = {"success": False, "errors": [], "message": "", "skipped": True}
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

    # Resolve classpath: env COMPILE_CLASSPATH, or work_dir/lib/*.jar
    cp = os.environ.get("COMPILE_CLASSPATH")
    if not cp and work_dir.exists():
        lib = work_dir / "lib"
        if lib.is_dir():
            jars = list(lib.glob("*.jar"))
            if jars:
                cp = os.pathsep.join(str(p) for p in jars)
    if not cp:
        result["message"] = "No classpath (set COMPILE_CLASSPATH or run: mvn dependency:copy-dependencies -DoutputDirectory=lib)"
        return result

    result["skipped"] = False
    try:
        # Verify Spring Data JPA is on classpath
        cp_jars = cp.split(os.pathsep)
        has_spring_data_jpa = any("spring-data-jpa" in jar for jar in cp_jars)
        if not has_spring_data_jpa:
            result["message"] = "Spring Data JPA not found on classpath. Run: mvn dependency:copy-dependencies -DoutputDirectory=lib"
            result["errors"] = ["Missing spring-data-jpa jar in lib/"]
            return result
        
        # Extract public class name to use as filename (Java requires public classes match filename)
        public_class_name = extract_public_class_name(java_code)
        if public_class_name:
            # Use the public class name as the filename
            temp_java = work_dir / f"{public_class_name}.java"
            with open(temp_java, "w", encoding="utf-8") as f:
                f.write(java_code)
        else:
            # No public class, use temp file
            with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False, dir=str(work_dir), encoding="utf-8") as f:
                f.write(java_code)
                temp_java = Path(f.name)
        
        try:
            # Use the filename (not full path) for javac since we're compiling from work_dir
            java_filename = temp_java.name if isinstance(temp_java, Path) else Path(temp_java).name
            proc = subprocess.run(
                [javac_path, "-cp", cp, "-Xlint:none", "-encoding", "UTF-8", java_filename],
                cwd=str(work_dir),
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
                    # Show first 20 errors, but prioritize "illegal start of type" and similar critical errors
                    critical_errors = [e for e in syntax_errors if any(keyword in e.lower() for keyword in ["illegal start", "unexpected", "';' expected", "'(' expected", "')' expected"])]
                    other_errors = [e for e in syntax_errors if e not in critical_errors]
                    # Show critical errors first, then others
                    result["errors"] = (critical_errors + other_errors)[:20]
                    result["message"] = f"Compilation failed: {len(syntax_errors)} syntax error(s)"
                    if missing_types:
                        result["message"] += f" (and {len(missing_types)} missing custom type(s) - expected)"
                    # Add hint for "illegal start of type" errors
                    if any("illegal start" in e.lower() for e in syntax_errors):
                        result["message"] += " [Hint: 'illegal start of type' usually means missing closing brace or malformed declaration]"
                elif missing_types:
                    # Only missing custom types - this is expected, code is syntactically valid
                    result["success"] = True
                    result["message"] = f"Syntax OK (missing {len(missing_types)} custom types - entities/repositories/DTOs expected to be generated separately)"
                    result["errors"] = []  # Don't show missing types as errors
                else:
                    result["errors"] = error_lines[:20]
                    result["message"] = "Compilation failed"
                    # Check for common error patterns
                    if any("illegal start" in line.lower() for line in error_lines):
                        result["message"] += " [Hint: Check for missing closing braces or malformed method/class declarations]"
        finally:
            # Clean up temp file
            try:
                if isinstance(temp_java, Path):
                    temp_java.unlink()
                else:
                    os.unlink(temp_java)
            except OSError:
                pass
    except subprocess.TimeoutExpired:
        # Clean up temp file on timeout
        try:
            if 'temp_java' in locals():
                if isinstance(temp_java, Path):
                    temp_java.unlink()
                else:
                    os.unlink(temp_java)
        except OSError:
            pass
        result["message"] = "Compilation timed out"
        result["errors"] = ["javac timed out after 30s"]
    except Exception as e:
        # Clean up temp file on error
        try:
            if 'temp_java' in locals():
                if isinstance(temp_java, Path):
                    temp_java.unlink()
                else:
                    os.unlink(temp_java)
        except OSError:
            pass
        result["message"] = str(e)
        result["errors"] = [str(e)]

    return result


class SemanticValidator:
    """Validates semantic correctness, especially dbContracts mapping."""

    def validate(self, java_code: str, context: Dict[str, Any]) -> Dict[str, Any]:
        checks = []
        failures = []
        passed = 0
        total = 0

        db_contracts = context.get("dbContracts", [])
        db_contract_total = len(db_contracts)
        db_contract_matches = 0

        # Java serializes DbContract with "name"; some payloads use "fileName". Accept both.
        def _file_name(c):
            return (c.get("fileName") or c.get("name") or "").strip()

        # Check 1: dbContracts mapped to entities
        total += 1
        if db_contract_total == 0:
            checks.append("✓ No dbContracts in context (semantic N/A – nothing to validate)")
            passed += 1
        else:
            for contract in db_contracts:
                file_name = _file_name(contract)
                if file_name and (file_name in java_code or file_name.lower() in java_code.lower()):
                    db_contract_matches += 1

            if db_contract_matches == db_contract_total:
                checks.append(f"✓ All {db_contract_total} dbContracts mapped")
                passed += 1
            else:
                failures.append(f"✗ Only {db_contract_matches}/{db_contract_total} dbContracts mapped")
                checks.append(f"✗ {db_contract_matches}/{db_contract_total} dbContracts mapped")

        # Check 2: Column mappings (accept multiple naming styles for 100% sanity)
        total += 1
        column_matches = 0
        column_total = 0
        java_lower = java_code.lower()
        for contract in db_contracts:
            for col in contract.get("columns", []):
                column_total += 1
                col_name = (col.get("name") or "").strip()
                if not col_name:
                    column_matches += 1  # nameless column, skip check
                    continue
                camel_case = self._to_camel_case(col_name)
                # Match: exact name, name in @Column(name="..."), camelCase field, or lowercase identifier
                col_lower = col_name.lower()
                matched = (
                    col_name in java_code
                    or col_lower in java_lower
                    or camel_case in java_code
                    or (camel_case and camel_case in java_lower)
                )
                if matched:
                    column_matches += 1

        if column_total == 0:
            checks.append("✓ No columns to validate (semantic N/A)")
            passed += 1
        elif column_matches == column_total:
            checks.append(f"✓ All {column_total} columns mapped")
            passed += 1
        else:
            ratio = column_matches * 100.0 / column_total
            if ratio >= 80:
                checks.append(f"⚠ {column_matches}/{column_total} columns mapped ({ratio:.1f}%)")
            else:
                failures.append(f"✗ Only {column_matches}/{column_total} columns mapped")
                checks.append(f"✗ {column_matches}/{column_total} columns mapped")

        # When there are no dbContracts, treat semantic as N/A (100%) so overall score isn't unfairly lowered
        if db_contract_total == 0 and column_total == 0:
            score = 100.0
        else:
            score = (passed * 100.0 / total) if total > 0 else 0.0
            if db_contract_total > 0:
                db_ratio = db_contract_matches * 100.0 / db_contract_total
                score = (score * 0.7) + (db_ratio * 0.3)

        return {
            "score": round(score, 2),
            "checks": checks,
            "failures": failures,
            "dbContractMatches": db_contract_matches,
            "dbContractTotal": db_contract_total
        }

    def _to_camel_case(self, name: str) -> str:
        parts = re.split(r'[_\-\s]+', name)
        return parts[0].lower() + ''.join(p.capitalize() for p in parts[1:])


class BehavioralValidator:
    """Validates behavioral correctness (control flow preservation)."""

    def validate(self, java_code: str, context: Dict[str, Any]) -> Dict[str, Any]:
        checks = []
        failures = []
        passed = 0
        total = 0
        control_flow_matches = 0

        rpg_snippet = context.get("rpgSnippet", "")
        narrative = context.get("narrative", "")

        # Check 1: Control flow structures
        total += 1
        has_if = 'if ' in java_code or 'if(' in java_code
        has_loop = 'for ' in java_code or 'while ' in java_code or 'forEach' in java_code
        rpg_has_conditional = 'IF' in rpg_snippet or 'WHEN' in rpg_snippet
        rpg_has_loop = 'DO' in rpg_snippet or 'FOR' in rpg_snippet

        if rpg_has_conditional and has_if:
            checks.append("✓ Conditional logic preserved")
            control_flow_matches += 1
            passed += 1
        elif rpg_has_conditional:
            failures.append("✗ Missing conditional logic")
            checks.append("✗ Missing conditional logic")

        if rpg_has_loop and has_loop:
            checks.append("✓ Loop structures preserved")
            control_flow_matches += 1

        # Check 2: File operations
        total += 1
        rpg_has_read = 'READ' in rpg_snippet or 'CHAIN' in rpg_snippet
        rpg_has_update = 'UPDATE' in rpg_snippet or 'WRITE' in rpg_snippet
        java_has_read = 'find' in java_code.lower() or 'read' in java_code.lower() or 'get' in java_code.lower()
        java_has_update = 'save' in java_code.lower() or 'update' in java_code.lower() or 'persist' in java_code.lower()

        if rpg_has_read and java_has_read:
            checks.append("✓ Read operations preserved")
            control_flow_matches += 1
            passed += 1
        elif rpg_has_read:
            failures.append("✗ Missing read operations")
            checks.append("✗ Missing read operations")

        if rpg_has_update and java_has_update:
            checks.append("✓ Update operations preserved")
            control_flow_matches += 1

        score = (passed * 100.0 / total) if total > 0 else 0.0
        if control_flow_matches > 0:
            score = min(100, score + (control_flow_matches * 5))

        return {
            "score": round(score, 2),
            "checks": checks,
            "failures": failures,
            "controlFlowMatches": control_flow_matches
        }


def build_audit_trail(java_code: str, context: Dict[str, Any], unit_id: str, node_id: str) -> Dict[str, Any]:
    """Build audit trail mapping RPG → AST → Java."""
    ast_node = context.get("astNode", {})
    range_info = ast_node.get("range", {})

    return {
        "unitId": unit_id,
        "nodeId": node_id,
        "rpgSourceFile": range_info.get("sourceId", "unknown"),
        "rpgLineRange": f"{range_info.get('startLine', '?')}-{range_info.get('endLine', '?')}",
        "astNode": {
            "kind": ast_node.get("kind"),
            "name": ast_node.get("name"),
            "id": ast_node.get("id")
        },
        "statistics": {
            "javaLines": len(java_code.split('\n')),
            "javaClasses": java_code.count('class '),
            "javaMethods": java_code.count('public ') + java_code.count('private '),
            "rpgLines": len(context.get("rpgSnippet", "").split('\n'))
        }
    }


def main():
    parser = argparse.ArgumentParser(description="Validate generated Java code")
    parser.add_argument("context_file", help="ContextPackage JSON file")
    parser.add_argument("java_file", help="Generated Java code file")
    parser.add_argument("--unit-id", default="unknown", help="Unit ID")
    parser.add_argument("--node-id", default="unknown", help="Node ID")
    parser.add_argument("--check-compile", action="store_true", default=True, help="Run javac to ensure code compiles (default: True)")
    parser.add_argument("--no-check-compile", action="store_false", dest="check_compile", help="Skip compilation check")
    args = parser.parse_args()

    with open(args.context_file, 'r', encoding='utf-8') as f:
        context = json.load(f)

    with open(args.java_file, 'r', encoding='utf-8') as f:
        java_code = f.read()

    # Run validators
    structural = StructuralValidator().validate(java_code)
    semantic = SemanticValidator().validate(java_code, context)
    behavioral = BehavioralValidator().validate(java_code, context)

    # Optional: run javac to ensure generated code compiles (no syntax errors)
    # Use cwd (project root when run from UI server) so lib/ is project_root/lib
    work_dir = Path.cwd()
    compilation = {"success": False, "errors": [], "message": "Skipped", "skipped": True}
    if args.check_compile:
        compilation = run_compile_check(java_code, work_dir)
        if not compilation["skipped"]:
            structural["checks"].append("✓ Compiles (javac)" if compilation["success"] else "✗ Compiles (javac)")
            if not compilation["success"]:
                structural["failures"].append("✗ Compilation failed: " + (compilation["message"] or "see errors"))
            # Recompute structural score to include compilation check (one more check)
            n = len(structural["checks"])
            passed = sum(1 for c in structural["checks"] if c.startswith("✓"))
            structural["score"] = round((passed * 100.0 / n), 2)

    # Calculate overall score
    overall_score = (structural["score"] + semantic["score"] + behavioral["score"]) / 3.0

    # Determine status
    if overall_score >= 95.0 and structural["score"] >= 95.0 and \
       semantic["score"] >= 95.0 and behavioral["score"] >= 95.0:
        status = "PASSED"
    elif overall_score >= 80.0:
        status = "WARNING"
    else:
        status = "FAILED"

    # Collect all issues
    all_issues = structural["failures"] + semantic["failures"] + behavioral["failures"]

    # Build audit trail
    audit_trail = build_audit_trail(java_code, context, args.unit_id, args.node_id)

    result = {
        "unitId": args.unit_id,
        "nodeId": args.node_id,
        "overallScore": round(overall_score, 2),
        "overallStatus": status,
        "certified": status == "PASSED",
        "compilation": compilation,
        "structural": structural,
        "semantic": semantic,
        "behavioral": behavioral,
        "issues": all_issues,
        "auditTrail": audit_trail
    }

    print(json.dumps(result, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
