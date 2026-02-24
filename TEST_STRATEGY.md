# Scania Claims Module Test Strategy

## Executive Summary

This document outlines a comprehensive testing strategy for the Scania Claims Module (HS1210) migrated to Pure Java. The strategy covers 6 client-provided test scenarios, mapping RPG concepts to Java equivalents, and establishing a multi-layered testing approach.

---

## 1. Test Architecture Overview

### 1.1 Testing Pyramid

```
                    ┌─────────────────┐
                    │   E2E Tests     │  (6 scenarios)
                    │  (REST API)    │
                    └─────────────────┘
                  ┌─────────────────────┐
                  │ Integration Tests   │  (Cross-module)
                  │  (Mocked Services)  │
                  └─────────────────────┘
            ┌─────────────────────────────────┐
            │      Unit Tests                 │
            │  (Services, Repositories, DTOs)  │
            └─────────────────────────────────┘
```

### 1.2 Test Layers

1. **Unit Tests** (`*Test.java`): Test individual components in isolation
   - Service methods
   - Repository queries
   - DTO validation
   - Business logic

2. **Integration Tests** (`*IT.java`): Test component interactions
   - Service + Repository (with test database)
   - Controller + Service (with mocked dependencies)
   - Cross-module communication (with mocked external services)

3. **End-to-End Tests** (`*E2ETest.java`): Test full workflows
   - REST API calls simulating user actions
   - Database state verification
   - External service mocking (WireMock/MockServer)

---

## 2. Mapping RPG Concepts to Java Test Equivalents

| RPG Concept | Java Equivalent | Test Approach |
|-------------|----------------|---------------|
| **Subroutine (SR09, SR20, etc.)** | Service method | Unit test with mocked dependencies |
| **External Program Call (HS1212, HS1220)** | External Service/API | Mock with `@MockBean` or WireMock |
| **Database File (HSG71PF, AUFWKO)** | JPA Repository | Test with H2 in-memory DB or `@DataJpaTest` |
| **Screen Interaction (Selection 6, F20)** | REST API endpoint | E2E test with HTTP client |
| **Status Code Table (HSGSCPF)** | Enum/Entity | Test with test data fixtures |
| **Modulus Check (HS0012)** | Validation Service | Unit test with various inputs |
| **Factory Interface (WP_SC01)** | External API Client | Mock HTTP responses |
| **Visual Indicator (SR_FARBE)** | DTO field/Response attribute | Assert response JSON |

---

## 3. Test Scenario Breakdown

### Scenario 1: Full Warranty Claim Lifecycle (Happy Path)

**RPG Flow:**
- Selection 6 (ERSTELLEN) → HS1212 (Claim Editor) → Selection 2 (ÄNDERN) → Selection 9 (STATUS ÄNDERN) → HS1220 (Status Manager) → Selection 10 (DIREKT VERSENDEN) → WP_SC01 (Factory Interface)

**Java Test Structure:**

```java
@SpringBootTest
@AutoConfigureMockMvc
class ClaimLifecycleE2ETest {
    
    @MockBean
    private ClaimEditorService claimEditorService;  // HS1212 mock
    
    @MockBean
    private StatusManagerService statusManagerService;  // HS1220 mock
    
    @MockBean
    private FactoryInterfaceClient factoryInterfaceClient;  // WP_SC01 mock
    
    @Autowired
    private ClaimRepository claimRepository;
    
    @Test
    void testFullClaimLifecycle() {
        // Given
        VehicleMaster vehicle = createVehicleMaster("WDB123456");
        when(claimEditorService.createClaim(any())).thenReturn(SUCCESS);
        when(statusManagerService.transitionStatus(any(), eq("READY"))).thenReturn(SUCCESS);
        when(factoryInterfaceClient.transmitClaim(any())).thenReturn(TransmissionResult.ok());
        
        // When
        ClaimCreationRequest request = new ClaimCreationRequest("WDB123456");
        ResponseEntity<ClaimDto> createResponse = restTemplate.postForEntity(
            "/api/claims/create", request, ClaimDto.class);
        
        ClaimDto claim = createResponse.getBody();
        
        // Edit claim
        ClaimUpdateRequest updateRequest = new ClaimUpdateRequest(...);
        restTemplate.put("/api/claims/" + claim.getId(), updateRequest);
        
        // Change status to READY
        restTemplate.postForEntity(
            "/api/claims/" + claim.getId() + "/status/READY", null, Void.class);
        
        // Transmit claim
        restTemplate.postForEntity(
            "/api/claims/" + claim.getId() + "/transmit", null, TransmissionResult.class);
        
        // Then
        Claim savedClaim = claimRepository.findById(claim.getId()).orElseThrow();
        assertThat(savedClaim.getStatus()).isEqualTo(ClaimStatus.SENT);
        verify(factoryInterfaceClient, times(1)).transmitClaim(any());
    }
}
```

**Test Files:**
- `ClaimLifecycleE2ETest.java` (E2E)
- `ClaimCreationServiceTest.java` (Unit)
- `StatusTransitionServiceTest.java` (Unit)
- `FactoryInterfaceClientTest.java` (Unit with mocked HTTP)

---

### Scenario 2: VIN Entry Validation (Integrity Check)

**RPG Flow:**
- Selection 6 → VIN input → HS0012 (Modulus 10 Check) → Error if invalid

**Java Test Structure:**

```java
@SpringBootTest
class VinValidationTest {
    
    @MockBean
    private ModulusCheckService modulusCheckService;  // HS0012 mock
    
    @Test
    void testInvalidVinRejected() {
        // Given
        when(modulusCheckService.validateVin("BAD_VIN")).thenReturn(ValidationResult.invalid("Prüfziffer falsch"));
        
        // When
        ClaimCreationRequest request = new ClaimCreationRequest("BAD_VIN");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/claims/create", request, ErrorResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("Prüfziffer falsch");
        assertThat(claimRepository.count()).isEqualTo(0);
    }
    
    @Test
    void testValidVinAccepted() {
        // Given
        when(modulusCheckService.validateVin("GOOD_VIN")).thenReturn(ValidationResult.valid());
        
        // When
        ClaimCreationRequest request = new ClaimCreationRequest("GOOD_VIN");
        ResponseEntity<ClaimDto> response = restTemplate.postForEntity(
            "/api/claims/create", request, ClaimDto.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getVin()).isEqualTo("GOOD_VIN");
    }
}
```

**Test Files:**
- `VinValidationE2ETest.java` (E2E)
- `ModulusCheckServiceTest.java` (Unit - test algorithm)
- `ClaimCreationServiceTest.java` (Unit - test validation integration)

---

### Scenario 3: Minimum Value Claim Auto-Write-Off

**RPG Flow:**
- Claim with value < threshold → HS1219M (Write-Off Routine) → Status = CLOSED → No WP_SC01 call

**Java Test Structure:**

```java
@SpringBootTest
class MinimumValueWriteOffTest {
    
    @MockBean
    private WriteOffService writeOffService;  // HS1219M mock
    
    @MockBean
    private FactoryInterfaceClient factoryInterfaceClient;  // WP_SC01 mock
    
    @Value("${claim.minimum-threshold:50.00}")
    private BigDecimal minimumThreshold;
    
    @Test
    void testLowValueClaimAutoWriteOff() {
        // Given
        Claim claim = createClaimWithValue(new BigDecimal("15.00"));
        when(writeOffService.writeOffClaim(any())).thenReturn(WriteOffResult.success());
        
        // When
        restTemplate.postForEntity(
            "/api/claims/" + claim.getId() + "/write-off", null, Void.class);
        
        // Then
        Claim updatedClaim = claimRepository.findById(claim.getId()).orElseThrow();
        assertThat(updatedClaim.getStatus()).isEqualTo(ClaimStatus.CLOSED);
        assertThat(updatedClaim.getStatus()).isEqualTo(ClaimStatus.WRITTEN_OFF);
        verify(factoryInterfaceClient, never()).transmitClaim(any());
        verify(writeOffService, times(1)).writeOffClaim(any());
    }
}
```

**Test Files:**
- `MinimumValueWriteOffE2ETest.java` (E2E)
- `WriteOffServiceTest.java` (Unit)
- `ClaimThresholdConfigurationTest.java` (Unit - test threshold logic)

---

### Scenario 4: Bulk Import of Open Workshop Invoices

**RPG Flow:**
- F20 (ALLE OFFENEN RECHNUNGEN ÜBERNEHMEN) → SR20 (Database Cursor) → Query AUFWKO → Create claims

**Java Test Structure:**

```java
@SpringBootTest
@Transactional
class BulkInvoiceImportTest {
    
    @Autowired
    private WorkshopOrderRepository workshopOrderRepository;  // AUFWKO equivalent
    
    @Autowired
    private ClaimRepository claimRepository;
    
    @Test
    void testBulkImportWorkshopInvoices() {
        // Given
        List<WorkshopOrder> orders = create5OpenWorkshopOrders();
        orders.forEach(workshopOrderRepository::save);
        
        // When
        ResponseEntity<BulkImportResult> response = restTemplate.postForEntity(
            "/api/claims/import-workshop-invoices", null, BulkImportResult.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getImportedCount()).isEqualTo(5);
        assertThat(claimRepository.count()).isEqualTo(5);
        
        List<Claim> importedClaims = claimRepository.findAll();
        assertThat(importedClaims).allMatch(c -> c.getSource() == ClaimSource.WORKSHOP_INVOICE);
    }
}
```

**Test Files:**
- `BulkInvoiceImportE2ETest.java` (E2E)
- `WorkshopInvoiceImportServiceTest.java` (Unit)
- `WorkshopOrderRepositoryTest.java` (`@DataJpaTest`)

---

### Scenario 5: Submission Deadline Extension Request

**RPG Flow:**
- Expired claim → SR_G70 (Deadline Authority) → Request extension → Update SubmissionDeadline

**Java Test Structure:**

```java
@SpringBootTest
class DeadlineExtensionTest {
    
    @MockBean
    private DeadlineAuthorityService deadlineAuthorityService;  // SR_G70 mock
    
    @Test
    void testDeadlineExtensionRequest() {
        // Given
        Claim expiredClaim = createExpiredClaim();
        LocalDate newDeadline = LocalDate.now().plusDays(30);
        when(deadlineAuthorityService.requestExtension(any()))
            .thenReturn(ExtensionResult.authorized(newDeadline));
        
        // When
        ResponseEntity<ClaimDto> response = restTemplate.postForEntity(
            "/api/claims/" + expiredClaim.getId() + "/request-deadline-extension",
            null, ClaimDto.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Claim updatedClaim = claimRepository.findById(expiredClaim.getId()).orElseThrow();
        assertThat(updatedClaim.getSubmissionDeadline()).isEqualTo(newDeadline);
        assertThat(updatedClaim.isExpired()).isFalse();
    }
}
```

**Test Files:**
- `DeadlineExtensionE2ETest.java` (E2E)
- `DeadlineAuthorityServiceTest.java` (Unit)
- `ReleaseRequestRepositoryTest.java` (`@DataJpaTest` - HSG70F)

---

### Scenario 6: Rejected Claim Visual Indicator

**RPG Flow:**
- SR09 (LISTENANFANG) → Load claims → SR_FARBE → Check status → Apply red color

**Java Test Structure:**

```java
@SpringBootTest
class RejectedClaimVisualIndicatorTest {
    
    @Autowired
    private ClaimStatusRepository claimStatusRepository;  // HSGSCPF equivalent
    
    @Test
    void testRejectedClaimDisplayedInRed() {
        // Given
        Claim rejectedClaim = createRejectedClaim("REJ");
        ClaimStatus status = new ClaimStatus("REJ", "Rejected", StatusColor.RED);
        claimStatusRepository.save(status);
        
        // When
        ResponseEntity<ClaimListResponse> response = restTemplate.getForEntity(
            "/api/claims/list", ClaimListResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ClaimListItemDto rejectedItem = response.getBody().getClaims().stream()
            .filter(c -> c.getId().equals(rejectedClaim.getId()))
            .findFirst()
            .orElseThrow();
        
        assertThat(rejectedItem.getStatusColor()).isEqualTo(StatusColor.RED);
        assertThat(rejectedItem.getStatusCode()).isEqualTo("REJ");
        assertThat(rejectedItem.isHighlighted()).isTrue();
    }
}
```

**Test Files:**
- `RejectedClaimVisualIndicatorE2ETest.java` (E2E)
- `ClaimListServiceTest.java` (Unit - test SR_FARBE logic)
- `ClaimStatusRepositoryTest.java` (`@DataJpaTest`)

---

## 4. Mocking Strategy

### 4.1 External Services (RPG Programs)

| External Service | Mock Strategy | Test Double Type |
|------------------|---------------|------------------|
| **HS1212 (Claim Editor)** | `@MockBean ClaimEditorService` | Spring Mock Bean |
| **HS1220 (Status Manager)** | `@MockBean StatusManagerService` | Spring Mock Bean |
| **HS0012 (Modulus Check)** | `@MockBean ModulusCheckService` | Spring Mock Bean |
| **HS1219M (Write-Off)** | `@MockBean WriteOffService` | Spring Mock Bean |
| **SR_G70 (Deadline Authority)** | `@MockBean DeadlineAuthorityService` | Spring Mock Bean |
| **WP_SC01 (Factory Interface)** | WireMock Server | HTTP Mock Server |

### 4.2 Database Files

- **HSG71PF (Claims)**: `ClaimRepository` with H2 in-memory DB
- **AUFWKO (Workshop Orders)**: `WorkshopOrderRepository` with H2
- **HSGSCPF (Status Codes)**: `ClaimStatusRepository` with test data fixtures
- **HSG70F (Release Requests)**: `ReleaseRequestRepository` with H2

### 4.3 Configuration

```java
@SpringBootTest
@TestPropertySource(properties = {
    "claim.minimum-threshold=50.00",
    "factory-interface.enabled=true",
    "factory-interface.url=http://localhost:${wiremock.server.port}"
})
class BaseE2ETest {
    // Base test configuration
}
```

---

## 5. Test Data Management

### 5.1 Test Fixtures

Create test data builders:

```java
public class ClaimTestDataBuilder {
    public static Claim.Builder aClaim() {
        return Claim.builder()
            .vin("WDB123456")
            .status(ClaimStatus.OPEN)
            .totalValue(new BigDecimal("100.00"))
            .submissionDeadline(LocalDate.now().plusDays(30));
    }
    
    public static Claim.Builder anExpiredClaim() {
        return aClaim()
            .submissionDeadline(LocalDate.now().minusDays(10));
    }
    
    public static Claim.Builder aLowValueClaim() {
        return aClaim()
            .totalValue(new BigDecimal("15.00"));
    }
    
    public static Claim.Builder aRejectedClaim() {
        return aClaim()
            .status(ClaimStatus.REJECTED);
    }
}
```

### 5.2 Database Cleanup

```java
@SpringBootTest
@Transactional
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
class BaseIntegrationTest {
    // Base integration test
}
```

---

## 6. Test Organization

### 6.1 Directory Structure

```
src/test/java/com/scania/warranty/
├── unit/
│   ├── service/
│   │   ├── ClaimCreationServiceTest.java
│   │   ├── ModulusCheckServiceTest.java
│   │   └── WriteOffServiceTest.java
│   ├── repository/
│   │   └── ClaimRepositoryTest.java
│   └── dto/
│       └── ClaimCreationRequestDtoTest.java
├── integration/
│   ├── ClaimServiceIntegrationTest.java
│   └── ClaimControllerIntegrationTest.java
└── e2e/
    ├── ClaimLifecycleE2ETest.java
    ├── VinValidationE2ETest.java
    ├── MinimumValueWriteOffE2ETest.java
    ├── BulkInvoiceImportE2ETest.java
    ├── DeadlineExtensionE2ETest.java
    └── RejectedClaimVisualIndicatorE2ETest.java
```

### 6.2 Test Naming Convention

- **Unit Tests**: `{ClassName}Test.java`
- **Integration Tests**: `{ClassName}IT.java` or `{Feature}IntegrationTest.java`
- **E2E Tests**: `{Scenario}E2ETest.java`

---

## 7. Assertion Strategy

### 7.1 Response Assertions

```java
// Status code
assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

// Response body
assertThat(response.getBody())
    .extracting(ClaimDto::getStatus, ClaimDto::getVin)
    .containsExactly(ClaimStatus.OPEN, "WDB123456");

// Database state
assertThat(claimRepository.findById(claimId))
    .isPresent()
    .get()
    .extracting(Claim::getStatus)
    .isEqualTo(ClaimStatus.SENT);
```

### 7.2 Mock Verification

```java
verify(factoryInterfaceClient, times(1)).transmitClaim(any());
verify(factoryInterfaceClient, never()).transmitClaim(any());
verify(modulusCheckService).validateVin("BAD_VIN");
```

---

## 8. Continuous Integration Strategy

### 8.1 Test Execution Order

1. **Unit Tests** (fastest, run first)
2. **Integration Tests** (medium speed)
3. **E2E Tests** (slowest, run last)

### 8.2 Maven Configuration

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*IT.java</include>
        </includes>
        <excludes>
            <exclude>**/*E2ETest.java</exclude>
        </excludes>
    </configuration>
</plugin>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*E2ETest.java</include>
        </includes>
    </configuration>
</plugin>
```

---

## 9. Coverage Goals

- **Unit Tests**: 80%+ line coverage
- **Integration Tests**: All service interactions
- **E2E Tests**: All 6 client scenarios

---

## 10. Next Steps

1. **Create Test Infrastructure**
   - Set up WireMock for external services
   - Create test data builders
   - Configure H2 test database

2. **Implement Unit Tests First**
   - Start with core services (ClaimCreationService, ModulusCheckService)
   - Add repository tests
   - Add DTO validation tests

3. **Add Integration Tests**
   - Service + Repository integration
   - Controller + Service integration

4. **Implement E2E Tests**
   - Start with Scenario 1 (Happy Path)
   - Add remaining 5 scenarios incrementally

5. **CI/CD Integration**
   - Configure test execution in pipeline
   - Add coverage reporting
   - Set up test result dashboards

---

## 11. Dependencies Required

```xml
<dependencies>
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.wiremock</groupId>
        <artifactId>wiremock-standalone</artifactId>
        <version>3.0.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 12. Risk Mitigation

| Risk | Mitigation |
|------|------------|
| **External service unavailability** | Use WireMock/MockServer for all external calls |
| **Test data pollution** | Use `@Transactional` and `@DirtiesContext` |
| **Flaky tests** | Use test containers for consistent DB state |
| **Slow E2E tests** | Run in parallel, use test profiles |
| **Missing test coverage** | Set coverage thresholds in CI |

---

## Appendix: RPG → Java Mapping Reference

| RPG Component | Java Component | Test File |
|---------------|----------------|-----------|
| HS1210 (Main Menu) | `ClaimController` | `ClaimControllerIT.java` |
| n404 (Subroutine) | `ClaimSubfileService` | `ClaimSubfileServiceTest.java` |
| SR09 (LISTENANFANG) | `loadClaimList()` | `ClaimListServiceTest.java` |
| SR20 (Invoice Cursor) | `WorkshopInvoiceImportService` | `WorkshopInvoiceImportServiceTest.java` |
| SR_FARBE (Color Check) | `applyStatusColor()` | `ClaimListServiceTest.java` |
| SR_G70 (Deadline) | `DeadlineAuthorityService` | `DeadlineAuthorityServiceTest.java` |
| SR_MINIMUM (Write-Off) | `WriteOffService` | `WriteOffServiceTest.java` |
| HSG71PF (Claims File) | `ClaimRepository` | `ClaimRepositoryTest.java` |
| HSGSCPF (Status Codes) | `ClaimStatusRepository` | `ClaimStatusRepositoryTest.java` |
| AUFWKO (Workshop Orders) | `WorkshopOrderRepository` | `WorkshopOrderRepositoryTest.java` |
