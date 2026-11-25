import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { ArrowRight, Banknote, FileText, ShieldCheck, Sparkles, TrendingUp, Users } from "lucide-react";
import { Badge, Button, Card, CardContent, CardHeader, CardTitle } from "../../components/common/ui";
import { ErrorState, PageHeader, StatusBadge } from "../../components/design/Primitives";
import { NrbBuildingIcon } from "../../components/design/NrbBuildingIcon";
import { dealsApi } from "../../api/dealsApi";
import { nrbApi } from "../../api/aiApi";
import { useAuth } from "../../auth/AuthContext";
import { formatCurrency, formatDateTime } from "../../lib/format";

export default function DashboardPage() {
  const { user, hasPermission } = useAuth();

  const dealsQuery = useQuery({ queryKey: ["deals", "dashboard"], queryFn: () => dealsApi.list(0, 6), enabled: hasPermission("DEAL_READ") });
  const forexQuery = useQuery({ queryKey: ["nrb", "forex", "latest"], queryFn: () => nrbApi.latest(), enabled: hasPermission("AI_FOREX_READ") });
  const statusQuery = useQuery({ queryKey: ["nrb", "status"], queryFn: () => nrbApi.status(), enabled: hasPermission("AI_SOURCE_READ") });

  const deals = dealsQuery.data?.items ?? [];
  const usd = forexQuery.data?.rates.find((rate) => rate.iso3 === "USD");

  return (
    <div className="pars-perspective space-y-6 pars-enter">
      <PageHeader
        eyebrow="Overview · Welcome back"
        title={`Namaste, ${user?.fullName || user?.username || "officer"}`}
        description="Portfolio operations, official NRB references and institutional knowledge in one secure, auditable workspace."
        actions={
          <>
            {hasPermission("DEAL_WRITE") ? <Button asChild className="pars-btn-3d"><Link to="/deals/new"><FileText className="h-4 w-4" /> New deal</Link></Button> : null}
            {hasPermission("AI_CHAT") ? <Button asChild variant="outline" className="pars-btn-3d"><Link to="/ai/chat"><Sparkles className="h-4 w-4" /> Ask PARS AI</Link></Button> : null}
          </>
        }
      />

      {/* 3D hero card */}
      <div className="pars-card-3d relative overflow-hidden p-6">
        <div className="absolute right-4 top-4 opacity-10"><NrbBuildingIcon className="h-28 w-28" /></div>
        <div className="grid gap-4 lg:grid-cols-[1.4fr_1fr]">
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-green-accent-600">Institutional snapshot</p>
            <h2 className="mt-1 text-2xl font-semibold tracking-tight text-navy-900 dark:text-slate-100">PARS NRB — Nepal Rastra Bank</h2>
            <p className="mt-2 max-w-xl text-sm text-slate-600 dark:text-slate-300">
              Your session is authenticated with {user?.permissions?.length ?? 0} server-resolved permissions. All actions remain subject to backend authorization and audit.
            </p>
            <div className="mt-4 flex flex-wrap gap-2">
              <Badge variant="secondary">{user?.roles?.join(" · ") || "Authenticated"}</Badge>
              <Badge variant="outline">{user?.lastSuccessfulLoginAt ? `Last sign-in ${formatDateTime(user.lastSuccessfulLoginAt)}` : "First session"}</Badge>
              <Badge variant="outline">Nepal Standard Time</Badge>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="rounded-xl border border-slate-200 bg-white/70 p-4 dark:border-slate-700 dark:bg-slate-900/50">
              <p className="text-[10px] font-semibold uppercase tracking-widest text-slate-500">Your access</p>
              <p className="mt-1 text-2xl font-bold text-navy-900 dark:text-slate-100">{user?.permissions?.length ?? 0}</p>
              <p className="text-xs text-slate-500">permissions</p>
            </div>
            <div className="rounded-xl border border-slate-200 bg-white/70 p-4 dark:border-slate-700 dark:bg-slate-900/50">
              <p className="text-[10px] font-semibold uppercase tracking-wider text-slate-500">Deals</p>
              <p className="mt-1 text-2xl font-bold text-navy-900 dark:text-slate-100">{dealsQuery.data?.meta?.totalElements ?? 0}</p>
              <p className="text-xs text-slate-500">records</p>
            </div>
            <div className="rounded-xl border border-slate-200 bg-white/70 p-4 dark:border-slate-700 dark:bg-slate-900/50">
              <p className="text-[10px] font-semibold uppercase tracking-wider text-slate-500">USD (NRB)</p>
              <p className="mt-1 text-2xl font-bold text-navy-900 dark:text-slate-100">{usd ? String(usd.buy) : "—"}</p>
              <p className="text-xs text-slate-500">buy / {usd ? String(usd.sell) : "—"} sell</p>
            </div>
            <div className="rounded-xl border border-slate-200 bg-white/70 p-4 dark:border-slate-700 dark:bg-slate-900/50">
              <p className="text-[10px] font-semibold uppercase tracking-wider text-slate-500">NRB sources</p>
              <p className="mt-1 text-2xl font-bold text-navy-900 dark:text-slate-100">{String(statusQuery.data?.syncedSources ?? 0)}</p>
              <p className="text-xs text-slate-500">of {String(statusQuery.data?.sources ?? 0)} synced</p>
            </div>
          </div>
        </div>
      </div>

      {/* 3D floating stat row */}
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {[
          { label: "Roles", value: user?.roles?.length ?? 0, icon: Users, tone: "text-navy-600" },
          { label: "Permissions", value: user?.permissions?.length ?? 0, icon: ShieldCheck, tone: "text-green-accent-600" },
          { label: "Recent deals", value: deals.length, icon: FileText, tone: "text-navy-600" },
          { label: "NRB documents", value: Number(statusQuery.data?.syncedSources ?? 0), icon: TrendingUp, tone: "text-green-accent-600" },
        ].map((item) => (
          <Card key={item.label} className="pars-card-3d">
            <CardContent className="flex items-center gap-3 p-4">
              <div className={cn("flex h-10 w-10 items-center justify-center rounded-lg bg-slate-100 dark:bg-slate-800", item.tone)}><item.icon className="h-5 w-5" /></div>
              <div>
                <p className="text-2xl font-bold text-navy-900 dark:text-slate-100">{item.value}</p>
                <p className="text-xs text-slate-500">{item.label}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <Card className="pars-card-3d lg:col-span-2">
          <CardHeader className="flex flex-row items-center justify-between border-b border-slate-100 pb-3 dark:border-slate-800">
            <CardTitle className="text-base">Recent deals</CardTitle>
            <Button asChild variant="ghost" size="sm"><Link to="/deals">View all <ArrowRight className="h-3.5 w-3.5" /></Link></Button>
          </CardHeader>
          <CardContent className="p-0">
            {!hasPermission("DEAL_READ") ? <p className="p-5 text-sm text-slate-500">You do not have permission to view deals.</p> : dealsQuery.isError ? <div className="p-4"><ErrorState message="Unable to load deals." onRetry={() => void dealsQuery.refetch()} /></div> : deals.length === 0 ? <p className="p-5 text-sm text-slate-500">No deals recorded yet.</p> : (
              <div className="overflow-x-auto">
                <table className="w-full min-w-[560px] text-left text-sm">
                  <thead className="bg-slate-50 text-[11px] uppercase tracking-wider text-slate-500 dark:bg-slate-900/60"><tr><th className="px-4 py-2.5">Document</th><th className="px-4 py-2.5">Date</th><th className="px-4 py-2.5">Amount</th><th className="px-4 py-2.5">Status</th></tr></thead>
                  <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                    {deals.map((deal) => (
                      <tr key={deal.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                        <td className="px-4 py-3 font-mono text-xs">{deal.documentNo}</td>
                        <td className="px-4 py-3 text-xs text-slate-500">{deal.dealDate ? formatDateTime(deal.dealDate) : "—"}</td>
                        <td className="px-4 py-3">{formatCurrency(deal.amount)}</td>
                        <td className="px-4 py-3"><StatusBadge value={deal.docStatus ?? "DRAFT"} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="pars-card-3d">
          <CardHeader className="border-b border-slate-100 pb-3 dark:border-slate-800"><CardTitle className="text-base">Quick actions</CardTitle></CardHeader>
          <CardContent className="grid gap-2 p-4">
            {[
              { to: "/deals", label: "Open deal workspace", icon: FileText, permission: "DEAL_READ" },
              { to: "/nrb/forex", label: "Check official forex rates", icon: Banknote, permission: "AI_FOREX_READ" },
              { to: "/ai/chat", label: "Ask the knowledge assistant", icon: Sparkles, permission: "AI_CHAT" },
              { to: "/nrb/sources", label: "Review NRB sources", icon: ShieldCheck, permission: "AI_SOURCE_READ" },
            ].filter((item) => hasPermission(item.permission)).map((item) => (
              <Link key={item.to} to={item.to} className="flex items-center gap-3 rounded-lg border border-slate-200 px-3 py-2.5 text-sm transition hover:border-navy-300 hover:bg-slate-50 dark:border-slate-700 dark:hover:bg-slate-800">
                <item.icon className="h-4 w-4 text-navy-600 dark:text-green-accent-300" />
                <span className="flex-1">{item.label}</span>
                <ArrowRight className="h-3.5 w-3.5 text-slate-400" />
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>

      {hasPermission("AI_CHAT") ? (
        <Card className="pars-card-3d border-green-accent-100 bg-green-accent-50/40 dark:border-green-accent-900/40 dark:bg-green-accent-900/10">
          <CardContent className="flex flex-col gap-3 p-5 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div className="flex items-center gap-2"><Sparkles className="h-4 w-4 text-green-accent-600" /><p className="text-sm font-semibold">PARS AI knowledge assistant</p><Badge variant="success">Grounded</Badge></div>
              <p className="mt-1 text-xs text-slate-600 dark:text-slate-300">Answers are grounded in approved documents and official NRB data, with verifiable citations.</p>
            </div>
            <Button asChild className="pars-btn-3d"><Link to="/ai/chat">Start a conversation <ArrowRight className="h-3.5 w-3.5" /></Link></Button>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}

function cn(...classes: (string | false | null | undefined)[]) {
  return classes.filter(Boolean).join(" ");
}
