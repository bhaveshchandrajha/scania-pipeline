import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { UiSchemaService } from '../services/ui-schema.service';
import { ClaimService } from '../services/claim.service';
import { ListScreenSchema, UiSchemaAction } from '../models/ui-schema.model';
import { ClaimListItem, ClaimSearchCriteria } from '../models/claim.model';

@Component({
  selector: 'app-claims-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './claims-list.component.html',
  styleUrl: './claims-list.component.scss'
})
export class ClaimsListComponent implements OnInit {
  schema: ListScreenSchema | null = null;
  claims: ClaimListItem[] = [];
  loading = false;
  error: string | null = null;
  selectedClaim: ClaimListItem | null = null;

  companyCode = '001';
  openClaimsOnly = false;
  ascending = true;
  showFilterDialog = false;

  constructor(
    private uiSchemaService: UiSchemaService,
    private claimService: ClaimService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.uiSchemaService.getSchema('HS1210D').subscribe({
      next: (s) => {
        if (s.type === 'list') {
          this.schema = s;
          this.companyCode =
            s.dataSource.params?.['companyCode']?.value ?? '001';
          this.loadClaims();
        }
      },
      error: (err) => (this.error = err.message || 'Failed to load schema')
    });
  }

  loadClaims(): void {
    if (!this.schema) return;
    this.loading = true;
    this.error = null;

    const criteria: ClaimSearchCriteria = {
      companyCode: this.companyCode || '001',
      openClaimsOnly: this.openClaimsOnly,
      ascending: this.ascending
    };

    this.claimService.search(criteria).subscribe({
      next: (data) => {
        this.claims = Array.isArray(data) ? data : (data as any)?.claims ?? [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Failed to load claims';
        this.loading = false;
      }
    });
  }

  onRefresh(): void {
    this.loadClaims();
  }

  selectRow(item: ClaimListItem): void {
    this.selectedClaim = this.selectedClaim === item ? null : item;
  }

  /**
   * Generic action handler – uses declarative schema (action.url, action.method, action.navigate)
   * so no hardcoded switch is needed. Schema-driven behavior from migration pipeline.
   */
  onAction(action: UiSchemaAction): void {
    if (action.requiresSelection && !this.selectedClaim) {
      alert('Please select a claim row first.');
      return;
    }
    if (action.action === 'historyBack') {
      window.history.back();
      return;
    }
    if (action.reuseDataSource) {
      this.loadClaims();
      return;
    }
    if (action.navigate) {
      const path = this.resolveNavigatePath(action.navigate);
      const hasPlaceholders = action.navigate.includes('{{') && action.navigate.includes('}}');
      if (hasPlaceholders && !this.selectedClaim) {
        alert('Please select a claim row first.');
        return;
      }
      if (path && path.replace(/\//g, '').length > 0) {
        this.router.navigateByUrl(path.startsWith('/') ? path : '/' + path);
      }
      return;
    }
    if (action.type === 'delete' || (action.url && action.method === 'DELETE')) {
      this.executeDeleteAction(action);
      return;
    }
    if (action.type === 'apiCall' || (action.url && action.method && action.method !== 'DELETE')) {
      this.executeApiCall(action);
      return;
    }
    switch (action.action) {
      case 'scrollToTop':
        window.scrollTo({ top: 0, behavior: 'smooth' });
        break;
      case 'toggleSortOrder':
        this.ascending = !this.ascending;
        this.loadClaims();
        break;
      case 'openSortDialog':
        this.ascending = !this.ascending;
        this.loadClaims();
        break;
      case 'openFilterDialog':
        this.showFilterDialog = !this.showFilterDialog;
        break;
      case 'showOperatorGuidance':
        alert('Bedienerführung (Operator Guidance): Help for this screen is available.'); // TODO: load from schema or config
        break;
      case 'selectAllOpenInvoices':
        this.openClaimsOnly = true;
        this.loadClaims();
        break;
      default:
        break;
    }
  }

  private executeApiCall(action: UiSchemaAction): void {
    const url = this.resolveNavigatePath(action.url || '');
    if (!url) {
      alert('Cannot execute: missing URL or selected row.');
      return;
    }
    const method = (action.method || 'POST').toUpperCase();
    const req = this.http.request<unknown>(method, url, { body: null });
    req.subscribe({
      next: () => {
        this.loadClaims();
      },
      error: (err) => (this.error = err.error?.message || err.message || 'Request failed')
    });
  }

  private executeDeleteAction(action: UiSchemaAction): void {
    const row = this.selectedClaim as Record<string, unknown> | null;
    if (!row) return;
    const companyCode = row['companyCode'] != null ? String(row['companyCode']) : '';
    const claimNumber = row['claimNumber'] != null ? String(row['claimNumber']) : '';
    if (!companyCode || !claimNumber) {
      alert('Cannot delete: missing company code or claim number.');
      return;
    }
    if (!confirm(`Delete claim ${claimNumber}?`)) return;
    this.claimService.delete(companyCode, claimNumber).subscribe({
      next: () => {
        this.selectedClaim = null;
        this.loadClaims();
      },
      error: (err) => (this.error = err.error?.message || err.message || 'Delete failed')
    });
  }

  private resolveNavigatePath(template: string): string {
    const row = this.selectedClaim as Record<string, unknown> | null;
    if (!row) {
      return template.replace(/\{\{[^}]+\}\}/g, '');
    }
    return template.replace(/\{\{(\w+)\}\}/g, (_, key) => {
      const val = row[key];
      return val != null ? String(val) : '';
    });
  }

  getValue(item: ClaimListItem, dtoField: string): string | number | undefined {
    return (item as Record<string, unknown>)[dtoField] as string | number | undefined;
  }
}
