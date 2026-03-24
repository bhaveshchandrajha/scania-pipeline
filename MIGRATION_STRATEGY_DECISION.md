# Migration Strategy Decision: RPG-Native vs Direct Pure Java

**Date:** February 13, 2026  
**Question:** Should we use RPG-native migrated Java code as a starting point, or build application-centric Java directly from RPG + context package?

---

## The Two Approaches

### Approach 1: RPG-Native Java → Refactor to Pure Java

**Process:**
```
RPG Source + AST + Context Package
    ↓
[RPG-Native Java Migration] (Current pipeline)
    ↓
HS1210_n404.java (RPG-native, validated)
    ↓
[Refactoring Phase]
    ↓
Pure Java Application (ClaimService, ClaimRepository, etc.)
```

**Characteristics:**
- **Two-phase migration:** Correctness first, then architecture
- **Uses existing pipeline:** Current `migrate_with_claude.py` output
- **Validated correctness:** Schema mapping verified, business logic preserved
- **Refactoring step:** Transform RPG-native code to pure Java

### Approach 2: RPG + Context → Direct Pure Java

**Process:**
```
RPG Source + AST + Context Package
    ↓
[Enhanced Migration Pipeline]
    ↓
Pure Java Application (Direct)
    ↓
ClaimService, ClaimRepository, etc. (No refactoring needed)
```

**Characteristics:**
- **Single-phase migration:** Architecture from the start
- **Enhanced prompt:** Migration prompt includes pure Java requirements
- **No refactoring:** Output is already application-centric
- **Risk:** May lose correctness if prompt isn't perfect

---

## Comparison Matrix

| Aspect | Approach 1: RPG-Native → Refactor | Approach 2: Direct Pure Java |
|--------|-----------------------------------|------------------------------|
| **Correctness** | ✅ High (validated RPG-native code) | ⚠️ Medium (depends on prompt quality) |
| **Architecture** | ⚠️ Two steps (native → refactor) | ✅ One step (direct) |
| **Traceability** | ✅ Clear (RPG → native → pure) | ⚠️ Direct (may lose intermediate step) |
| **Validation** | ✅ Can validate at each step | ⚠️ Must validate final output |
| **Time** | ⚠️ Longer (two phases) | ✅ Faster (one phase) |
| **Risk** | ✅ Lower (validated intermediate) | ⚠️ Higher (no intermediate validation) |
| **Maintenance** | ✅ Easier (can reference native code) | ⚠️ Harder (no intermediate reference) |

---

## Recommended Approach: **Hybrid Strategy**

### Phase 1: RPG-Native Migration (Current Pipeline)

**Purpose:** Establish correctness and traceability

**Output:**
- RPG-native Java code (`HS1210_n404.java`)
- Validated schema mapping (all columns mapped)
- Preserved business logic
- Clear traceability to RPG source

**Why Keep This:**
1. **Validation:** Can verify correctness before refactoring
2. **Reference:** Always have RPG-native code as "source of truth"
3. **Incremental:** Can migrate and validate one unit at a time
4. **Safety Net:** If refactoring breaks something, can compare to native code

### Phase 2: Enhanced Prompt for Pure Java (New Pipeline)

**Purpose:** Generate pure Java directly when confidence is high

**Enhancement:** Update `migrate_with_claude.py` prompt to include:
- Pure Java architecture requirements
- Domain-driven design patterns
- Modern Java features
- Layered structure (domain/service/repository/web)

**Output:**
- Pure Java code (`ClaimService.java`, `ClaimRepository.java`, etc.)
- Application-centric architecture
- Modern Java patterns

**When to Use:**
- For new migrations (after Phase 1 is validated)
- For critical paths (where architecture matters most)
- When RPG-native patterns are well-understood

### Phase 3: Refactoring Pipeline (Optional)

**Purpose:** Transform Phase 1 output to Pure Java

**Process:**
- Take RPG-native Java as input
- Refactor using LLM or manual process
- Output pure Java

**When to Use:**
- For existing RPG-native code
- When you want to preserve intermediate step
- For incremental improvement

---

## Recommended Strategy: **Two-Track Approach**

### Track A: RPG-Native Migration (Keep Current)

**Use For:**
- Initial migrations (establish correctness)
- Complex business logic (need validation)
- Critical paths (safety first)
- Learning phase (understand RPG patterns)

**Process:**
```
RPG + Context → RPG-Native Java → Validate → Use as-is OR Refactor
```

**Benefits:**
- ✅ Validated correctness
- ✅ Clear traceability
- ✅ Can use immediately
- ✅ Reference for refactoring

### Track B: Direct Pure Java Migration (New)

**Use For:**
- Well-understood patterns
- Standard CRUD operations
- After Track A is validated
- When architecture is priority

**Process:**
```
RPG + Context → Pure Java (Enhanced Prompt) → Validate → Use directly
```

**Benefits:**
- ✅ Modern architecture from start
- ✅ No refactoring needed
- ✅ Faster for standard patterns
- ✅ Better for new code

---

## Implementation Plan

### Step 1: Keep Current Pipeline (Track A)

**Action:** Continue using `migrate_with_claude.py` as-is

**Output:** RPG-native Java (`HS1210_n404.java`)

**Use Cases:**
- Initial migration of all units
- Complex business logic
- Validation and testing

### Step 2: Create Enhanced Pipeline (Track B)

**Action:** Create `migrate_to_pure_java.py` with enhanced prompt

**Enhanced Prompt Includes:**
```python
## Target Architecture

Produce Java code following these patterns:

1. **Layered Architecture:**
   - `domain/` - Entities, value objects, enums
   - `service/` - Business logic (one service per use case)
   - `repository/` - Data access (Spring Data JPA)
   - `dto/` - Request/response objects
   - `web/` - REST controllers (optional)

2. **Domain-Driven Design:**
   - Use domain names (Claim, not HSG71LF2)
   - Extract value objects (ClaimSearchCriteria, not SubfileFilter)
   - Use enums (ClaimStatus, not magic numbers)

3. **Modern Java:**
   - Use Java Records for DTOs
   - Use Streams for data processing
   - Use Optional properly
   - Use dependency injection

4. **Preserve Database Mapping:**
   - Keep all @Column(name="...") mappings unchanged
   - Preserve table names (@Table(name="HSG71LF2"))
```

**Output:** Pure Java application code

**Use Cases:**
- New migrations (after Track A validated)
- Standard patterns
- When architecture is priority

### Step 3: Refactoring Pipeline (Optional)

**Action:** Create `refactor_to_pure_java.py`

**Process:**
```python
# Takes RPG-native Java as input
# Refactors to pure Java
# Outputs refactored code
```

**Use Cases:**
- Transform existing RPG-native code
- Incremental improvement
- When you want both versions

---

## Decision Matrix: Which Track to Use?

### Use Track A (RPG-Native) When:
- ✅ First time migrating this unit
- ✅ Complex business logic
- ✅ Need validation step
- ✅ Want traceability
- ✅ Learning RPG patterns

### Use Track B (Direct Pure Java) When:
- ✅ Pattern is well-understood
- ✅ Standard CRUD operations
- ✅ Architecture is priority
- ✅ Track A already validated similar patterns
- ✅ Want modern code from start

### Use Refactoring When:
- ✅ Have RPG-native code already
- ✅ Want to improve incrementally
- ✅ Need both versions for comparison
- ✅ Manual refactoring is preferred

---

## Recommended Workflow

### Initial Phase (Months 1-2)

1. **Migrate all units using Track A** (RPG-native)
   - Establish correctness
   - Validate schema mapping
   - Understand business logic
   - Build reference library

2. **Validate and test**
   - Verify correctness
   - Test business logic
   - Document patterns

### Maturation Phase (Months 3-4)

3. **Create Track B** (Direct Pure Java)
   - Enhance migration prompt
   - Test on well-understood units
   - Compare with Track A output

4. **Use Track B for new migrations**
   - Standard patterns → Track B
   - Complex logic → Track A
   - Refactor Track A output as needed

### Optimization Phase (Ongoing)

5. **Refine both tracks**
   - Improve Track A prompt (better RPG-native code)
   - Improve Track B prompt (better pure Java)
   - Build refactoring pipeline

6. **Choose based on context**
   - Use decision matrix
   - Track metrics (correctness, time, quality)
   - Iterate and improve

---

## Practical Example

### Scenario: Migrating HS1210_n404

**Option 1: Track A (RPG-Native)**
```bash
# Current pipeline
python3 migrate_with_claude.py context_index/HS1210_n404.json
# Output: HS1210_n404.java (RPG-native)

# Validate
python3 validate_java.py context_index/HS1210_n404.json HS1210_n404.java

# Use as-is OR refactor later
```

**Option 2: Track B (Direct Pure Java)**
```bash
# Enhanced pipeline (to be created)
python3 migrate_to_pure_java.py context_index/HS1210_n404.json
# Output: 
#   - domain/Claim.java
#   - service/ClaimSearchService.java
#   - repository/ClaimRepository.java
#   - dto/ClaimSearchCriteria.java

# Validate
python3 validate_java.py context_index/HS1210_n404.json [output files]
```

**Option 3: Refactor Track A Output**
```bash
# Refactor pipeline (to be created)
python3 refactor_to_pure_java.py HS1210_n404.java
# Output: Pure Java version

# Compare and validate
```

---

## Final Recommendation

### **Use Both Tracks Strategically**

1. **Start with Track A** (RPG-native)
   - Establish correctness foundation
   - Build reference library
   - Validate patterns

2. **Develop Track B** (Direct Pure Java)
   - Create enhanced prompt
   - Test on well-understood units
   - Compare with Track A

3. **Use Decision Matrix**
   - Complex/unknown → Track A
   - Standard/understood → Track B
   - Existing code → Refactor

4. **Iterate and Improve**
   - Refine prompts based on results
   - Build refactoring pipeline
   - Optimize workflow

### **Key Principle:**
**Correctness First, Architecture Second**

- Track A ensures correctness (validated RPG-native)
- Track B provides architecture (pure Java from start)
- Refactoring bridges the gap (improve incrementally)

---

## Next Steps

1. **Keep Track A** (current pipeline) - continue using for initial migrations
2. **Create Track B** (enhanced pipeline) - develop `migrate_to_pure_java.py`
3. **Test Both** - compare outputs, validate correctness
4. **Choose Strategically** - use decision matrix for each unit
5. **Iterate** - refine prompts based on results

This hybrid approach gives you:
- ✅ **Safety** (Track A validated correctness)
- ✅ **Speed** (Track B direct architecture)
- ✅ **Flexibility** (choose based on context)
- ✅ **Quality** (both tracks improve over time)
