package np.org.nrb.pars.ai.rag;

import io.micrometer.core.instrument.MeterRegistry;
import np.org.nrb.pars.ai.config.ParsAiProperties;
import np.org.nrb.pars.ai.domain.AiDtos;
import np.org.nrb.pars.ai.live.AiQueryRouter;
import np.org.nrb.pars.ai.live.LiveDataService;
import np.org.nrb.pars.ai.service.AiPermissionService;
import np.org.nrb.pars.ai.service.AiKillSwitchService;
import np.org.nrb.pars.nrb.domain.NrbForexViews.ForexDayView;
import np.org.nrb.pars.nrb.service.NrbForexService;
import np.org.nrb.pars.common.exception.Exceptions;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** Orchestrates routing, retrieval, grounding, abstention and auditable usage. */
@Service
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class RagAnswerService {

    public record Answer(String text, List<AiDtos.AiSourceResponse> sources, boolean insufficientEvidence) {}

    public static final String CAPABILITY_MESSAGE =
            "I answer questions using approved PARS NRB documents that have been published in the AI knowledge base. "
            + "For example, the currently published 'PARS FX Deal Guidelines' covers FX deal approval and settlement timing. "
            + "If your question is not covered, ask an administrator to upload and publish the relevant document under AI Documents, then ask again.";

    private final HybridRetrievalService retrieval;
    private final AiPromptService prompt;
    private final ParsAiProperties properties;
    private final AiPermissionService permissions;
    private final LiveDataService liveData;
    private final NrbForexService forexService;
    private final AiKillSwitchService killSwitch;
    private final JdbcTemplate jdbc;
    private final Semaphore concurrency;
    private final MeterRegistry meters;

    public RagAnswerService(HybridRetrievalService retrieval, AiPromptService prompt, ParsAiProperties properties,
                            AiPermissionService permissions, LiveDataService liveData, NrbForexService forexService,
                            AiKillSwitchService killSwitch, JdbcTemplate jdbc, MeterRegistry meters) {
        this.retrieval = retrieval;
        this.prompt = prompt;
        this.properties = properties;
        this.permissions = permissions;
        this.liveData = liveData;
        this.forexService = forexService;
        this.killSwitch = killSwitch;
        this.jdbc = jdbc;
        this.meters = meters;
        this.concurrency = new Semaphore(properties.getMaxConcurrentRequests());
    }

    public Answer answer(String question) {
        meters.counter("pars.ai.chat.requests").increment();
        if (killSwitch.isDisabled()) {
            throw Exceptions.forbidden("AI generation is temporarily disabled by an administrator");
        }

        AiQueryRouter.Route route = AiQueryRouter.route(question);
        if (route == AiQueryRouter.Route.CAPABILITY) {
            return new Answer(CAPABILITY_MESSAGE, List.of(), false);
        }

        checkDailyQuota();
        boolean acquired = false;
        try {
            acquired = concurrency.tryAcquire(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                meters.counter("pars.ai.ratelimit").increment();
                throw Exceptions.tooManyRequests("AI is busy. Please retry shortly.");
            }

            if (route == AiQueryRouter.Route.FOREX_LIVE) {
                return answerFromForex(question, forexService.latest());
            }
            if (route == AiQueryRouter.Route.FOREX_HISTORICAL) {
                return answerFromHistoricalForex(question);
            }
            if (route != AiQueryRouter.Route.DOCUMENTS && route != AiQueryRouter.Route.NRB_DOCUMENT) {
                return answerFromLiveData(question, route);
            }
            return answerFromDocuments(question);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Exceptions.tooManyRequests("AI request interrupted");
        } catch (RuntimeException e) {
            meters.counter("pars.ai.errors").increment();
            recordUsage(question, true);
            throw e;
        } finally {
            if (acquired) {
                concurrency.release();
            }
        }
    }

    private Answer answerFromForex(String question, ForexDayView day) {
        StringBuilder facts = new StringBuilder("Official NRB forex rates (indicative, not bank transaction rates):\n");
        facts.append("rate date: ").append(day.date()).append('\n');
        facts.append("published on: ").append(day.publishedOn()).append('\n');
        facts.append("last modified: ").append(day.modifiedOn()).append('\n');
        facts.append("source URL: ").append(day.sourceUrl()).append('\n');
        for (var rate : day.rates()) {
            facts.append(rate.iso3()).append(" (").append(rate.name()).append("), unit ")
                    .append(rate.unit()).append(": buy ").append(rate.buy())
                    .append(", sell ").append(rate.sell()).append('\n');
        }
        appendConversion(question, day, facts);
        String text = prompt.answerFromFacts(question, facts.toString());
        return new Answer(text == null || text.isBlank() ? abstention() : text,
                java.util.List.of(new AiDtos.AiSourceResponse(null, "NRB Forex Rates", day.date().getYear(), 0,
                        "", null, java.math.BigDecimal.ONE, "PUBLISHED", day.sourceUrl())), false);
    }


    /** Deterministic conversion from an official NRB rate — never computed by the model. */
    private void appendConversion(String question, ForexDayView day, StringBuilder facts) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?i)\\b(\\d+(?:\\.\\d+)?)\\s*(USD|EUR|GBP|AUD|CAD|SGD|JPY|CNY|INR)\\b")
                .matcher(question == null ? "" : question);
        if (!m.find()) {
            return;
        }
        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(m.group(1));
            String code = m.group(2).toUpperCase(java.util.Locale.ROOT);
            for (var rate : day.rates()) {
                if (code.equals(rate.iso3()) && rate.sell() != null) {
                    java.math.BigDecimal converted = amount.multiply(rate.sell())
                            .divide(java.math.BigDecimal.valueOf(rate.unit()), 2, java.math.RoundingMode.HALF_UP);
                    facts.append("Deterministic conversion from official rate (indicative): ")
                            .append(amount).append(' ').append(code).append(" = NPR ").append(converted)
                            .append(" using NRB selling rate ").append(rate.sell())
                            .append(" per ").append(rate.unit()).append(" unit, date ").append(day.date()).append('\n');
                    return;
                }
            }
        } catch (RuntimeException ignored) {
            // Never fabricate a conversion when the source data is malformed.
        }
    }

    private Answer answerFromHistoricalForex(String question) {
        java.util.regex.Matcher dateMatcher = java.util.regex.Pattern
                .compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b").matcher(question);
        if (!dateMatcher.find()) {
            return new Answer("Please include a date in YYYY-MM-DD format to query historical NRB rates.",
                    List.of(), false);
        }
        java.time.LocalDate date = java.time.LocalDate.parse(dateMatcher.group(1));
        java.util.regex.Matcher codeMatcher = java.util.regex.Pattern
                .compile("\\b([A-Z]{3})\\b").matcher(question);
        String code = codeMatcher.find() ? codeMatcher.group(1) : "USD";
        try {
            ForexDayView day = forexService.byCurrency(code, date);
            String facts = "Official NRB historical forex rate:\n"
                    + "date: " + day.date() + "\n"
                    + "currency: " + code + "\n"
                    + "published on: " + day.publishedOn() + "\n"
                    + "source URL: " + day.sourceUrl() + "\n"
                    + "rates: " + day.rates().stream()
                            .filter(r -> code.equalsIgnoreCase(r.iso3()))
                            .map(r -> "buy " + r.buy() + ", sell " + r.sell() + ", unit " + r.unit())
                            .findFirst().orElse("not available") + "\n";
            String text = prompt.answerFromFacts(question, facts);
            return new Answer(text == null || text.isBlank() ? abstention() : text,
                    List.of(new AiDtos.AiSourceResponse(null, "NRB Forex Rates " + date, day.date().getYear(), 0,
                            "", null, java.math.BigDecimal.ONE, "PUBLISHED", day.sourceUrl())), false);
        } catch (RuntimeException e) {
            return new Answer("No published NRB rate is available for " + code + " on " + date + ".",
                    List.of(), false);
        }
    }

    private Answer answerFromLiveData(String question, AiQueryRouter.Route route) {
        String facts = switch (route) {
            case SELF -> liveData.selfContext();
            case DEALS -> liveData.dealsContext();
            case APP_GUIDE -> liveData.appGuideContext();
            default -> "";
        };
        if (facts == null || facts.isBlank()) {
            return new Answer(abstention(), List.of(), true);
        }
        String text = prompt.answerFromFacts(question, facts);
        recordUsage(question, false);
        return new Answer(text == null || text.isBlank() ? abstention() : text,
                List.of(), false);
    }

    private Answer answerFromDocuments(String question) {
        List<HybridRetrievalService.Evidence> evidence = retrieval.retrieve(question);
        if (evidence.isEmpty()) {
            meters.counter("pars.ai.retrieval.empty").increment();
            return new Answer(abstention(), List.of(), true);
        }
        double bestSimilarity = evidence.stream()
                .mapToDouble(HybridRetrievalService.Evidence::similarity).max().orElse(0);
        if (bestSimilarity < properties.getEvidenceThreshold()) {
            return new Answer(abstention(), sources(evidence), true);
        }
        String text = prompt.answer(question, evidence);
        recordUsage(question, false);
        return new Answer(text == null || text.isBlank() ? abstention() : text,
                sources(evidence), false);
    }

    private void checkDailyQuota() {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM ai.ai_usage_events
                WHERE user_id = ? AND event_type = 'CHAT' AND created_at > now() - interval '1 day'
                """, Long.class, permissions.userId());
        if (count != null && count >= properties.getDailyQuestionLimit()) {
            meters.counter("pars.ai.ratelimit").increment();
            throw Exceptions.tooManyRequests("Daily AI question limit reached");
        }
    }

    private void recordUsage(String question, boolean failed) {
        try {
            jdbc.update("""
                    INSERT INTO ai.ai_usage_events (id, user_id, event_type, tokens_in, estimated_cost, trace_id, created_at)
                    VALUES (?, ?, ?, ?, 0, ?, now())
                    """, UUID.randomUUID(), permissions.userId(), failed ? "CHAT_FAILED" : "CHAT",
                    Math.min(question.length(), properties.getMaxInputCharacters()),
                    MDC.get("traceId"));
        } catch (RuntimeException ignored) {
            // Usage recording must never break the user's answer.
        }
    }


    private String abstention() {
        return properties.getAbstentionMessage()
                + "\n\nOfficial NRB source: https://www.nrb.org.np/"
                + " (Forex API documentation: https://www.nrb.org.np/api-docs-v1/)";
    }

    private List<AiDtos.AiSourceResponse> sources(List<HybridRetrievalService.Evidence> evidence) {
        return evidence.stream().map(e -> new AiDtos.AiSourceResponse(
                e.documentId(), e.title(), e.version(), e.page(), e.section(), e.chunkId(),
                BigDecimal.valueOf(e.score()).setScale(4, RoundingMode.HALF_UP), e.status(), e.sourceUrl())).toList();
    }
}
