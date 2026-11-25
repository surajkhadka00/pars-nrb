import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { useNavigate } from "react-router-dom";
import { Alert, Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input, Label } from "../../components/common/ui";
import { useAuth } from "../../auth/AuthContext";
import { ApiError } from "../../api/client";

const loginSchema = z.object({
  username: z.string().min(3, "Username must be at least 3 characters").max(64, "Username must be at most 64 characters"),
  password: z.string().min(1, "Password is required"),
});

export type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const mutation = useMutation({
    mutationFn: (values: LoginFormValues) => login(values.username, values.password),
    onSuccess: () => {
      navigate("/", { replace: true });
    },
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: "", password: "" },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await mutation.mutateAsync(values);
    } catch {
      // Error is surfaced through mutation error state.
    }
  });

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <img src="/nrb-logo.jpg" alt="Nepal Rastra Bank official seal" className="mx-auto h-16 w-16 rounded-full bg-white p-1 shadow-sm ring-1 ring-slate-200" />
          <CardTitle className="mt-3 text-2xl">Sign in to PARS</CardTitle>
          <CardDescription>Portfolio Analysis and Reporting System for Nepal Rastra Bank</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={onSubmit} noValidate>
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input id="username" autoComplete="username" placeholder="Enter your username" {...register("username")} />
              {errors.username ? <p className="text-sm text-red-600">{errors.username.message}</p> : null}
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" autoComplete="current-password" placeholder="Enter your password" {...register("password")} />
              {errors.password ? <p className="text-sm text-red-600">{errors.password.message}</p> : null}
            </div>
            {mutation.isError ? (
              <ErrorMessage error={mutation.error} />
            ) : null}
            <Button type="submit" className="w-full" disabled={mutation.isPending || isSubmitting}>
              {mutation.isPending ? "Signing in…" : "Sign in"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

function ErrorMessage({ error }: { error: unknown }) {
  const message = error instanceof ApiError ? error.message : error instanceof Error ? error.message : "Unable to sign in";
  return <Alert variant="destructive">{message}</Alert>;
}
