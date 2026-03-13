package com.scania.warranty.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * Ensures data dir exists and logs H2 Console URL.
 * File-based H2: console uses same embedded URL as app (no TCP).
 */
@Configuration
@Profile("!rds")
public class H2ConsoleConfig {

    @Bean
    ApplicationRunner h2ConsoleSetup(Environment env) {
        return args -> {
            String url = env.getProperty("spring.datasource.url", "");
            if (url.contains("jdbc:h2:file:")) {
                new File(System.getProperty("user.dir"), "data").mkdirs();
                String jdbcUrl = url.split(";")[0];
                System.out.println("H2 Console: http://localhost:8081/h2-connect.html");
                System.out.println("  JDBC URL: " + jdbcUrl);
                System.out.println("  User: sa, Password: (empty), Driver: Generic H2 (Embedded)");
            }
        };
    }
}
