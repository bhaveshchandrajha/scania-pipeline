package com.scania.warranty.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves Angular index.html for /angular and /angular/ so the SPA loads correctly.
 * Spring's resource handler does not serve index.html for directory paths in some configurations.
 */
@RestController
public class AngularIndexController {

    @GetMapping(value = {"/angular", "/angular/"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> angularIndex() {
        Resource resource = new ClassPathResource("static/angular/index.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(resource);
    }
}
