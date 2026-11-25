import {
  Activity,
  Banknote,
  Building2,
  CalendarDays,
  Coins,
  FileText,
  Gauge,
  Globe,
  Landmark,
  LayoutDashboard,
  Lock,
  ScrollText,
  ShieldCheck,
  Sparkles,
  Upload,
  Users,
  Wallet,
} from "lucide-react";
import { AppShell, type NavEntry } from "../design/AppShell";

const NAV: NavEntry[] = [
  { to: "/", label: "Dashboard", icon: <LayoutDashboard className="h-4 w-4" />, section: "Workspace" },
  { to: "/deals", label: "Deals", icon: <FileText className="h-4 w-4" />, permission: "DEAL_READ", section: "Portfolio" },
  { to: "/counterparties", label: "Counterparties", icon: <Building2 className="h-4 w-4" />, permission: "COUNTERPARTY_READ", section: "Portfolio" },
  { to: "/currencies", label: "Currencies", icon: <Coins className="h-4 w-4" />, permission: "CURRENCY_READ", section: "Portfolio" },
  { to: "/exchange-rates", label: "Exchange Rates", icon: <Banknote className="h-4 w-4" />, permission: "CURRENCY_READ", section: "Portfolio" },
  { to: "/fiscal-years", label: "Fiscal Years", icon: <CalendarDays className="h-4 w-4" />, permission: "FISCALYEAR_READ", section: "Portfolio" },
  { to: "/deal-types", label: "Deal Types", icon: <Landmark className="h-4 w-4" />, permission: "DEALTYPE_READ", section: "Portfolio" },
  { to: "/countries", label: "Countries", icon: <Globe className="h-4 w-4" />, permission: "COUNTRY_READ", section: "Portfolio" },
  { to: "/instrument-codes", label: "Instrument Codes", icon: <FileText className="h-4 w-4" />, permission: "INSTRUMENT_READ", section: "Portfolio" },
  { to: "/nostros", label: "Nostro Accounts", icon: <Wallet className="h-4 w-4" />, permission: "NOSTRO_READ", section: "Portfolio" },
  { to: "/users", label: "Users", icon: <Users className="h-4 w-4" />, permission: "USER_READ", section: "Administration" },
  { to: "/roles", label: "Roles & Permissions", icon: <ShieldCheck className="h-4 w-4" />, permission: "ROLE_READ", section: "Administration" },
  { to: "/positions", label: "Positions", icon: <Landmark className="h-4 w-4" />, permission: "POSITION_READ", section: "Administration" },
  { to: "/sessions", label: "Sessions", icon: <Activity className="h-4 w-4" />, permission: "SESSION_REVOKE", section: "Administration" },
  { to: "/failed-attempts", label: "Failed Logins", icon: <Lock className="h-4 w-4" />, permission: "FAILED_ATTEMPT_READ", section: "Administration" },
  { to: "/audit-logs", label: "Audit Logs", icon: <ScrollText className="h-4 w-4" />, adminOnly: true, section: "Administration" },
  { to: "/ai/chat", label: "AI Assistant", icon: <Sparkles className="h-4 w-4" />, permission: "AI_CHAT", section: "Intelligence" },
  { to: "/ai/documents", label: "AI Documents", icon: <Upload className="h-4 w-4" />, permission: "AI_DOCUMENT_READ", section: "Intelligence" },
  { to: "/ai/admin", label: "AI Administration", icon: <Gauge className="h-4 w-4" />, permission: "AI_ADMIN", section: "Intelligence" },
  { to: "/nrb/forex", label: "NRB Forex", icon: <Banknote className="h-4 w-4" />, permission: "AI_FOREX_READ", section: "Intelligence" },
  { to: "/nrb/sources", label: "NRB Sources", icon: <Globe className="h-4 w-4" />, permission: "AI_SOURCE_READ", section: "Intelligence" },
  { to: "/nrb/documents", label: "NRB Documents", icon: <FileText className="h-4 w-4" />, permission: "AI_DOCUMENT_READ", section: "Intelligence" },
];

export default function AppLayout() {
  return <AppShell nav={NAV} />;
}
