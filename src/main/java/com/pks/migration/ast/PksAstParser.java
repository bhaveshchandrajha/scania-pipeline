package com.pks.migration.ast;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pks.migration.ast.model.AstNode;
import com.pks.migration.ast.model.DbContract;
import com.pks.migration.ast.model.PksAst;
import com.pks.migration.ast.model.Symbol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Step 1: Parser for PKS AST JSON.
 *
 * Responsibilities:
 * - Load the AST JSON using Jackson.
 * - Extract all symbols (sym.var, sym.file, etc.) from symbolTable.
 * - Extract all dbContracts.nativeFiles entries into DbContract models.
 * - Extract all nodes (with sem, range, basic props) into AstNode models.
 */
public class PksAstParser {

    private final ObjectMapper objectMapper;

    public PksAstParser() {
        this.objectMapper = new ObjectMapper(new JsonFactory());
    }

    public PksAst parse(Path astPath) throws IOException {
        try (InputStream in = Files.newInputStream(astPath)) {
            JsonNode root = objectMapper.readTree(in);
            Map<String, JsonNode> types = readTypes(root);
            Map<String, Symbol> symbols = readSymbols(root);
            Map<String, DbContract> dbContracts = readDbContracts(root, types);
            Map<String, AstNode> nodes = readNodes(root);
            return new PksAst(root, symbols, dbContracts, nodes);
        }
    }

    private Map<String, JsonNode> readTypes(JsonNode root) {
        JsonNode typesNode = root.path("types");
        Map<String, JsonNode> result = new HashMap<>();
        if (typesNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = typesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    private Map<String, Symbol> readSymbols(JsonNode root) {
        JsonNode symbolTable = root.path("symbolTable");
        Map<String, Symbol> result = new HashMap<>();
        if (!symbolTable.isObject()) {
            return result;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = symbolTable.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> e = fields.next();
            String symbolId = e.getKey();
            JsonNode sym = e.getValue();
            String name = textOrNull(sym, "name");
            String kind = textOrNull(sym, "kind");
            String typeId = textOrNull(sym, "typeId");
            String declNodeId = textOrNull(sym, "declNodeId");
            String scopeId = textOrNull(sym, "scopeId");

            Symbol model = new Symbol(symbolId, name, kind, typeId, declNodeId, scopeId, sym);
            result.put(symbolId, model);
        }
        return result;
    }

    private Map<String, DbContract> readDbContracts(JsonNode root, Map<String, JsonNode> types) {
        JsonNode dbContractsRoot = root.path("dbContracts").path("nativeFiles");
        Map<String, DbContract> result = new HashMap<>();
        if (!dbContractsRoot.isArray()) {
            return result;
        }

        for (JsonNode fileNode : dbContractsRoot) {
            String symbolId = textOrNull(fileNode, "symbolId");
            String name = textOrNull(fileNode, "name");
            String library = textOrNull(fileNode, "library");
            String typeId = textOrNull(fileNode, "typeId");

            // Build column list with type information resolved through types[]
            List<DbContract.Column> columns = new ArrayList<>();
            Set<String> keyColumns = readKeyColumnNames(fileNode);
            JsonNode colsNode = fileNode.path("columns");
            if (colsNode.isArray()) {
                for (JsonNode col : colsNode) {
                    String colName = textOrNull(col, "name");
                    String colTypeId = textOrNull(col, "typeId");
                    boolean nullable = col.path("nullable").asBoolean(false);
                    boolean key = keyColumns.contains(colName);

                    TypeInfo typeInfo = resolveTypeInfo(colTypeId, types);
                    DbContract.Column column = new DbContract.Column(
                            colName,
                            colTypeId,
                            typeInfo.sqlType,
                            typeInfo.length,
                            typeInfo.scale,
                            key,
                            nullable
                    );
                    columns.add(column);
                }
            }

            DbContract contract = new DbContract(symbolId, name, library, typeId, columns, fileNode);
            if (symbolId != null) {
                result.put(symbolId, contract);
            }
        }
        return result;
    }

    private Set<String> readKeyColumnNames(JsonNode fileNode) {
        Set<String> keys = new HashSet<>();
        JsonNode keysNode = fileNode.path("keys");
        if (keysNode.isArray()) {
            for (JsonNode keyNode : keysNode) {
                String name = textOrNull(keyNode, "name");
                if (name != null) {
                    keys.add(name);
                }
            }
        }
        return keys;
    }

    private Map<String, AstNode> readNodes(JsonNode root) {
        JsonNode nodes = root.path("nodes");
        Map<String, AstNode> result = new HashMap<>();
        if (!nodes.isArray()) {
            return result;
        }

        for (JsonNode node : nodes) {
            String id = textOrNull(node, "id");
            String kind = textOrNull(node, "kind");

            // Name is usually under props.name for procedures / subroutines.
            String name = null;
            JsonNode props = node.path("props");
            if (props.isObject()) {
                name = textOrNull(props, "name");
            }

            JsonNode sem = node.path("sem");
            List<String> referencedSymbols = new ArrayList<>();
            if (sem.isObject()) {
                Iterator<String> fieldNames = sem.fieldNames();
                while (fieldNames.hasNext()) {
                    referencedSymbols.add(fieldNames.next());
                }
            }

            AstNode.Range range = null;
            JsonNode rangeNode = node.path("range");
            if (rangeNode.isObject()) {
                String fileId = textOrNull(rangeNode, "fileId");
                int startLine = rangeNode.path("startLine").asInt(-1);
                int startCol = rangeNode.has("startCol")
                        ? rangeNode.path("startCol").asInt()
                        : rangeNode.path("startColumn").asInt(-1);
                int endLine = rangeNode.path("endLine").asInt(-1);
                int endCol = rangeNode.has("endCol")
                        ? rangeNode.path("endCol").asInt()
                        : rangeNode.path("endColumn").asInt(-1);
                range = new AstNode.Range(fileId, startLine, startCol, endLine, endCol);
            }

            AstNode model = new AstNode(id, kind, name, sem, referencedSymbols, range, node);
            if (id != null) {
                result.put(id, model);
            }
        }
        return result;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return child != null && !child.isNull() ? child.asText() : null;
    }

    /**
     * Minimal type info needed for narrative / JPA schema.
     */
    private static final class TypeInfo {
        final String sqlType;
        final Integer length;
        final Integer scale;

        TypeInfo(String sqlType, Integer length, Integer scale) {
            this.sqlType = sqlType;
            this.length = length;
            this.scale = scale;
        }
    }

    private TypeInfo resolveTypeInfo(String typeId, Map<String, JsonNode> types) {
        if (typeId == null) {
            return new TypeInfo(null, null, null);
        }
        JsonNode typeNode = types.get(typeId);
        if (typeNode == null) {
            // Best-effort decode for known patterns such as t.char.65, t.dec11_2
            if (typeId.startsWith("t.char.")) {
                Integer len = parseIntSafe(typeId.substring("t.char.".length()));
                String sql = len != null ? "CHAR(" + len + ")" : "CHAR";
                return new TypeInfo(sql, len, null);
            }
            if (typeId.startsWith("t.dec")) {
                String rest = typeId.substring("t.dec".length());
                String[] parts = rest.split("_");
                Integer precision = parseIntSafe(parts[0]);
                Integer scale = parts.length > 1 ? parseIntSafe(parts[1]) : 0;
                String sql = precision != null
                        ? "DECIMAL(" + precision + "," + (scale != null ? scale : 0) + ")"
                        : "DECIMAL";
                return new TypeInfo(sql, precision, scale);
            }
            return new TypeInfo(typeId, null, null);
        }

        String category = textOrNull(typeNode, "category");
        if ("numeric".equals(category)) {
            Integer precision = typeNode.has("precision") ? typeNode.get("precision").asInt() : null;
            Integer scale = typeNode.has("scale") ? typeNode.get("scale").asInt() : null;
            String sql = precision != null
                    ? "DECIMAL(" + precision + "," + (scale != null ? scale : 0) + ")"
                    : "DECIMAL";
            return new TypeInfo(sql, precision, scale);
        }
        if ("char".equals(category)) {
            // length is encoded in the typeId, e.g. t.char.65
            Integer len = null;
            int dotIdx = typeId.lastIndexOf('.');
            if (dotIdx >= 0 && dotIdx + 1 < typeId.length()) {
                len = parseIntSafe(typeId.substring(dotIdx + 1));
            }
            String sql = len != null ? "CHAR(" + len + ")" : "CHAR";
            return new TypeInfo(sql, len, null);
        }

        return new TypeInfo(typeId, null, null);
    }

    private Integer parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

