package com.pks.migration.ast.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Represents a single AST node (statement, subroutine, procedure, etc.).
 *
 * This is deliberately minimal and schema-agnostic; all remaining details
 * are accessible from the underlying JsonNode.
 */
public class AstNode {

    public record Range(
            String fileId,
            int startLine,
            int startColumn,
            int endLine,
            int endColumn
    ) {}

    private final String id;
    private final String kind;
    private final String name;
    private final JsonNode sem;
    private final List<String> referencedSymbolIds;
    private final Range range;
    private final JsonNode raw;

    public AstNode(String id,
                   String kind,
                   String name,
                   JsonNode sem,
                   List<String> referencedSymbolIds,
                   Range range,
                   JsonNode raw) {
        this.id = id;
        this.kind = kind;
        this.name = name;
        this.sem = sem;
        this.referencedSymbolIds = List.copyOf(referencedSymbolIds);
        this.range = range;
        this.raw = raw;
    }

    public String getId() {
        return id;
    }

    public String getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public JsonNode getSem() {
        return sem;
    }

    public List<String> getReferencedSymbolIds() {
        return referencedSymbolIds;
    }

    public Range getRange() {
        return range;
    }

    public JsonNode getRaw() {
        return raw;
    }
}

