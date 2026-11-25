/** Original, Nepal-inspired institutional building mark. Not the official NRB logo. */
export function NrbBuildingIcon({ className = "h-8 w-8", title = "PARS Nepal Rastra Bank" }: {
  className?: string;
  title?: string;
}) {
  return (
    <svg viewBox="0 0 48 48" role="img" aria-label={title} className={className} fill="none">
      <title>{title}</title>
      <rect x="4" y="17" width="40" height="5" rx="1" fill="currentColor" opacity="0.95" />
      <path d="M24 5 6 15h36L24 5Z" fill="currentColor" opacity="0.9" />
      <rect x="7" y="24" width="4" height="15" fill="currentColor" opacity="0.85" />
      <rect x="15" y="24" width="4" height="15" fill="currentColor" opacity="0.85" />
      <rect x="23" y="24" width="4" height="15" fill="currentColor" opacity="0.85" />
      <rect x="31" y="24" width="4" height="15" fill="currentColor" opacity="0.85" />
      <rect x="39" y="24" width="4" height="15" fill="currentColor" opacity="0.85" />
      <rect x="5" y="39" width="38" height="4" rx="1" fill="currentColor" />
      <path d="M10 22v2M38 22v2" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}
