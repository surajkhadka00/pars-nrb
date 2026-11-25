import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Button, Card, CardContent, CardHeader, CardTitle, Input, Skeleton } from "../../components/common/ui";
import { exchangeRatesApi } from "../../api/exchangeRatesApi";
import { useAuth } from "../../auth/AuthContext";

export default function ExchangeRatesPage() {
  const { hasPermission } = useAuth();
  const queryClient = useQueryClient();
  const [rateDate, setRateDate] = useState("");
  const [file, setFile] = useState<File | null>(null);

  const ratesQuery = useQuery({
    queryKey: ["exchange-rates"],
    queryFn: () => exchangeRatesApi.list(0, 50),
  });

  const uploadMutation = useMutation({
    mutationFn: (payload: { file: File; rateDate: string }) => exchangeRatesApi.upload(payload.file, payload.rateDate),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["exchange-rates"] });
      setFile(null);
      setRateDate("");
    },
  });

  const items = ratesQuery.data ?? [];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Exchange rates</h1>
        <p className="text-sm text-slate-500">Foreign exchange rates used for deal valuation</p>
      </div>

      <Card>
        <CardHeader className="pb-0">
          <CardTitle>Import exchange rate</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700">Rate date</label>
              <Input type="date" value={rateDate} onChange={(event) => setRateDate(event.target.value)} />
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700">Excel file</label>
              <input
                type="file"
                accept=".xlsx,.xls,.csv"
                onChange={(event) => setFile(event.target.files?.[0] ?? null)}
                className="block w-full text-sm text-slate-500 file:mr-4 file:rounded-md file:border-0 file:bg-slate-100 file:px-4 file:py-2 file:text-sm file:font-medium file:text-slate-700 hover:file:bg-slate-200"
              />
            </div>
          </div>
          {uploadMutation.isError ? <Alert variant="destructive">Unable to import exchange rates: {uploadMutation.error instanceof Error ? uploadMutation.error.message : "Unknown error"}</Alert> : null}
          <Button
            disabled={!file || !rateDate || !hasPermission("CURRENCY_WRITE")}
            onClick={() => file && rateDate ? uploadMutation.mutate({ file, rateDate }) : undefined}
          >
            {uploadMutation.isPending ? "Importing…" : "Import rates"}
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-0">
          {ratesQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {ratesQuery.isError ? <div className="p-6"><Alert variant="destructive">Unable to load exchange rates: {ratesQuery.error instanceof Error ? ratesQuery.error.message : "Unknown error"}</Alert></div> : null}
          {!ratesQuery.isPending && !ratesQuery.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Base currency</th>
                    <th className="px-4 py-3">Counter currency</th>
                    <th className="px-4 py-3">Rate</th>
                    <th className="px-4 py-3">Rate date</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((rate) => (
                    <tr key={rate.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{rate.id}</td>
                      <td className="px-4 py-3">{rate.baseCurrencyIsoCode}</td>
                      <td className="px-4 py-3">{rate.currencyIsoCode}</td>
                      <td className="px-4 py-3">{rate.rate}</td>
                      <td className="px-4 py-3">{rate.rateDate ? new Date(rate.rateDate).toLocaleDateString() : "-"}</td>
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
