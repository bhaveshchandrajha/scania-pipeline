package com.pks.migration.ast.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a symbol from the AST (e.g. sym.var, sym.file).
 *
 * Typical fields (depending on kind):
 * - symbolId: fully qualified id (e.g. sym.var.AKTDAT, sym.file.HSAHKPF)
 * - name: short RPG name (AKTDAT, HSAHKPF)
 * - kind: file, var, const, parm, etc.
 * - typeId: reference into the AST "types" table (e.g. t.dec11_2, t.file.HSAHKPF)
 * - declNodeId / scopeId: where it was declared.
 *
 * The underlying JsonNode is preserved for lossless access.
 */
public class Symbol {

    private final String symbolId;
    private final String name;
    private final String kind;
    private final String typeId;
    private final String declNodeId;
    private final String scopeId;
    private final JsonNode raw;

    public Symbol(String symbolId,
                  String name,
                  String kind,
                  String typeId,
                  String declNodeId,
                  String scopeId,
                  JsonNode raw) {
        this.symbolId = symbolId;
        this.name = name;
        this.kind = kind;
        this.typeId = typeId;
        this.declNodeId = declNodeId;
        this.scopeId = scopeId;
        this.raw = raw;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getDeclNodeId() {
        return declNodeId;
    }

    public String getScopeId() {
        return scopeId;
    }

    public JsonNode getRaw() {
        return raw;
    }
}

