import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ClaimService } from '../services/claim.service';
import { ClaimDetailResponse, ClaimHistoryEntry } from '../models/claim.model';

@Component({
  selector: 'app-claim-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './claim-detail.component.html',
  styleUrl: './claim-detail.component.scss'
})
export class ClaimDetailComponent implements OnInit {
  companyCode: string | null = null;
  claimNumber: string | null = null;
  detail: ClaimDetailResponse | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private claimService: ClaimService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((p) => {
      this.companyCode = p['companyCode'] ?? null;
      this.claimNumber = p['claimNumber'] ?? null;
      this.loadDetail();
    });
  }

  loadDetail(): void {
    if (!this.companyCode || !this.claimNumber) {
      return;
    }
    this.loading = true;
    this.error = null;
    this.detail = null;
    this.claimService.getDetail(this.companyCode, this.claimNumber).subscribe({
      next: (d) => {
        this.detail = d;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || err.message || 'Failed to load claim';
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigateByUrl('/claims');
  }

  /** Backend sends ordered history; empty if older API. */
  get historyRows(): ClaimHistoryEntry[] {
    const d = this.detail;
    if (!d?.history?.length) {
      return [];
    }
    return d.history;
  }

  deleteClaim(): void {
    if (!this.companyCode || !this.claimNumber) {
      return;
    }
    if (!confirm(`Delete claim ${this.claimNumber}?`)) {
      return;
    }
    this.claimService.delete(this.companyCode, this.claimNumber).subscribe({
      next: () => this.router.navigateByUrl('/claims'),
      error: (err) => alert(err.error?.message || err.message || 'Delete failed')
    });
  }
}
