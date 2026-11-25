import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Badge, Button, Card, CardContent, Skeleton } from "../../components/common/ui";
import { nrbApi } from "../../api/aiApi";
import { useAuth } from "../../auth/AuthContext";

export default function NrbSourcesPage() {
  const { hasPermission } = useAuth();
  const client = useQueryClient();

  const sourcesQuery = useQuery({
    queryKey: ["nrb", "sources"],
    queryFn: () => nrbApi.sources(),
    enabled: hasPermission("AI_SOURCE_READ"),
  });

  const statusQuery = useQuery({
    queryKey: ["nrb", "status"],
    queryFn: () => nrbApi.status(),
    enabled: hasPermission("AI_SOURCE_READ"),
  });

  const invalidate = () => {
    void client.invalidateQueries({ queryKey: ["nrb"] });
  };

  const toggle = useMutation({
    mutationFn: (vars: { id: string; enabled: boolean }) => nrbApi.setEnabled(vars.id, vars.enabled),
    onSuccess: invalidate,
  });

  const sync = useMutation({
    mutationFn: (id: string) => nrbApi.syncSource(id),
    onSuccess: invalidate,
  });

  const syncAll = useMutation({
    mutationFn: () => nrbApi.syncAll(),
    onSuccess: invalidate,
  });

  if (!hasPermission("AI_SOURCE_READ")) {
    return <Alert variant="destructive">You do not have permission to view NRB sources.</Alert>;
  }

  const sources = sourcesQuery.data ?? [];
  const canManage = hasPermission("AI_SOURCE_MANAGE");

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">NRB knowledge sources</h1>
          <p className="text-sm text-slate-500">Configured official sources for controlled discovery and ingestion</p>
        </div>
        {canManage ? <Button onClick={() => syncAll.mutate()}>Sync all enabled sources</Button> : null}
      </div>

      {statusQuery.data ? (
        <Card><CardContent className="p-4 text-sm text-slate-600">
          Sources: {String(statusQuery.data.sources)} · Enabled: {String(statusQuery.data.enabledSources)} · Synced: {String(statusQuery.data.syncedSources)} · Status: {String(statusQuery.data.status)}
        </CardContent></Card>
      ) : null}

      <Card>
        <CardContent className="p-0">
          {sourcesQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {sourcesQuery.isError ? <div className="p-6"><Alert variant="destructive">Unable to load sources</Alert></div> : null}
          {!sourcesQuery.isPending && !sourcesQuery.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr><th className="px-4 py-3">Name</th><th className="px-4 py-3">Type</th><th className="px-4 py-3">Category</th><th className="px-4 py-3">Status</th><th className="px-4 py-3">Enabled</th><th className="px-4 py-3 text-right">Actions</th></tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {sources.map((source) => (
                    <tr key={source.id}>
                      <td className="px-4 py-3"><a className="text-navy-700 underline" href={source.url} target="_blank" rel="noopener noreferrer">{source.name}</a></td>
                      <td className="px-4 py-3">{source.type}</td>
                      <td className="px-4 py-3">{source.category}</td>
                      <td className="px-4 py-3"><Badge variant={source.status === "SYNCED" ? "success" : source.status === "FAILED" ? "danger" : "secondary"}>{source.status}</Badge></td>
                      <td className="px-4 py-3">{source.enabled ? "YES" : "NO"}</td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex justify-end gap-2">
                          {canManage ? (
                            <>
                              <Button size="sm" variant="outline" onClick={() => toggle.mutate({ id: source.id, enabled: !source.enabled })}>{source.enabled ? "Disable" : "Enable"}</Button>
                              {source.type !== "API" ? <Button size="sm" onClick={() => sync.mutate(source.id)}>Sync</Button> : null}
                            </>
                          ) : null}
                        </div>
                      </td>
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
