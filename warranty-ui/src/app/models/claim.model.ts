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
  /** Repair date (yyyyMMdd as string from API). */
  repairDate?: string;
  chassisNumber?: string;
  chassisNr?: string;
  customerNumber?: string;
  customerNr?: string;
  customerName?: string;
  statusDescription?: string;
  statusText?: string;
  statusCode?: number;
  demandCode?: string;
  statusCodeSde?: number;
  errorCount?: number;
  colorIndicator?: string;
  orderNumber?: string;
}

export interface ClaimErrorSummary {
  errorNr: string;
  sequenceNr: string;
  description: string;
  demandCode: string;
  processingStatus: number;
}

/** Ordered timeline: STATUS first, then ERROR lines (matches backend ClaimHistoryEntryDto). */
export interface ClaimHistoryEntry {
  entryType: string;
  title: string;
  detail: string;
  reference: string;
}

export interface ClaimDetailResponse {
  claim: ClaimListItem;
  history: ClaimHistoryEntry[];
  errors: ClaimErrorSummary[];
}

export interface FailedClaimItem {
  id: number;
  companyCode: string;
  invoiceNr: string;
  invoiceDate: string;
  workshopCode: string;
  failureReason: string;
  repairAgeDays: number | null;
  failedAt: string;
}
