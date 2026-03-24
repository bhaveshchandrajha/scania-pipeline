# Shared State and Cross-Module Flow: RPG → Java Migration

**Date:** February 13, 2026  
**Goal:** Understand how to handle shared objects, cross-module communication, and preserve application-wide context when migrating from RPG to Java.

---

## 1. How RPG Handles Shared State and Flow

### 1.1 RPG Shared State Mechanisms

**RPG Program Structure:**
```
HS1210 (Main Program)
├── Global Variables (module-level)
│   ├── ZL1, ZL2, ZL4 (counters)
│   ├── MARK11, MARK12 (selection marks)
│   └── Indicators 01-99 (control flags)
├── Data Structures (DS)
│   ├── NEU2 (shared data structure)
│   └── AHK550 (shared data structure)
├── External Files (shared across programs)
│   ├── HSG71LF2 (claim file)
│   └── HSG73PF (failure file)
└── Subroutines (shared logic)
    ├── SB10N (initialize)
    ├── SB100 (build subfile)
    └── MARK (process selection)
```

**RPG Shared State Patterns:**

1. **Module-Level Variables**
   ```rpg
   D ZL1              S              5 0
   D MARK12           S             14
   D IND50            S               N
   ```
   - Accessible to all subroutines in the program
   - Persists across subroutine calls
   - Shared state for the entire program execution

2. **External Data Structures**
   ```rpg
   D NEU2             DS                  QUALIFIED
   D  NEU2XJ                         2 0
   D  NEU2XM                         2 0
   D  NEU2XT                         2 0
   ```
   - Shared across multiple programs via `EXTERN` keyword
   - Common area for inter-program communication

3. **Program-to-Program Calls**
   ```rpg
   CALL 'HS1212'
   PARM CNR
   PARM DATAUF
   ```
   - Passes parameters
   - May share files/data structures
   - Returns control to caller

4. **Common Areas (Shared Memory)**
   ```rpg
   D COMMON_AREA      DS                  BASED(COMMON_PTR)
   ```
   - Shared memory across multiple programs
   - Used for application-wide state

5. **File Sharing**
   ```rpg
   FHSG71LF2  IF   E           K DISK
   ```
   - Files opened at program level
   - Shared across subroutines
   - Position maintained across calls

---

## 2. Java Patterns for Shared State

### 2.1 Application Context (Spring)

**Pattern: Application-Level State**

```java
@Component
@Scope("singleton")  // Default: one instance for entire application
public class ApplicationContext {
    private final Map<String, Object> sharedState = new ConcurrentHashMap<>();
    
    public void setSharedValue(String key, Object value) {
        sharedState.put(key, value);
    }
    
    public <T> T getSharedValue(String key, Class<T> type) {
        return type.cast(sharedState.get(key));
    }
}
```

**Use Cases:**
- Application-wide configuration
- Shared caches
- Global counters
- System-wide flags

### 2.2 Request/Session Scope (Web Applications)

**Pattern: Request-Scoped State**

```java
@Component
@Scope("request")  // One instance per HTTP request
public class RequestContext {
    private String userId;
    private String sessionId;
    private ClaimSearchCriteria searchCriteria;
    
    // State persists for the duration of one HTTP request
}
```

**Pattern: Session-Scoped State**

```java
@Component
@Scope("session")  // One instance per HTTP session
public class SessionContext {
    private String userId;
    private ClaimListState claimListState;  // Preserves search state across requests
    private Map<String, Object> userPreferences;
}
```

**Use Cases:**
- User session data
- Multi-step workflows
- UI state preservation
- User preferences

### 2.3 Service Layer State Management

**Pattern: Stateless Services (Recommended)**

```java
@Service
@Transactional
public class ClaimService {
    // No instance variables for business state
    // All state passed as parameters
    
    public ClaimDto findClaim(String id) {
        // Stateless: uses repositories, returns DTOs
    }
    
    public ClaimSearchResult searchClaims(ClaimSearchCriteria criteria) {
        // Stateless: criteria passed in, result returned
    }
}
```

**Pattern: Stateful Services (When Needed)**

```java
@Service
@Scope("request")  // Stateful per request
public class ClaimWorkflowService {
    private ClaimWorkflowState workflowState;  // Request-scoped state
    
    public void startWorkflow(Claim claim) {
        workflowState = new ClaimWorkflowState(claim);
    }
    
    public void processStep(String stepId) {
        // Uses workflowState
    }
}
```

---

## 3. Cross-Module Communication Patterns

### 3.1 Service-to-Service Calls (Synchronous)

**RPG Pattern:**
```rpg
CALL 'HS1212'
PARM CNR
PARM DATAUF
```

**Java Pattern:**
```java
@Service
public class ClaimService {
    private final ValidationService validationService;
    private final NotificationService notificationService;
    
    @Autowired
    public ClaimService(
        ValidationService validationService,
        NotificationService notificationService
    ) {
        this.validationService = validationService;
        this.notificationService = notificationService;
    }
    
    public ClaimDto createClaim(CreateClaimRequest request) {
        // Validate
        validationService.validateClaim(request);
        
        // Create
        Claim claim = claimRepository.save(toEntity(request));
        
        // Notify
        notificationService.notifyClaimCreated(claim);
        
        return toDto(claim);
    }
}
```

### 3.2 Event-Driven Communication (Asynchronous)

**Pattern: Spring Events**

```java
// Event
public class ClaimCreatedEvent {
    private final Claim claim;
    private final String userId;
    // ...
}

// Publisher
@Service
public class ClaimService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public ClaimDto createClaim(CreateClaimRequest request) {
        Claim claim = claimRepository.save(toEntity(request));
        
        // Publish event
        eventPublisher.publishEvent(new ClaimCreatedEvent(claim, request.getUserId()));
        
        return toDto(claim);
    }
}

// Subscriber
@Component
public class ClaimNotificationListener {
    @EventListener
    public void handleClaimCreated(ClaimCreatedEvent event) {
        // Handle notification asynchronously
        notificationService.sendNotification(event.getClaim());
    }
}
```

### 3.3 Shared Data Structures → DTOs/Value Objects

**RPG Pattern:**
```rpg
D NEU2             DS                  QUALIFIED
D  NEU2XJ                         2 0
D  NEU2XM                         2 0
D  NEU2XT                         2 0
```

**Java Pattern:**
```java
// Value Object (immutable)
public record DateComponents(
    int year,
    int month,
    int day
) {
    public LocalDate toLocalDate() {
        return LocalDate.of(year, month, day);
    }
    
    public static DateComponents fromLocalDate(LocalDate date) {
        return new DateComponents(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }
}

// DTO (mutable, for data transfer)
public class ClaimWorkflowState {
    private String claimId;
    private DateComponents claimDate;
    private ClaimStatus status;
    private List<String> errors;
    
    // Getters/setters
}
```

---

## 4. Preserving Global Application View

### 4.1 Application State Registry

**Pattern: Centralized State Management**

```java
@Component
public class ApplicationStateRegistry {
    // Application-wide state
    private final Map<String, ApplicationModule> modules = new ConcurrentHashMap<>();
    private final Map<String, SharedDataStructure> sharedStructures = new ConcurrentHashMap<>();
    
    public void registerModule(String moduleId, ApplicationModule module) {
        modules.put(moduleId, module);
    }
    
    public ApplicationModule getModule(String moduleId) {
        return modules.get(moduleId);
    }
    
    public <T extends SharedDataStructure> T getSharedStructure(String name, Class<T> type) {
        return type.cast(sharedStructures.get(name));
    }
    
    // Application-wide view
    public ApplicationView getApplicationView() {
        return ApplicationView.builder()
            .modules(new ArrayList<>(modules.values()))
            .sharedStructures(new ArrayList<>(sharedStructures.values()))
            .build();
    }
}
```

### 4.2 Module Registry Pattern

**Pattern: Discoverable Modules**

```java
public interface ApplicationModule {
    String getModuleId();
    String getModuleName();
    List<String> getDependencies();
    ModuleState getState();
}

@Component
public class ClaimModule implements ApplicationModule {
    @Override
    public String getModuleId() {
        return "CLAIM";
    }
    
    @Override
    public List<String> getDependencies() {
        return List.of("VALIDATION", "NOTIFICATION");
    }
    
    @Override
    public ModuleState getState() {
        return ModuleState.builder()
            .services(getClaimServices())
            .entities(getClaimEntities())
            .build();
    }
}

@Component
public class ModuleRegistry {
    private final List<ApplicationModule> modules;
    
    @Autowired
    public ModuleRegistry(List<ApplicationModule> modules) {
        this.modules = modules;
    }
    
    public ApplicationModuleGraph buildDependencyGraph() {
        // Build graph of module dependencies
        return new ApplicationModuleGraph(modules);
    }
}
```

### 4.3 Cross-Module Flow Tracking

**Pattern: Request Tracing**

```java
@Component
public class FlowTracker {
    private static final ThreadLocal<FlowContext> flowContext = new ThreadLocal<>();
    
    public void startFlow(String flowId, String sourceModule, String targetModule) {
        FlowContext context = new FlowContext(flowId, sourceModule, targetModule);
        flowContext.set(context);
    }
    
    public void trackStep(String stepName, Object data) {
        FlowContext context = flowContext.get();
        if (context != null) {
            context.addStep(stepName, data);
        }
    }
    
    public FlowContext getCurrentFlow() {
        return flowContext.get();
    }
    
    public void endFlow() {
        flowContext.remove();
    }
}

// Usage
@Service
public class ClaimService {
    @Autowired
    private FlowTracker flowTracker;
    
    public ClaimDto createClaim(CreateClaimRequest request) {
        flowTracker.startFlow("CREATE_CLAIM", "CLAIM", "VALIDATION");
        
        try {
            flowTracker.trackStep("validate", request);
            validationService.validateClaim(request);
            
            flowTracker.trackStep("create", request);
            Claim claim = claimRepository.save(toEntity(request));
            
            return toDto(claim);
        } finally {
            flowTracker.endFlow();
        }
    }
}
```

---

## 5. Mapping RPG Patterns to Java

### 5.1 RPG Global Variables → Java Application Context

**RPG:**
```rpg
D ZL1              S              5 0
D MARK12           S             14
```

**Java Option 1: Application Context**
```java
@Component
public class ApplicationCounters {
    private final AtomicInteger claimListCount = new AtomicInteger(0);
    private final AtomicInteger subfileCount = new AtomicInteger(0);
    
    public int getClaimListCount() {
        return claimListCount.get();
    }
    
    public void incrementClaimListCount() {
        claimListCount.incrementAndGet();
    }
}
```

**Java Option 2: Request Context**
```java
@Component
@Scope("request")
public class ClaimListContext {
    private int recordCount;
    private String selectedClaimId;
    
    // State for one request
}
```

### 5.2 RPG External Data Structures → Java Shared Services

**RPG:**
```rpg
D NEU2             DS                  EXTERN QUALIFIED
```

**Java:**
```java
@Service
public class SharedDataStructureService {
    private final Map<String, SharedData> sharedData = new ConcurrentHashMap<>();
    
    public SharedData getSharedData(String key) {
        return sharedData.computeIfAbsent(key, k -> new SharedData());
    }
    
    public void updateSharedData(String key, SharedData data) {
        sharedData.put(key, data);
    }
}
```

### 5.3 RPG Program Calls → Java Service Calls

**RPG:**
```rpg
CALL 'HS1212'
PARM CNR
PARM DATAUF
```

**Java:**
```java
@Service
public class ClaimService {
    private final ValidationService validationService;
    
    public ClaimDto processClaim(String claimNumber, LocalDate claimDate) {
        // Equivalent to CALL 'HS1212' PARM CNR PARM DATAUF
        validationService.validateClaim(claimNumber, claimDate);
        
        // Continue processing
        return findClaim(claimNumber);
    }
}
```

### 5.4 RPG Common Areas → Java Shared Cache/State

**RPG:**
```rpg
D COMMON_AREA      DS                  BASED(COMMON_PTR)
```

**Java:**
```java
@Service
public class SharedStateService {
    @Cacheable("commonArea")
    public CommonArea getCommonArea(String areaId) {
        return commonAreaRepository.findById(areaId)
            .orElseGet(() -> createDefaultCommonArea(areaId));
    }
    
    @CacheEvict(value = "commonArea", key = "#areaId")
    public void updateCommonArea(String areaId, CommonArea area) {
        commonAreaRepository.save(area);
    }
}
```

---

## 6. Practical Example: Claim Processing Flow

### 6.1 RPG Flow (HS1210 → HS1212)

```
HS1210 (Claim List)
├── Initialize (SB10N)
│   └── Sets ZL4, MARK12, indicators
├── Build Subfile (SB100)
│   ├── Reads HSG71LF2
│   ├── Filters claims
│   └── Sets ZL1, ZL2
├── User Selection (MARK)
│   └── Sets MARK11, MARK12
└── Process Selection
    └── CALL 'HS1212' (Claim Detail)
        ├── PARM CNR (claim number)
        ├── PARM DATAUF (date)
        └── Returns to HS1210
```

### 6.2 Java Flow (Pure Java)

```java
// Request-scoped context (replaces RPG global variables)
@Component
@Scope("request")
public class ClaimListContext {
    private int recordCount;           // ZL1
    private int subfileCount;           // ZL2
    private int position;               // ZL4
    private String selectedClaimId;     // MARK12
    private ClaimSearchCriteria criteria;
}

// Service 1: Claim List
@Service
public class ClaimListService {
    @Autowired
    private ClaimListContext context;
    
    @Autowired
    private ClaimRepository claimRepository;
    
    public ClaimSearchResult initializeAndSearch(ClaimSearchCriteria criteria) {
        // Initialize (SB10N)
        context.setPosition(0);
        context.setSelectedClaimId(null);
        context.setCriteria(criteria);
        
        // Build subfile (SB100)
        return buildClaimSubfile(criteria);
    }
    
    private ClaimSearchResult buildClaimSubfile(ClaimSearchCriteria criteria) {
        List<Claim> claims = claimRepository.findByCriteria(criteria);
        
        context.setRecordCount(claims.size());
        
        return ClaimSearchResult.builder()
            .claims(claims.stream().map(this::toDto).collect(Collectors.toList()))
            .totalCount(claims.size())
            .build();
    }
}

// Service 2: Claim Detail (called from Claim List)
@Service
public class ClaimDetailService {
    @Autowired
    private ClaimRepository claimRepository;
    
    @Autowired
    private ValidationService validationService;
    
    public ClaimDto getClaimDetail(String claimNumber, LocalDate claimDate) {
        // Equivalent to CALL 'HS1212' PARM CNR PARM DATAUF
        
        // Validate
        validationService.validateClaimAccess(claimNumber, claimDate);
        
        // Load
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
            .orElseThrow(() -> new ClaimNotFoundException(claimNumber));
        
        return toDto(claim);
    }
}

// Controller: Orchestrates flow
@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    @Autowired
    private ClaimListService claimListService;
    
    @Autowired
    private ClaimDetailService claimDetailService;
    
    @GetMapping("/search")
    public ResponseEntity<ClaimSearchResult> searchClaims(
        @ModelAttribute ClaimSearchCriteria criteria
    ) {
        return ResponseEntity.ok(claimListService.initializeAndSearch(criteria));
    }
    
    @GetMapping("/{claimNumber}")
    public ResponseEntity<ClaimDto> getClaimDetail(
        @PathVariable String claimNumber,
        @RequestParam LocalDate claimDate
    ) {
        return ResponseEntity.ok(
            claimDetailService.getClaimDetail(claimNumber, claimDate)
        );
    }
}
```

---

## 7. Strategies for Preserving Global View

### 7.1 Application Metadata Registry

```java
@Component
public class ApplicationMetadataRegistry {
    private final Map<String, ModuleMetadata> modules = new ConcurrentHashMap<>();
    private final Map<String, ServiceMetadata> services = new ConcurrentHashMap<>();
    private final Map<String, EntityMetadata> entities = new ConcurrentHashMap<>();
    
    public void registerModule(ModuleMetadata metadata) {
        modules.put(metadata.getId(), metadata);
    }
    
    public ApplicationView getApplicationView() {
        return ApplicationView.builder()
            .modules(new ArrayList<>(modules.values()))
            .services(new ArrayList<>(services.values()))
            .entities(new ArrayList<>(entities.values()))
            .build();
    }
}

// Usage: Auto-register on startup
@Component
public class ClaimModuleRegistrar {
    @PostConstruct
    public void register() {
        applicationMetadataRegistry.registerModule(
            ModuleMetadata.builder()
                .id("CLAIM")
                .name("Claim Processing")
                .services(List.of("ClaimListService", "ClaimDetailService"))
                .entities(List.of("Claim", "ClaimStatus"))
                .build()
        );
    }
}
```

### 7.2 Dependency Graph Visualization

```java
@Component
public class ApplicationDependencyGraph {
    private final Map<String, List<String>> dependencies = new ConcurrentHashMap<>();
    
    public void addDependency(String moduleId, String dependsOn) {
        dependencies.computeIfAbsent(moduleId, k -> new ArrayList<>()).add(dependsOn);
    }
    
    public Graph<String> buildGraph() {
        Graph<String> graph = new Graph<>();
        dependencies.forEach((module, deps) -> {
            graph.addNode(module);
            deps.forEach(dep -> graph.addEdge(module, dep));
        });
        return graph;
    }
    
    public String visualize() {
        // Generate DOT format or JSON for visualization
        return buildGraph().toDotFormat();
    }
}
```

### 7.3 Cross-Module State Tracking

```java
@Component
public class CrossModuleStateTracker {
    private final Map<String, ModuleState> moduleStates = new ConcurrentHashMap<>();
    
    public void updateModuleState(String moduleId, ModuleState state) {
        moduleStates.put(moduleId, state);
    }
    
    public ApplicationStateSnapshot getApplicationSnapshot() {
        return ApplicationStateSnapshot.builder()
            .moduleStates(new HashMap<>(moduleStates))
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

---

## 8. Best Practices Summary

### 8.1 State Management

✅ **Do:**
- Use request/session scope for user-specific state
- Use application scope for shared caches/config
- Pass state as parameters (stateless services)
- Use ThreadLocal for request-scoped state

❌ **Don't:**
- Use static mutable variables
- Share state via global singletons (unless necessary)
- Mix business state with infrastructure state

### 8.2 Cross-Module Communication

✅ **Do:**
- Use dependency injection (Spring)
- Use events for loose coupling
- Use DTOs for data transfer
- Document dependencies

❌ **Don't:**
- Create circular dependencies
- Use direct instantiation
- Share mutable objects across modules

### 8.3 Global View Preservation

✅ **Do:**
- Register modules on startup
- Build dependency graphs
- Track cross-module flows
- Provide application metadata API

❌ **Don't:**
- Hard-code module relationships
- Ignore dependency management
- Lose traceability

---

## 9. Migration Checklist

### Phase 1: Identify Shared State
- [ ] Map RPG global variables → Java scope (application/request/session)
- [ ] Map RPG external data structures → Java shared services/DTOs
- [ ] Map RPG common areas → Java cache/shared state

### Phase 2: Design Cross-Module Communication
- [ ] Map RPG program calls → Java service calls
- [ ] Design service interfaces
- [ ] Define DTOs for data transfer
- [ ] Plan event-driven communication (if needed)

### Phase 3: Implement State Management
- [ ] Create application context (if needed)
- [ ] Create request/session contexts (if needed)
- [ ] Implement shared services
- [ ] Add caching (if needed)

### Phase 4: Preserve Global View
- [ ] Create module registry
- [ ] Build dependency graph
- [ ] Add flow tracking
- [ ] Create application metadata API

### Phase 5: Test and Validate
- [ ] Test cross-module calls
- [ ] Verify state isolation
- [ ] Validate dependency graph
- [ ] Test flow tracking

---

## 10. Example: Complete Migration Pattern

**RPG Program Structure:**
```
HS1210 (Main)
├── Global: ZL1, ZL2, MARK12
├── Calls: HS1212 (Detail)
└── Uses: HSG71LF2 (file)
```

**Java Application Structure:**
```
com.scania.warranty/
├── domain/
│   └── Claim.java
├── service/
│   ├── ClaimListService.java      (HS1210)
│   └── ClaimDetailService.java    (HS1212)
├── repository/
│   └── ClaimRepository.java        (HSG71LF2)
├── dto/
│   ├── ClaimSearchCriteria.java
│   └── ClaimSearchResult.java
├── context/
│   └── ClaimListContext.java       (@Scope("request"))
└── web/
    └── ClaimController.java
```

**Flow:**
```
HTTP Request
  → ClaimController
    → ClaimListService (uses ClaimListContext)
      → ClaimRepository
        → ClaimDetailService (cross-module call)
          → Returns to ClaimListService
            → Returns to Controller
              → HTTP Response
```

This preserves the global view while using proper Java patterns for shared state and cross-module communication.
