import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Alert, Badge, Button, Card, CardContent, Skeleton } from "../../components/common/ui";
import { sessionsApi } from "../../api/sessionsApi";
import { useAuth } from "../../auth/AuthContext";
import { formatDateTime } from "../../lib/format";

export default function SessionsPage() {
  const { user, hasPermission, logoutAll } = useAuth();
  const queryClient = useQueryClient();

  const sessionsQuery = useQuery({
    queryKey: ["auth", "sessions"],
    queryFn: () => sessionsApi.list(0, 50),
  });

  const revokeMutation = useMutation({
    mutationFn: (sessionId: string) => sessionsApi.revoke(sessionId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["auth", "sessions"] });
    },
  });

  const items = sessionsQuery.data ?? [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Active sessions</h1>
          <p className="text-sm text-slate-500">Sessions issued to this account</p>
        </div>
        <Button
          variant="outline"
          disabled={!hasPermission("SESSION_REVOKE")}
          onClick={() => void logoutAll()}
        >
          Sign out everywhere
        </Button>
      </div>

      <Card>
        <CardContent className="p-0">
          {sessionsQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {sessionsQuery.isError ? <div className="p-6"><Alert variant="destructive">Unable to load sessions: {sessionsQuery.error instanceof Error ? sessionsQuery.error.message : "Unknown error"}</Alert></div> : null}
          {!sessionsQuery.isPending && !sessionsQuery.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Session</th>
                    <th className="px-4 py-3">IP address</th>
                    <th className="px-4 py-3">User agent</th>
                    <th className="px-4 py-3">Issued at</th>
                    <th className="px-4 py-3">Last activity</th>
                    <th className="px-4 py-3">Expires at</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((session) => (
                    <tr key={session.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">
                        {session.id}
                        {session.id === user?.sessionId ? <Badge variant="success" >CURRENT</Badge> : null}
                      </td>
                      <td className="px-4 py-3 font-mono text-xs">{session.ipAddress ?? "-"}</td>
                      <td className="px-4 py-3">{session.userAgent ?? "-"}</td>
                      <td className="px-4 py-3">{formatDateTime(session.issuedAt)}</td>
                      <td className="px-4 py-3">{formatDateTime(session.lastActivityAt)}</td>
                      <td className="px-4 py-3">{formatDateTime(session.expiresAt)}</td>
                      <td className="px-4 py-3">{session.revokedAt ? <Badge variant="danger">REVOKED</Badge> : <Badge variant="secondary">ACTIVE</Badge>}</td>
                      <td className="px-4 py-3 text-right">
                        <Button
                          variant="destructive"
                          size="sm"
                          disabled={session.revokedAt !== null || session.id === user?.sessionId || !hasPermission("SESSION_REVOKE")}
                          onClick={() => revokeMutation.mutate(session.id)}
                        >
                          Revoke
                        </Button>
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
