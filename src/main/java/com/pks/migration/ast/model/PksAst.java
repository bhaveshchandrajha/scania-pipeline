package com.pks.migration.ast.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * High-level in-memory representation of a PKS AST file.
 *
 * It exposes:
 * - the raw JsonNode (lossless access to any schema detail)
 * - symbols (sym.var, sym.file, etc.)
 * - database contracts (dbContracts.nativeFiles)
 * - AST nodes (procedures, subroutines, statements, …)
 */
public class PksAst {

    private final JsonNode root;
    private final Map<String, Symbol> symbolsById;
    private final Map<String, DbContract> dbContractsBySymbolId;
    private final Map<String, AstNode> nodesById;

    public PksAst(JsonNode root,
                  Map<String, Symbol> symbolsById,
                  Map<String, DbContract> dbContractsBySymbolId,
                  Map<String, AstNode> nodesById) {
        this.root = root;
        this.symbolsById = symbolsById;
        this.dbContractsBySymbolId = dbContractsBySymbolId;
        this.nodesById = nodesById;
    }

    public JsonNode getRoot() {
        return root;
    }

    public Map<String, Symbol> getSymbolsById() {
        return symbolsById;
    }

    public Map<String, DbContract> getDbContractsBySymbolId() {
        return dbContractsBySymbolId;
    }

    public Map<String, AstNode> getNodesById() {
        return nodesById;
    }

    public Symbol getSymbol(String symbolId) {
        return symbolsById.get(symbolId);
    }

    public DbContract getDbContractBySymbolId(String symbolId) {
        return dbContractsBySymbolId.get(symbolId);
    }

    public AstNode getNode(String nodeId) {
        return nodesById.get(nodeId);
    }

    public List<AstNode> getAllNodes() {
        return List.copyOf(nodesById.values());
    }
}

