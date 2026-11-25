import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { fiscalYearsApi } from "../../api/fiscalYearsApi";
import { useAuth } from "../../auth/AuthContext";

export default function FiscalYearsPage() {
  const { hasPermission } = useAuth();

  const query = useQuery({
    queryKey: ["fiscal-years"],
    queryFn: () => fiscalYearsApi.list(0, 50),
  });

  const items = query.data ?? [];

  if (!hasPermission("FISCALYEAR_READ")) {
    return <Alert variant="destructive">You do not have permission to view fiscal years.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Fiscal years</h1>
        <p className="text-sm text-slate-500">Fiscal year master data</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load fiscal years: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Value</th>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Start date</th>
                    <th className="px-4 py-3">End date</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((year) => (
                    <tr key={year.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{year.id}</td>
                      <td className="px-4 py-3">{year.value}</td>
                      <td className="px-4 py-3">{year.name}</td>
                      <td className="px-4 py-3">{year.startDate ? new Date(year.startDate).toLocaleDateString() : "-"}</td>
                      <td className="px-4 py-3">{year.endDate ? new Date(year.endDate).toLocaleDateString() : "-"}</td>
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
