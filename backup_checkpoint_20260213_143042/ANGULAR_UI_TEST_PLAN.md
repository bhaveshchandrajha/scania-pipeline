# Angular UI Test Plan: HS1210D Without DDS Source

**Objective:** Build a functional Angular UI for the `HS1210D` display file using only:
- Migrated Java service (`HS1210_n404.java`)
- Display file metadata
- RPG narrative and snippets

---

## Step 1: Analyze Java Service Structure

### Service: `ClaimProcessingService` (HS1210_n404.java)

**Key Methods:**
1. `buildClaimSubfile(SubfileFilter, String, boolean)` → `SubfileResult`
   - Loads and filters claim list
   - Returns subfile records for display

2. `initializeSubfileProcessing(SubfileContext)`
   - Initializes screen state variables

3. `processMarkSelection(SubfileContext)`
   - Handles user selection marks

**DTOs Identified:**
- `SubfileFilter` - Filter criteria
- `SubfileResult` - Display data
- `SubfileContext` - Screen state
- `SubfileRecord` - Individual claim row

**Entities:**
- `HSG71LF2` - Claim entity
- `HSG73PF`, `HSAHKLF3`, etc. - Related entities

---

## Step 2: Design REST API Layer (Java)

### Endpoints Needed:

```java
@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    
    @Autowired
    private ClaimProcessingService service;
    
    // Load claim list (subfile)
    @GetMapping("/subfile")
    public ResponseEntity<SubfileResult> getClaimSubfile(
        @RequestParam(required = false) String pkz,
        @RequestParam(required = false, defaultValue = "true") boolean ascending,
        @ModelAttribute SubfileFilter filter
    ) {
        SubfileResult result = service.buildClaimSubfile(filter, pkz, ascending);
        return ResponseEntity.ok(result);
    }
    
    // Initialize screen
    @PostMapping("/initialize")
    public ResponseEntity<SubfileContext> initializeScreen(
        @RequestBody(required = false) SubfileContext context
    ) {
        if (context == null) {
            context = new SubfileContext();
        }
        service.initializeSubfileProcessing(context);
        return ResponseEntity.ok(context);
    }
    
    // Handle selection
    @PostMapping("/selection")
    public ResponseEntity<SubfileContext> processSelection(
        @RequestBody SubfileContext context
    ) {
        service.processMarkSelection(context);
        return ResponseEntity.ok(context);
    }
}
```

**Action Items:**
- [ ] Create `ClaimController.java`
- [ ] Create request/response DTOs matching service signatures
- [ ] Add error handling
- [ ] Add validation

---

## Step 3: Create Angular TypeScript Interfaces

### File: `src/app/models/claim.models.ts`

```typescript
// Match Java DTOs
export interface SubfileFilter {
  useLogicalFile?: boolean;
  filoff?: string;  // "J" for open claims
  statusCode?: string;
  // ... other filter fields from Java
}

export interface SubfileRecord {
  claimNr?: string;
  rechNr?: string;
  statusCode?: string;
  // ... fields from SubfileRecord in Java
}

export interface SubfileResult {
  zl1: number;
  zl2: number;
  records: SubfileRecord[];
}

export interface SubfileContext {
  mark11?: string;
  mark12?: string;
  mark21?: string;
  mark22?: string;
  zl4?: number;
  sub15x?: string;
  // ... other context fields
}
```

**Action Items:**
- [ ] Create TypeScript interfaces matching Java DTOs
- [ ] Document field purposes from narrative
- [ ] Add validation rules (if known)

---

## Step 4: Create Angular Service

### File: `src/app/services/claim.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SubfileResult, SubfileFilter, SubfileContext } from '../models/claim.models';

@Injectable({
  providedIn: 'root'
})
export class ClaimService {
  private apiUrl = 'http://localhost:8080/api/claims';

  constructor(private http: HttpClient) {}

  getClaimSubfile(filter: SubfileFilter, pkz?: string, ascending: boolean = true): Observable<SubfileResult> {
    let params = new HttpParams();
    if (pkz) params = params.set('pkz', pkz);
    params = params.set('ascending', ascending.toString());
    
    return this.http.get<SubfileResult>(`${this.apiUrl}/subfile`, { params });
  }

  initializeScreen(context?: SubfileContext): Observable<SubfileContext> {
    return this.http.post<SubfileContext>(`${this.apiUrl}/initialize`, context || {});
  }

  processSelection(context: SubfileContext): Observable<SubfileContext> {
    return this.http.post<SubfileContext>(`${this.apiUrl}/selection`, context);
  }
}
```

**Action Items:**
- [ ] Create Angular service
- [ ] Implement HTTP calls
- [ ] Add error handling
- [ ] Add loading state management

---

## Step 5: Create Angular Component

### File: `src/app/components/claim-list/claim-list.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { ClaimService } from '../../services/claim.service';
import { SubfileResult, SubfileFilter, SubfileContext, SubfileRecord } from '../../models/claim.models';

@Component({
  selector: 'app-claim-list',
  templateUrl: './claim-list.component.html',
  styleUrls: ['./claim-list.component.css']
})
export class ClaimListComponent implements OnInit {
  filter: SubfileFilter = {};
  context: SubfileContext = {};
  result: SubfileResult | null = null;
  loading = false;
  error: string | null = null;

  constructor(private claimService: ClaimService) {}

  ngOnInit(): void {
    this.initializeScreen();
  }

  initializeScreen(): void {
    this.claimService.initializeScreen().subscribe({
      next: (context) => {
        this.context = context;
        this.loadClaims();
      },
      error: (err) => this.error = err.message
    });
  }

  loadClaims(): void {
    this.loading = true;
    this.claimService.getClaimSubfile(this.filter).subscribe({
      next: (result) => {
        this.result = result;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

  handleSelection(record: SubfileRecord, action: string): void {
    // Map action codes: 2=Change, 4=Delete, 5=Display, etc.
    this.context.mark11 = action;
    this.claimService.processSelection(this.context).subscribe({
      next: (updatedContext) => {
        this.context = updatedContext;
        // Handle action (navigate, show dialog, etc.)
      }
    });
  }

  applyFilters(): void {
    this.loadClaims();
  }
}
```

**Action Items:**
- [ ] Create component class
- [ ] Implement initialization logic
- [ ] Implement filter handling
- [ ] Implement selection handling
- [ ] Add error handling

---

## Step 6: Create Angular Template

### File: `src/app/components/claim-list/claim-list.component.html`

```html
<div class="claim-list-container">
  <h2>Warranty Claims (HS1210D)</h2>
  
  <!-- Filter Section -->
  <div class="filter-section">
    <form (ngSubmit)="applyFilters()">
      <label>
        <input type="checkbox" [(ngModel)]="filter.useLogicalFile" name="useLogicalFile">
        Use Logical File
      </label>
      <label>
        <input type="checkbox" [(ngModel)]="filter.filoff" name="filoff" value="J">
        Open Claims Only
      </label>
      <button type="submit">Apply Filters</button>
    </form>
  </div>

  <!-- Error Display -->
  <div *ngIf="error" class="error">{{ error }}</div>

  <!-- Loading State -->
  <div *ngIf="loading">Loading claims...</div>

  <!-- Claim List Table -->
  <table *ngIf="result && !loading" class="claim-table">
    <thead>
      <tr>
        <th>Select</th>
        <th>Claim #</th>
        <th>Rech #</th>
        <th>Status</th>
        <!-- Add more columns based on SubfileRecord fields -->
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr *ngFor="let record of result.records">
        <td>
          <input type="checkbox" 
                 [checked]="context.mark12 === record.claimNr"
                 (change)="handleSelection(record, '2')">
        </td>
        <td>{{ record.claimNr }}</td>
        <td>{{ record.rechNr }}</td>
        <td>{{ record.statusCode }}</td>
        <td>
          <button (click)="handleSelection(record, '2')">Change</button>
          <button (click)="handleSelection(record, '4')">Delete</button>
          <button (click)="handleSelection(record, '5')">Display</button>
          <!-- More action buttons -->
        </td>
      </tr>
    </tbody>
  </table>

  <!-- Pagination/Count Info -->
  <div *ngIf="result">
    <p>Total: {{ result.zl1 }} records</p>
  </div>
</div>
```

**Action Items:**
- [ ] Design modern web UI (not 5250 terminal style)
- [ ] Add form fields based on filter DTO
- [ ] Add table columns based on SubfileRecord
- [ ] Add action buttons (2=Change, 4=Delete, etc.)
- [ ] Style with CSS

---

## Step 7: Testing Checklist

### Backend (Java REST API)
- [ ] REST endpoints respond correctly
- [ ] DTOs serialize/deserialize properly
- [ ] Error handling works
- [ ] CORS configured for Angular

### Frontend (Angular)
- [ ] Component loads without errors
- [ ] Service calls backend successfully
- [ ] Data displays in table
- [ ] Filters work
- [ ] Selection handling works
- [ ] Error messages display

### Integration
- [ ] End-to-end flow works
- [ ] Data matches between Java and Angular
- [ ] Actions trigger correct backend operations

---

## Limitations Without DDS Source

**What We're Missing:**
1. **Exact field layout** - Field positions, grouping
2. **Field labels** - User-facing text
3. **Field attributes** - Required, hidden, display-only
4. **Validation rules** - Field-level validation
5. **Indicator logic** - Conditional field visibility
6. **Screen sections** - Record format boundaries

**Workarounds:**
- Use modern web UI patterns (not terminal layout)
- Infer field purposes from variable names and narrative
- Add validation based on business logic
- Design responsive, user-friendly interface

---

## Expected Outcome

**Functional Angular UI that:**
- ✅ Connects to Java backend
- ✅ Displays claim list
- ✅ Supports filtering
- ✅ Handles user selections
- ✅ Performs CRUD operations
- ❌ May not match original 5250 screen layout exactly
- ❌ Field labels may need refinement
- ❌ Validation rules may need adjustment

**When DDS Source Arrives:**
- Can refine UI to match original layout
- Can add exact field attributes
- Can implement indicator-based logic
- Can generate forms automatically from DDS
