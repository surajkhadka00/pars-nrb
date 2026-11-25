import { useQuery } from "@tanstack/react-query";
import { Alert, Badge, Button, Card, CardContent, Skeleton } from "../../components/common/ui";
import { nrbApi } from "../../api/aiApi";
import { useAuth } from "../../auth/AuthContext";

export default function NrbDocumentsPage() {
  const { hasPermission } = useAuth();

  const documentsQuery = useQuery({
    queryKey: ["nrb", "documents"],
    queryFn: () => nrbApi.documents(),
    enabled: hasPermission("AI_DOCUMENT_READ"),
  });

  if (!hasPermission("AI_DOCUMENT_READ")) {
    return <Alert variant="destructive">You do not have permission to view NRB documents.</Alert>;
  }

  const documents = documentsQuery.data ?? [];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">NRB knowledge documents</h1>
        <p className="text-sm text-slate-500">Official publications discovered by controlled crawling</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {documentsQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {documentsQuery.isError ? <div className="p-6"><Alert variant="destructive">Unable to load NRB documents</Alert></div> : null}
          {!documentsQuery.isPending && !documentsQuery.isError && documents.length === 0 ? (
            <p className="p-6 text-sm text-slate-500">No documents discovered yet. Enable a source and run a sync.</p>
          ) : null}
          {documents.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr><th className="px-4 py-3">Title</th><th className="px-4 py-3">Category</th><th className="px-4 py-3">Language</th><th className="px-4 py-3">Published</th><th className="px-4 py-3">Status</th><th className="px-4 py-3 text-right">Source</th></tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {documents.map((document) => (
                    <tr key={document.id}>
                      <td className="px-4 py-3">{document.title}</td>
                      <td className="px-4 py-3">{document.category ?? "-"}</td>
                      <td className="px-4 py-3">{document.language ?? "-"}</td>
                      <td className="px-4 py-3">{document.publicationDate ?? "-"}</td>
                      <td className="px-4 py-3"><Badge variant={document.published ? "success" : "secondary"}>{document.status}</Badge></td>
                      <td className="px-4 py-3 text-right">
                        <Button size="sm" variant="outline" asChild>
                          <a href={document.canonicalUrl} target="_blank" rel="noopener noreferrer">Open</a>
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </CardContent>
      </Card>
    </div>
  );
}
