import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Alert, Button, Card, CardContent, Input, Label, Skeleton } from "../../components/common/ui";
import { rolesApi } from "../../api/usersApi";
import { useAuth } from "../../auth/AuthContext";
import type { RoleResponse } from "../../api/types";

export default function RolesPage() {
  const { hasPermission } = useAuth();
  const queryClient = useQueryClient();
  const [code, setCode] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const rolesQuery = useQuery({
    queryKey: ["roles"],
    queryFn: () => rolesApi.list(0, 50),
  });

  const createMutation = useMutation({
    mutationFn: () => rolesApi.create({ code, name, description, permissions: [], version: 0 }),
    onSuccess: () => {
      setCode("");
      setName("");
      setDescription("");
      void queryClient.invalidateQueries({ queryKey: ["roles"] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: (variables: { id: string; role: RoleResponse }) =>
      rolesApi.update(variables.id, {
        code: variables.role.code,
        name: variables.role.name,
        description: variables.role.description,
        permissions: variables.role.permissions,
        version: variables.role.version,
      }),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["roles"] }),
  });

  const removeMutation = useMutation({
    mutationFn: (id: string) => rolesApi.remove(id),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["roles"] }),
  });

  const items = rolesQuery.data ?? [];

  if (!hasPermission("ROLE_READ")) {
    return <Alert variant="destructive">You do not have permission to view roles.</Alert>;
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Roles</h1>
          <p className="text-sm text-slate-500">Role master data</p>
        </div>
        <div className="flex items-end gap-2">
          <div className="space-y-1.5">
            <Label>Code</Label>
            <Input value={code} onChange={(e) => setCode(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label>Name</Label>
            <Input value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label>Description</Label>
            <Input value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
          <Button disabled={createMutation.isPending || !code || !name} onClick={() => createMutation.mutate()}>Create</Button>
        </div>
      </div>

      <Card>
        <CardContent className="p-0">
          {rolesQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {rolesQuery.isError ? <div className="p-6"><Alert variant="destructive">Unable to load roles: {rolesQuery.error instanceof Error ? queryErrorMessage(rolesQuery.error) : "Unknown error"}</Alert></div> : null}
          {!rolesQuery.isPending && !rolesQuery.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Id</th>
                    <th className="px-4 py-3">Code</th>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Permissions</th>
                    <th className="px-4 py-3">Version</th>
                    <th className="px-4 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {items.map((role) => (
                    <tr key={role.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono text-xs">{role.id}</td>
                      <td className="px-4 py-3 font-mono font-medium">{role.code}</td>
                      <td className="px-4 py-3">{role.name}</td>
                      <td className="px-4 py-3">{role.permissions.join(", ")}</td>
                      <td className="px-4 py-3">{role.version}</td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex justify-end gap-2">
                          <Button variant="outline" size="sm" disabled={updateMutation.isPending} onClick={() => updateMutation.mutate({ id: role.id, role })}>Edit</Button>
                          <Button variant="destructive" size="sm" disabled={removeMutation.isPending} onClick={() => removeMutation.mutate(role.id)}>Delete</Button>
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

function queryErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Unknown error";
}
