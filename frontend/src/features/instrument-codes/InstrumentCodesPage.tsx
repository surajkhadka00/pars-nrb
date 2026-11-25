import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { instrumentCodesApi } from "../../api/instrumentCodesApi";
import { useAuth } from "../../auth/AuthContext";

export default function InstrumentCodesPage() {
  const { hasPermission } = useAuth();

  const query = useQuery({
    queryKey: ["instrument-codes"],
    queryFn: () => instrumentCodesApi.list(0, 50),
  });

  const items = query.data ?? [];

  if (!hasPermission("INSTRUMENT_READ")) {
    return <Alert variant="destructive">You do not have permission to view instrument codes.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Instrument codes</h1>
        <p className="text-sm text-slate-500">Instrument code master data</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load instrument codes: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Deal type</th>
                    <th className="px-4 py-3">Value</th>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Active</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((instrumentCode) => (
                    <tr key={instrumentCode.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{instrumentCode.id}</td>
                      <td className="px-4 py-3">{instrumentCode.dealTypeName ?? instrumentCode.dealTypeId}</td>
                      <td className="px-4 py-3">{instrumentCode.value}</td>
                      <td className="px-4 py-3">{instrumentCode.name}</td>
                      <td className="px-4 py-3">{instrumentCode.active ? "YES" : "NO"}</td>
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
