import { useQuery } from "@tanstack/react-query";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Download, Trash2, Upload } from "lucide-react";
import { Alert, Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Skeleton } from "../../components/common/ui";
import { dealsApi } from "../../api/dealsApi";
import { useAuth } from "../../auth/AuthContext";
import { useRef, useState, type ChangeEvent } from "react";
import { formatCurrency, formatDateTime } from "../../lib/format";
import type { DealResponse } from "../../api/types";

export default function DealDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { hasPermission } = useAuth();

  const dealQuery = useQuery({
    queryKey: ["deals", "detail", id],
    queryFn: () => dealsApi.get(id as string),
    enabled: !!id && hasPermission("DEAL_READ"),
  });

  const attachmentsQuery = useQuery({
    queryKey: ["deal-attachments", id],
    queryFn: () => dealsApi.listAttachments(id as string),
    enabled: !!id && hasPermission("DEAL_READ"),
  });

  if (!id) {
    return <Alert variant="destructive">Deal id is missing.</Alert>;
  }

  const deal: DealResponse | undefined = dealQuery.data;

  if (dealQuery.isPending) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-8 w-full" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  if (dealQuery.isError || !deal) {
    return (
      <div className="space-y-4">
        <Button variant="outline" size="sm" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-4 w-4" /> Back to deals
        </Button>
        <Alert variant="destructive">Unable to load deal: {dealQuery.error instanceof Error ? dealQuery.error.message : "Unknown error"}</Alert>
      </div>
    );
  }

  const attachments = attachmentsQuery.data ?? [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <Button variant="ghost" size="sm" onClick={() => navigate(-1)}>
            <ArrowLeft className="h-4 w-4" /> Back
          </Button>
          <h1 className="mt-2 text-2xl font-semibold text-slate-900">{deal.documentNo}</h1>
          <p className="text-sm text-slate-500">Deal details</p>
        </div>
        <div className="flex gap-2">
          {hasPermission("DEAL_WRITE") ? (
            <Button asChild>
              <Link to={`/deals/${deal.id}/edit`}>Edit deal</Link>
            </Button>
          ) : null}
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Deal information</CardTitle>
          <CardDescription>Fetched from GET /api/v1/deals/{deal.id}</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <Detail label="Deal type" value={deal.dealTypeId} />
          <Detail label="Instrument code" value={deal.instrumentCodeId} />
          <Detail label="Direction" value={deal.direction} />
          <Detail label="Document status" value={deal.docStatus} />
          <Detail label="Amount" value={formatCurrency(deal.amount)} />
          <Detail label="Allocated amount" value={formatCurrency(deal.allotedAmt)} />
          <Detail label="Deal date" value={deal.dealDate ? formatDateTime(deal.dealDate) : undefined} />
          <Detail label="Value date" value={deal.valueDate ? formatDateTime(deal.valueDate) : undefined} />
          <Detail label="Maturity date" value={deal.maturityDate ? formatDateTime(deal.maturityDate) : undefined} />
          <Detail label="Counterparty" value={deal.counterpartyId} />
          <Detail label="Our nostro" value={deal.ourNostroId} />
          <Detail label="Their nostro" value={deal.theirNostroId} />
          <Detail label="Version" value={String(deal.version)} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Attachments</CardTitle>
          <CardDescription>Uploaded through POST /api/v1/deal-attachments</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {attachmentsQuery.isPending ? (
            <Skeleton className="h-20 w-full" />
          ) : attachmentsQuery.isError ? (
            <Alert variant="destructive">Unable to load attachments.</Alert>
          ) : attachments.length === 0 ? (
            <p className="text-sm text-slate-500">No attachments for this deal.</p>
          ) : (
            <ul className="divide-y divide-slate-100 rounded-md border border-slate-200">
              {attachments.map((a) => (
                <li key={a.id} className="flex items-center justify-between px-4 py-2 text-sm">
                  <div>
                    <p className="font-medium text-slate-700">{a.originalFileName}</p>
                    <p className="text-xs text-slate-500">
                      {a.contentType} - {a.fileSize} bytes - {formatDateTime(a.uploadedAt)}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => void dealsApi.downloadAttachment(deal.id, a.id)}
                    >
                      <Download className="h-3.5 w-3.5" /> Download
                    </Button>
                    {hasPermission("DEAL_WRITE") ? (
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => void dealsApi.removeAttachment(deal.id, a.id)}
                      >
                        <Trash2 className="h-3.5 w-3.5" /> Delete
                      </Button>
                    ) : null}
                  </div>
                </li>
              ))}
            </ul>
          )}
          {hasPermission("DEAL_WRITE") ? (
            <AttachmentUploader dealId={deal.id} />
          ) : null}
        </CardContent>
      </Card>
    </div>
  );
}

function Detail({ label, value }: { label: string; value?: string | null }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-slate-500">{label}</p>
      <p className="mt-1 text-sm font-medium text-slate-900">{value ?? "-"}</p>
    </div>
  );
}

function AttachmentUploader({ dealId }: { dealId: string }) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    setUploading(true);
    setError(null);
    dealsApi.uploadAttachment(dealId, file)
      .then(() => {
        if (inputRef.current) {
          inputRef.current.value = "";
        }
      })
      .catch((cause: unknown) => {
        setError(cause instanceof Error ? cause.message : "Upload failed");
      })
      .finally(() => setUploading(false));
  }

  return (
    <div className="space-y-2">
      <input ref={inputRef} type="file" onChange={handleFileChange} className="block w-full text-sm text-slate-500 file:mr-4 file:rounded-md file:border-0 file:bg-slate-100 file:px-4 file:py-2 file:text-sm file:font-medium file:text-slate-700 hover:file:bg-slate-200" />      {uploading ? <p className="text-sm text-slate-500">Uploading…</p> : null}
      {error ? <p className="text-sm text-red-600">{error}</p> : null}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Upload className="h-3.5 w-3.5" /> Multipart upload via POST /api/v1/deal-attachments
      </div>
    </div>
  );
}
