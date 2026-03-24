package com.scania.warranty.web;

import com.scania.warranty.service.DealerConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Demo controller to execute migrated read SQLs and show successful migration to the client.
 * After Maven build and running the app, open:
 *   GET /api/demo/migrated-queries  — runs all migrated read queries and returns a status report.
 */
@RestController
@RequestMapping("/api/demo")
public class MigrationDemoController {

    private final DealerConfigurationService dealerConfigurationService;

    public MigrationDemoController(DealerConfigurationService dealerConfigurationService) {
        this.dealerConfigurationService = dealerConfigurationService;
    }

    /**
     * Execute all migrated read SQLs and return a report. Use this URL to show the client
     * that the migration was successful (each query runs and returns a result or graceful empty).
     * Example: GET http://localhost:8081/api/demo/migrated-queries
     */
    @GetMapping("/migrated-queries")
    public ResponseEntity<Map<String, Object>> runMigratedQueries() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("title", "HS1210_n1 migrated read SQLs – execution report");
        report.put("summary", "Each entry below corresponds to a read SQL migrated from RPG to Spring Data JPA @Query.");

        List<Map<String, Object>> queries = new java.util.ArrayList<>();

        // 1. SELECT ... FROM SystemConfiguration WHERE key = '1'
        Map<String, Object> q1 = runQuery("findDefaultConfiguration", "SELECT s FROM SystemConfiguration s WHERE s.key = '1'", () -> {
            Optional<String> dealerId = dealerConfigurationService.getDefaultDealerId();
            int maxDays = dealerConfigurationService.getMaxClaimAgeDays();
            return Map.of("defaultDealerId", dealerId.orElse(null), "maxClaimAgeDays", maxDays);
        });
        queries.add(q1);

        // 2. SELECT UPPER(:text) FROM SYSIBM.SYSDUMMY1
        Map<String, Object> q2 = runQuery("toUpperCase", "SELECT UPPER(:text) FROM SYSIBM.SYSDUMMY1", () -> {
            String result = dealerConfigurationService.toUpperCase("hello");
            return Map.of("input", "hello", "result", result);
        });
        queries.add(q2);

        // 3. SELECT wkt_sid FROM hswktf WHERE wkt_id = :id
        Map<String, Object> q3 = runQuery("findWorkTicketSidById", "SELECT wkt_sid FROM hswktf WHERE wkt_id = :id", () -> {
            Optional<Long> sid = dealerConfigurationService.findWorkTicketSid(1L);
            return Map.of("workTicketId", 1, "wkt_sid", sid.orElse(null));
        });
        queries.add(q3);

        // 4. SELECT LISTAGG(...) FROM HSGPSPF WHERE ...
        Map<String, Object> q4 = runQuery("findAggregatedPositionsByDealerAndClaim",
                "SELECT LISTAGG(DIGITS(GPS030) CONCAT DIGITS(GPS150)) ... FROM HSGPSPF WHERE GPS000 = :dealerId AND GPS010 = :claimNo",
                () -> {
                    Optional<String> list = dealerConfigurationService.findAggregatedPositions("00001", "CLAIM001");
                    return Map.of("dealerId", "00001", "claimNo", "CLAIM001", "aggregatedPositions", list.orElse(null));
                });
        queries.add(q4);

        report.put("queries", queries);

        long ok = queries.stream().filter(q -> "OK".equals(q.get("status"))).count();
        report.put("successCount", ok);
        report.put("totalCount", queries.size());
        report.put("migrationSuccessful", ok == queries.size());

        return ResponseEntity.ok(report);
    }

    /**
     * Individual endpoints for each migrated query (optional; useful for Swagger/Postman).
     */
    @GetMapping("/default-config")
    public ResponseEntity<Map<String, Object>> defaultConfig() {
        Optional<String> dealerId = dealerConfigurationService.getDefaultDealerId();
        int maxDays = dealerConfigurationService.getMaxClaimAgeDays();
        return ResponseEntity.ok(Map.of(
                "defaultDealerId", dealerId.orElse("(none)"),
                "maxClaimAgeDays", maxDays,
                "query", "findDefaultConfiguration"
        ));
    }

    @GetMapping("/uppercase")
    public ResponseEntity<Map<String, Object>> uppercase(@RequestParam(defaultValue = "hello") String text) {
        String result = dealerConfigurationService.toUpperCase(text);
        return ResponseEntity.ok(Map.of("input", text, "result", result != null ? result : "", "query", "toUpperCase"));
    }

    @GetMapping("/workticket-sid")
    public ResponseEntity<Map<String, Object>> workTicketSid(@RequestParam(defaultValue = "1") Long id) {
        Optional<Long> sid = dealerConfigurationService.findWorkTicketSid(id);
        return ResponseEntity.ok(Map.of("workTicketId", id, "wkt_sid", sid.orElse(null), "query", "findWorkTicketSidById"));
    }

    @GetMapping("/aggregated-positions")
    public ResponseEntity<Map<String, Object>> aggregatedPositions(
            @RequestParam(defaultValue = "00001") String dealerId,
            @RequestParam(defaultValue = "CLAIM001") String claimNo) {
        Optional<String> list = dealerConfigurationService.findAggregatedPositions(dealerId, claimNo);
        return ResponseEntity.ok(Map.of(
                "dealerId", dealerId,
                "claimNo", claimNo,
                "aggregatedPositions", list.orElse(null),
                "query", "findAggregatedPositionsByDealerAndClaim"
        ));
    }

    private Map<String, Object> runQuery(String name, String sqlDescription, Supplier<Map<String, Object>> action) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("name", name);
        entry.put("sql", sqlDescription);
        try {
            Map<String, Object> result = action.get();
            entry.put("status", "OK");
            entry.put("result", result);
        } catch (Exception e) {
            entry.put("status", "ERROR");
            entry.put("error", e.getMessage());
        }
        return entry;
    }
}
