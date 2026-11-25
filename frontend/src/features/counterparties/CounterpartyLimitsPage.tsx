import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { counterpartiesApi } from "../../api/counterpartiesApi";
import { useAuth } from "../../auth/AuthContext";

export default function CounterpartyLimitsPage() {
  const { hasPermission } = useAuth();

  const query = useQuery({
    queryKey: ["counterparty-limits"],
    queryFn: () => counterpartiesApi.listLimits(0, 50),
  });

  const items = query.data ?? [];

  if (!hasPermission("COUNTERPARTY_READ")) {
    return <Alert variant="destructive">You do not have permission to view counterparty limits.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Counterparty limits</h1>
        <p className="text-sm text-slate-500">Counterparty limit rules</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load counterparty limits: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
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
                  {items.map((limit) => (
                    <tr key={limit.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{limit.id}</td>
                      <td className="px-4 py-3">{limit.counterpartyName ?? limit.counterpartyId}</td>
                      <td className="px-4 py-3">{limit.dateFrom ? new Date(limit.dateFrom).toLocaleDateString() : "-"}</td>
                      <td className="px-4 py-3">{limit.dateTo ? new Date(limit.dateTo).toLocaleDateString() : "-"}</td>
                      <td className="px-4 py-3">{limit.limitType ?? "-"}</td>
                      <td className="px-4 py-3">{limit.limitValue ?? "-"}</td>
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
