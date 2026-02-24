# Track B Implementation Plan: Direct Pure Java Migration

**Date:** February 13, 2026  
**Status:** Planning Phase  
**Objective:** Build enhanced migration pipeline that generates Pure Java application code directly from RPG + Context Package

---

## 1. Overview

### 1.1 Goal

Create `migrate_to_pure_java.py` that generates:
- **Layered architecture:** domain/service/repository/web
- **Domain-driven design:** Claim (not HSG71LF2), ClaimStatus (not magic numbers)
- **Modern Java:** Records, Streams, Optional, Enums
- **Pure Java patterns:** Stateless services, dependency injection, proper separation

### 1.2 Key Differences from Track A

| Aspect | Track A (RPG-Native) | Track B (Pure Java) |
|--------|---------------------|---------------------|
| **Output Structure** | Single service class | Layered (domain/service/repository/web) |
| **Naming** | RPG-style (HSG71LF2, SubfileContext) | Domain names (Claim, ClaimSearchCriteria) |
| **State Management** | Context objects (SubfileContext) | Request-scoped or stateless |
| **Architecture** | Monolithic service | Layered architecture |
| **Java Features** | Basic Java | Modern Java 17+ (Records, Streams, Enums) |

---

## 2. Target Architecture

### 2.1 Package Structure

```
com.scania.warranty/
├── domain/
│   ├── Claim.java                    (Entity - replaces HSG71LF2)
│   ├── ClaimStatus.java              (Enum - replaces magic numbers)
│   ├── FilterOption.java             (Enum - replaces magic strings)
│   └── ClaimSearchCriteria.java      (Value Object - replaces SubfileFilter)
├── repository/
│   └── ClaimRepository.java          (Spring Data JPA - replaces direct queries)
├── service/
│   ├── ClaimSearchService.java       (Business logic - replaces buildClaimSubfile)
│   └── ClaimService.java             (CRUD operations)
├── dto/
│   ├── ClaimDto.java                 (Response DTO)
│   ├── ClaimListItemDto.java         (List item DTO - replaces SubfileRecord)
│   └── ClaimSearchRequest.java       (Request DTO)
├── web/
│   └── ClaimController.java          (REST API - optional)
└── exception/
    └── ClaimNotFoundException.java   (Custom exceptions)
```

### 2.2 Domain Model Mapping

| RPG Concept | Pure Java Domain Concept |
|------------|--------------------------|
| `HSG71LF2` (file) | `Claim` (entity) |
| `SubfileContext` | `ClaimListState` or request-scoped context |
| `SubfileFilter` | `ClaimSearchCriteria` (value object) |
| `SubfileResult` | `ClaimSearchResult` or `Page<ClaimDto>` |
| `SubfileRecord` | `ClaimListItemDto` |
| Status code `99` | `ClaimStatus.EXCLUDED` (enum) |
| Magic string `"J"` | `FilterOption.OPEN_CLAIMS_ONLY` (enum) |
| Indicators 50-58 | Domain flags or enums |
| `initializeSubfileProcessing()` | `initializeClaimSearch()` |
| `buildClaimSubfile()` | `searchClaims()` or `findClaims()` |

---

## 3. Enhanced Prompt Design

### 3.1 Prompt Structure

The enhanced prompt will include:

1. **Target Architecture Section**
   - Package structure
   - Layer responsibilities
   - Naming conventions

2. **Domain-Driven Design Requirements**
   - Extract domain entities
   - Create value objects
   - Use enums for constants

3. **Modern Java Requirements**
   - Use Java Records for DTOs
   - Use Streams for data processing
   - Use Optional properly
   - Use dependency injection

4. **Database Mapping Preservation**
   - Keep all `@Column(name="...")` mappings
   - Preserve table names
   - Maintain schema compatibility

5. **Cross-Module Communication**
   - Service-to-service calls
   - Shared state patterns
   - Request-scoped contexts

### 3.2 Prompt Template Outline

```python
def build_pure_java_prompt(context: dict) -> str:
    """
    Build prompt for Pure Java migration (Track B).
    
    Includes:
    - Target architecture requirements
    - Domain-driven design patterns
    - Modern Java features
    - Database mapping preservation
    """
    return f"""
    ## Target Architecture
    
    Generate Java code following this structure:
    
    ### Package Layout
    - `domain/` - Entities, value objects, enums
    - `service/` - Business logic (one service per use case)
    - `repository/` - Data access (Spring Data JPA)
    - `dto/` - Request/response objects
    - `web/` - REST controllers (optional)
    
    ### Domain-Driven Design
    - Extract domain entities: Claim (not HSG71LF2), ClaimStatus (not int)
    - Create value objects: ClaimSearchCriteria (not SubfileFilter)
    - Use enums: ClaimStatus, FilterOption (not magic strings/numbers)
    
    ### Modern Java (Java 17+)
    - Use Records for DTOs: `public record ClaimDto(...)`
    - Use Streams: `claims.stream().filter(...).map(...).collect(...)`
    - Use Optional: `Optional.ofNullable(...).orElseThrow(...)`
    - Use dependency injection: `@Autowired` constructor injection
    
    ### Database Mapping (CRITICAL - PRESERVE)
    - Keep all `@Column(name="EXACT_DB_NAME")` mappings unchanged
    - Preserve table names: `@Table(name="HSG71LF2")`
    - Maintain schema compatibility
    
    ## Context Package
    {context_sections}
    
    ## Requirements
    1. Generate layered architecture (domain/service/repository)
    2. Use domain names (Claim, not HSG71LF2)
    3. Use enums (ClaimStatus, not magic numbers)
    4. Use modern Java features
    5. Preserve all @Column mappings
    """
```

---

## 4. Implementation Steps

### Step 1: Create Enhanced Prompt Builder

**File:** `migrate_to_pure_java.py`

**Components:**
1. `build_pure_java_prompt()` - Enhanced prompt builder
2. `extract_domain_concepts()` - Extract domain entities from context
3. `generate_architecture_guidance()` - Architecture requirements
4. `preserve_db_mapping()` - Ensure @Column mappings preserved

### Step 2: Domain Concept Extraction

**Function:** Analyze context to extract:
- Entity names (from dbContracts)
- Value objects (from RPG variables/data structures)
- Enums (from magic strings/numbers)
- Service boundaries (from RPG subroutines)

### Step 3: Architecture Template Generation

**Function:** Generate architecture guidance:
- Package structure
- Layer responsibilities
- Naming conventions
- Dependency patterns

### Step 4: Output Structure

**Function:** Generate multiple files:
- Domain entities
- Repositories
- Services
- DTOs
- Controllers (optional)

### Step 5: Validation

**Function:** Validate output:
- All @Column mappings preserved
- Architecture follows structure
- Domain names used (not file names)
- Modern Java features used

---

## 5. File Structure

### 5.1 New Script: `migrate_to_pure_java.py`

```python
#!/usr/bin/env python3
"""
Track B: Direct Pure Java Migration Pipeline

Generates Pure Java application code with:
- Layered architecture (domain/service/repository/web)
- Domain-driven design
- Modern Java features
- Preserved database mappings
"""

import argparse
import json
from pathlib import Path
from typing import Dict, List

def build_pure_java_prompt(context: dict) -> str:
    """Build enhanced prompt for Pure Java migration."""
    # Extract domain concepts
    domain_concepts = extract_domain_concepts(context)
    
    # Build architecture guidance
    architecture_guidance = generate_architecture_guidance(domain_concepts)
    
    # Build prompt with all sections
    return f"""
    {architecture_guidance}
    
    ## Context Package
    {format_context_sections(context)}
    
    ## Requirements
    {generate_requirements(domain_concepts)}
    """

def extract_domain_concepts(context: dict) -> Dict:
    """Extract domain concepts from context."""
    # Analyze dbContracts for entity names
    # Analyze RPG variables for value objects
    # Analyze magic values for enums
    pass

def generate_architecture_guidance(domain_concepts: Dict) -> str:
    """Generate architecture requirements."""
    pass

def main():
    """Main entry point."""
    parser = argparse.ArgumentParser()
    parser.add_argument("context_file", help="Context package JSON")
    parser.add_argument("--output-dir", default=".", help="Output directory")
    parser.add_argument("--model", default="claude-sonnet-4-5")
    parser.add_argument("--max-tokens", type=int, default=64000)
    args = parser.parse_args()
    
    # Load context
    with open(args.context_file) as f:
        context = json.load(f)
    
    # Build prompt
    prompt = build_pure_java_prompt(context)
    
    # Call LLM
    # Generate code
    # Write to output directory
```

### 5.2 Output Structure

**Single Unit Migration Output:**
```
output/
├── domain/
│   ├── Claim.java
│   ├── ClaimStatus.java
│   └── ClaimSearchCriteria.java
├── repository/
│   └── ClaimRepository.java
├── service/
│   └── ClaimSearchService.java
├── dto/
│   ├── ClaimDto.java
│   └── ClaimListItemDto.java
└── web/
    └── ClaimController.java
```

---

## 6. Domain Concept Extraction Strategy

### 6.1 Entity Extraction

**From `dbContracts`:**
```python
def extract_entities(db_contracts: List[Dict]) -> List[Entity]:
    """
    Extract domain entities from dbContracts.
    
    Rules:
    - File name HSG71LF2 → Claim entity
    - File name HSG73PF → ClaimFailure entity
    - Use domain glossary for naming
    """
    entities = []
    for contract in db_contracts:
        file_name = contract.get("fileName") or contract.get("name")
        domain_name = map_to_domain_name(file_name)  # HSG71LF2 → Claim
        entities.append(Entity(
            domain_name=domain_name,
            table_name=file_name,
            columns=contract.get("columns", [])
        ))
    return entities
```

### 6.2 Value Object Extraction

**From RPG Variables:**
```python
def extract_value_objects(context: dict) -> List[ValueObject]:
    """
    Extract value objects from RPG variables/data structures.
    
    Examples:
    - SubfileFilter → ClaimSearchCriteria
    - SubfileContext → ClaimListState
    """
    # Analyze symbolMetadata for data structures
    # Map to domain value objects
    pass
```

### 6.3 Enum Extraction

**From Magic Values:**
```python
def extract_enums(context: dict) -> List[Enum]:
    """
    Extract enums from magic strings/numbers.
    
    Examples:
    - Status code 99 → ClaimStatus.EXCLUDED
    - Magic string "J" → FilterOption.OPEN_CLAIMS_ONLY
    """
    # Analyze RPG code for magic values
    # Map to domain enums
    pass
```

---

## 7. Architecture Guidance Generation

### 7.1 Package Structure Template

```python
ARCHITECTURE_TEMPLATE = """
## Target Architecture

### Package Structure
```
com.scania.warranty/
├── domain/
│   ├── {entity_name}.java          (Entity)
│   ├── {enum_name}.java             (Enum)
│   └── {value_object_name}.java    (Value Object)
├── repository/
│   └── {entity_name}Repository.java (Spring Data JPA)
├── service/
│   └── {service_name}Service.java   (Business Logic)
├── dto/
│   ├── {entity_name}Dto.java        (Response DTO)
│   └── {request_name}.java          (Request DTO)
└── web/
    └── {entity_name}Controller.java (REST API - optional)
```

### Layer Responsibilities

- **domain/**: Entities, value objects, enums (domain model)
- **repository/**: Data access (Spring Data JPA repositories)
- **service/**: Business logic (stateless services)
- **dto/**: Data transfer objects (request/response)
- **web/**: REST controllers (HTTP handling)
"""
```

### 7.2 Naming Conventions

```python
NAMING_CONVENTIONS = """
### Naming Conventions

- **Entities**: Domain names (Claim, not HSG71LF2)
- **Services**: Domain + Service (ClaimSearchService, not buildClaimSubfile)
- **Repositories**: Entity + Repository (ClaimRepository)
- **DTOs**: Entity + Dto (ClaimDto, ClaimListItemDto)
- **Enums**: Domain + Type (ClaimStatus, FilterOption)
- **Methods**: Domain language (searchClaims, not buildClaimSubfile)
- **Fields**: camelCase (claimNumber, not RECHNR)
- **@Column**: Preserve DB names (@Column(name="RECHNR"))
"""
```

---

## 8. Implementation Checklist

### Phase 1: Core Script Development
- [ ] Create `migrate_to_pure_java.py`
- [ ] Implement `build_pure_java_prompt()`
- [ ] Implement `extract_domain_concepts()`
- [ ] Implement `generate_architecture_guidance()`
- [ ] Add LLM integration (Anthropic API)
- [ ] Add output file generation

### Phase 2: Domain Extraction
- [ ] Entity extraction from dbContracts
- [ ] Value object extraction from RPG variables
- [ ] Enum extraction from magic values
- [ ] Domain glossary integration

### Phase 3: Prompt Enhancement
- [ ] Architecture requirements section
- [ ] Domain-driven design guidance
- [ ] Modern Java requirements
- [ ] Database mapping preservation
- [ ] Cross-module communication patterns

### Phase 4: Output Generation
- [ ] Multi-file output structure
- [ ] Package directory creation
- [ ] File naming conventions
- [ ] Code formatting

### Phase 5: Validation
- [ ] @Column mapping preservation check
- [ ] Architecture compliance check
- [ ] Domain naming check
- [ ] Compilation check

### Phase 6: Testing
- [ ] Test on HS1210_n404 context
- [ ] Compare with Track A output
- [ ] Validate correctness
- [ ] Validate architecture
- [ ] Document findings

---

## 9. Testing Strategy

### 9.1 Test Cases

**Test Case 1: HS1210_n404**
- Input: `context_index/HS1210_n404.json`
- Expected: Layered Claim domain structure
- Validate: @Column mappings preserved

**Test Case 2: HS1212_n498**
- Input: `context_index/HS1212_n498.json`
- Expected: Different domain entities
- Validate: Architecture consistency

**Test Case 3: Comparison**
- Compare Track A vs Track B output
- Verify correctness preserved
- Verify architecture improved

### 9.2 Validation Criteria

✅ **Correctness:**
- All @Column mappings preserved
- Business logic preserved
- Database compatibility maintained

✅ **Architecture:**
- Layered structure (domain/service/repository)
- Domain names used (not file names)
- Modern Java features used

✅ **Quality:**
- Code compiles
- Follows Java conventions
- Proper separation of concerns

---

## 10. Success Metrics

### 10.1 Architecture Metrics
- ✅ Layered structure present
- ✅ Domain names used (not file names)
- ✅ Enums used (not magic values)
- ✅ Modern Java features used

### 10.2 Correctness Metrics
- ✅ All @Column mappings preserved
- ✅ Business logic preserved
- ✅ Database compatibility maintained

### 10.3 Quality Metrics
- ✅ Code compiles
- ✅ Follows Java conventions
- ✅ Proper dependency injection
- ✅ Stateless services

---

## 11. Next Steps

1. **Review this plan** - Confirm approach and priorities
2. **Create domain glossary** - Map RPG concepts to domain names
3. **Implement core script** - `migrate_to_pure_java.py`
4. **Test on pilot unit** - HS1210_n404
5. **Iterate and refine** - Based on results
6. **Document patterns** - For future migrations

---

## 12. Domain Glossary (To Be Created)

**Entity Mapping:**
- HSG71LF2 → Claim
- HSG73PF → ClaimFailure
- HSG70F → ClaimHeader
- (etc.)

**Value Object Mapping:**
- SubfileFilter → ClaimSearchCriteria
- SubfileContext → ClaimListState
- SubfileResult → ClaimSearchResult

**Enum Mapping:**
- Status code 99 → ClaimStatus.EXCLUDED
- Magic string "J" → FilterOption.OPEN_CLAIMS_ONLY
- (etc.)

**Service Mapping:**
- buildClaimSubfile → searchClaims
- initializeSubfileProcessing → initializeClaimSearch
- (etc.)

---

**Status:** Ready for implementation  
**Backup:** ✅ Created (checkpoint-before-track-b)  
**Next:** Begin implementation of `migrate_to_pure_java.py`
