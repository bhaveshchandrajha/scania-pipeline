import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

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
      </div>
    </div>
  `,
  styles: [`
    .claim-detail { padding: 16px; }
    .detail-content { margin: 16px 0; }
    .hint { color: #64748b; font-size: 12px; }
    .action-btn { padding: 6px 12px; cursor: pointer; }
  `]
})
export class ClaimDetailComponent {
  companyCode: string | null = null;
  claimNumber: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.route.params.subscribe(p => {
      this.companyCode = p['companyCode'] ?? null;
      this.claimNumber = p['claimNumber'] ?? null;
    });
  }

  goBack(): void {
    window.history.back();
  }
}
