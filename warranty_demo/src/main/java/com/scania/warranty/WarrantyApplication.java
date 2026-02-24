package com.scania.warranty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot Application for Warranty Claim Management System
 * 
 * This is the main entry point for the Pure Java application migrated from RPG.
 * 
 * To run:
 *   mvn spring-boot:run
 * 
 * Or:
 *   java -jar target/warranty-claim-management-1.0.0.jar
 * 
 * The application will start on http://localhost:8081
 */
@SpringBootApplication(scanBasePackages = "com.scania.warranty")
@EntityScan(basePackages = "com.scania.warranty.domain")
@EnableJpaRepositories(basePackages = "com.scania.warranty.repository")
public class WarrantyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WarrantyApplication.class, args);
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Warranty Claim Management System");
        System.out.println("=".repeat(70));
        System.out.println("Application started successfully!");
        System.out.println("API Documentation: http://localhost:8081/api/claims");
        System.out.println("Demo UI: http://localhost:8081/demo.html");
        System.out.println("=".repeat(70) + "\n");
    }
}
