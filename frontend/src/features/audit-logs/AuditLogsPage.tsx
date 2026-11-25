import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { auditLogsApi } from "../../api/auditLogsApi";

export default function AuditLogsPage() {
  const query = useQuery({
    queryKey: ["audit-logs"],
    queryFn: () => auditLogsApi.list(0, 50),
  });

  const items = query.data ?? [];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Audit logs</h1>
        <p className="text-sm text-slate-500">Administrative audit trail of backend operations</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load audit logs: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Method</th>
                    <th className="px-4 py-3">Path</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Duration ms</th>
                    <th className="px-4 py-3">User</th>
                    <th className="px-4 py-3">Created at</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((entry) => (
                    <tr key={entry.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{entry.id}</td>
                      <td className="px-4 py-3 font-mono text-xs">{entry.method}</td>
                      <td className="px-4 py-3 font-mono text-xs">{entry.path}</td>
                      <td className="px-4 py-3">{entry.status}</td>
                      <td className="px-4 py-3">{entry.durationMs}</td>
                      <td className="px-4 py-3">{entry.userId ?? "-"}</td>
                      <td className="px-4 py-3">{entry.createdAt ? new Date(entry.createdAt).toLocaleString() : "-"}</td>
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
