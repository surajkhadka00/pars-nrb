import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Skeleton } from "../../components/common/ui";
import { countriesApi } from "../../api/countriesApi";
import { useAuth } from "../../auth/AuthContext";

export default function CountriesPage() {
  const { hasPermission } = useAuth();

  const query = useQuery({
    queryKey: ["countries"],
    queryFn: () => countriesApi.list(0, 50),
  });

  const items = query.data ?? [];

  if (!hasPermission("COUNTRY_READ")) {
    return <Alert variant="destructive">You do not have permission to view countries.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Countries</h1>
        <p className="text-sm text-slate-500">Country master data</p>
      </div>

      <Card>
        <CardContent className="p-0">
          {query.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {query.isError ? <div className="p-6"><Alert variant="destructive">Unable to load countries: {query.error instanceof Error ? query.error.message : "Unknown error"}</Alert></div> : null}
          {!query.isPending && !query.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Value</th>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Active</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((country) => (
                    <tr key={country.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{country.id}</td>
                      <td className="px-4 py-3">{country.value}</td>
                      <td className="px-4 py-3">{country.name}</td>
                      <td className="px-4 py-3">{country.active ? "YES" : "NO"}</td>
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
