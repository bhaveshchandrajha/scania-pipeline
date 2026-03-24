import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ClaimService } from '../services/claim.service';

@Component({
  selector: 'app-claim-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="claim-detail">
      <h1>Claim Detail</h1>
      <div class="detail-content" *ngIf="companyCode && claimNumber">
        <p><strong>Company:</strong> {{ companyCode }}</p>
        <p><strong>Claim Nr.:</strong> {{ claimNumber }}</p>
        <p class="hint">Full claim detail view can be extended with API call to fetch claim data.</p>
      </div>
      <div class="actions">
        <button type="button" class="action-btn" (click)="goBack()">Back</button>
        <button type="button" class="action-btn action-delete" (click)="deleteClaim()">Delete Claim</button>
      </div>
    </div>
  `,
  styles: [`
    .claim-detail { padding: 16px; }
    .detail-content { margin: 16px 0; }
    .hint { color: #64748b; font-size: 12px; }
    .action-btn { padding: 6px 12px; cursor: pointer; margin-right: 8px; }
    .action-delete { background: #dc2626; color: white; border: none; }
  `]
})
export class ClaimDetailComponent {
  companyCode: string | null = null;
  claimNumber: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private claimService: ClaimService
  ) {
    this.route.params.subscribe(p => {
      this.companyCode = p['companyCode'] ?? null;
      this.claimNumber = p['claimNumber'] ?? null;
    });
  }

  goBack(): void {
    window.history.back();
  }

  deleteClaim(): void {
    if (!this.companyCode || !this.claimNumber) return;
    if (!confirm(`Delete claim ${this.claimNumber}?`)) return;
    this.claimService.delete(this.companyCode, this.claimNumber).subscribe({
      next: () => this.router.navigateByUrl('/claims'),
      error: (err) => alert(err.error?.message || err.message || 'Delete failed')
    });
  }
}
