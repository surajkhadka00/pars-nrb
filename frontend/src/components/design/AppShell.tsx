import { useEffect, useMemo, useState, type ReactNode } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  Bell,
  ChevronDown,
  Clock,
  Command,
  Globe,
  LogOut,
  Menu,
  Search,
  ShieldAlert,
  Star,
  Sun,
  X,
} from "lucide-react";
import { Badge, Button, Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, Input } from "../common/ui";
import { Breadcrumb } from "./Primitives";
import { useAuth } from "../../auth/AuthContext";
import { useTheme } from "./ThemeProvider";
import { useLanguage } from "./LanguageProvider";
import { cn } from "../../utils/cn";

export interface NavEntry {
  to: string;
  label: string;
  icon: ReactNode;
  permission?: string;
  adminOnly?: boolean;
  section: string;
}

const FAVORITES_KEY = "pars.favorites";
const RECENT_KEY = "pars.recent";

function readList(key: string): string[] {
  try {
    const value = JSON.parse(localStorage.getItem(key) ?? "[]");
    return Array.isArray(value) ? value.filter((item): item is string => typeof item === "string") : [];
  } catch {
    return [];
  }
}

export function AppShell({ nav }: { nav: NavEntry[] }) {
  const { user, hasPermission, hasRole, logout, logoutAll } = useAuth();
  const { theme, resolved, setTheme } = useTheme();
  const { language, setLanguage, t } = useLanguage();
  const location = useLocation();
  const navigate = useNavigate();

  const [mobileOpen, setMobileOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [profileOpen, setProfileOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [favorites, setFavorites] = useState<string[]>(() => readList(FAVORITES_KEY));
  const [recent, setRecent] = useState<string[]>(() => readList(RECENT_KEY));

  const visible = useMemo(
    () => nav.filter((item) => {
      if (item.adminOnly && !hasRole("ADMIN")) return false;
      if (item.permission && !hasPermission(item.permission)) return false;
      return true;
    }),
    [nav, hasPermission, hasRole],
  );

  useEffect(() => {
    // Route change is an external event: close overlays and persist navigation history.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setMobileOpen(false);
    setProfileOpen(false);
    setNotificationsOpen(false);
    setRecent((current) => {
      const next = [location.pathname, ...current.filter((path) => path !== location.pathname)].slice(0, 6);
      localStorage.setItem(RECENT_KEY, JSON.stringify(next));
      return next;
    });
  }, [location.pathname]);

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "/" && !/INPUT|TEXTAREA|SELECT/i.test((event.target as HTMLElement)?.tagName ?? "")) {
        event.preventDefault();
        setSearchOpen(true);
      }
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        setSearchOpen(true);
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, []);

  const toggleFavorite = (path: string) => {
    setFavorites((current) => {
      const next = current.includes(path) ? current.filter((item) => item !== path) : [...current, path];
      localStorage.setItem(FAVORITES_KEY, JSON.stringify(next));
      return next;
    });
  };

  const current = visible.find((item) => location.pathname.startsWith(item.to));
  const results = visible.filter((item) => item.label.toLowerCase().includes(searchText.toLowerCase())).slice(0, 8);
  const favoriteItems = visible.filter((item) => favorites.includes(item.to));

  const navList = (items: NavEntry[]) => (
    <ul className="space-y-0.5">
      {items.map((item) => (
        <li key={item.to} className="group relative">
          <NavLink
            to={item.to}
            className={({ isActive }) => cn(
              "flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm transition-colors duration-150",
              isActive
                ? "bg-navy-800 font-medium text-white shadow-sm"
                : "text-navy-100/80 hover:bg-white/10 hover:text-white",
            )}
          >
            {item.icon}
            <span className="flex-1 truncate">{item.label}</span>
            <button
              type="button"
              aria-label={favorites.includes(item.to) ? `Remove ${item.label} from favorites` : `Add ${item.label} to favorites`}
              onClick={(event) => { event.preventDefault(); toggleFavorite(item.to); }}
              className={cn("rounded p-0.5 opacity-0 transition-opacity group-hover:opacity-100 focus:opacity-100", favorites.includes(item.to) ? "text-amber-300 opacity-100" : "text-navy-200")}
            >
              <Star className="h-3.5 w-3.5" fill={favorites.includes(item.to) ? "currentColor" : "none"} />
            </button>
          </NavLink>
        </li>
      ))}
    </ul>
  );

  return (
    <div className="min-h-screen bg-[var(--color-surface)] text-slate-800 dark:text-slate-100">
      {/* Desktop sidebar */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col border-r border-navy-800 bg-navy-900 lg:flex">
        <div className="flex h-16 items-center gap-2.5 border-b border-navy-800 px-4">
          <img src="/nrb-logo.jpg" alt="Nepal Rastra Bank official seal" className="h-9 w-9 rounded-full bg-white p-0.5 ring-2 ring-white/20" />
          <div className="leading-tight">
            <p className="text-sm font-semibold tracking-wide text-white">PARS</p>
            <p className="text-[10px] uppercase tracking-widest text-navy-200">Nepal Rastra Bank</p>
          </div>
        </div>
        <div className="flex-1 overflow-y-auto px-3 py-4">
          {favoriteItems.length > 0 ? (
            <div className="mb-5">
              <p className="mb-2 px-3 text-[10px] font-semibold uppercase tracking-widest text-navy-300">Favorites</p>
              {navList(favoriteItems)}
            </div>
          ) : null}
          {Array.from(new Set(visible.map((item) => item.section))).map((section) => (
            <div key={section} className="mb-5">
              <p className="mb-2 px-3 text-[10px] font-semibold uppercase tracking-widest text-navy-300">{section}</p>
              {navList(visible.filter((item) => item.section === section && !favorites.includes(item.to)))}
            </div>
          ))}
        </div>
        <div className="border-t border-navy-800 p-4 text-[10px] text-navy-300">
          PARS NRB · Secure institutional workspace
        </div>
      </aside>

      {/* Mobile drawer */}
      {mobileOpen ? (
        <div className="fixed inset-0 z-50 lg:hidden">
          <button type="button" aria-label="Close navigation" className="absolute inset-0 bg-navy-950/60" onClick={() => setMobileOpen(false)} />
          <div className="pars-enter absolute inset-y-0 left-0 flex w-72 max-w-[85vw] flex-col border-r border-navy-800 bg-navy-900 shadow-xl" role="dialog" aria-modal="true" aria-label="Navigation menu">
            <div className="flex h-16 items-center justify-between border-b border-navy-800 px-4">
              <div className="flex items-center gap-2"><img src="/nrb-logo.jpg" alt="Nepal Rastra Bank official seal" className="h-7 w-7 rounded-full bg-white p-0.5" /><span className="font-semibold text-white">PARS</span></div>
              <Button variant="ghost" size="icon" onClick={() => setMobileOpen(false)} aria-label="Close menu"><X className="h-5 w-5 text-white" /></Button>
            </div>
            <nav className="flex-1 overflow-y-auto px-3 py-4">{navList(visible)}</nav>
          </div>
        </div>
      ) : null}

      {/* Main column */}
      <div className="lg:pl-64">
        <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/95 px-3 py-2.5 backdrop-blur sm:px-5 dark:border-slate-700 dark:bg-slate-900/95">
          <div className="flex items-center gap-2">
            <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => setMobileOpen(true)} aria-label="Open navigation"><Menu className="h-5 w-5" /></Button>
            <button
              type="button"
              onClick={() => setSearchOpen(true)}
              className="flex h-9 flex-1 items-center gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-left text-xs text-slate-500 transition hover:border-navy-300 sm:max-w-md dark:border-slate-700 dark:bg-slate-800"
            >
              <Search className="h-4 w-4" />
              <span className="flex-1">{t("search")} deals, documents, forex…</span>
              <kbd className="hidden rounded border border-slate-300 bg-white px-1.5 py-0.5 text-[10px] sm:block dark:border-slate-600 dark:bg-slate-900">/</kbd>
            </button>

            <div className="ml-auto flex items-center gap-1">
              <Button variant="ghost" size="icon" onClick={() => setSearchOpen(true)} aria-label="Command menu"><Command className="h-4 w-4" /></Button>
              <div className="relative">
                <Button variant="ghost" size="icon" onClick={() => setNotificationsOpen((v) => !v)} aria-label="Notifications"><Bell className="h-4 w-4" /><span className="absolute right-1.5 top-1.5 h-1.5 w-1.5 rounded-full bg-green-accent-500" /></Button>
                {notificationsOpen ? (
                  <div className="pars-enter absolute right-0 top-10 w-72 rounded-lg border border-slate-200 bg-white p-3 shadow-lg dark:border-slate-700 dark:bg-slate-900" role="status">
                    <p className="text-sm font-semibold">Notifications</p>
                    <p className="mt-2 text-xs text-slate-500">No new alerts. Scheduled synchronizations run at 06:00 Nepal time.</p>
                    <div className="mt-3 border-t pt-2 text-[11px] text-slate-400">Recent: {recent.slice(0, 3).join(" · ") || "None"}</div>
                  </div>
                ) : null}
              </div>
              <select
                aria-label="Theme"
                value={theme}
                onChange={(event) => setTheme(event.target.value as typeof theme)}
                className="hidden h-8 rounded-md border border-slate-200 bg-white px-1.5 text-xs sm:block dark:border-slate-700 dark:bg-slate-900"
              >
                <option value="light"><Sun className="inline h-3 w-3" /> Light</option>
                <option value="dark">Dark</option>
                <option value="system">System</option>
              </select>
              <select
                aria-label="Language"
                value={language}
                onChange={(event) => setLanguage(event.target.value as typeof language)}
                className="hidden h-8 rounded-md border border-slate-200 bg-white px-1.5 text-xs sm:block dark:border-slate-700 dark:bg-slate-900"
              >
                <option value="en">English</option>
                <option value="ne">नेपाली</option>
              </select>
              <div className="relative">
                <button type="button" onClick={() => setProfileOpen((v) => !v)} className="flex items-center gap-2 rounded-lg px-2 py-1.5 text-xs hover:bg-slate-100 dark:hover:bg-slate-800" aria-label="User menu">
                  <span className="flex h-7 w-7 items-center justify-center rounded-full bg-navy-700 text-xs font-semibold text-white">{(user?.fullName ?? user?.username ?? "U").charAt(0).toUpperCase()}</span>
                  <span className="hidden max-w-28 truncate font-medium sm:block">{user?.fullName || user?.username}</span>
                  <ChevronDown className="h-3.5 w-3.5 text-slate-400" />
                </button>
                {profileOpen ? (
                  <div className="pars-enter absolute right-0 top-10 w-56 rounded-lg border border-slate-200 bg-white p-1.5 shadow-lg dark:border-slate-700 dark:bg-slate-900">
                    <div className="px-2 py-2 text-[11px] text-slate-500">{user?.roles?.join(", ")} · {resolved} theme</div>
                    <button type="button" className="flex w-full items-center gap-2 rounded px-2 py-2 text-left text-sm hover:bg-slate-50 dark:hover:bg-slate-800" onClick={async () => { await logout(); navigate("/login"); }}><LogOut className="h-4 w-4" /> {t("logout")}</button>
                    {hasRole("ADMIN") ? <button type="button" className="flex w-full items-center gap-2 rounded px-2 py-2 text-left text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-950/30" onClick={async () => { await logoutAll(); navigate("/login"); }}><ShieldAlert className="h-4 w-4" /> Sign out everywhere</button> : null}
                  </div>
                ) : null}
              </div>
            </div>
          </div>
        </header>

        <main className="mx-auto w-full max-w-[1600px] px-3 py-5 sm:px-6 sm:py-6">
          <div className="mb-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <Breadcrumb items={[{ label: "Home" }, { label: current?.section ?? "Workspace" }, { label: current?.label ?? "PARS" }]} />
            <span className="flex items-center gap-1.5 text-[11px] text-slate-400"><Clock className="h-3.5 w-3.5" /> Nepal Standard Time · {new Date().toLocaleTimeString("en-GB", { timeZone: "Asia/Kathmandu", hour: "2-digit", minute: "2-digit" })}</span>
          </div>
          <Outlet />
        </main>
      </div>

      {/* Global search / command menu */}
      <Dialog open={searchOpen} onOpenChange={setSearchOpen}>
        <DialogContent className="top-[15%] max-w-xl">
          <DialogHeader>
            <DialogTitle>Quick navigation</DialogTitle>
            <DialogDescription>Search authorized pages. Press Escape to close.</DialogDescription>
          </DialogHeader>
          <Input autoFocus value={searchText} onChange={(event) => setSearchText(event.target.value)} placeholder="Type a page name…" aria-label="Search pages" />
          <div className="max-h-72 overflow-y-auto">
            {results.length === 0 ? <p className="py-6 text-center text-sm text-slate-500">No matching authorized pages.</p> : (
              <ul className="mt-2 space-y-1">
                {results.map((item) => (
                  <li key={item.to}>
                    <button type="button" className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-sm hover:bg-slate-100 dark:hover:bg-slate-800" onClick={() => { navigate(item.to); setSearchOpen(false); setSearchText(""); }}>
                      {item.icon}<span className="flex-1">{item.label}</span><Badge variant="outline" className="text-[10px]">{item.section}</Badge>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
          <p className="flex items-center gap-2 border-t pt-2 text-[11px] text-slate-400"><Globe className="h-3.5 w-3.5" /> Only pages you are authorized to see are listed.</p>
        </DialogContent>
      </Dialog>
    </div>
  );
}
