import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ClaimDetailResponse,
  ClaimListItem,
  ClaimSearchCriteria,
  FailedClaimItem
} from '../models/claim.model';

@Injectable({ providedIn: 'root' })
export class ClaimService {
  private readonly apiBase = '/api/claims';

  constructor(private http: HttpClient) {}

  search(criteria: ClaimSearchCriteria): Observable<ClaimListItem[]> {
    return this.http.post<ClaimListItem[]>(`${this.apiBase}/search`, criteria);
  }

  create(params: {
    companyCode: string;
    invoiceNumber: string;
    invoiceDate: string;
    orderNumber: string;
    workshopType: string;
  }): Observable<string> {
    const httpParams = new HttpParams()
      .set('companyCode', params.companyCode)
      .set('invoiceNumber', params.invoiceNumber)
      .set('invoiceDate', params.invoiceDate)
      .set('orderNumber', params.orderNumber)
      .set('workshopType', params.workshopType);
    return this.http.post(`${this.apiBase}/create`, null, { params: httpParams, responseType: 'text' });
  }

  delete(companyCode: string, claimNumber: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBase}/${companyCode}/${claimNumber}`);
  }

  /** Claim header + ordered history (current status first, then error subfile). */
  getDetail(companyCode: string, claimNumber: string): Observable<ClaimDetailResponse> {
    return this.http.get<ClaimDetailResponse>(`${this.apiBase}/${companyCode}/${claimNumber}`);
  }

  /** Validation failures (e.g. repair date &gt; 19 days), newest first. */
  listFailedClaims(companyCode: string): Observable<FailedClaimItem[]> {
    return this.http.get<FailedClaimItem[]>(`${this.apiBase}/failed`, {
      params: { companyCode }
    });
  }
}
