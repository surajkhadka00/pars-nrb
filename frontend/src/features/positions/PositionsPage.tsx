import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { positionsApi } from "../../api/usersApi";
import { useAuth } from "../../auth/AuthContext";

export default function PositionsPage() {
  const { hasPermission } = useAuth();

  const query = useQuery({
    queryKey: ["positions"],
    queryFn: () => positionsApi.list(0, 50),
  });

  const items = query.data ?? [];

  if (!hasPermission("POSITION_READ")) {
    return <Alert variant="destructive">You do not have permission to view positions.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Positions</h1>
        <p className="text-sm text-slate-500">Organizational positions</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load positions: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Code</th>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Description</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((position) => (
                    <tr key={position.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{position.id}</td>
                      <td className="px-4 py-3">{position.code}</td>
                      <td className="px-4 py-3">{position.name}</td>
                      <td className="px-4 py-3">{position.description ?? "-"}</td>
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
