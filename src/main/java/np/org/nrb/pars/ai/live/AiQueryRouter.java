package np.org.nrb.pars.ai.live;

import java.util.Locale;

/**
 * Deterministic, server-side query router. Rule-based on purpose: the model never
 * receives SQL, tool access, or unrestricted service calls. New intents can be added
 * here without changing the RAG pipeline.
 */
public final class AiQueryRouter {

    public enum Route { DOCUMENTS, SELF, DEALS, APP_GUIDE, CAPABILITY, FOREX_LIVE, FOREX_HISTORICAL, NRB_DOCUMENT }

    private static final String[] SELF = {
            "my username", "who am i", "my details", "my profile", "my info", "my account",
            "what is my name", "my email", "my role", "my permission", "my position", "my id"
    };
    private static final String[] DEALS = {
            "my deals", "show deals", "list deals", "recent deals", "how many deals",
            "deal list", "deal status", "show me deals", "all deals"
    };
    private static final String[] GUIDE = {
            "how to add", "how do i add", "how to create", "how do i create", "add a deal",
            "create a deal", "new deal", "how to use", "how can i add"
    };
    private static final String[] CAPABILITY = {
            "how can you help me", "what can you do", "who are you", "what do you do",
            "help me", "what can you help", "what is your purpose"
    };
    private static final String[] FOREX_LIVE = {
            "exchange rate", "exchange for", "rate for", "rate of", "how much is", "convert",
            "rate today", "today rate", "current rate", "latest rate", "usd rate", "dollar rate",
            "usd exchange", "npr rate", "inr rate", "what is the rate", "today's rate",
            "currency rate", "buying rate", "selling rate"
    };
    private static final String[] FOREX_HISTORICAL = {
            "exchange rate on", "rate on 20", "historical rate", "rate for 20", "old exchange rate"
    };
    private static final String[] NRB_DOCUMENT = {
            "nrb circular", "fx circular", "foreign exchange circular", "monetary policy",
            "nrb notice", "nrb report", "nrb guideline", "circular about", "nrb publication"
    };

    private AiQueryRouter() {}

    public static Route route(String question) {
        String q = (question == null ? "" : question.toLowerCase(Locale.ROOT))
                .trim().replaceAll("[?.!]+$", "").trim();
        if (contains(q, CAPABILITY)) {
            return Route.CAPABILITY;
        }
        if (contains(q, FOREX_HISTORICAL)) {
            return Route.FOREX_HISTORICAL;
        }
        if (contains(q, FOREX_LIVE)) {
            return Route.FOREX_LIVE;
        }
        if (contains(q, NRB_DOCUMENT)) {
            return Route.NRB_DOCUMENT;
        }
        if (contains(q, SELF)) {
            return Route.SELF;
        }
        if (contains(q, GUIDE)) {
            return Route.APP_GUIDE;
        }
        if (contains(q, DEALS)) {
            return Route.DEALS;
        }
        return Route.DOCUMENTS;
    }

    private static boolean contains(String question, String[] phrases) {
        for (String phrase : phrases) {
            if (question.contains(phrase)) {
                return true;
            }
        }
        return false;
    }
}
