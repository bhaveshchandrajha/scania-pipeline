/**
 * UI schema models - driven by DDS/RPG display file metadata.
 * Matches warranty_demo ui-schemas (HS1210D.json, Welcome.json).
 */
export interface UiSchemaColumn {
  id: string;
  label: string;
  dtoField: string;
  width?: number;
}

export interface UiSchemaAction {
  id: string;
  type: string;
  label: string;
  /** Declarative: URL for API call */
  url?: string;
  /** Declarative: HTTP method (GET, POST, etc.) */
  method?: string;
  /** Declarative: Route to navigate to (supports {{field}} from selected row) */
  navigate?: string;
  /** Declarative: Built-in action (historyBack, scrollToTop, openSortDialog, etc.) */
  action?: string;
  /** When true, re-execute dataSource instead of a separate API call */
  reuseDataSource?: boolean;
}

export interface UiSchemaFilter {
  id: string;
  label: string;
  type: string;
  param: string;
  default?: string;
}

export interface UiSchemaDataSource {
  method: string;
  url: string;
  params?: Record<string, { source: string; value: string }>;
}

export interface ListScreenSchema {
  screenId: string;
  type: 'list';
  title: string;
  dataSource: UiSchemaDataSource;
  columns: UiSchemaColumn[];
  filters?: UiSchemaFilter[];
  actions?: UiSchemaAction[];
}

export interface WelcomeScreenSchema {
  screenId: string;
  type: 'welcome';
  title: string;
  subtitle?: string;
  message?: string;
  links?: { label: string; url: string; description?: string; external?: boolean }[];
}

export type UiSchema = ListScreenSchema | WelcomeScreenSchema;
