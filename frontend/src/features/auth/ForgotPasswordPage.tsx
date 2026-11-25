import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input, Label } from "../../components/common/ui";
import { authApi } from "../../api/authApi";

const forgotSchema = z.object({
  email: z.string().email("Please enter a valid email address"),
});

export function ForgotPasswordPage() {
  const mutation = useMutation({
    mutationFn: (values: { email: string }) => authApi.requestPasswordReset(values.email),
  });

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<{ email: string }>({
    resolver: zodResolver(forgotSchema),
    defaultValues: { email: "" },
  });

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Reset password</CardTitle>
          <CardDescription>Enter the email address linked to your PARS account</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit((values) => mutation.mutate(values))} noValidate>
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" autoComplete="email" placeholder="you@example.com" {...register("email")} />
              {errors.email ? <p className="text-sm text-red-600">{errors.email.message}</p> : null}
            </div>
            {mutation.isError ? <AlertDestructive message={mutation.error instanceof Error ? mutation.error.message : "Unable to send reset request"} /> : null}
            <Button type="submit" className="w-full" disabled={mutation.isPending}>
              {mutation.isPending ? "Sending…" : "Send reset link"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

function AlertDestructive({ message }: { message: string }) {
  return <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{message}</div>;
}
