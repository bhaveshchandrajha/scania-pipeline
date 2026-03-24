package com.scania.warranty.web;

import com.scania.warranty.config.DataInitializer;
import com.scania.warranty.service.SeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Manual seed endpoint for demo data. Call POST /api/seed when invoices/claims are missing.
 * If HSAHKLF3 (invoices) is empty, call POST /api/seed-invoices first.
 */
@RestController
@RequestMapping("/api")
public class SeedController {

    private final DataInitializer dataInitializer;
    private final SeedService seedService;

    public SeedController(DataInitializer dataInitializer, SeedService seedService) {
        this.dataInitializer = dataInitializer;
        this.seedService = seedService;
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed() {
        try {
            dataInitializer.seedData();
            return ResponseEntity.ok(Map.of("status", "ok", "message", "Demo data seeded successfully"));
        } catch (Exception e) {
            String hint = "";
            if (e.getMessage() != null && e.getMessage().contains("invoice")) {
                hint = " Try: delete ./data/warranty_db* and restart, then POST /api/seed again.";
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", e.getMessage() + hint));
        }
    }

    /** Seed only invoices (HSAHKLF3). Use when claims exist but invoices table is empty. */
    @PostMapping("/seed-invoices")
    public ResponseEntity<Map<String, Object>> seedInvoices() {
        try {
            int created = seedService.seedInvoices();
            return ResponseEntity.ok(Map.of("status", "ok", "invoicesCreated", created,
                    "message", "Created " + created + " invoices in HSAHKLF3"));
        } catch (Exception e) {
            String hint = " If schema mismatch: stop app, delete ./data/warranty_db and warranty_db.mv.db, restart.";
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", e.getMessage() + hint));
        }
    }
}
