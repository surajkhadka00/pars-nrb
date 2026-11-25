import { useState, type FormEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Badge, Button, Card, CardContent, CardHeader, CardTitle, Input, Label, Skeleton } from "../../components/common/ui";
import { aiApi } from "../../api/aiApi";
import { useAuth } from "../../auth/AuthContext";
import { formatDateTime } from "../../lib/format";

export default function AiDocumentsPage() {
  const { hasPermission } = useAuth();
  const queryClient = useQueryClient();
  const [file, setFile] = useState<File | null>(null);
  const [title, setTitle] = useState("");
  const [category, setCategory] = useState("");
  const [classification, setClassification] = useState("INTERNAL");
  const [error, setError] = useState<string | null>(null);

  const documentsQuery = useQuery({
    queryKey: ["ai", "documents"],
    queryFn: () => aiApi.documents(),
    enabled: hasPermission("AI_DOCUMENT_READ"),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ["ai", "documents"] });

  const uploadMutation = useMutation({
    mutationFn: () => aiApi.uploadDocument(file as File, title, classification, category),
    onSuccess: () => {
      setFile(null);
      setTitle("");
      setCategory("");
      setError(null);
      invalidate();
    },
    onError: (cause: unknown) => setError(cause instanceof Error ? cause.message : "Upload failed"),
  });

  const actionMutation = useMutation({
    mutationFn: (vars: { id: string; action: "publish" | "archive" | "reindex" | "delete" }) => {
      if (vars.action === "publish") return aiApi.publish(vars.id);
      if (vars.action === "archive") return aiApi.archive(vars.id);
      if (vars.action === "reindex") return aiApi.reindex(vars.id);
      return aiApi.deleteDocument(vars.id);
    },
    onSuccess: invalidate,
  });

  if (!hasPermission("AI_DOCUMENT_READ")) {
    return <Alert variant="destructive">You do not have permission to view AI documents.</Alert>;
  }

  const documents = documentsQuery.data ?? [];

  function submit(event: FormEvent) {
    event.preventDefault();
    if (!file || !title.trim()) {
      return;
    }
    uploadMutation.mutate();
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">AI knowledge documents</h1>
        <p className="text-sm text-slate-500">Upload, review and publish documents for the PARS AI assistant</p>
      </div>

      {hasPermission("AI_DOCUMENT_UPLOAD") ? (
        <Card>
          <CardHeader className="pb-3"><CardTitle>Upload document</CardTitle></CardHeader>
          <CardContent>
            <form className="grid gap-4 sm:grid-cols-2" onSubmit={submit}>
              <div className="space-y-1.5"><Label>Title</Label><Input value={title} onChange={(e) => setTitle(e.target.value)} required /></div>
              <div className="space-y-1.5"><Label>Category</Label><Input value={category} onChange={(e) => setCategory(e.target.value)} /></div>
              <div className="space-y-1.5">
                <Label>Access classification</Label>
                <select className="flex h-9 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm" value={classification} onChange={(e) => setClassification(e.target.value)}>
                  <option value="PUBLIC">PUBLIC</option>
                  <option value="INTERNAL">INTERNAL</option>
                  <option value="ROLE_BASED">ROLE_BASED</option>
                  <option value="RESTRICTED">RESTRICTED</option>
                </select>
              </div>
              <div className="space-y-1.5">
                <Label>File (PDF, DOCX, XLSX, TXT, HTML)</Label>
                <input type="file" accept=".pdf,.docx,.xlsx,.txt,.html" onChange={(e) => setFile(e.target.files?.[0] ?? null)} className="block w-full text-sm text-slate-500 file:mr-4 file:rounded-md file:border-0 file:bg-slate-100 file:px-4 file:py-2 file:text-sm file:font-medium" />
              </div>
              {error ? <div className="sm:col-span-2"><Alert variant="destructive">{error}</Alert></div> : null}
              <div className="sm:col-span-2">
                <Button type="submit" disabled={!file || !title.trim() || uploadMutation.isPending}>
                  {uploadMutation.isPending ? "Uploading…" : "Upload and queue for ingestion"}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      ) : null}

      <Card>
        <CardContent className="p-0">
          {documentsQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {documentsQuery.isError ? <div className="p-6"><Alert variant="destructive">Unable to load documents</Alert></div> : null}
          {!documentsQuery.isPending && !documentsQuery.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Title</th>
                    <th className="px-4 py-3">Category</th>
                    <th className="px-4 py-3">Classification</th>
                    <th className="px-4 py-3">Version</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Uploaded</th>
                    <th className="px-4 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {documents.map((document) => (
                    <tr key={document.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-medium">{document.title}</td>
                      <td className="px-4 py-3">{document.category ?? "-"}</td>
                      <td className="px-4 py-3">{document.accessClassification}</td>
                      <td className="px-4 py-3">v{document.docVersion}</td>
                      <td className="px-4 py-3"><Badge variant={document.publicationStatus === "PUBLISHED" ? "success" : document.publicationStatus === "FAILED" ? "danger" : "secondary"}>{document.publicationStatus}</Badge></td>
                      <td className="px-4 py-3">{formatDateTime(document.uploadedAt)}</td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex justify-end gap-2">
                          {document.publicationStatus === "PENDING_REVIEW" && hasPermission("AI_DOCUMENT_APPROVE") ? (
                            <Button size="sm" onClick={() => actionMutation.mutate({ id: document.id, action: "publish" })}>Publish</Button>
                          ) : null}
                          {document.publicationStatus === "PUBLISHED" && hasPermission("AI_DOCUMENT_APPROVE") ? (
                            <Button size="sm" variant="outline" onClick={() => actionMutation.mutate({ id: document.id, action: "archive" })}>Archive</Button>
                          ) : null}
                          {hasPermission("AI_DOCUMENT_REINDEX") ? (
                            <Button size="sm" variant="outline" onClick={() => actionMutation.mutate({ id: document.id, action: "reindex" })}>Re-index</Button>
                          ) : null}
                          {hasPermission("AI_DOCUMENT_DELETE") ? (
                            <Button size="sm" variant="destructive" onClick={() => actionMutation.mutate({ id: document.id, action: "delete" })}>Delete</Button>
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
