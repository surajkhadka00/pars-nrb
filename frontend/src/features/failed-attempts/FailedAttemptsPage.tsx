import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { failedAttemptsApi } from "../../api/sessionsApi";

export default function FailedAttemptsPage() {
  const query = useQuery({
    queryKey: ["failed-attempts"],
    queryFn: () => failedAttemptsApi.list(0, 50),
  });

  const items = query.data ?? [];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Failed login attempts</h1>
        <p className="text-sm text-slate-500">Security events returned by the PARS authentication service</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="space-y-2 p-6"><Skeleton className="h-8 w-1/3" /><Skeleton className="h-8 w-full" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load failed attempts: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Username</th>
                    <th className="px-4 py-3">IP address</th>
                    <th className="px-4 py-3">Attempted at</th>
                    <th className="px-4 py-3">Reason</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((attempt) => (
                    <tr key={attempt.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{attempt.id}</td>
                      <td className="px-4 py-3">{attempt.username}</td>
                      <td className="px-4 py-3 font-mono text-xs">{attempt.ipAddress ?? "-"}</td>
                      <td className="px-4 py-3">{attempt.attemptedAt ? new Date(attempt.attemptedAt).toLocaleString() : "-"}</td>
                      <td className="px-4 py-3">{attempt.reason}</td>
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
