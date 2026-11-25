import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { currenciesApi } from "../../api/currenciesApi";
import { useAuth } from "../../auth/AuthContext";

export default function CurrenciesPage() {
  const { hasPermission } = useAuth();

  const query = useQuery({
    queryKey: ["currencies"],
    queryFn: () => currenciesApi.list(0, 50),
  });

  const items = query.data ?? [];

  if (!hasPermission("CURRENCY_READ")) {
    return <Alert variant="destructive">You do not have permission to view currencies.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Currencies</h1>
        <p className="text-sm text-slate-500">Currency master data</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load currencies: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">ISO code</th>
                    <th className="px-4 py-3">Symbol</th>
                    <th className="px-4 py-3">Description</th>
                    <th className="px-4 py-3">Standard precision</th>
                    <th className="px-4 py-3">Euro</th>
                    <th className="px-4 py-3">EMU member</th>
                    <th className="px-4 py-3">Active</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((currency) => (
                    <tr key={currency.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{currency.id}</td>
                      <td className="px-4 py-3 font-medium">{currency.isoCode}</td>
                      <td className="px-4 py-3">{currency.curSymbol ?? "-"}</td>
                      <td className="px-4 py-3">{currency.description}</td>
                      <td className="px-4 py-3">{currency.stdPrecision}</td>
                      <td className="px-4 py-3">{currency.euro ? "YES" : "NO"}</td>
                      <td className="px-4 py-3">{currency.emuMember ? "YES" : "NO"}</td>
                      <td className="px-4 py-3">{currency.active ? "YES" : "NO"}</td>
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
