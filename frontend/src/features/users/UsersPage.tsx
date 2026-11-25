import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { Alert, Badge, Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input, Label, Skeleton } from "../../components/common/ui";
import { positionsApi } from "../../api/usersApi";
import { usersApi } from "../../api/usersApi";
import { useAuth } from "../../auth/AuthContext";
import type { CreateUserRequest, UpdateUserRequest, UserResponse } from "../../api/types";

export default function UsersPage() {
  const { hasPermission } = useAuth();
  const queryClient = useQueryClient();
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<UserResponse | null>(null);
  const [form, setForm] = useState<CreateUserRequest>({
    username: "",
    email: "",
    password: "",
    fullName: "",
    mobile: null,
    address: null,
    positionId: null,
    roleIds: [],
  });

  const usersQuery = useQuery({
    queryKey: ["users"],
    queryFn: () => usersApi.list(0, 50),
  });

  const positionsQuery = useQuery({
    queryKey: ["positions"],
    queryFn: () => positionsApi.list(0, 100),
  });

  const createMutation = useMutation({
    mutationFn: (request: CreateUserRequest) => usersApi.create(request),
    onSuccess: () => {
      setCreating(false);
      void queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: (request: UpdateUserRequest) => usersApi.update(editing!.id, request),
    onSuccess: () => {
      setEditing(null);
      void queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });

  const lockMutation = useMutation({
    mutationFn: (id: string) => usersApi.lock(id),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["users"] }),
  });

  const unlockMutation = useMutation({
    mutationFn: (id: string) => usersApi.unlock(id),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["users"] }),
  });

  const users = usersQuery.data ?? [];

  function submitCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    createMutation.mutate(form);
  }

  function submitUpdate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!editing) {
      return;
    }
    updateMutation.mutate({
      email: form.email,
      fullName: form.fullName,
      mobile: form.mobile,
      address: form.address,
      positionId: form.positionId,
      enabled: editing.enabled,
      roleIds: editing.roles,
      version: editing.version,
    });
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Users</h1>
          <p className="text-sm text-slate-500">Application user management</p>
        </div>
        <Button disabled={!hasPermission("USER_WRITE")} onClick={() => { setEditing(null); setCreating(true); setForm({ username: "", email: "", password: "", fullName: "", mobile: null, address: null, positionId: null, roleIds: [] }); }}>
          New user
        </Button>
      </div>

      {creating ? (
        <Card>
          <CardHeader>
            <CardTitle>Create user</CardTitle>
            <CardDescription>POST /api/v1/users</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="grid gap-4 sm:grid-cols-2" onSubmit={submitCreate}>
              <div className="space-y-1.5"><Label>Username</Label><Input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>Email</Label><Input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>Password</Label><Input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>Full name</Label><Input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>Mobile</Label><Input value={form.mobile ?? ""} onChange={(e) => setForm({ ...form, mobile: e.target.value || null })} /></div>
              <div className="space-y-1.5"><Label>Address</Label><Input value={form.address ?? ""} onChange={(e) => setForm({ ...form, address: e.target.value || null })} /></div>
              <div className="space-y-1.5"><Label>Position</Label><select className="flex h-9 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm" value={form.positionId ?? ""} onChange={(e) => setForm({ ...form, positionId: e.target.value || null })}>{positionsQuery.data?.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}{positionsQuery.data?.length === 0 ? <option value="">No positions</option> : null}</select></div>
              <div className="flex items-end gap-2">
                <Button type="submit" disabled={createMutation.isPending}>Save</Button>
                <Button type="button" variant="outline" onClick={() => setCreating(false)}>Cancel</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      ) : null}

      {editing ? (
        <Card>
          <CardHeader>
            <CardTitle>Edit user</CardTitle>
            <CardDescription>PUT /api/v1/users/{editing.id}</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="grid gap-4 sm:grid-cols-2" onSubmit={submitUpdate}>
              <div className="space-y-1.5"><Label>Email</Label><Input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>Full name</Label><Input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>Mobile</Label><Input value={form.mobile ?? ""} onChange={(e) => setForm({ ...form, mobile: e.target.value || null })} /></div>
              <div className="space-y-1.5"><Label>Address</Label><Input value={form.address ?? ""} onChange={(e) => setForm({ ...form, address: e.target.value || null })} /></div>
              <div className="space-y-1.5"><Label>Position</Label><select className="flex h-9 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm" value={form.positionId ?? ""} onChange={(e) => setForm({ ...form, positionId: e.target.value || null })}>{positionsQuery.data?.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}{positionsQuery.data?.length === 0 ? <option value="">No positions</option> : null}</select></div>
              <div className="flex items-end gap-2">
                <Button type="submit" disabled={updateMutation.isPending}>Save</Button>
                <Button type="button" variant="outline" onClick={() => setEditing(null)}>Cancel</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      ) : null}

      <Card>
        <CardContent className="p-0">
          {usersQuery.isPending ? <div className="p-6"><Skeleton className="h-8 w-64" /></div> : null}
          {usersQuery.isError ? <div className="p-6"><Alert variant="destructive">Unable to load users: {usersQuery.error instanceof Error ? usersQuery.error.message : "Unknown error"}</Alert></div> : null}
          {!usersQuery.isPending && !usersQuery.isError && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Username</th>
                    <th className="px-4 py-3">Email</th>
                    <th className="px-4 py-3">Full name</th>
                    <th className="px-4 py-3">Position</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Roles</th>
                    <th className="px-4 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {users.map((user) => (
                    <tr key={user.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-medium">{user.username}</td>
                      <td className="px-4 py-3">{user.email}</td>
                      <td className="px-4 py-3">{user.fullName}</td>
                      <td className="px-4 py-3">{user.positionName ?? "-"}</td>
                      <td className="px-4 py-3">{user.enabled ? <Badge variant="success">ENABLED</Badge> : <Badge variant="danger">DISABLED</Badge>}</td>
                      <td className="px-4 py-3">{user.roles.join(", ")}</td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex justify-end gap-2">
                          <Button variant="outline" size="sm" disabled={!hasPermission("USER_WRITE")} onClick={() => { setEditing(user); setForm({ username: user.username, email: user.email, password: "", fullName: user.fullName, mobile: user.mobile, address: user.address, positionId: user.positionId, roleIds: [] }); }}>Edit</Button>
                          {user.enabled ? <Button variant="outline" size="sm" disabled={!hasPermission("USER_WRITE")} onClick={() => lockMutation.mutate(user.id)}>Lock</Button> : <Button variant="outline" size="sm" disabled={!hasPermission("USER_WRITE")} onClick={() => unlockMutation.mutate(user.id)}>Unlock</Button>}
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
