import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { dealTypesApi } from "../../api/dealTypesApi";

export default function DealTypesPage() {
  const query = useQuery({
    queryKey: ["deal-types"],
    queryFn: () => dealTypesApi.list(0, 50),
  });

  const items = query.data ?? [];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Deal types</h1>
        <p className="text-sm text-slate-500">Deal type master data</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load deal types: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Value</th>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Description</th>
                    <th className="px-4 py-3">Instrument code applicable</th>
                    <th className="px-4 py-3">Active</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((dealType) => (
                    <tr key={dealType.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{dealType.id}</td>
                      <td className="px-4 py-3">{dealType.value}</td>
                      <td className="px-4 py-3">{dealType.name}</td>
                      <td className="px-4 py-3">{dealType.description ?? "-"}</td>
                      <td className="px-4 py-3">{dealType.instrumentCodeApplicable ? "YES" : "NO"}</td>
                      <td className="px-4 py-3">{dealType.active ? "YES" : "NO"}</td>
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
