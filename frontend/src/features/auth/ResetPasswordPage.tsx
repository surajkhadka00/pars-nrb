import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { useNavigate } from "react-router-dom";
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input, Label } from "../../components/common/ui";
import { authApi } from "../../api/authApi";

const resetSchema = z.object({
  token: z.string().min(1, "Reset token is required"),
  newPassword: z.string().min(8, "Password must be at least 8 characters"),
});

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const mutation = useMutation({
    mutationFn: (values: { token: string; newPassword: string }) => authApi.resetPassword(values.token, values.newPassword),
    onSuccess: () => navigate("/login"),
  });

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<{ token: string; newPassword: string }>({
    resolver: zodResolver(resetSchema),
    defaultValues: { token: "", newPassword: "" },
  });

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Reset password</CardTitle>
          <CardDescription>Enter the reset token and choose a new password</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit((values) => mutation.mutate(values))} noValidate>
            <div className="space-y-2">
              <Label htmlFor="token">Reset token</Label>
              <Input id="token" placeholder="Token from the reset email" {...register("token")} />
              {errors.token ? <p className="text-sm text-red-600">{errors.token.message}</p> : null}
            </div>
            <div className="space-y-2">
              <Label htmlFor="newPassword">New password</Label>
              <Input id="newPassword" type="password" autoComplete="new-password" {...register("newPassword")} />
              {errors.newPassword ? <p className="text-sm text-red-600">{errors.newPassword.message}</p> : null}
            </div>
            {mutation.isError ? <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{mutation.error instanceof Error ? mutation.error.message : "Unable to reset password"}</div> : null}
            <Button type="submit" className="w-full" disabled={mutation.isPending}>
              {mutation.isPending ? "Resetting…" : "Reset password"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
