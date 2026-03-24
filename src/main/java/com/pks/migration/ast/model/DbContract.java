package com.pks.migration.ast.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Represents a database contract (IBM i physical / logical file definition).
 *
 * This is derived from the AST's dbContracts.nativeFiles entries.
 * It is the primary source of truth for enforcing anti-hallucination rules
 * when generating JPA entities.
 */
public class DbContract {

    public record Column(
            String name,
            String typeId,
            String sqlType,
            Integer length,
            Integer scale,
            boolean key,
            boolean nullable
    ) {}

    private final String symbolId; // e.g. sym.file.HSAHKPF
    private final String name;     // e.g. HSAHKPF
    private final String library;  // e.g. HSSRC
    private final String typeId;   // e.g. t.file.HSAHKPF
    private final List<Column> columns;
    private final JsonNode raw;

    public DbContract(String symbolId,
                      String name,
                      String library,
                      String typeId,
                      List<Column> columns,
                      JsonNode raw) {
        this.symbolId = symbolId;
        this.name = name;
        this.library = library;
        this.typeId = typeId;
        this.columns = List.copyOf(columns);
        this.raw = raw;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public String getName() {
        return name;
    }

    public String getLibrary() {
        return library;
    }

    public String getTypeId() {
        return typeId;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public JsonNode getRaw() {
        return raw;
    }
}

