// ---------------------------------------------------------------------------
// Shared API envelope returned by every Spring Boot endpoint.
// ---------------------------------------------------------------------------

export interface ApiMeta {
  page: number | null;
  size: number | null;
  totalElements: number | null;
  totalPages: number | null;
  generatedAt: string | null;
}

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
  meta: ApiMeta | null;
  timestamp: string;
  traceId: string | null;
}

// ---------------------------------------------------------------------------
// Auth
// ---------------------------------------------------------------------------

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface MeResponse {
  id: string;
  username: string;
  email: string;
  fullName: string;
  sessionId: string;
  roles: string[];
  permissions: string[];
  lastSuccessfulLoginAt: string | null;
}

export interface SessionResponse {
  id: string;
  familyId: string;
  ipAddress: string | null;
  userAgent: string | null;
  issuedAt: string;
  lastActivityAt: string;
  expiresAt: string;
  revokedAt: string | null;
  current: boolean;
}

export interface FailedAttemptResponse {
  id: string;
  username: string;
  ipAddress: string;
  attemptedAt: string;
  reason: string;
}

export interface ForgotPasswordStatusResponse {
  valid: boolean;
  expiresAt: string | null;
}

// ---------------------------------------------------------------------------
// Common option / list wrappers
// ---------------------------------------------------------------------------

export interface OptionItem {
  id: string;
  label: string;
}

// ---------------------------------------------------------------------------
// Audit log
// ---------------------------------------------------------------------------

export interface AuditLogResponse {
  id: string;
  method: string;
  path: string;
  status: number;
  payload: string | null;
  durationMs: number;
  userId: string | null;
  createdAt: string;
}

// ---------------------------------------------------------------------------
// Deals
// ---------------------------------------------------------------------------

export interface DealResponse {
  id: string;
  documentNo: string;
  dealTypeId: string;
  instrumentCodeId: string | null;
  userId: string | null;
  approvedById: string | null;
  rejectedById: string | null;
  submittedById: string | null;
  verifiedById: string | null;
  currencyId: string | null;
  counterCurrencyId: string | null;
  ourNostroId: string | null;
  theirNostroId: string | null;
  counterpartyId: string | null;
  iiCounterpartyId: string | null;
  iiiCounterpartyId: string | null;
  allotedAmt: number | null;
  allotedUnits: number | null;
  amount: number | null;
  bidAmt: number | null;
  contraAmt: number | null;
  coupon: number | null;
  discountedPricePerUnit: number | null;
  discountedValue: number | null;
  effectiveYield: number | null;
  exchangeRate: number | null;
  iiRate: number | null;
  iiiRate: number | null;
  interestRate: number | null;
  payableAmt: number | null;
  rolloverPreviousRate: number | null;
  rolloverValueAmount: number | null;
  totalCost: number | null;
  totalPayable: number | null;
  tenureInDays: number | null;
  bbgCode: string | null;
  bondCode: string | null;
  contactMode: string | null;
  cusipNo: string | null;
  description: string | null;
  direction: string | null;
  direction2: string | null;
  docAction: string | null;
  docStatus: string | null;
  help: string | null;
  iiContactMode: string | null;
  iiContactPersonOrCode: string | null;
  iiiContactMode: string | null;
  iiiContactPersonOrCode: string | null;
  interestPaymentFrequency: string | null;
  marketType: string | null;
  memoRefNo: string | null;
  ourCustodian: string | null;
  theirCustodian: string | null;
  rejectReason: string | null;
  rolloverDealRefNo: string | null;
  rolloverOption: string | null;
  rolloverRemarks: string | null;
  series: string | null;
  trader: string | null;
  biddingDate: string | null;
  dateApproved: string | null;
  dateRejected: string | null;
  dateSubmitted: string | null;
  dateVerified: string | null;
  dealDate: string | null;
  dealTime: string | null;
  lastInterestPaymentDate: string | null;
  maturityDate: string | null;
  memoApprovedDate: string | null;
  nextInterestPaymentDate: string | null;
  originalIssueDate: string | null;
  rolloverPreviousValueDate: string | null;
  valueDate: string | null;
  active: boolean;
  rollover: boolean;
  processed: boolean;
  createdBy: string | null;
  createdAt: string | null;
  updatedBy: string | null;
  updatedAt: string | null;
  version: number;
}

export interface DealRequest {
  dealTypeId: string;
  instrumentCodeId: string | null;
  userId: string | null;
  approvedById: string | null;
  rejectedById: string | null;
  submittedById: string | null;
  verifiedById: string | null;
  currencyId: string | null;
  counterCurrencyId: string | null;
  ourNostroId: string | null;
  theirNostroId: string | null;
  counterpartyId: string | null;
  iiCounterpartyId: string | null;
  iiiCounterpartyId: string | null;
  allotedAmt: number | null;
  allotedUnits: number | null;
  amount: number | null;
  bidAmt: number | null;
  contraAmt: number | null;
  coupon: number | null;
  discountedPricePerUnit: number | null;
  discountedValue: number | null;
  effectiveYield: number | null;
  exchangeRate: number | null;
  iiRate: number | null;
  iiiRate: number | null;
  interestRate: number | null;
  payableAmt: number | null;
  rolloverPreviousRate: number | null;
  rolloverValueAmount: number | null;
  totalCost: number | null;
  totalPayable: number | null;
  tenureInDays: number | null;
  bbgCode: string | null;
  bondCode: string | null;
  contactMode: string | null;
  cusipNo: string | null;
  description: string | null;
  direction: string | null;
  direction2: string | null;
  docAction: string | null;
  docStatus: string | null;
  help: string | null;
  iiContactMode: string | null;
  iiContactPersonOrCode: string | null;
  iiiContactMode: string | null;
  iiiContactPersonOrCode: string | null;
  interestPaymentFrequency: string | null;
  marketType: string | null;
  memoRefNo: string | null;
  ourCustodian: string | null;
  theirCustodian: string | null;
  rejectReason: string | null;
  rolloverDealRefNo: string | null;
  rolloverOption: string | null;
  rolloverRemarks: string | null;
  series: string | null;
  trader: string | null;
  biddingDate: string | null;
  dateApproved: string | null;
  dateRejected: string | null;
  dateSubmitted: string | null;
  dateVerified: string | null;
  dealDate: string | null;
  dealTime: string | null;
  lastInterestPaymentDate: string | null;
  maturityDate: string | null;
  memoApprovedDate: string | null;
  nextInterestPaymentDate: string | null;
  originalIssueDate: string | null;
  rolloverPreviousValueDate: string | null;
  valueDate: string | null;
  active: boolean | null;
  rollover: boolean | null;
  processed: boolean | null;
  version: number | null;
}

export interface DealAttachmentResponse {
  id: string;
  dealId: string;
  originalFileName: string;
  storedFileName: string;
  contentType: string;
  fileSize: number;
  uploadedById: string | null;
  uploadedByUsername: string | null;
  uploadedAt: string;
}

// ---------------------------------------------------------------------------
// Master data
// ---------------------------------------------------------------------------

export interface CounterpartyResponse {
  id: string;
  value: string;
  name: string;
  description: string | null;
  address: string | null;
  userId: string | null;
  bloombergCode: string | null;
  countryId: string | null;
  comments: string | null;
  contactName: string | null;
  email: string | null;
  investmentGrade: string | null;
  ownBank: boolean;
  limitType: string | null;
  limitValue: number | null;
  location: string | null;
  phone: string | null;
  position: string | null;
  reutersDealingCode: string | null;
  active: boolean;
  version: number;
}

export interface CounterpartyLimitResponse {
  id: string;
  counterpartyId: string;
  counterpartyName: string | null;
  dateFrom: string | null;
  dateTo: string | null;
  description: string | null;
  limitCriteria: string | null;
  limitType: string | null;
  limitValue: number | null;
  active: boolean;
  version: number;
}

export interface CurrencyResponse {
  id: string;
  isoCode: string;
  curSymbol: string | null;
  description: string;
  stdPrecision: number;
  costingPrecision: number;
  euro: boolean;
  emuMember: boolean;
  emuEntryDate: string | null;
  emuRate: number | null;
  roundOffFactor: number | null;
  active: boolean;
  version: number;
}

export interface CountryResponse {
  id: string;
  value: string;
  name: string;
  active: boolean;
  createdBy: string | null;
  createdAt: string | null;
  updatedBy: string | null;
  updatedAt: string | null;
  version: number;
}

export interface DealTypeResponse {
  id: string;
  value: string;
  name: string;
  description: string | null;
  contactMode: string | null;
  instrumentCodeApplicable: boolean;
  limitType: string | null;
  limitValue: number | null;
  active: boolean;
  version: number;
}

export interface DealTypeLimitResponse {
  id: string;
  dealTypeId: string;
  dealTypeName: string | null;
  currencyId: string | null;
  currencyIsoCode: string | null;
  dateFrom: string | null;
  dateTo: string | null;
  description: string | null;
  limitCriteria: string | null;
  limitType: string | null;
  limitValue: number | null;
  active: boolean;
  version: number;
}

export interface FiscalYearResponse {
  id: string;
  value: string;
  name: string;
  startDate: string;
  endDate: string;
  createdBy: string | null;
  updatedBy: string | null;
  createdAt: string | null;
  updatedAt: string | null;
  version: number;
}

export interface InstrumentCodeResponse {
  id: string;
  dealTypeId: string;
  dealTypeName: string | null;
  value: string;
  name: string;
  description: string | null;
  active: boolean;
  version: number;
}

export interface NostroResponse {
  id: string;
  counterpartyId: string;
  currencyId: string;
  currencyIsoCode: string;
  value: string;
  name: string;
  description: string | null;
  glAcNo: string | null;
  iban: string | null;
  swiftCode: string | null;
  active: boolean;
  version: number;
}

export interface ExchangeRateResponse {
  id: string;
  baseCurrencyId: string;
  baseCurrencyIsoCode: string;
  unit: number;
  currencyId: string;
  currencyIsoCode: string;
  currencyDescription: string;
  rate: number;
  rateDate: string;
  version: number;
}

// ---------------------------------------------------------------------------
// Users / RBAC
// ---------------------------------------------------------------------------

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  fullName: string;
  mobile: string | null;
  address: string | null;
  positionId: string | null;
  positionName: string | null;
  hasImage: boolean;
  enabled: boolean;
  expired: boolean;
  lockedUntil: string | null;
  lastSuccessfulLoginAt: string | null;
  inactiveExpireAt: string | null;
  roles: string[];
  version: number;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
  mobile: string | null;
  address: string | null;
  positionId: string | null;
  roleIds: string[];
}

export interface UpdateUserRequest {
  email: string;
  fullName: string;
  mobile: string | null;
  address: string | null;
  positionId: string | null;
  enabled: boolean;
  roleIds: string[];
  version: number;
}

export interface RoleResponse {
  id: string;
  code: string;
  name: string;
  description: string | null;
  permissions: string[];
  version: number;
}

export interface PermissionResponse {
  id: string;
  code: string;
  name: string;
  description: string | null;
  version: number;
}

export interface UserApiAccessResponse {
  id: string;
  permissionId: string;
  permissionCode: string;
  effect: string;
  version: number;
}

// ---------------------------------------------------------------------------
// Positions
// ---------------------------------------------------------------------------

export interface PositionResponse {
  id: string;
  code: string;
  name: string;
  description: string | null;
  version: number;
}

// ---------------------------------------------------------------------------
// Errors
// ---------------------------------------------------------------------------

export interface ApiErrorBody {
  success: boolean;
  code: string;
  message: string;
  data: unknown;
  meta: ApiMeta | null;
  timestamp: string;
  traceId: string | null;
}

// ---------------------------------------------------------------------------
// Deal create/update request (mirrors DealRequest; numbers as JSON numbers).
// ---------------------------------------------------------------------------

export interface CounterpartyRequest {
  value: string;
  name: string;
  description: string | null;
  address: string | null;
  userId: string | null;
  bloombergCode: string | null;
  countryId: string | null;
  comments: string | null;
  contactName: string | null;
  email: string | null;
  investmentGrade: string | null;
  ownBank: boolean;
  limitType: string | null;
  limitValue: number | null;
  location: string | null;
  phone: string | null;
  position: string | null;
  reutersDealingCode: string | null;
  active: boolean;
  version: number;
}

export interface CounterpartyLimitRequest {
  counterpartyId: string;
  dateFrom: string | null;
  dateTo: string | null;
  description: string | null;
  limitCriteria: string | null;
  limitType: string | null;
  limitValue: number | null;
  active: boolean;
  version: number;
}

export interface CurrencyRequest {
  isoCode: string;
  curSymbol: string | null;
  description: string;
  stdPrecision: number;
  costingPrecision: number;
  euro: boolean;
  emuMember: boolean;
  emuEntryDate: string | null;
  emuRate: number | null;
  roundOffFactor: number | null;
  active: boolean;
  version: number;
}

export interface DealTypeRequest {
  value: string;
  name: string;
  description: string | null;
  contactMode: string | null;
  instrumentCodeApplicable: boolean;
  limitType: string | null;
  limitValue: number | null;
  active: boolean;
  version: number;
}

export interface DealTypeLimitRequest {
  dealTypeId: string;
  currencyId: string | null;
  dateFrom: string | null;
  dateTo: string | null;
  description: string | null;
  limitCriteria: string | null;
  limitType: string | null;
  limitValue: number | null;
  active: boolean;
  version: number;
}

export interface FiscalYearRequest {
  value: string;
  name: string;
  startDate: string;
  endDate: string;
  version: number;
}

export interface InstrumentCodeRequest {
  dealTypeId: string;
  value: string;
  name: string;
  description: string | null;
  active: boolean;
  version: number;
}

export interface NostroRequest {
  counterpartyId: string;
  currencyId: string;
  value: string;
  name: string;
  description: string | null;
  glAcNo: string | null;
  iban: string | null;
  swiftCode: string | null;
  active: boolean;
  version: number;
}

export interface ExchangeRateRequest {
  baseCurrencyId: string;
  unit: number;
  currencyId: string;
  rate: number;
  rateDate: string;
}

export interface ExchangeRateImportResponse {
  id: string;
  baseCurrencyIsoCode: string;
  currencyIsoCode: string;
  rate: number;
  rateDate: string;
}

export interface CountryRequest {
  value: string;
  name: string;
  active: boolean;
  version: number;
}

export interface DealTypeRequest {
  value: string;
  name: string;
  description: string | null;
  contactMode: string | null;
  instrumentCodeApplicable: boolean;
  limitType: string | null;
  limitValue: number | null;
  active: boolean;
  version: number;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
  mobile: string | null;
  address: string | null;
  positionId: string | null;
  roleIds: string[];
}

export interface UpdateUserRequest {
  email: string;
  fullName: string;
  mobile: string | null;
  address: string | null;
  positionId: string | null;
  enabled: boolean;
  roleIds: string[];
  version: number;
}

export interface RoleRequest {
  code: string;
  name: string;
  description: string | null;
  permissions: string[];
  version: number;
}

export interface PositionResponse {
  id: string;
  code: string;
  name: string;
  description: string | null;
  version: number;
}

export interface UserApiAccessRequest {
  permissionId: string;
  effect: string;
}
