# Converting RPG-Native Java to Pure Java Application

**Date:** February 13, 2026  
**Goal:** Transform the current RPG-mapped Java code into a complete, production-ready Pure Java application following modern Java/Spring best practices.

---

## 1. Understanding the Current State: "RPG-Native Java"

### 1.1 What "RPG-Native Java" Means

The current migrated code (`HS1210_n404.java`) is **functionally correct** but follows RPG patterns and idioms:

**RPG-Native Characteristics:**

1. **Subroutine-to-Method Mapping**
   ```java
   // RPG: Subroutine SB10N
   public void initializeSubfileProcessing(SubfileContext context) {
       // Direct translation of RPG subroutine
   }
   ```

2. **Indicator-Based State Management**
   ```java
   context.setIndicator50(false);
   context.setIndicator51(false);
   // ... 9 more indicators
   // RPG uses indicators (01-99) for control flow
   ```

3. **RPG-Style Variable Names**
   ```java
   context.setZl4(0);        // RPG-style: ZL4 (zero-length 4)
   context.setSub15x("");     // RPG-style: SUB15X
   context.setMark12("");     // RPG-style: MARK12
   ```

4. **Magic Strings and Codes**
   ```java
   if ("J".equals(filter.getFiloff())) {  // "J" = German "Ja" (Yes)
       // Open claims only
   }
   if (claim.getStatusCodeSde() == 99) {  // Magic number
       // Skip certain status
   }
   ```

5. **Sequential, Step-by-Step Logic**
   ```java
   // Mirrors RPG's sequential execution
   result.setZl1(0);
   result.setZl2(0);
   List<HSG71LF2> claims;
   if (ascending) {
       if (filter.isUseLogicalFile()) {
           claims = hsg71Repository.findByPakzOrderByClaimNrAsc(pkz);
       } else {
           claims = hsg71Repository.findByPakzOrderByRechNrAsc(pkz);
       }
   }
   ```

6. **Large Service Classes**
   - Single service class with many methods (mirrors RPG program structure)
   - Methods often correspond 1:1 to RPG subroutines
   - Business logic, data access, and state management mixed together

### 1.2 What's Good About Current Code

✅ **Correctness:**
- All database columns mapped correctly (`@Column(name="...")`)
- No schema hallucination
- Business logic preserved accurately

✅ **Functionality:**
- Works correctly
- Uses Spring annotations (`@Service`, `@Transactional`)
- Uses JPA repositories
- Type-safe (uses `Optional`, `BigDecimal`, etc.)

✅ **Traceability:**
- Clear mapping from RPG to Java
- Comments reference original RPG subroutines
- Easy to verify correctness

---

## 2. What "Pure Java" Means

### 2.1 Pure Java Characteristics

**Pure Java Application:**

1. **Layered Architecture**
   ```
   domain/          - Entities, value objects, domain models
   service/         - Business logic, use cases
   repository/     - Data access (JPA repositories)
   web/            - REST controllers, DTOs
   dto/            - Request/response objects
   exception/      - Custom exceptions
   config/         - Configuration classes
   ```

2. **Domain-Driven Design**
   ```java
   // Instead of: SubfileContext (RPG concept)
   // Use: ClaimListState or ClaimSearchCriteria (domain concept)
   
   // Instead of: HSG71LF2 (file name)
   // Use: Claim or WarrantyClaim (domain entity)
   ```

3. **Modern Java Features**
   ```java
   // Use Java Records for DTOs
   public record ClaimSearchCriteria(
       String claimNumber,
       LocalDate fromDate,
       ClaimStatus status
   ) {}
   
   // Use Optional properly
   Optional<Claim> claim = claimRepository.findById(id);
   return claim.map(this::toDto).orElseThrow(ClaimNotFoundException::new);
   
   // Use Streams for data processing
   List<ClaimDto> claims = claimRepository.findAll()
       .stream()
       .filter(this::isOpenClaim)
       .map(this::toDto)
       .collect(Collectors.toList());
   ```

4. **Clear Separation of Concerns**
   ```java
   // Service: Business logic only
   @Service
   public class ClaimService {
       public ClaimDto findClaim(String id) {
           // Business logic here
       }
   }
   
   // Repository: Data access only
   public interface ClaimRepository extends JpaRepository<Claim, String> {
       List<Claim> findByStatus(ClaimStatus status);
   }
   
   // Controller: HTTP handling only
   @RestController
   public class ClaimController {
       @GetMapping("/claims/{id}")
       public ResponseEntity<ClaimDto> getClaim(@PathVariable String id) {
           // HTTP handling here
       }
   }
   ```

5. **Domain Language**
   ```java
   // Instead of: initializeSubfileProcessing()
   // Use: initializeClaimListSearch()
   
   // Instead of: buildClaimSubfile()
   // Use: searchClaims() or findClaims()
   
   // Instead of: processMarkSelection()
   // Use: handleClaimSelection() or selectClaim()
   ```

6. **Proper Error Handling**
   ```java
   // Instead of: silent returns or magic numbers
   // Use: Custom exceptions
   if (claim == null) {
       throw new ClaimNotFoundException("Claim not found: " + id);
   }
   ```

---

## 3. Conversion Strategy: RPG-Native → Pure Java

### 3.1 Phase 1: Analysis and Planning

**Step 1: Identify Domain Concepts**

From RPG code, extract domain concepts:

| RPG Concept | Pure Java Domain Concept |
|------------|--------------------------|
| `HSG71LF2` (file) | `Claim` or `WarrantyClaim` (entity) |
| `SubfileContext` | `ClaimListState` or `ClaimSearchCriteria` |
| `SubfileResult` | `ClaimSearchResult` or `Page<ClaimDto>` |
| `SubfileRecord` | `ClaimListItemDto` |
| `initializeSubfileProcessing()` | `initializeClaimSearch()` |
| `buildClaimSubfile()` | `searchClaims()` or `findClaims()` |
| Indicator 50-58 | Enum: `ClaimListFlag` or separate boolean fields |
| Magic string "J" | Enum: `FilterOption.OPEN_CLAIMS_ONLY` |
| Status code 99 | Enum: `ClaimStatus.EXCLUDED` |

**Step 2: Design Target Architecture**

```
com.scania.warranty/
├── domain/
│   ├── Claim.java                    (entity)
│   ├── ClaimStatus.java              (enum)
│   ├── ClaimSearchCriteria.java      (value object)
│   └── ClaimListItem.java            (value object)
├── repository/
│   └── ClaimRepository.java          (JPA repository)
├── service/
│   ├── ClaimService.java             (business logic)
│   └── ClaimSearchService.java       (search logic)
├── dto/
│   ├── ClaimDto.java                 (response DTO)
│   ├── ClaimListItemDto.java         (list item DTO)
│   └── ClaimSearchRequest.java       (request DTO)
├── web/
│   └── ClaimController.java          (REST API)
└── exception/
    └── ClaimNotFoundException.java   (custom exception)
```

**Step 3: Create Domain Glossary**

Document the mapping:
- RPG file names → Domain entity names
- RPG subroutines → Service methods
- RPG indicators → Domain flags/enums
- RPG magic strings → Constants/enums

---

### 3.2 Phase 2: Refactoring Steps

#### Step 1: Extract Domain Entities

**Current (RPG-Native):**
```java
@Entity
@Table(name = "HSG71LF2")
public class HSG71LF2 {
    @Column(name = "PAKZ")
    private String pakz;
    
    @Column(name = "RECHNR")
    private String rechNr;
    // ...
}
```

**Pure Java:**
```java
@Entity
@Table(name = "HSG71LF2")  // Keep DB table name
public class Claim {
    @Column(name = "PAKZ")
    private String packageCode;  // Domain name, DB name preserved
    
    @Column(name = "RECHNR")
    private String claimNumber;  // Domain name, DB name preserved
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "STATUS_CODE_SDE")
    private ClaimStatus status;  // Use enum instead of int
    
    // Domain methods
    public boolean isOpen() {
        return status.isOpen();
    }
    
    public boolean isMinimumClaim() {
        return "00000000".equals(claimNumber);
    }
}
```

#### Step 2: Replace Indicators with Domain Types

**Current (RPG-Native):**
```java
public class SubfileContext {
    private boolean indicator50;
    private boolean indicator51;
    // ... 9 more indicators
}
```

**Pure Java:**
```java
public class ClaimSearchCriteria {
    private boolean showOnlyOpenClaims;
    private boolean useLogicalFile;
    private boolean applyStatusFilter;
    // ... meaningful names
    
    // Or use an enum for flags
    private Set<ClaimListFlag> flags;
}

public enum ClaimListFlag {
    SHOW_OPEN_ONLY,
    USE_LOGICAL_FILE,
    APPLY_STATUS_FILTER
}
```

#### Step 3: Replace Magic Values with Constants/Enums

**Current (RPG-Native):**
```java
if ("J".equals(filter.getFiloff())) {  // Magic string
    // Open claims
}
if (claim.getStatusCodeSde() == 99) {  // Magic number
    // Skip
}
```

**Pure Java:**
```java
public enum FilterOption {
    OPEN_CLAIMS_ONLY("J"),
    ALL_CLAIMS("N");
    
    private final String code;
    FilterOption(String code) { this.code = code; }
}

public enum ClaimStatus {
    PENDING(0),
    APPROVED(20),
    REJECTED(11),
    EXCLUDED(99);
    
    private final int code;
    ClaimStatus(int code) { this.code = code; }
    
    public boolean isOpen() {
        return code < 20 && code != 5;
    }
}

// Usage
if (filter.getOption() == FilterOption.OPEN_CLAIMS_ONLY) {
    // Open claims
}
if (claim.getStatus() == ClaimStatus.EXCLUDED) {
    // Skip
}
```

#### Step 4: Refactor Service Methods

**Current (RPG-Native):**
```java
public SubfileResult buildClaimSubfile(SubfileFilter filter, String pkz, boolean ascending) {
    SubfileResult result = new SubfileResult();
    result.setZl1(0);
    result.setZl2(0);
    // ... 200+ lines of sequential logic
}
```

**Pure Java:**
```java
@Service
public class ClaimSearchService {
    
    private final ClaimRepository claimRepository;
    private final ClaimMapper claimMapper;
    
    public ClaimSearchResult searchClaims(ClaimSearchCriteria criteria) {
        Specification<Claim> spec = buildSpecification(criteria);
        Pageable pageable = PageRequest.of(0, criteria.getPageSize());
        
        Page<Claim> claims = claimRepository.findAll(spec, pageable);
        
        return ClaimSearchResult.builder()
            .claims(claims.map(claimMapper::toListItemDto))
            .totalCount(claims.getTotalElements())
            .build();
    }
    
    private Specification<Claim> buildSpecification(ClaimSearchCriteria criteria) {
        return Specification.where(
            criteria.getPackageCode() != null 
                ? (root, query, cb) -> cb.equal(root.get("packageCode"), criteria.getPackageCode())
                : null
        ).and(
            criteria.isOpenClaimsOnly()
                ? (root, query, cb) -> cb.lessThan(root.get("status"), ClaimStatus.APPROVED)
                : null
        );
    }
}
```

#### Step 5: Separate Concerns

**Current (RPG-Native):**
- One large service class doing everything

**Pure Java:**
```java
// Domain entity
@Entity
public class Claim { /* ... */ }

// Repository (data access)
public interface ClaimRepository extends JpaRepository<Claim, String> {
    List<Claim> findByPackageCodeAndStatus(String packageCode, ClaimStatus status);
}

// Service (business logic)
@Service
public class ClaimService {
    public ClaimDto findClaim(String id) {
        Claim claim = claimRepository.findById(id)
            .orElseThrow(() -> new ClaimNotFoundException(id));
        return claimMapper.toDto(claim);
    }
}

// Controller (HTTP)
@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    @GetMapping("/{id}")
    public ResponseEntity<ClaimDto> getClaim(@PathVariable String id) {
        return ResponseEntity.ok(claimService.findClaim(id));
    }
}
```

#### Step 6: Use Modern Java Features

**Current (RPG-Native):**
```java
List<SubfileRecord> records = new ArrayList<>();
for (HSG71LF2 claim : claims) {
    if (result.getZl1() >= 9999) break;
    if (!applyClaimFilters(claim, filter)) continue;
    // ... manual filtering
    SubfileRecord record = buildSubfileRecord(claim);
    records.add(record);
}
```

**Pure Java:**
```java
List<ClaimListItemDto> items = claims.stream()
    .limit(MAX_RESULTS)
    .filter(this::matchesCriteria)
    .map(claimMapper::toListItemDto)
    .collect(Collectors.toList());
```

---

### 3.3 Phase 3: Implementation Approach

#### Option A: Manual Refactoring (Recommended for Critical Paths)

**Pros:**
- Full control over design decisions
- Learning opportunity for team
- Can apply domain expertise

**Cons:**
- Time-consuming
- Requires Java expertise

**Process:**
1. Start with one service class
2. Extract domain entities
3. Refactor methods one at a time
4. Add tests
5. Repeat for next service

#### Option B: LLM-Assisted Refactoring

**Pros:**
- Faster than manual
- Consistent style
- Can process multiple classes

**Cons:**
- Requires review
- May miss domain nuances

**Process:**
1. Create refactoring prompt:
   ```
   Refactor this RPG-native Java code into pure Java:
   - Extract domain entities (Claim instead of HSG71LF2)
   - Replace indicators with enums/flags
   - Replace magic values with constants
   - Use modern Java features (Streams, Optional, Records)
   - Separate into layers (domain, service, repository, web)
   - Keep all @Column(name="...") mappings unchanged
   ```
2. Run refactoring for each service class
3. Review and test
4. Iterate

#### Option C: Hybrid Approach (Recommended)

1. **Phase 1:** Use current RPG-native code as "source of truth"
2. **Phase 2:** Refactor critical paths manually
3. **Phase 3:** Use LLM-assisted refactoring for remaining code
4. **Phase 4:** Review, test, and standardize

---

## 4. Practical Example: Converting HS1210_n404.java

### Current Structure (RPG-Native)

```java
@Service
public class ClaimProcessingService {
    public void initializeSubfileProcessing(SubfileContext context) {
        context.setZl4(0);
        context.setIndicator50(false);
        // ... 9 more indicators
    }
    
    public SubfileResult buildClaimSubfile(SubfileFilter filter, String pkz, boolean ascending) {
        // 200+ lines of sequential logic
    }
}
```

### Target Structure (Pure Java)

```java
// Domain
@Entity
@Table(name = "HSG71LF2")
public class Claim {
    @Id
    @Column(name = "PAKZ")
    private String packageCode;
    
    @Column(name = "RECHNR")
    private String claimNumber;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "STATUS_CODE_SDE")
    private ClaimStatus status;
    
    public boolean isOpen() {
        return status.isOpen();
    }
}

public enum ClaimStatus {
    PENDING(0), APPROVED(20), EXCLUDED(99);
    // ...
}

// Repository
public interface ClaimRepository extends JpaRepository<Claim, String> {
    List<Claim> findByPackageCodeOrderByClaimNumberAsc(String packageCode);
    List<Claim> findByPackageCodeAndStatus(String packageCode, ClaimStatus status);
}

// Service
@Service
public class ClaimSearchService {
    private final ClaimRepository claimRepository;
    
    public ClaimSearchResult searchClaims(ClaimSearchCriteria criteria) {
        Specification<Claim> spec = buildSpecification(criteria);
        Page<Claim> claims = claimRepository.findAll(spec, PageRequest.of(0, 100));
        
        return ClaimSearchResult.builder()
            .claims(claims.map(this::toDto))
            .totalCount(claims.getTotalElements())
            .build();
    }
}

// DTO
public record ClaimSearchCriteria(
    String packageCode,
    boolean openClaimsOnly,
    ClaimStatus statusFilter
) {}

public record ClaimSearchResult(
    List<ClaimListItemDto> claims,
    long totalCount
) {}

// Controller
@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    @GetMapping("/search")
    public ResponseEntity<ClaimSearchResult> searchClaims(
        @ModelAttribute ClaimSearchCriteria criteria
    ) {
        return ResponseEntity.ok(claimSearchService.searchClaims(criteria));
    }
}
```

---

## 5. Checklist for Pure Java Conversion

### Domain Layer
- [ ] Extract domain entities (Claim, not HSG71LF2)
- [ ] Create value objects (ClaimSearchCriteria, not SubfileFilter)
- [ ] Define enums (ClaimStatus, FilterOption)
- [ ] Add domain methods (isOpen(), isMinimumClaim())

### Service Layer
- [ ] Separate business logic from data access
- [ ] Use domain language in method names
- [ ] Replace indicators with enums/flags
- [ ] Replace magic values with constants
- [ ] Use modern Java features (Streams, Optional)

### Repository Layer
- [ ] Use Spring Data JPA Specifications
- [ ] Create custom query methods
- [ ] Separate read/write operations if needed

### Web Layer
- [ ] Create REST controllers
- [ ] Use DTOs for request/response
- [ ] Add proper error handling
- [ ] Add validation

### Testing
- [ ] Unit tests for services
- [ ] Integration tests for repositories
- [ ] API tests for controllers
- [ ] Verify all @Column mappings preserved

---

## 6. Benefits of Pure Java Application

### Maintainability
- ✅ Clear domain language (easier to understand)
- ✅ Separation of concerns (easier to modify)
- ✅ Modern Java features (easier to read)

### Scalability
- ✅ Layered architecture (easier to scale)
- ✅ Proper use of Spring Data (better performance)
- ✅ Caching opportunities (service layer)

### Testability
- ✅ Isolated components (easier to test)
- ✅ Dependency injection (easier to mock)
- ✅ Clear interfaces (easier to verify)

### Team Productivity
- ✅ Standard Java patterns (easier onboarding)
- ✅ IDE support (better autocomplete)
- ✅ Documentation (self-documenting code)

---

## 7. Migration Path Summary

1. **Keep RPG-native code** as source of truth (correctness)
2. **Refactor incrementally** to pure Java (architecture)
3. **Preserve @Column mappings** (database compatibility)
4. **Add tests** at each step (safety)
5. **Document domain glossary** (consistency)

**Result:** A complete, production-ready Pure Java application that:
- ✅ Works correctly (from RPG-native phase)
- ✅ Follows modern Java patterns (from refactoring)
- ✅ Maintains database compatibility (@Column preserved)
- ✅ Is maintainable and scalable (layered architecture)
