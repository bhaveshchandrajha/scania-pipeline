import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UiSchemaService } from '../services/ui-schema.service';
import { WelcomeScreenSchema } from '../models/ui-schema.model';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './welcome.component.html',
  styleUrl: './welcome.component.scss'
})
export class WelcomeComponent implements OnInit {
  schema: WelcomeScreenSchema | null = null;
  error: string | null = null;

  constructor(private uiSchemaService: UiSchemaService) {}

  /** Map display file URLs to Angular routes */
  getRoute(url: string): string {
    if (url === '/hs1210d.html' || url.startsWith('/hs1210d')) return '/claims';
    if (url === '/demo.html') return '/demo';
    return url;
  }

  ngOnInit(): void {
    this.uiSchemaService.getSchema('Welcome').subscribe({
      next: (s) => {
        if (s.type === 'welcome') {
          this.schema = s;
        }
      },
      error: (err) => (this.error = err.message || 'Failed to load schema')
    });
  }
}
