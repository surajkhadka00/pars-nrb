import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Badge, Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input, Skeleton } from "../../components/common/ui";
import { aiApi } from "../../api/aiApi";
import { useAuth } from "../../auth/AuthContext";
import { formatDateTime } from "../../lib/format";

export default function AiAdminPage() {
  const { hasPermission } = useAuth();
  const client = useQueryClient();
  const [reason, setReason] = useState("");

  const killSwitchQuery = useQuery({
    queryKey: ["ai", "kill-switch"],
    queryFn: () => aiApi.killSwitchStatus(),
    enabled: hasPermission("AI_ADMIN"),
  });

  const killSwitchMutation = useMutation({
    mutationFn: (disabled: boolean) => aiApi.setKillSwitch(disabled, reason),
    onSuccess: () => void client.invalidateQueries({ queryKey: ["ai", "kill-switch"] }),
  });

  const configQuery = useQuery({
    queryKey: ["ai", "config"],
    queryFn: () => aiApi.config(),
    enabled: hasPermission("AI_ADMIN"),
  });

  const metricsQuery = useQuery({
    queryKey: ["ai", "metrics"],
    queryFn: () => aiApi.metrics(),
    enabled: hasPermission("AI_METRICS_READ"),
  });

  const jobsQuery = useQuery({
    queryKey: ["ai", "jobs"],
    queryFn: () => aiApi.jobs(),
    enabled: hasPermission("AI_ADMIN"),
  });

  if (!hasPermission("AI_ADMIN") && !hasPermission("AI_METRICS_READ")) {
    return <Alert variant="destructive">You do not have permission to view AI administration.</Alert>;
  }

  const metrics = metricsQuery.data;
  const jobs = jobsQuery.data ?? [];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">AI administration</h1>
        <p className="text-sm text-slate-500">Configuration status, model usage and ingestion health</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Questions" value={metrics?.totalQuestions ?? "—"} />
        <StatCard label="Failed questions" value={metrics?.failedQuestions ?? "—"} />
        <StatCard label="Published documents" value={metrics?.publishedDocuments ?? "—"} />
        <StatCard label="Failed ingestion jobs" value={metrics?.failedJobs ?? "—"} />
        <StatCard label="Feedback entries" value={metrics?.feedbackCount ?? "—"} />
        <StatCard label="Average rating" value={metrics?.averageFeedback ?? "—"} />
      </div>

      {hasPermission("AI_ADMIN") ? (
        <Card>
          <CardHeader>
            <CardTitle>AI kill switch</CardTitle>
            <CardDescription>Disable AI generation immediately without affecting core PARS operations</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <div className="flex-1 space-y-2">
              <p className="text-sm">
                Current state: {killSwitchQuery.data?.disabled ? <Badge variant="danger">DISABLED</Badge> : <Badge variant="success">ENABLED</Badge>}
              </p>
              <Input value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Reason (recorded in audit log)" aria-label="Kill switch reason" />
            </div>
            <Button
              variant={killSwitchQuery.data?.disabled ? "default" : "destructive"}
              disabled={killSwitchMutation.isPending}
              onClick={() => killSwitchMutation.mutate(!killSwitchQuery.data?.disabled)}
            >
              {killSwitchQuery.data?.disabled ? "Re-enable AI" : "Disable AI"}
            </Button>
          </CardContent>
        </Card>
      ) : null}

      {hasPermission("AI_ADMIN") ? (
        <Card>
          <CardHeader>
            <CardTitle>Configuration</CardTitle>
            <CardDescription>Secret values are never returned by the API</CardDescription>
          </CardHeader>
          <CardContent>
            {configQuery.isPending ? <Skeleton className="h-24 w-full" /> : null}
            {configQuery.isError ? <Alert variant="destructive">Unable to load AI configuration</Alert> : null}
            {configQuery.data ? (
              <dl className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {Object.entries(configQuery.data).map(([key, value]) => (
                  <div key={key}>
                    <dt className="text-xs uppercase tracking-wide text-slate-500">{key}</dt>
                    <dd className="text-sm font-medium text-slate-800">{String(value)}</dd>
                  </div>
                ))}
              </dl>
            ) : null}
          </CardContent>
        </Card>
      ) : null}

      {hasPermission("AI_ADMIN") ? (
        <Card>
          <CardHeader><CardTitle>Ingestion jobs</CardTitle></CardHeader>
          <CardContent className="p-0">
            {jobsQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
            {!jobsQuery.isPending && jobs.length === 0 ? <p className="p-6 text-sm text-slate-500">No ingestion jobs yet.</p> : null}
            {jobs.length > 0 ? (
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr><th className="px-4 py-3">Document</th><th className="px-4 py-3">Status</th><th className="px-4 py-3">Stage</th><th className="px-4 py-3">Attempts</th><th className="px-4 py-3">Created</th></tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {jobs.map((job) => (
                    <tr key={job.id}>
                      <td className="px-4 py-3 font-mono text-xs">{job.documentId}</td>
                      <td className="px-4 py-3"><Badge variant={job.status === "FAILED" ? "danger" : job.status === "PENDING_REVIEW" ? "success" : "secondary"}>{job.status}</Badge></td>
                      <td className="px-4 py-3">{job.stage ?? "-"}</td>
                      <td className="px-4 py-3">{job.attempts}</td>
                      <td className="px-4 py-3">{formatDateTime(job.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : null}
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: string | number }) {
  return (
    <Card>
      <CardContent className="p-4">
        <p className="text-xs uppercase tracking-wide text-slate-500">{label}</p>
        <p className="mt-1 text-2xl font-semibold text-slate-900">{value}</p>
      </CardContent>
    </Card>
  );
}
