import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { counterpartiesApi } from "../../api/counterpartiesApi";
import { useAuth } from "../../auth/AuthContext";
import { formatCurrency } from "../../lib/format";

export default function CounterpartiesPage() {
  const { hasPermission } = useAuth();

  const counterpartiesQuery = useQuery({
    queryKey: ["counterparties"],
    queryFn: () => counterpartiesApi.list(0, 50),
  });

  const limitsQuery = useQuery({
    queryKey: ["counterparty-limits"],
    queryFn: () => counterpartiesApi.listLimits(0, 50),
  });

  const counterparties = counterpartiesQuery.data ?? [];
  const limits = limitsQuery.data ?? [];

  if (!hasPermission("COUNTERPARTY_READ")) {
    return <Alert variant="destructive">You do not have permission to view counterparties.</Alert>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Counterparties</h1>
          <p className="text-sm text-slate-500">Counterparty master data and limits</p>
        </div>
      </div>

      <Card>
        <CardContent className="p-0">
          {counterpartiesQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {counterpartiesQuery.isError ? <div className="p-6"><Alert variant="destructive">Unable to load counterparties: {counterpartiesQuery.error instanceof Error ? counterpartiesQuery.error.message : "Unknown error"}</Alert></div> : null}
          {!counterpartiesQuery.isPending && !counterpartiesQuery.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Value</th>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Bloomberg code</th>
                    <th className="px-4 py-3">Country</th>
                    <th className="px-4 py-3">Investment grade</th>
                    <th className="px-4 py-3">Own bank</th>
                    <th className="px-4 py-3">Active</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {counterparties.map((counterparty) => (
                    <tr key={counterparty.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{counterparty.id}</td>
                      <td className="px-4 py-3">{counterparty.value}</td>
                      <td className="px-4 py-3">{counterparty.name}</td>
                      <td className="px-4 py-3">{counterparty.bloombergCode ?? "-"}</td>
                      <td className="px-4 py-3">{counterparty.countryId ?? "-"}</td>
                      <td className="px-4 py-3">{counterparty.investmentGrade ?? "-"}</td>
                      <td className="px-4 py-3">{counterparty.ownBank ? "YES" : "NO"}</td>
                      <td className="px-4 py-3">{counterparty.active ? "YES" : "NO"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-0">
          <div className="border-b border-slate-200 px-4 py-3">
            <h2 className="text-base font-semibold text-slate-900">Counterparty limits</h2>
          </div>
          {limitsQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {limitsQuery.isError ? <div className="p-6"><Alert variant="destructive">Unable to load counterparty limits: {limitsQuery.error instanceof Error ? limitsQuery.error.message : "Unknown error"}</Alert></div> : null}
          {!limitsQuery.isPending && !limitsQuery.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Counterparty</th>
                    <th className="px-4 py-3">Date from</th>
                    <th className="px-4 py-3">Date to</th>
                    <th className="px-4 py-3">Limit type</th>
                    <th className="px-4 py-3">Limit value</th>
                    <th className="px-4 py-3">Active</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {limits.map((limit) => (
                    <tr key={limit.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{limit.id}</td>
                      <td className="px-4 py-3">{limit.counterpartyName ?? limit.counterpartyId}</td>
                      <td className="px-4 py-3">{limit.dateFrom ? new Date(limit.dateFrom).toLocaleDateString() : "-"}</td>
                      <td className="px-4 py-3">{limit.dateTo ? new Date(limit.dateTo).toLocaleDateString() : "-"}</td>
                      <td className="px-4 py-3">{limit.limitType ?? "-"}</td>
                      <td className="px-4 py-3">{formatCurrency(limit.limitValue)}</td>
                      <td className="px-4 py-3">{limit.active ? "YES" : "NO"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
