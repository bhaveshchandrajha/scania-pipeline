import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClaimListItem, ClaimSearchCriteria } from '../models/claim.model';

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
}
