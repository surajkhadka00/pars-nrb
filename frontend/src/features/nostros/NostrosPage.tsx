import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { nostrosApi } from "../../api/nostrosApi";
import { useAuth } from "../../auth/AuthContext";

export default function NostrosPage() {
  const { hasPermission } = useAuth();

  const query = useQuery({
    queryKey: ["nostros"],
    queryFn: () => nostrosApi.list(0, 50),
  });

  const items = query.data ?? [];

  if (!hasPermission("NOSTRO_READ")) {
    return <Alert variant="destructive">You do not have permission to view nostro accounts.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Nostro accounts</h1>
        <p className="text-sm text-slate-500">Our nostro account master data</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load nostro accounts: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Counterparty</th>
                    <th className="px-4 py-3">Currency</th>
                    <th className="px-4 py-3">Value</th>
                    <th className="px-4 py-3">GL account no</th>
                    <th className="px-4 py-3">SWIFT</th>
                    <th className="px-4 py-3">Active</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((nostro) => (
                    <tr key={nostro.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{nostro.id}</td>
                      <td className="px-4 py-3">{nostro.counterpartyId}</td>
                      <td className="px-4 py-3">{nostro.currencyIsoCode}</td>
                      <td className="px-4 py-3">{nostro.value}</td>
                      <td className="px-4 py-3">{nostro.glAcNo ?? "-"}</td>
                      <td className="px-4 py-3">{nostro.swiftCode ?? "-"}</td>
                      <td className="px-4 py-3">{nostro.active ? "YES" : "NO"}</td>
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
