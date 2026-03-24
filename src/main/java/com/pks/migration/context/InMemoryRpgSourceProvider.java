package com.pks.migration.context;

import java.util.Map;

/**
 * Simple in-memory implementation usable for testing and local experimentation.
 */
public class InMemoryRpgSourceProvider implements RpgSourceProvider {

    private final Map<String, String> sourcesByFileId;

    public InMemoryRpgSourceProvider(Map<String, String> sourcesByFileId) {
        this.sourcesByFileId = Map.copyOf(sourcesByFileId);
    }

    @Override
    public String getSource(String fileId) {
        return sourcesByFileId.get(fileId);
    }
}

