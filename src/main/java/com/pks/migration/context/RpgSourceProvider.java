package com.pks.migration.context;

/**
 * Abstraction for retrieving RPG source text.
 *
 * The AST only stores file identifiers and line ranges. Implementations of this
 * interface are responsible for resolving those identifiers to actual source
 * text (for example by loading members from an IBM i export, a database, or
 * pre-extracted flat files).
 */
public interface RpgSourceProvider {

    /**
     * Returns the entire RPG source for the given file identifier, or null if
     * the source is not available.
     */
    String getSource(String fileId);

    /**
     * Returns a snippet of the RPG source spanning the given (1-based) line
     * range. Default implementations may delegate to {@link #getSource(String)}
     * and slice the text.
     */
    default String getSnippet(String fileId, int startLine, int endLine) {
        String full = getSource(fileId);
        if (full == null) {
            return null;
        }
        String[] lines = full.split("\\R", -1);
        int from = Math.max(1, startLine);
        int to = Math.min(lines.length, endLine);
        StringBuilder sb = new StringBuilder();
        for (int i = from; i <= to; i++) {
            sb.append(lines[i - 1]).append(System.lineSeparator());
        }
        return sb.toString();
    }
}

