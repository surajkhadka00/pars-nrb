import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Alert, Badge, Button, Card, CardContent, CardHeader, CardTitle, Input, Label, Skeleton } from "../../components/common/ui";
import { nrbApi } from "../../api/aiApi";
import { useAuth } from "../../auth/AuthContext";
import { formatDateTime } from "../../lib/format";

export default function NrbForexPage() {
  const { hasPermission } = useAuth();
  const [from, setFrom] = useState("2026-11-16");
  const [to, setTo] = useState("2026-11-18");
  const [code, setCode] = useState("USD");
  const [date, setDate] = useState("2026-11-18");
  const [searched, setSearched] = useState(false);

  const latestQuery = useQuery({
    queryKey: ["nrb", "forex", "latest"],
    queryFn: () => nrbApi.latest(),
    enabled: hasPermission("AI_FOREX_READ"),
  });

  const historicalQuery = useQuery({
    queryKey: ["nrb", "forex", "historical", from, to],
    queryFn: () => nrbApi.historical(from, to),
    enabled: hasPermission("AI_FOREX_READ") && searched,
  });

  const currencyQuery = useQuery({
    queryKey: ["nrb", "forex", "currency", code, date],
    queryFn: () => nrbApi.byCurrency(code, date),
    enabled: hasPermission("AI_FOREX_READ") && searched,
  });

  if (!hasPermission("AI_FOREX_READ")) {
    return <Alert variant="destructive">You do not have permission to view NRB forex rates.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Official NRB forex rates</h1>
        <p className="text-sm text-slate-500">Indicative rates from Nepal Rastra Bank — not commercial-bank transaction rates</p>
      </div>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle>Latest published rates</CardTitle>
        </CardHeader>
        <CardContent>
          {latestQuery.isPending ? <Skeleton className="h-24 w-full" /> : null}
          {latestQuery.isError ? <Alert variant="destructive">Unable to load the latest rates</Alert> : null}
          {latestQuery.data ? (
            <div className="space-y-3">
              <p className="text-sm text-slate-600">
                Rate date: <strong>{latestQuery.data.date}</strong> · Published: {formatDateTime(latestQuery.data.publishedOn)} · Source:{" "}
                <a className="text-navy-700 underline" href={latestQuery.data.sourceUrl} target="_blank" rel="noopener noreferrer">NRB official API</a>
              </p>
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                    <tr><th className="px-3 py-2">Currency</th><th className="px-3 py-2">Unit</th><th className="px-3 py-2">Buy</th><th className="px-3 py-2">Sell</th></tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {latestQuery.data.rates.map((rate) => (
                      <tr key={rate.iso3}>
                        <td className="px-3 py-2">{rate.iso3} — {rate.name}</td>
                        <td className="px-3 py-2">{rate.unit}</td>
                        <td className="px-3 py-2">{String(rate.buy)}</td>
                        <td className="px-3 py-2">{String(rate.sell)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ) : null}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2"><CardTitle>Historical query</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-wrap items-end gap-3">
            <div className="space-y-1.5"><Label>From</Label><Input type="date" value={from} onChange={(e) => setFrom(e.target.value)} /></div>
            <div className="space-y-1.5"><Label>To</Label><Input type="date" value={to} onChange={(e) => setTo(e.target.value)} /></div>
            <div className="space-y-1.5"><Label>Currency</Label><Input value={code} onChange={(e) => setCode(e.target.value.toUpperCase())} maxLength={3} /></div>
            <div className="space-y-1.5"><Label>Date</Label><Input type="date" value={date} onChange={(e) => setDate(e.target.value)} /></div>
            <Button onClick={() => setSearched(true)}>Search</Button>
          </div>
          {searched && historicalQuery.data ? (
            <div className="space-y-2">
              {historicalQuery.data.map((day) => (
                <div key={day.date} className="rounded-md border border-slate-200 p-3 text-sm">
                  <Badge variant="secondary">{day.date}</Badge> — {day.rates.length} rates
                </div>
              ))}
            </div>
          ) : null}
          {searched && currencyQuery.data ? (
            <div className="rounded-md bg-slate-50 p-3 text-sm">
              {code} on {date}: buy {String(currencyQuery.data.rates[0]?.buy ?? "-")} / sell {String(currencyQuery.data.rates[0]?.sell ?? "-")}
            </div>
          ) : null}
        </CardContent>
      </Card>
    </div>
  );
}
