package com.pks.migration.context;

import com.pks.migration.ast.PksAstParser;
import com.pks.migration.ast.model.AstNode;
import com.pks.migration.ast.model.DbContract;
import com.pks.migration.ast.model.PksAst;
import com.pks.migration.narrative.SemanticNarrativeBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Step 3: Context Assembler (Prompt Orchestrator).
 *
 * Given:
 * - a PKS AST JSON file
 * - a subroutine / procedure node id
 * - an RPG source provider
 *
 * it returns a ContextPackage that bundles:
 * - Narrative: "what" (business intent, from SemanticNarrativeBuilder)
 * - RPG snippet: "how" (implementation, sliced via AST range)
 * - AST metadata: "constraints" (dbContracts, node range, raw node JSON)
 */
public class ContextAssembler {

    private final PksAstParser parser;
    private final SemanticNarrativeBuilder narrativeBuilder;

    public ContextAssembler() {
        this(new PksAstParser(), new SemanticNarrativeBuilder());
    }

    public ContextAssembler(PksAstParser parser, SemanticNarrativeBuilder narrativeBuilder) {
        this.parser = parser;
        this.narrativeBuilder = narrativeBuilder;
    }

    public ContextPackage buildContext(Path astPath,
                                       String nodeId,
                                       RpgSourceProvider rpgSourceProvider) throws IOException {
        PksAst ast = parser.parse(astPath);
        AstNode node = ast.getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("No AST node with id: " + nodeId);
        }

        String narrative = narrativeBuilder.buildMarkdown(ast, node);

        String rpgSnippet = null;
        AstNode.Range range = node.getRange();
        if (range != null && rpgSourceProvider != null) {
            rpgSnippet = rpgSourceProvider.getSnippet(
                    range.fileId(),
                    range.startLine(),
                    range.endLine()
            );
        }

        // Determine which dbContracts are relevant based on file symbols
        List<ContextPackage.DbContractView> dbs = new ArrayList<>();
        for (String symbolId : node.getReferencedSymbolIds()) {
            if (!symbolId.startsWith("sym.file.")) {
                continue;
            }
            DbContract contract = ast.getDbContractBySymbolId(symbolId);
            if (contract == null) {
                continue;
            }
            dbs.add(new ContextPackage.DbContractView(
                    contract.getSymbolId(),
                    contract.getName(),
                    contract.getLibrary(),
                    contract.getTypeId(),
                    contract.getColumns()
            ));
        }

        return new ContextPackage(
                node.getId(),
                node.getKind(),
                node.getName(),
                node.getRange(),
                node.getRaw(),
                narrative,
                rpgSnippet,
                dbs
        );
    }
}

