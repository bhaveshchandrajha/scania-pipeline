#!/usr/bin/env python3
"""
Minimal Anthropic (Claude) migration demo.

This script:
- Reads a single ContextPackage JSON (as produced by the Java pipeline)
- Builds a prompt for RPG→Java migration for that subroutine
- Calls the Anthropic API (Claude) and prints the generated Java code

USAGE:
  export ANTHROPIC_API_KEY=sk-ant-...
  pip install anthropic

  python migrate_with_claude.py context_index/HS1210_n404.json
  -> Writes generated Java to HS1210_n404.java (same base name as the JSON, in cwd).

  python migrate_with_claude.py context_index/HS1210_n404.json --stream
  -> Same; streams progress to stderr and writes HS1210_n404.java when done.

  stdout still contains the full Java (for UI server / piping); the .java file is always written.

NOTE:
- The ANTHROPIC_API_KEY MUST be provided via environment variable.
- Do NOT hard-code secrets into this script or the repo.
"""

import argparse
import json
import os
from pathlib import Path
from textwrap import dedent

import anthropic


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


def fix_unbalanced_braces(java_code: str) -> str:
    """
    If there are more { than }, append the missing } so the code is compilable.
    Note: This is a simple fix that only handles missing closing braces at the end.
    For structural issues (missing braces in the middle), the code may still have syntax errors.
    """
    open_c = java_code.count("{")
    close_c = java_code.count("}")
    if open_c <= close_c:
        return java_code
    missing = open_c - close_c
    # Append missing closing braces before any trailing newlines/whitespace
    stripped = java_code.rstrip()
    return stripped + "\n" + ("}\n" * missing) + java_code[len(stripped):]


def check_code_truncation(java_code: str, max_tokens: int = 64000) -> bool:
    """
    Check if the generated code might be truncated.
    Returns True if code appears truncated (e.g., ends mid-statement, very close to token limit).
    """
    if not java_code:
        return False
    # Estimate tokens (rough approximation: ~4 chars per token for code)
    estimated_tokens = len(java_code) / 4
    # Check if code ends abruptly (not with closing brace or semicolon)
    last_non_whitespace = java_code.rstrip()[-50:].strip()
    # If code ends without proper closing (no }, no ;, no class end), might be truncated
    if estimated_tokens > max_tokens * 0.9:  # Within 90% of max tokens
        if not last_non_whitespace.endswith('}') and not last_non_whitespace.endswith(';'):
            return True
    return False


def build_prompt(context: dict) -> str:
    """
    Build a single-string user prompt from a ContextPackage (full context, no truncation).

    The ContextPackage is expected to have at least:
    - narrative: Markdown summary of the subroutine
    - rpgSnippet: RPG source code
    - dbContracts: list of DB contracts with columns/types
    - symbolMetadata: optional extra symbol information
    """
    ast_node = context.get("astNode", {})
    narrative = context.get("narrative", "")
    rpg_snippet = context.get("rpgSnippet", "")
    db_contracts = context.get("dbContracts", [])
    symbol_metadata = context.get("symbolMetadata", {})
    display_files = context.get("displayFiles", [])

    node_id = ast_node.get("id")
    kind = ast_node.get("kind")

    db_json = json.dumps(db_contracts, indent=2, ensure_ascii=False)
    symbols_json = json.dumps(symbol_metadata, indent=2, ensure_ascii=False)

    # Build explicit column checklist for 100% mapping: total count + list per contract
    column_checklist_lines = []
    total_columns = 0
    for c in db_contracts:
        name = (c.get("fileName") or c.get("name") or "?").strip()
        cols = c.get("columns") or []
        if cols:
            col_names = [str(col.get("name", "")).strip() for col in cols if col.get("name")]
            total_columns += len(col_names)
            column_checklist_lines.append(f"- {name}: " + ", ".join(col_names))
    column_checklist = "\n".join(column_checklist_lines) if column_checklist_lines else "(no columns in contracts)"

    # Display files (DSPF) section for UI-aware code generation
    if display_files:
        display_lines = []
        for df in display_files:
            name = df.get("name") or df.get("symbolId") or "?"
            file_id = df.get("fileId") or ""
            display_lines.append(f"- **{name}** (symbolId: {df.get('symbolId', '')}, fileId: {file_id})")
            if df.get("ddsSource"):
                display_lines.append("  DDS source (screen layout):")
                display_lines.append("  ```dds")
                display_lines.append(df.get("ddsSource", "")[:8000])  # cap length
                if len(df.get("ddsSource", "")) > 8000:
                    display_lines.append("  ... (truncated)")
                display_lines.append("  ```")
        display_files_section = (
            "## Display files (DSPF) – for UI building\n"
            "The following display files are used by this unit. Use them to generate UI-related code "
            "(e.g. screen DTOs, form fields, or comments describing screen layout) in the target Java.\n\n"
            + "\n".join(display_lines)
            + "\n\n"
        )
    else:
        display_files_section = ""

    column_checklist_blurb = (
        f"⚠️ CRITICAL REQUIREMENT - 100% COLUMN MAPPING IS MANDATORY ⚠️\n\n"
        f"The following lists EVERY SINGLE COLUMN that MUST appear in your Java code. "
        f"Your Java code MUST include EVERY ONE as a field with @Column annotation. "
        f"Use @Column(name=\"EXACT_NAME\") with the exact contract column name if the Java field name differs. "
        f"DO NOT skip, omit, summarize, or abbreviate any columns.\n\n"
        f"TOTAL COLUMNS TO MAP: {total_columns}\n"
        f"YOUR CODE MUST HAVE EXACTLY {total_columns} @Column ANNOTATIONS.\n"
        f"This will be validated - incomplete mapping will fail validation."
    )

    return dedent(
        f"""
        You are an expert IBM i (AS/400) RPG and Java architect.
        Your task is to migrate a single RPG subroutine/procedure to idiomatic Java,
        with **zero hallucinations** on data structures.

        ## Unit to migrate
        - AST node id: {node_id}
        - Kind: {kind}

        ## Business / semantic narrative (primary source of intent)
        The following Markdown narrative describes what the RPG logic does
        and which files/variables it uses:

        --- NARRATIVE START ---
        {narrative}
        --- NARRATIVE END ---

        ## RPG source (primary source of control flow)
        Here is the original RPG (SQLRPGLE/RPGLE) snippet for this node's range:

        --- RPG SOURCE START ---
        {rpg_snippet}
        --- RPG SOURCE END ---

        ## Database contracts (single source of truth for schema)
        The following JSON describes IBM i physical/logical files and columns
        used by this unit. All Java JPA entities, SQL, and field types MUST
        match these definitions exactly (names, lengths, precisions, scales).
        Do NOT invent new columns or change existing ones.

        --- DB CONTRACTS START (JSON) ---
        {db_json}
        --- DB CONTRACTS END ---

        ## ⚠️ MANDATORY COLUMN CHECKLIST - 100% MAPPING REQUIRED ⚠️
        {column_checklist_blurb}

        {column_checklist}

        **FINAL REMINDER**: Before finishing your code, count all @Column annotations. The count MUST equal {total_columns}. If it doesn't, add the missing @Column fields!

        **IMPORTANT FOR LARGE CONTEXTS**: If this context has many dbContracts ({len(db_contracts)} contracts, {total_columns} total columns), 
        you MUST generate COMPLETE entities for ALL contracts, even if the code is very long. Do NOT truncate or skip entities.
        Prioritize completeness: generate all {len(db_contracts)} entities with all {total_columns} columns, even if it means a very long output.

        {display_files_section}

        ## Symbol metadata
        Additional symbols (variables, data structures, k-lists, etc.) referenced
        by this unit:

        --- SYMBOL METADATA START (JSON) ---
        {symbols_json}
        --- SYMBOL METADATA END ---

        ## Requirements for your answer

        1. **Target**: Produce Java 17+ code that represents this single unit's logic,
           suitable to live in a Spring-style or clean-architecture service layer.
        2. **Data structures** (CRITICAL – 100% column mapping is MANDATORY and will be validated):
           - You MUST create a JPA @Entity for **every** file listed in `dbContracts` above. Do not skip any.
           - **CRITICAL**: Each entity MUST include **EVERY SINGLE COLUMN** from that contract's "columns" array
             as a field with @Column annotation. This is non-negotiable.
           - Use @Column(name="EXACT_NAME") with the exact contract column name when the Java field name differs.
           - **DO NOT** summarize, abbreviate, omit, or skip any columns. The total number of @Column fields 
             in your code MUST EXACTLY EQUAL the total number shown in the Mandatory column checklist above.
           - If the checklist shows 52 columns total, your code MUST have exactly 52 @Column annotations.
           - This is a hard requirement: incomplete column mapping will cause validation to fail.
           - Represent working variables (non-database fields) as method parameters or DTOs, but ALL database
             columns from dbContracts MUST be in entity classes with @Column annotations.
        3. **Logic**:
           - Recreate the control flow and business rules from the RPG snippet,
             but in clear, idiomatic Java (no GOTO; use structured control flow).
        4. **No hallucinations on schema**:
           - If some detail is not present in `dbContracts` or the narrative,
             keep it generic or add a comment; do NOT fabricate columns, tables,
             or types.
        3. **Column mapping verification** (before finishing):
           - Count the total number of @Column annotations in your generated code.
           - This count MUST match the total from the Mandatory column checklist above.
           - If it doesn't match, you MUST add the missing @Column fields before finishing.
           - Example: If checklist shows 52 columns, your code must have exactly 52 @Column annotations.
        4. **Imports** (mandatory for Spring/JPA):
           - When using JPA repositories, include: `import org.springframework.data.jpa.repository.JpaRepository;`
           - When using Spring annotations, include: `import org.springframework.stereotype.Service;`, `import org.springframework.beans.factory.annotation.Autowired;`, `import org.springframework.transaction.annotation.Transactional;`
           - When using JPA entities, include: `import jakarta.persistence.*;`
           - Always use import statements (not fully qualified names) for better readability.
        5. **Output format**:
           - Output ONLY pure Java code (one or more classes) with comments as needed.
           - Do NOT include markdown code fences (```java or ```), explanations, or any non-Java text.
           - Do not restate the prompt or explain; just provide compilable Java starting with package/import statements.
        6. **Syntax**: Your output must be valid, compilable Java. Every opening brace {{ must
           have a matching closing brace }}. Before finishing, verify: count of {{ equals count of }}.
        7. **Display files (when present)**: If the context includes a "Display files (DSPF)" section,
           use it to inform UI-related code: add comments or DTOs that reflect screen/form structure,
           or document which service methods correspond to which display operations (EXFMT/READ), so the
           target application can support UI building.
        """
    ).strip()


def main() -> None:
    parser = argparse.ArgumentParser(description="Migrate a single ContextPackage to Java using Claude.")
    parser.add_argument(
        "context_file",
        help="Path to a ContextPackage JSON file (e.g. context_package_n422.json)",
    )
    parser.add_argument(
        "--model",
        default="claude-sonnet-4-5",
        help="Anthropic model name (default: claude-sonnet-4-5)",
    )
    parser.add_argument(
        "--max-tokens",
        type=int,
        default=64000,  # Max for claude-sonnet-4-5 (limit 64000)
        help="Max tokens for the response (default: 64000). Claude Sonnet 4.5 allows up to 64000.",
    )
    parser.add_argument(
        "--stream",
        action="store_true",
        help="Stream the response (shows progress)",
    )
    args = parser.parse_args()

    from anthropic_env import load_anthropic_from_env_files

    load_anthropic_from_env_files(Path(__file__).resolve().parent)

    # Output Java file: same base name as the context JSON, in current working directory
    output_java = Path.cwd() / (Path(args.context_file).stem + ".java")

    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        raise SystemExit(
            "ANTHROPIC_API_KEY env var is not set.\n"
            "Please export your key:\n"
            "  export ANTHROPIC_API_KEY='sk-ant-...'\n"
            "Then retry this command."
        )
    
    # Validate API key format
    if not api_key.startswith("sk-ant-"):
        print("⚠️  Warning: API key doesn't start with 'sk-ant-'. This might be invalid.")

    import sys
    
    with open(args.context_file, "r", encoding="utf-8") as f:
        context = json.load(f)

    # Log context size for debugging
    context_size_kb = len(json.dumps(context)) / 1024
    db_contracts = context.get("dbContracts", [])
    total_columns = sum(len(c.get("columns", [])) for c in db_contracts)
    print(f"// Context package size: {context_size_kb:.1f} KB", file=sys.stderr)
    print(f"// DB Contracts: {len(db_contracts)}, Total columns: {total_columns}", file=sys.stderr)
    
    # Warn if context is very large and may need more tokens
    if total_columns > 250:
        estimated_output_tokens = total_columns * 50  # Rough estimate: ~50 tokens per column (entity field + getter/setter)
        if estimated_output_tokens > args.max_tokens * 0.8:
            print(f"// ⚠️  WARNING: Large context detected ({total_columns} columns).", file=sys.stderr)
            print(f"//    Estimated output tokens: ~{estimated_output_tokens}. Current max_tokens: {args.max_tokens}", file=sys.stderr)
            print(f"//    Consider increasing --max-tokens if migration is incomplete.", file=sys.stderr)
    
    print(f"// This may take 60-120 seconds for large packages...", file=sys.stderr)

    user_prompt = build_prompt(context)
    prompt_size_kb = len(user_prompt) / 1024
    print(f"// Prompt size: {prompt_size_kb:.1f} KB", file=sys.stderr)

    client = anthropic.Anthropic(api_key=api_key)

    # Add timeout and better error handling
    import time
    start_time = time.time()
    
    # Print initial status immediately
    print("// Starting migration...", file=sys.stderr, flush=True)
    
    try:
        if args.stream:
            # Streaming mode: show progress as tokens arrive
            print("// Streaming response (you'll see code appear incrementally)...", file=sys.stderr)
            with client.messages.stream(
                model=args.model,
                max_tokens=args.max_tokens,
                temperature=0.2,
                messages=[
                    {
                        "role": "user",
                        "content": user_prompt,
                    }
                ],
                timeout=600.0,  # 10 minutes to match UI server timeout
            ) as stream:
                token_count = 0
                java_code_stream = ""
                for text_block in stream.text_stream:
                    print(text_block, end="", flush=True)
                    java_code_stream += text_block
                    token_count += len(text_block.split())
                    if token_count % 100 == 0:
                        print(f"\n// Generated ~{token_count} tokens so far...", file=sys.stderr, end="")
                print(f"\n// Stream complete. Total tokens: ~{token_count}", file=sys.stderr)
                
                # Clean and fix braces for streamed output too
                java_code_stream = clean_java_code(java_code_stream)
                if check_code_truncation(java_code_stream, args.max_tokens):
                    print("// ⚠️  WARNING: Code may be truncated (near token limit).", file=sys.stderr)
                    print(f"//    Consider increasing --max-tokens (current: {args.max_tokens}) or splitting the migration.", file=sys.stderr)
                    print(f"//    Generated code length: {len(java_code_stream)} chars (~{len(java_code_stream)/4:.0f} tokens)", file=sys.stderr)
                java_code_stream = fix_unbalanced_braces(java_code_stream)
                if java_code_stream.count("{") != java_code_stream.count("}"):
                    print("// Warning: braces still unbalanced after auto-fix", file=sys.stderr)
                output_java.write_text(java_code_stream, encoding="utf-8")
                print(f"// Wrote {output_java}", file=sys.stderr)
        else:
            # Non-streaming mode: wait for complete response
            print("// Sending request to LLM (this may take 60-120 seconds for large prompts)...", file=sys.stderr)
            response = client.messages.create(
                model=args.model,
                max_tokens=args.max_tokens,
                temperature=0.2,
                messages=[
                    {
                        "role": "user",
                        "content": user_prompt,
                    }
                ],
                timeout=600.0,  # 10 minutes to match UI server timeout  # 5 minute timeout
            )
            elapsed = time.time() - start_time
            print(f"// LLM response received in {elapsed:.1f} seconds", file=sys.stderr)
            
            # Collect full Java from response, clean markdown fences, and fix unbalanced braces if needed
            java_code = ""
            for block in response.content:
                if getattr(block, "type", None) == "text":
                    java_code += block.text
            java_code = clean_java_code(java_code)  # Remove markdown fences
            
            # Check for truncation
            if check_code_truncation(java_code, args.max_tokens):
                print("// ⚠️  WARNING: Code may be truncated (near token limit).", file=sys.stderr)
                print(f"//    Consider increasing --max-tokens (current: {args.max_tokens}) or splitting the migration.", file=sys.stderr)
                print(f"//    Generated code length: {len(java_code)} chars (~{len(java_code)/4:.0f} tokens)", file=sys.stderr)
            
            java_code = fix_unbalanced_braces(java_code)
            if java_code.count("{") != java_code.count("}"):
                print("// Warning: braces still unbalanced after auto-fix", file=sys.stderr)
            output_java.write_text(java_code, encoding="utf-8")
            print(f"// Wrote {output_java}", file=sys.stderr)
            print(java_code)
    except anthropic.RateLimitError as e:
        elapsed = time.time() - start_time
        print(f"// Rate limit (429) after {elapsed:.1f} seconds.", file=sys.stderr)
        print("// Wait one minute and retry, or use a smaller node.", file=sys.stderr)
        raise SystemExit(129)  # 129 = rate limit, so UI can detect it
    except Exception as e:
        elapsed = time.time() - start_time
        print(f"// Error after {elapsed:.1f} seconds: {e}", file=sys.stderr)
        raise


if __name__ == "__main__":
    main()

