import type { ReactNode } from "react";
import {
  AlertCircle,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Copy,
  FileText,
  Info,
  RefreshCw,
  Search,
  ThumbsDown,
  ThumbsUp,
  X,
} from "lucide-react";
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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Skeleton,
} from "../common/ui";
import { cn } from "../../utils/cn";

export function PageHeader({ title, description, actions, eyebrow }: {
  title: string;
  description?: string;
  actions?: ReactNode;
  eyebrow?: string;
}) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div className="min-w-0">
        {eyebrow ? <p className="mb-1 text-xs font-semibold uppercase tracking-widest text-green-accent-600">{eyebrow}</p> : null}
        <h1 className="truncate text-xl font-semibold tracking-tight text-navy-900 sm:text-2xl dark:text-slate-100">{title}</h1>
        {description ? <p className="mt-1 max-w-2xl text-sm text-slate-500 dark:text-slate-400">{description}</p> : null}
      </div>
      {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
    </div>
  );
}

export function Breadcrumb({ items }: { items: { label: string; href?: string }[] }) {
  return (
    <nav aria-label="Breadcrumb" className="flex flex-wrap items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
      {items.map((item, index) => (
        <span key={item.label} className="flex items-center gap-1.5">
          {index > 0 ? <span aria-hidden="true">/</span> : null}
          {item.href ? <a href={item.href} className="hover:text-navy-700 hover:underline dark:hover:text-slate-200">{item.label}</a> : <span className="font-medium text-navy-800 dark:text-slate-200">{item.label}</span>}
        </span>
      ))}
    </nav>
  );
}

export function SearchInput({ value, onChange, placeholder = "Search…", label = "Search" }: {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  label?: string;
}) {
  return (
    <div className="relative w-full sm:max-w-xs">
      <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" aria-hidden="true" />
      <Input
        aria-label={label}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        className="pl-9 pr-8"
      />
      {value ? (
        <button type="button" aria-label="Clear search" onClick={() => onChange("")} className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-slate-400 hover:text-slate-700">
          <X className="h-3.5 w-3.5" />
        </button>
      ) : null}
    </div>
  );
}

export function FilterBar({ children, onReset }: { children: ReactNode; onReset?: () => void }) {
  return (
    <div className="mb-4 flex flex-col gap-3 rounded-lg border border-slate-200 bg-white p-3 sm:flex-row sm:flex-wrap sm:items-end dark:border-slate-700 dark:bg-slate-900/60">
      {children}
      {onReset ? <Button variant="ghost" size="sm" onClick={onReset} className="sm:ml-auto">Reset filters</Button> : null}
    </div>
  );
}

export function Pagination({ page, size, total, onPage, onSize }: {
  page: number;
  size: number;
  total: number;
  onPage: (page: number) => void;
  onSize?: (size: number) => void;
}) {
  const pages = Math.max(1, Math.ceil(total / size));
  return (
    <div className="flex flex-col gap-3 border-t border-slate-200 px-4 py-3 text-xs text-slate-500 sm:flex-row sm:items-center sm:justify-between dark:border-slate-700">
      <div className="flex items-center gap-2">
        <span>Rows per page</span>
        <select
          aria-label="Rows per page"
          className="rounded-md border border-slate-300 bg-white px-2 py-1 text-xs dark:border-slate-600 dark:bg-slate-900"
          value={size}
          onChange={(event) => onSize?.(Number(event.target.value))}
        >
          {[10, 20, 50].map((value) => <option key={value} value={value}>{value}</option>)}
        </select>
        <span className="ml-2">{total} record(s) · page {page + 1} of {pages}</span>
      </div>
      <div className="flex items-center gap-2">
        <Button variant="outline" size="sm" disabled={page <= 0} onClick={() => onPage(page - 1)} aria-label="Previous page"><ChevronLeft className="h-4 w-4" /> Previous</Button>
        <Button variant="outline" size="sm" disabled={page >= pages - 1} onClick={() => onPage(page + 1)} aria-label="Next page">Next <ChevronRight className="h-4 w-4" /></Button>
      </div>
    </div>
  );
}

export interface Column<T> {
  key: string;
  header: string;
  render: (row: T) => ReactNode;
  className?: string;
}

export function DataTable<T>({ columns, rows, rowKey, loading, emptyText = "No records found.", onRowClick }: {
  columns: Column<T>[];
  rows: T[];
  rowKey: (row: T) => string;
  loading?: boolean;
  emptyText?: string;
  onRowClick?: (row: T) => void;
}) {
  if (loading) return <TableSkeleton rows={5} columns={columns.length} />;
  if (rows.length === 0) return <EmptyState title={emptyText} description="Adjust filters or add a new record to get started." />;

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[640px] text-left text-sm">
        <thead className="bg-slate-50 text-[11px] font-semibold uppercase tracking-wider text-slate-500 dark:bg-slate-900/70 dark:text-slate-400">
          <tr>{columns.map((column) => <th key={column.key} scope="col" className={cn("px-4 py-3", column.className)}>{column.header}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
          {rows.map((row) => (
            <tr
              key={rowKey(row)}
              onClick={onRowClick ? () => onRowClick(row) : undefined}
              className={cn("transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/50", onRowClick ? "cursor-pointer" : "")}
            >
              {columns.map((column) => <td key={column.key} className={cn("px-4 py-3 align-middle", column.className)}>{column.render(row)}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function StatusBadge({ value, tone }: { value: string; tone?: "success" | "warning" | "danger" | "neutral" }) {
  const resolved = tone ?? (/ACTIVE|PUBLISHED|ENABLED|APPROVED|PAID/i.test(value) ? "success" : /FAILED|REJECTED|DISABLED|ERROR/i.test(value) ? "danger" : /PENDING|REVIEW|DRAFT/i.test(value) ? "warning" : "neutral");
  return <Badge variant={resolved === "neutral" ? "secondary" : resolved}>{value}</Badge>;
}

export function FormField({ label, htmlFor, error, hint, required, children }: {
  label: string;
  htmlFor?: string;
  error?: string;
  hint?: string;
  required?: boolean;
  children: ReactNode;
}) {
  return (
    <div className="space-y-1.5">
      <Label htmlFor={htmlFor}>{label}{required ? <span className="ml-0.5 text-red-500" aria-hidden="true">*</span> : null}</Label>
      {children}
      {hint && !error ? <p className="text-xs text-slate-500">{hint}</p> : null}
      {error ? <p className="flex items-center gap-1 text-xs text-red-600" role="alert"><AlertCircle className="h-3.5 w-3.5" />{error}</p> : null}
    </div>
  );
}

export function PermissionNotice({ permission }: { permission: string }) {
  return (
    <Alert variant="destructive" className="pars-enter">
      <div className="flex items-start gap-2">
        <Info className="mt-0.5 h-4 w-4" aria-hidden="true" />
        <div>
          <p className="font-medium">Permission required</p>
          <p className="text-sm">You need <code className="rounded bg-red-100 px-1 py-0.5 text-xs dark:bg-red-900/40">{permission}</code> to use this feature. Contact your administrator.</p>
        </div>
      </div>
    </Alert>
  );
}

export function EmptyState({ title, description, action }: { title: string; description?: string; action?: ReactNode }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-slate-300 px-6 py-12 text-center dark:border-slate-700">
      <FileText className="h-8 w-8 text-slate-300" aria-hidden="true" />
      <p className="mt-3 text-sm font-medium text-slate-700 dark:text-slate-200">{title}</p>
      {description ? <p className="mt-1 max-w-sm text-xs text-slate-500">{description}</p> : null}
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="rounded-lg border border-red-200 bg-red-50 p-5 dark:border-red-900/50 dark:bg-red-950/30" role="alert">
      <div className="flex items-start gap-3">
        <AlertCircle className="mt-0.5 h-5 w-5 text-red-600" aria-hidden="true" />
        <div className="flex-1">
          <p className="text-sm font-medium text-red-800 dark:text-red-300">Something went wrong</p>
          <p className="mt-1 text-sm text-red-700 dark:text-red-400">{message}</p>
        </div>
        {onRetry ? <Button size="sm" variant="outline" onClick={onRetry}><RefreshCw className="h-3.5 w-3.5" /> Retry</Button> : null}
      </div>
    </div>
  );
}

export function TableSkeleton({ rows = 5, columns = 4 }: { rows?: number; columns?: number }) {
  return (
    <div className="space-y-2 p-4" aria-busy="true" aria-label="Loading data">
      {Array.from({ length: rows }).map((_, row) => (
        <div key={row} className="grid gap-3" style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}>
          {Array.from({ length: columns }).map((__, col) => <Skeleton key={col} className="pars-shimmer h-8 w-full rounded" />)}
        </div>
      ))}
    </div>
  );
}

export function ConfirmationDialog({ open, onOpenChange, title, description, confirmLabel = "Confirm", destructive, onConfirm }: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description: string;
  confirmLabel?: string;
  destructive?: boolean;
  onConfirm: () => void;
}) {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription>{description}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancel</AlertDialogCancel>
          <AlertDialogAction onClick={onConfirm} className={destructive ? "bg-red-600 hover:bg-red-700" : ""}>{confirmLabel}</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

export function Drawer({ open, onOpenChange, title, description, children }: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  children: ReactNode;
}) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="left-auto right-0 top-0 h-full max-w-md translate-x-0 translate-y-0 rounded-none border-l p-0">
        <div className="border-b border-slate-200 p-5 dark:border-slate-700">
          <DialogHeader>
            <DialogTitle>{title}</DialogTitle>
            {description ? <DialogDescription>{description}</DialogDescription> : null}
          </DialogHeader>
        </div>
        <div className="overflow-y-auto p-5">{children}</div>
      </DialogContent>
    </Dialog>
  );
}

export function AlertBanner({ kind = "info", message }: { kind?: "info" | "success" | "warning"; message: string }) {
  const tone = kind === "success" ? "border-green-accent-300 bg-green-accent-50 text-green-accent-900" : kind === "warning" ? "border-amber-300 bg-amber-50 text-amber-900" : "border-navy-200 bg-navy-50 text-navy-800";
  return (
    <div className={cn("mb-4 flex items-center gap-2 rounded-lg border px-4 py-2.5 text-sm", tone)} role="status">
      {kind === "success" ? <CheckCircle2 className="h-4 w-4" /> : <Info className="h-4 w-4" />}
      {message}
    </div>
  );
}

export function AIChatBubble({ role, children, timestamp }: { role: "user" | "assistant"; children: ReactNode; timestamp?: string }) {
  const user = role === "user";
  return (
    <div className={cn("pars-enter flex", user ? "justify-end" : "justify-start")}>
      <div className={cn("max-w-[88%] rounded-2xl px-4 py-3 text-sm shadow-sm", user ? "rounded-br-sm bg-navy-800 text-white" : "rounded-bl-sm border border-slate-200 bg-white text-slate-800 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100")}>
        {children}
        {timestamp ? <p className={cn("mt-2 text-[10px]", user ? "text-navy-200" : "text-slate-400")}>{timestamp}</p> : null}
      </div>
    </div>
  );
}

export function AISourceCard({ title, url, page, section, published }: {
  title: string;
  url?: string | null;
  page?: number;
  section?: string;
  published?: string;
}) {
  return (
    <div className="rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs dark:border-slate-700 dark:bg-slate-800/50">
      {url ? <a href={url} target="_blank" rel="noopener noreferrer" className="font-medium text-navy-700 underline dark:text-green-accent-300">{title}</a> : <p className="font-medium">{title}</p>}
      <p className="mt-1 text-slate-500">
        {[published, page ? `p. ${page}` : null, section].filter(Boolean).join(" · ") || "Official PARS NRB source"}
      </p>
    </div>
  );
}

export function AIResponseActions({ onCopy, onRetry, onFeedback, copied }: {
  onCopy: () => void;
  onRetry?: () => void;
  onFeedback?: (rating: "up" | "down") => void;
  copied?: boolean;
}) {
  return (
    <div className="flex items-center gap-1 border-t border-slate-100 pt-2 dark:border-slate-800">
      <Button variant="ghost" size="sm" onClick={onCopy} aria-label="Copy answer">{copied ? <CheckCircle2 className="h-3.5 w-3.5" /> : <Copy className="h-3.5 w-3.5" />} Copy</Button>
      {onRetry ? <Button variant="ghost" size="sm" onClick={onRetry}><RefreshCw className="h-3.5 w-3.5" /> Retry</Button> : null}
      {onFeedback ? (
        <div className="ml-auto flex gap-1">
          <Button variant="ghost" size="icon" onClick={() => onFeedback("up")} aria-label="Good answer"><ThumbsUp className="h-3.5 w-3.5" /></Button>
          <Button variant="ghost" size="icon" onClick={() => onFeedback("down")} aria-label="Poor answer"><ThumbsDown className="h-3.5 w-3.5" /></Button>
        </div>
      ) : null}
    </div>
  );
}
