import { Navigate, Route, Routes } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import { RequireAuth, RequireGuest } from "./guards";
import AiAdminPage from "../features/ai/AiAdminPage";
import AiChatPage from "../features/ai/AiChatPage";
import AiDocumentsPage from "../features/ai/AiDocumentsPage";
import NrbDocumentsPage from "../features/nrb/NrbDocumentsPage";
import NrbForexPage from "../features/nrb/NrbForexPage";
import NrbSourcesPage from "../features/nrb/NrbSourcesPage";
import AuditLogsPage from "../features/audit-logs/AuditLogsPage";
import { ForgotPasswordPage, LoginPage, ResetPasswordPage } from "../features/auth/AuthPages";
import DashboardPage from "../features/dashboard/DashboardPage";
import DealDetailPage from "../features/deals/DealDetailPage";
import DealFormPage from "../features/deals/DealFormPage";
import DealsPage from "../features/deals/DealsPage";
import DealTypeLimitsPage from "../features/deal-type-limits/DealTypeLimitsPage";
import DealTypesPage from "../features/deal-types/DealTypesPage";
import ExchangeRatesPage from "../features/exchange-rates/ExchangeRatesPage";
import FailedAttemptsPage from "../features/failed-attempts/FailedAttemptsPage";
import FiscalYearsPage from "../features/fiscal-years/FiscalYearsPage";
import InstrumentCodesPage from "../features/instrument-codes/InstrumentCodesPage";
import NostrosPage from "../features/nostros/NostrosPage";
import CounterpartiesPage from "../features/counterparties/CounterpartiesPage";
import CounterpartyLimitsPage from "../features/counterparties/CounterpartyLimitsPage";
import CountriesPage from "../features/countries/CountriesPage";
import CurrenciesPage from "../features/currencies/CurrenciesPage";
import PositionsPage from "../features/positions/PositionsPage";
import PermissionsPage from "../features/permissions/PermissionsPage";
import RolesPage from "../features/roles/RolesPage";
import SessionsPage from "../features/sessions/SessionsPage";
import UsersPage from "../features/users/UsersPage";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<RequireGuest><LoginPage /></RequireGuest>} />
      <Route path="/forgot-password" element={<RequireGuest><ForgotPasswordPage /></RequireGuest>} />
      <Route path="/reset-password" element={<RequireGuest><ResetPasswordPage /></RequireGuest>} />

      <Route
        element={
          <RequireAuth>
            <AppLayout />
          </RequireAuth>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="dashboard" element={<Navigate to="/" replace />} />
        <Route path="deals" element={<DealsPage />} />
        <Route path="deals/new" element={<DealFormPage />} />
        <Route path="deals/:id" element={<DealDetailPage />} />
        <Route path="deals/:id/edit" element={<DealFormPage />} />
        <Route path="counterparties" element={<CounterpartiesPage />} />
        <Route path="counterparty-limits" element={<CounterpartyLimitsPage />} />
        <Route path="currencies" element={<CurrenciesPage />} />
        <Route path="exchange-rates" element={<ExchangeRatesPage />} />
        <Route path="fiscal-years" element={<FiscalYearsPage />} />
        <Route path="deal-types" element={<DealTypesPage />} />
        <Route path="deal-type-limits" element={<DealTypeLimitsPage />} />
        <Route path="countries" element={<CountriesPage />} />
        <Route path="instrument-codes" element={<InstrumentCodesPage />} />
        <Route path="nostros" element={<NostrosPage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="roles" element={<RolesPage />} />
        <Route path="permissions" element={<PermissionsPage />} />
        <Route path="positions" element={<PositionsPage />} />
        <Route path="sessions" element={<SessionsPage />} />
        <Route path="failed-attempts" element={<FailedAttemptsPage />} />
        <Route path="audit-logs" element={<AuditLogsPage />} />
        <Route path="ai/chat" element={<AiChatPage />} />
        <Route path="ai/documents" element={<AiDocumentsPage />} />
        <Route path="ai/admin" element={<AiAdminPage />} />
        <Route path="nrb/forex" element={<NrbForexPage />} />
        <Route path="nrb/sources" element={<NrbSourcesPage />} />
        <Route path="nrb/documents" element={<NrbDocumentsPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
