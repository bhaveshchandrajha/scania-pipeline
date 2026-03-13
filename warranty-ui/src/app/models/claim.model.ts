/**
 * Claim models - match Java DTOs from warranty_demo.
 */
export interface ClaimSearchCriteria {
  companyCode?: string;
  statusFilter?: string;
  statusOperator?: string;
  chassisNumberFilter?: string;
  customerNumberFilter?: string;
  claimNumberSdeFilter?: string;
  claimTypeFilter?: string;
  ageFilterDays?: number;
  openClaimsOnly?: boolean;
  searchText?: string;
  ascending?: boolean;
  vehicleFilter?: string;
  customerFilter?: string;
  sdeClaimFilter?: string;
  minimumOnly?: boolean;
}

export interface ClaimListItem {
  companyCode?: string;
  claimNumber?: string;
  claimNr?: string;
  invoiceNumber?: string;
  invoiceNr?: string;
  formattedInvoiceDate?: string;
  invoiceDate?: string;
  chassisNumber?: string;
  chassisNr?: string;
  customerNumber?: string;
  customerNr?: string;
  customerName?: string;
  statusDescription?: string;
  demandCode?: string;
  statusCodeSde?: number;
  errorCount?: number;
  colorIndicator?: string;
  orderNumber?: string;
}
