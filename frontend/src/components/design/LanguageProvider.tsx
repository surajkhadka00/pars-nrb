import { createContext, useContext, useMemo, useState, type ReactNode } from "react";

export type Language = "en" | "ne";

const dictionary: Record<string, { en: string; ne: string }> = {
  dashboard: { en: "Dashboard", ne: "ड्यासबोर्ड" },
  deals: { en: "Deals", ne: "कारोबार" },
  counterparties: { en: "Counterparties", ne: "प्रतिपक्ष" },
  currencies: { en: "Currencies", ne: "मुद्राहरू" },
  users: { en: "Users", ne: "प्रयोगकर्ता" },
  settings: { en: "Settings", ne: "सेटिङहरू" },
  search: { en: "Search", ne: "खोज्नुहोस्" },
  logout: { en: "Sign out", ne: "बाहिर निस्कनुहोस्" },
  welcome: { en: "Welcome back", ne: "स्वागत छ" },
  overview: { en: "Overview", ne: "सारांश" },
  quickActions: { en: "Quick actions", ne: "द्रुत कार्यहरू" },
  noData: { en: "No records found", ne: "कुनै रेकर्ड फेला परेन" },
  loading: { en: "Loading…", ne: "लोड हुँदै…" },
  retry: { en: "Retry", ne: "पुन: प्रयास" },
  saved: { en: "Saved successfully", ne: "सफलतापूर्वक सुरक्षित" },
};

interface LanguageContextValue {
  language: Language;
  setLanguage: (language: Language) => void;
  t: (key: string) => string;
}

const LanguageContext = createContext<LanguageContextValue | null>(null);
const KEY = "pars.language";

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, setLanguageState] = useState<Language>(() => {
    const stored = localStorage.getItem(KEY);
    return stored === "ne" ? "ne" : "en";
  });

  const value = useMemo<LanguageContextValue>(
    () => ({
      language,
      setLanguage: (next) => {
        localStorage.setItem(KEY, next);
        setLanguageState(next);
        document.documentElement.lang = next;
      },
      t: (key) => dictionary[key]?.[language] ?? key,
    }),
    [language],
  );

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
}

export function useLanguage(): LanguageContextValue {
  const value = useContext(LanguageContext);
  if (!value) throw new Error("useLanguage must be used within LanguageProvider");
  return value;
}
