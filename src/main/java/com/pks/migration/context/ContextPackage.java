package com.pks.migration.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.pks.migration.ast.model.AstNode;
import com.pks.migration.ast.model.DbContract;

import java.util.List;

/**
 * Represents the final, LLM-ready context bundle for a single subroutine /
 * procedure.
 *
 * This strictly bundles:
 * - narrative (business intent)
 * - RPG snippet (legacy implementation)
 * - AST metadata (types, dbContracts, node range)
 */
public class ContextPackage {

    public record DbContractView(
            String symbolId,
            String name,
            String library,
            String typeId,
            List<DbContract.Column> columns
    ) {}

    private final String astNodeId;
    private final String astNodeKind;
    private final String astNodeName;
    private final AstNode.Range range;
    private final JsonNode astNodeRaw;

    private final String narrativeMarkdown;
    private final String rpgSnippet;
    private final List<DbContractView> dbContracts;

    public ContextPackage(String astNodeId,
                          String astNodeKind,
                          String astNodeName,
                          AstNode.Range range,
                          JsonNode astNodeRaw,
                          String narrativeMarkdown,
                          String rpgSnippet,
                          List<DbContractView> dbContracts) {
        this.astNodeId = astNodeId;
        this.astNodeKind = astNodeKind;
        this.astNodeName = astNodeName;
        this.range = range;
        this.astNodeRaw = astNodeRaw;
        this.narrativeMarkdown = narrativeMarkdown;
        this.rpgSnippet = rpgSnippet;
        this.dbContracts = List.copyOf(dbContracts);
    }

    public String getAstNodeId() {
        return astNodeId;
    }

    public String getAstNodeKind() {
        return astNodeKind;
    }

    public String getAstNodeName() {
        return astNodeName;
    }

    public AstNode.Range getRange() {
        return range;
    }

    public JsonNode getAstNodeRaw() {
        return astNodeRaw;
    }

    public String getNarrativeMarkdown() {
        return narrativeMarkdown;
    }

    public String getRpgSnippet() {
        return rpgSnippet;
    }

    public List<DbContractView> getDbContracts() {
        return dbContracts;
    }
}

