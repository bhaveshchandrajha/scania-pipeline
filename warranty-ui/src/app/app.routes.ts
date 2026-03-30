import { Routes } from '@angular/router';
import { WelcomeComponent } from './welcome/welcome.component';
import { ClaimsListComponent } from './claims-list/claims-list.component';
import { ClaimDetailComponent } from './claim-detail/claim-detail.component';
import { ClaimCreateComponent } from './claim-create/claim-create.component';
import { FailedClaimsComponent } from './failed-claims/failed-claims.component';

export const routes: Routes = [
  { path: '', component: WelcomeComponent },
  { path: 'claims', component: ClaimsListComponent },
  { path: 'claims/failed', component: FailedClaimsComponent },
  { path: 'claims/create', component: ClaimCreateComponent },
  { path: 'claims/:companyCode/:claimNumber', component: ClaimDetailComponent },
  { path: 'demo', redirectTo: '/', pathMatch: 'full' },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];
