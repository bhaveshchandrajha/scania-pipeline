import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UiSchema } from '../models/ui-schema.model';

@Injectable({ providedIn: 'root' })
export class UiSchemaService {
  private readonly apiBase = '/api/ui-schemas';

  constructor(private http: HttpClient) {}

  getSchema(screenId: string): Observable<UiSchema> {
    return this.http.get<UiSchema>(`${this.apiBase}/${screenId}`);
  }
}
