package np.org.nrb.pars.rbac.model;

public final class PermissionCodes {
    private PermissionCodes() {
    }

    public static final String USER_READ = "USER_READ";
    public static final String USER_WRITE = "USER_WRITE";
    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_WRITE = "ROLE_WRITE";
    public static final String PERMISSION_READ = "PERMISSION_READ";
    public static final String SESSION_REVOKE = "SESSION_REVOKE";
    public static final String FAILED_ATTEMPT_READ = "FAILED_ATTEMPT_READ";
    public static final String API_ACCESS_WRITE = "API_ACCESS_WRITE";
    public static final String DEAL_READ = "DEAL_READ";
    public static final String DEAL_WRITE = "DEAL_WRITE";
    public static final String DEALTYPE_READ = "DEALTYPE_READ";
    public static final String DEALTYPE_WRITE = "DEALTYPE_WRITE";
    public static final String INSTRUMENT_READ = "INSTRUMENT_READ";
    public static final String INSTRUMENT_WRITE = "INSTRUMENT_WRITE";
    public static final String DEALTYPELIMIT_READ = "DEALTYPELIMIT_READ";
    public static final String DEALTYPELIMIT_WRITE = "DEALTYPELIMIT_WRITE";
    public static final String NOSTRO_READ = "NOSTRO_READ";
    public static final String NOSTRO_WRITE = "NOSTRO_WRITE";
    public static final String CURRENCY_READ = "CURRENCY_READ";
    public static final String CURRENCY_WRITE = "CURRENCY_WRITE";
    public static final String COUNTERPARTY_READ = "COUNTERPARTY_READ";
    public static final String COUNTERPARTY_WRITE = "COUNTERPARTY_WRITE";
    public static final String COUNTERPARTYLIMIT_READ = "COUNTERPARTYLIMIT_READ";
    public static final String COUNTERPARTYLIMIT_WRITE = "COUNTERPARTYLIMIT_WRITE";
    public static final String FISCALYEAR_READ = "FISCALYEAR_READ";
    public static final String FISCALYEAR_WRITE = "FISCALYEAR_WRITE";
    public static final String COUNTRY_READ = "COUNTRY_READ";
    public static final String COUNTRY_WRITE = "COUNTRY_WRITE";
    public static final String POSITION_READ = "POSITION_READ";
    public static final String POSITION_WRITE = "POSITION_WRITE";

    // AI / RAG module (V14). Granted to ADMIN only by default.
    public static final String AI_CHAT = "AI_CHAT";
    public static final String AI_CONVERSATION_READ = "AI_CONVERSATION_READ";
    public static final String AI_DOCUMENT_READ = "AI_DOCUMENT_READ";
    public static final String AI_DOCUMENT_UPLOAD = "AI_DOCUMENT_UPLOAD";
    public static final String AI_DOCUMENT_APPROVE = "AI_DOCUMENT_APPROVE";
    public static final String AI_DOCUMENT_DELETE = "AI_DOCUMENT_DELETE";
    public static final String AI_DOCUMENT_REINDEX = "AI_DOCUMENT_REINDEX";
    public static final String AI_FEEDBACK = "AI_FEEDBACK";
    public static final String AI_ADMIN = "AI_ADMIN";
    public static final String AI_METRICS_READ = "AI_METRICS_READ";

    // NRB external-source integration (V17). ADMIN only by default.
    public static final String AI_FOREX_READ = "AI_FOREX_READ";
    public static final String AI_SOURCE_READ = "AI_SOURCE_READ";
    public static final String AI_SOURCE_MANAGE = "AI_SOURCE_MANAGE";
    public static final String AI_DOCUMENT_SYNC = "AI_DOCUMENT_SYNC";
}
