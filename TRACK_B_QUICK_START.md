# Track B Quick Start Guide

**Script:** `migrate_to_pure_java.py`  
**Purpose:** Generate Pure Java application code with layered architecture directly from RPG + Context Package

---

## Quick Start

### Prerequisites

```bash
export ANTHROPIC_API_KEY='sk-ant-...'
```

### Basic Usage

```bash
# Generate Pure Java code
python3 migrate_to_pure_java.py context_index/HS1210_n404.json

# With streaming (see progress)
python3 migrate_to_pure_java.py context_index/HS1210_n404.json --stream

# Custom output directory
python3 migrate_to_pure_java.py context_index/HS1210_n404.json --output-dir ./output
```

---

## What It Generates

### Output Structure

```
HS1210_n404_pure_java/
├── domain/
│   ├── Claim.java                    (Entity)
│   ├── ClaimStatus.java              (Enum)
│   └── ClaimSearchCriteria.java      (Value Object)
├── repository/
│   └── ClaimRepository.java          (Spring Data JPA)
├── service/
│   └── ClaimSearchService.java       (Business Logic)
├── dto/
│   ├── ClaimDto.java                 (Response DTO - Record)
│   └── ClaimListItemDto.java         (List Item DTO - Record)
└── web/
    └── ClaimController.java          (REST API - optional)
```

### Key Features

✅ **Layered Architecture**
- Domain entities, value objects, enums
- Repository interfaces (Spring Data JPA)
- Stateless service classes
- DTOs (Java Records)
- REST controllers (optional)

✅ **Domain-Driven Design**
- Domain names (Claim, not HSG71LF2)
- Enums (ClaimStatus, not magic numbers)
- Value objects (ClaimSearchCriteria, not SubfileFilter)

✅ **Modern Java**
- Java Records for DTOs
- Streams for data processing
- Optional for null handling
- Dependency injection

✅ **Database Compatibility**
- All @Column mappings preserved
- Table names preserved (@Table(name="HSG71LF2"))
- Schema compatibility maintained

---

## Comparison: Track A vs Track B

| Aspect | Track A (RPG-Native) | Track B (Pure Java) |
|--------|---------------------|---------------------|
| **Script** | `migrate_with_claude.py` | `migrate_to_pure_java.py` |
| **Output** | Single service class | Layered structure (multiple files) |
| **Naming** | RPG-style (HSG71LF2) | Domain names (Claim) |
| **Architecture** | Monolithic | Layered (domain/service/repository) |
| **Java Features** | Basic Java | Modern Java 17+ |
| **Use Case** | Validation, reference | Production application |

---

## Example Output

### Domain Entity (domain/Claim.java)
```java
package com.scania.warranty.domain;

@Entity
@Table(name="HSG71LF2")  // Preserved table name
public class Claim {
    @Id
    @Column(name="PAKZ")
    private String packageCode;  // Domain name, DB name preserved
    
    @Column(name="RECHNR")
    private String claimNumber;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name="STATUS_CODE_SDE")
    private ClaimStatus status;  // Enum, not int
}
```

### Service (service/ClaimSearchService.java)
```java
package com.scania.warranty.service;

@Service
public class ClaimSearchService {
    private final ClaimRepository claimRepository;
    
    @Autowired
    public ClaimSearchService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }
    
    public ClaimSearchResult searchClaims(ClaimSearchCriteria criteria) {
        // Modern Java: Streams, Optional, domain language
        List<Claim> claims = claimRepository.findAll()
            .stream()
            .filter(c -> matchesCriteria(c, criteria))
            .collect(Collectors.toList());
        
        return new ClaimSearchResult(claims);
    }
}
```

---

## Command Options

```bash
python3 migrate_to_pure_java.py [OPTIONS] context_file

Options:
  --output-dir DIR     Output directory (default: current directory)
  --model MODEL        Anthropic model (default: claude-sonnet-4-5)
  --max-tokens N       Max tokens (default: 64000)
  --stream             Stream response (shows progress)
```

---

## Next Steps

1. **Test the script:**
   ```bash
   python3 migrate_to_pure_java.py context_index/HS1210_n404.json --stream
   ```

2. **Review generated code:**
   - Check layered structure
   - Verify domain names
   - Validate @Column mappings

3. **Compare with Track A:**
   - Track A: `HS1210_n404.java` (RPG-native)
   - Track B: `HS1210_n404_pure_java/` (Pure Java)

4. **Iterate and refine:**
   - Update domain glossary
   - Improve prompt based on results
   - Add more patterns

---

## Troubleshooting

### Issue: Syntax errors in generated code
- **Solution:** Check prompt size, may need to increase `--max-tokens`

### Issue: Missing files in output
- **Solution:** Model may not have generated all layers, check prompt for completeness requirements

### Issue: Domain names not used
- **Solution:** Update `DOMAIN_GLOSSARY` in script with your mappings

### Issue: Code truncated
- **Solution:** Increase `--max-tokens` or split migration

---

**Status:** ✅ Ready to use  
**Backup:** Checkpoint created (`checkpoint-before-track-b`)  
**Next:** Test on pilot unit (HS1210_n404)
