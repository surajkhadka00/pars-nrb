# FRONTEND_AUDIT.md

## Actual stack

React 19, Vite 6, TypeScript strict, Tailwind 4, Radix primitives, TanStack Query 5, RHF/Zod, axios 1.21, Vitest/RTL (1 test). Scripts: dev/build/lint/test/test:coverage.

## Positive

- One active axios client (`api/client.ts`) with Bearer interceptor, single-flight refresh, no fake auth.
- Tokens in `sessionStorage` (tab-scoped), never localStorage.
- All pages permission-gated (`hasPermission`), 403 notice via `AccessDenied`.
- Reusable design system in `components/common/ui.tsx` (buttons, cards, dialogs, tables via page-level markup, badges, skeletons, toasts).
- External links use `rel="noopener noreferrer"`; no `dangerouslySetInnerHTML`; no sensitive console logging.

## Findings

| ID | Finding | Severity | Correction |
|---|---|---|---|
| AUD-021 | Sidebar `hidden md:flex`, no mobile drawer | H | MobileNavigation + drawer, focus trap, 320px test |
| AUD-022 | No ErrorBoundary | M | Route-level boundary with recovery |
| AUD-022 | No dark mode / system theme | M | ThemeProvider + tokens |
| AUD-022 | No i18n (English only) | M | LanguageProvider + Nepali resources |
| AUD-022 | No global search / command menu | M | Permission-aware search + `/` shortcut dialog |
| AUD-023 | Duplicate `src/api.ts` and `Spinner2` | L | Delete after import audit |
| AUD-024 | Mojibake in `index.html` title | L | Fix UTF-8 |
| FE-008 | `Field` label has no `htmlFor`/`id` wiring | M | Accessible FormField with generated ids |
| FE-009 | Skeletons are single-bar placeholders, not page-shaped | L | Shaped skeletons |
| FE-010 | Chat sources show title/URL but not persisted on reload | M | Backend fix AUD-010 + frontend render |
| FE-011 | No SSE streaming, stop/retry/regenerate/branch UI | M | Implement after backend streaming phase |
| FE-012 | Tables lack saved filters/pagination on navigation, rows-per-page, export | M | Shared DataTable/FilterBar |
| FE-013 | No print/export permission checks | M | Server-side export endpoints + audit |
| FE-014 | Nepal time/BS-Gregorian distinction not surfaced | M | Locale/date utility + labels |

## Target design system (to create before page rewrites)

AppShell, Sidebar, MobileNavigation, Topbar, PageHeader, Breadcrumb, Tabs, SearchInput, GlobalSearch, CommandMenu, DataTable, MobileDataCard, FilterBar, Pagination, Dialog, ConfirmationDialog, Drawer, Toast, EmptyState, ErrorState, Skeleton, StatusBadge, FormField, PermissionNotice, FileUploader, DocumentPreview, AIChatBubble, AISourceCard, AIResponseActions, NotificationCenter, AlertBanner, ThemeProvider, LanguageProvider.

Design tokens: navy primary, restrained green accent, neutral surfaces, 4/8px spacing scale, 8/12px radii, subtle borders, 150–220 ms transitions, `prefers-reduced-motion` support, visible focus rings, WCAG AA contrast.

## Verification

Vitest component tests, axe checks, keyboard-only pass, 320/768/1280/1920 viewports, 100%/125% zoom, dark/system theme, English/Nepali rendering, streaming/cancellation tests.
