import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ClaimService } from '../services/claim.service';

@Component({
  selector: 'app-claim-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="claim-create">
      <h1>Create Claim (CF06 Erstellen)</h1>
      <p class="hint">Create from seeded invoices (no claim yet): 88888/003, 77777/004, or 99999/002. Invoice 12345/001 already has a claim. If you get "Invoice not found", call <code>POST /api/seed</code> first to seed demo data.</p>

      <form (ngSubmit)="onSubmit()" class="create-form">
        <div class="form-group">
          <label>Company Code</label>
          <input type="text" [(ngModel)]="companyCode" name="companyCode" required placeholder="001" />
        </div>
        <div class="form-group">
          <label>Invoice Number</label>
          <input type="text" [(ngModel)]="invoiceNumber" name="invoiceNumber" required placeholder="12345" />
        </div>
        <div class="form-group">
          <label>Invoice Date</label>
          <input type="text" [(ngModel)]="invoiceDate" name="invoiceDate" required placeholder="20240115" />
        </div>
        <div class="form-group">
          <label>Order Number</label>
          <input type="text" [(ngModel)]="orderNumber" name="orderNumber" required placeholder="001" />
        </div>
        <div class="form-group">
          <label>Workshop Type / Area</label>
          <input type="text" [(ngModel)]="workshopType" name="workshopType" required placeholder="1" />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn primary" [disabled]="submitting">Create Claim</button>
          <button type="button" class="btn" (click)="goBack()">Back</button>
        </div>
      </form>

      <div class="message success" *ngIf="successMessage">
        {{ successMessage }}
        <a routerLink="/claims" class="link">View in list</a>
      </div>
      <div class="message error" *ngIf="errorMessage">{{ errorMessage }}</div>
    </div>
  `,
  styles: [`
    .claim-create { padding: 16px; max-width: 400px; }
    .hint { color: #64748b; font-size: 13px; margin-bottom: 16px; }
    .create-form { display: flex; flex-direction: column; gap: 12px; }
    .form-group label { display: block; font-weight: 500; margin-bottom: 4px; font-size: 13px; }
    .form-group input { width: 100%; padding: 8px 10px; border: 1px solid #cbd5e0; border-radius: 4px; }
    .form-actions { display: flex; gap: 8px; margin-top: 8px; }
    .btn { padding: 8px 16px; cursor: pointer; border-radius: 4px; border: 1px solid #cbd5e0; background: #fff; }
    .btn.primary { background: #059669; color: white; border-color: #047857; }
    .btn:disabled { opacity: 0.6; cursor: not-allowed; }
    .message { margin-top: 12px; padding: 10px; border-radius: 4px; font-size: 13px; }
    .message.success { background: #d1fae5; color: #065f46; }
    .message.error { background: #fee2e2; color: #991b1b; }
    .link { margin-left: 8px; color: #047857; font-weight: 500; }
  `]
})
export class ClaimCreateComponent {
  companyCode = '001';
  invoiceNumber = '88888';
  invoiceDate = '20240115';
  orderNumber = '003';
  workshopType = '1';
  submitting = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private claimService: ClaimService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.submitting = true;
    this.successMessage = '';
    this.errorMessage = '';
    this.claimService.create({
      companyCode: this.companyCode,
      invoiceNumber: this.invoiceNumber,
      invoiceDate: this.invoiceDate,
      orderNumber: this.orderNumber,
      workshopType: this.workshopType
    }).subscribe({
      next: (claimNr) => {
        this.submitting = false;
        this.successMessage = `Claim created: ${claimNr}. Go back to see it in the list.`;
      },
      error: (err) => {
        this.submitting = false;
        const body = err.error;
        let msg = (typeof body === 'object' && body?.message) ? body.message
          : (Array.isArray(body?.details) && body.details.length) ? body.details.join(', ')
          : typeof body === 'string' ? body
          : err.message || 'Failed to create claim';
        if (msg && msg.includes('Invoice not found')) {
          msg += ' — Try POST /api/seed first to seed demo invoices.';
        }
        this.errorMessage = msg;
      }
    });
  }

  goBack(): void {
    window.history.back();
  }
}
