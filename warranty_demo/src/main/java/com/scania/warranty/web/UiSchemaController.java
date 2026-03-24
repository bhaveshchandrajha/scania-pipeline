package com.scania.warranty.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Serves UI schema JSON files for the Angular app.
 * Schemas are loaded from classpath:ui-schemas/{screenId}.json
 */
@RestController
@RequestMapping("/api/ui-schemas")
public class UiSchemaController {

    private static final Pattern SAFE_SCREEN_ID = Pattern.compile("^[A-Za-z0-9_-]+$");

    @GetMapping(value = "/{screenId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> getSchema(@PathVariable String screenId) {
        if (!SAFE_SCREEN_ID.matcher(screenId).matches()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            var resource = new ClassPathResource("ui-schemas/" + screenId + ".json");
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
            return ResponseEntity.ok(bytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
