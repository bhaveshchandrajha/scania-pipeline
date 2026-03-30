import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ClaimService } from '../services/claim.service';
import { FailedClaimItem } from '../models/claim.model';

@Component({
  selector: 'app-failed-claims',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './failed-claims.component.html',
  styleUrl: './failed-claims.component.scss'
})
export class FailedClaimsComponent implements OnInit {
  companyCode = '001';
  rows: FailedClaimItem[] = [];
  loading = false;
  error: string | null = null;

  constructor(private claimService: ClaimService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.claimService.listFailedClaims(this.companyCode || '001').subscribe({
      next: (data) => {
        this.rows = Array.isArray(data) ? data : [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || err.message || 'Failed to load';
        this.loading = false;
      }
    });
  }
}
