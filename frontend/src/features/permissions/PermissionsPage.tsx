import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { permissionsApi } from "../../api/usersApi";
import { useAuth } from "../../auth/AuthContext";
import type { PermissionResponse } from "../../api/types";

export default function PermissionsPage() {
  const { hasPermission } = useAuth();

  const query = useQuery({
    queryKey: ["permissions"],
    queryFn: () => permissionsApi.list(0, 50),
  });

  const items = query.data ?? [];

  if (!hasPermission("PERMISSION_READ")) {
    return <Alert variant="destructive">You do not have permission to view permissions.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Permissions</h1>
        <p className="text-sm text-slate-500">Backend permission catalog</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load permissions: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Code</th>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Description</th>
                    <th className="px-4 py-3">Version</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((permission: PermissionResponse) => (
                    <tr key={permission.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{permission.id}</td>
                      <td className="px-4 py-3 font-mono font-medium">{permission.code}</td>
                      <td className="px-4 py-3">{permission.name}</td>
                      <td className="px-4 py-3">{permission.description ?? "-"}</td>
                      <td className="px-4 py-3">{permission.version}</td>
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
