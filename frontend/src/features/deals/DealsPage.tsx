import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link } from "react-router-dom";
import { Pencil, Plus, Trash2 } from "lucide-react";
import {
  Alert,
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Badge,
  Button,
  Card,
  CardContent,
} from "../../components/common/ui";
import { dealsApi, type DealPage } from "../../api/dealsApi";
import { useAuth } from "../../auth/AuthContext";
import { formatCurrency, formatDateTime } from "../../lib/format";
import type { DealResponse } from "../../api/types";

const PAGE_SIZE = 20;

export default function DealsPage() {
  const { hasPermission } = useAuth();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pendingDelete, setPendingDelete] = useState<DealResponse | null>(null);

  const dealsQuery = useQuery({
    queryKey: ["deals", "list", page, PAGE_SIZE],
    queryFn: () => dealsApi.list(page, PAGE_SIZE),
    enabled: hasPermission("DEAL_READ"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => dealsApi.remove(id),
    onSuccess: () => {
      setPendingDelete(null);
      void queryClient.invalidateQueries({ queryKey: ["deals"] });
    },
  });

  const deals: DealPage | undefined = dealsQuery.data;
  const items: DealResponse[] = deals?.items ?? [];
  const meta = deals?.meta;
  const totalPages = meta?.totalPages ?? 1;
  const isLastPage = page >= totalPages - 1;

  if (!hasPermission("DEAL_READ")) {
    return <Alert variant="destructive">You do not have permission to view deals.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Deals</h1>
          <p className="text-sm text-slate-500">Portfolio deals managed through PARS</p>
        </div>
        {hasPermission("DEAL_WRITE") ? (
          <Button asChild>
            <Link to="/deals/new">
              <Plus className="h-4 w-4" /> New deal
            </Link>
          </Button>
        ) : null}
      </div>

      <Card>
        <CardContent className="p-0">
          {dealsQuery.isPending ? (
            <div className="space-y-2 p-6">
              <div className="h-8 w-1/3 animate-pulse rounded bg-slate-200" />
              <div className="h-8 w-full animate-pulse rounded bg-slate-200" />
              <div className="h-8 w-full animate-pulse rounded bg-slate-200" />
            </div>
          ) : dealsQuery.isError ? (
            <div className="p-6">
              <Alert variant="destructive">
                Unable to load deals: {dealsQuery.error instanceof Error ? dealsQuery.error.message : "Unknown error"}
              </Alert>
            </div>
          ) : items.length === 0 ? (
            <div className="p-6">
              <p className="text-sm text-slate-500">No deals found.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Document</th>
                    <th className="px-4 py-3">Deal date</th>
                    <th className="px-4 py-3">Amount</th>
                    <th className="px-4 py-3">Currency</th>
                    <th className="px-4 py-3">Counterparty</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((deal) => (
                    <tr key={deal.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{deal.documentNo}</td>
                      <td className="px-4 py-3">{deal.dealDate ? formatDateTime(deal.dealDate) : "-"}</td>
                      <td className="px-4 py-3">{formatCurrency(deal.amount)}</td>
                      <td className="px-4 py-3">{deal.currencyId ?? "-"}</td>
                      <td className="px-4 py-3">{deal.counterpartyId ?? "-"}</td>
                      <td className="px-4 py-3">
                        <Badge variant={deal.docStatus === "ACTIVE" ? "success" : "secondary"}>{deal.docStatus ?? "-"}</Badge>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex justify-end gap-2">
                          <Button variant="outline" size="sm" asChild>
                            <Link to={`/deals/${deal.id}`}>Open</Link>
                          </Button>
                          {hasPermission("DEAL_WRITE") ? (
                            <>
                              <Button variant="outline" size="sm" asChild>
                                <Link to={`/deals/${deal.id}/edit`}>
                                  <Pencil className="h-3.5 w-3.5" /> Edit
                                </Link>
                              </Button>
                              <Button variant="destructive" size="sm" onClick={() => setPendingDelete(deal)}>
                                <Trash2 className="h-3.5 w-3.5" /> Delete
                              </Button>
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

      <div className="flex items-center justify-between text-sm text-slate-600">
        <span>
          Page {(meta?.page ?? page) + 1} - {meta?.totalElements ?? items.length} records
        </span>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" disabled={(meta?.page ?? page) === 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
            Previous
          </Button>
          <Button variant="outline" size="sm" disabled={isLastPage} onClick={() => setPage((p) => p + 1)}>
            Next
          </Button>
        </div>
      </div>

      <AlertDialog open={pendingDelete !== null} onOpenChange={(open) => { if (!open) setPendingDelete(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete deal</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete deal {pendingDelete?.documentNo}. This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              className="bg-red-600 text-white hover:bg-red-700"
              disabled={deleteMutation.isPending}
              onClick={() => pendingDelete ? deleteMutation.mutate(pendingDelete.id) : undefined}
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
