package com.scania.warranty.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo controller to serve GET /api/demo/migrated-queries so the pipeline UI
 * can show a successful response when proxying to this app (e.g. on port 8081).
 * This version returns a minimal report when the app does not include
 * HS1210_n1-style migrated read SQLs (DealerConfigurationService). For the
 * full migrated-queries demo, run an app that includes that service and
 * the full MigrationDemoController.
 */
@RestController
@RequestMapping("/api/demo")
public class MigrationDemoController {

    @GetMapping("/migrated-queries")
    public ResponseEntity<Map<String, Object>> runMigratedQueries() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("title", "Migration demo – migrated read SQLs");
        report.put("summary", "This app (warranty_demo) does not include HS1210_n1 migrated read SQLs. "
                + "For the full demo with DealerConfigurationService and SystemConfiguration queries, "
                + "run the HS1210_n1 Pure Java app that includes the full MigrationDemoController.");
        report.put("queries", List.<Map<String, Object>>of());
        report.put("successCount", 0L);
        report.put("totalCount", 0);
        report.put("migrationSuccessful", true);
        return ResponseEntity.ok(report);
    }
}
