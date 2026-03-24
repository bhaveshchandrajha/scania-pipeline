package com.pks.migration.narrative;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pks.migration.ast.model.AstNode;
import com.pks.migration.ast.model.DbContract;
import com.pks.migration.ast.model.PksAst;
import com.pks.migration.ast.model.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Step 2: Convert AST node "sem" blocks into an English narrative.
 *
 * This focuses on:
 * - Referenced files / dbContracts.
 * - Referenced variables, with type information where available.
 * - Raw sem JSON for lossless traceability.
 *
 * The result is a Markdown string suitable for indexing in a knowledge base.
 */
public class SemanticNarrativeBuilder {

    private final ObjectMapper mapper;

    public SemanticNarrativeBuilder() {
        this.mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String buildMarkdown(PksAst ast, AstNode node) {
        StringBuilder sb = new StringBuilder();

        sb.append("### Node ").append(node.getId())
                .append(": ").append(node.getKind());
        if (node.getName() != null) {
            sb.append(" `").append(node.getName()).append("`");
        }
        sb.append("\n\n");

        sb.append("This ").append(node.getKind().toLowerCase())
                .append(" references symbols derived from its semantic block and symbol table.\n\n");

        // Partition referenced symbols into files vs variables (and others)
        List<String> fileSymbols = new ArrayList<>();
        List<String> varSymbols = new ArrayList<>();
        List<String> otherSymbols = new ArrayList<>();

        for (String symbolId : node.getReferencedSymbolIds()) {
            if (symbolId.startsWith("sym.file.")) {
                fileSymbols.add(symbolId);
            } else if (symbolId.startsWith("sym.var.")) {
                varSymbols.add(symbolId);
            } else {
                otherSymbols.add(symbolId);
            }
        }

        // Files / DB Contracts
        if (!fileSymbols.isEmpty()) {
            sb.append("- **Files / DB Contracts**:\n");
            for (String symbolId : fileSymbols) {
                Symbol sym = ast.getSymbol(symbolId);
                DbContract contract = ast.getDbContractBySymbolId(symbolId);

                String fileName = sym != null && sym.getName() != null
                        ? sym.getName()
                        : symbolId;
                sb.append("  - ").append(fileName);
                if (contract != null) {
                    String colsSummary = contract.getColumns().stream()
                            .map(c -> {
                                StringBuilder colSb = new StringBuilder();
                                colSb.append(c.name());
                                if (c.sqlType() != null) {
                                    colSb.append(" ").append(c.sqlType());
                                }
                                return colSb.toString();
                            })
                            .collect(Collectors.joining(", "));
                    if (!colsSummary.isEmpty()) {
                        sb.append(" (columns: ").append(colsSummary).append(")");
                    }
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        // Variables
        if (!varSymbols.isEmpty()) {
            sb.append("- **Variables**:\n");
            for (String symbolId : varSymbols) {
                Symbol sym = ast.getSymbol(symbolId);
                String varName = sym != null && sym.getName() != null
                        ? sym.getName()
                        : symbolId;
                String typeId = sym != null ? sym.getTypeId() : null;
                sb.append("  - ").append(varName);
                if (typeId != null) {
                    sb.append(" (type: ").append(typeId).append(")");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        // Other symbols (optional)
        if (!otherSymbols.isEmpty()) {
            sb.append("- **Other symbols**:\n");
            for (String symbolId : otherSymbols) {
                Symbol sym = ast.getSymbol(symbolId);
                String name = sym != null && sym.getName() != null
                        ? sym.getName()
                        : symbolId;
                sb.append("  - ").append(name).append("\n");
            }
            sb.append("\n");
        }

        // Raw sem JSON for traceability
        if (node.getSem() != null && !node.getSem().isMissingNode() && !node.getSem().isNull()) {
            sb.append("- **Raw sem JSON**:\n");
            sb.append("```json\n");
            // node.getSem() is already a JsonNode; pretty-print it for readability.
            // Use an untyped Map to avoid unnecessary generic coupling here.
            Map<?, ?> semMap = mapper.convertValue(node.getSem(), Map.class);
            try {
                sb.append(mapper.writeValueAsString(semMap));
            } catch (JsonProcessingException e) {
                sb.append(node.getSem().toString());
            }
            sb.append("\n```\n");
        }

        return sb.toString();
    }
}

