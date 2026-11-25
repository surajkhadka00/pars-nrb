package np.org.nrb.pars.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Provider-independent AI configuration. Every value can be overridden with an
 * environment variable, e.g. PARS_AI_ENABLED, PARS_AI_PROVIDER, PARS_AI_API_KEY.
 * No secret is ever stored in source or committed configuration.
 */
@Data
@Component
@ConfigurationProperties(prefix = "pars.ai")
public class ParsAiProperties {

    /** Master switch. Default OFF: an unconfigured installation behaves exactly as before. */
    private boolean enabled = false;

    /** Provider id. Only "openai" adapter exists in this release. */
    private String provider = "openai";

    private String chatModel = "gpt-4o-mini";
    private String embeddingModel = "text-embedding-3-small";
    private String apiKey = "";
    private String baseUrl = "";

    private int maxTokens = 1500;
    private double temperature = 0.2;
    private Duration timeout = Duration.ofSeconds(30);
    private int maxConcurrentRequests = 4;

    /** MUST match the vector(1536) column created by Flyway V14. */
    private int embeddingDimensions = 1536;
    private String embeddingModelVersion = "1";

    private int maxInputCharacters = 8000;
    private int dailyQuestionLimit = 100;
    private int requestsPerMinute = 20;

    private int chunkSizeTokens = 600;
    private int chunkOverlapTokens = 100;
    private int retrievalTopK = 10;
    private int finalPassages = 5;
    private double similarityThreshold = 0.35;
    private double evidenceThreshold = 0.45;
    private boolean rerankerEnabled = false;

    private String storagePath = "/var/data/nrb-pars-ai";
    private long maxFileSizeBytes = 20L * 1024 * 1024;
    private Duration conversationRetention = Duration.ofDays(30);

    private String abstentionMessage =
            "I could not find sufficient supporting information in the approved PARS NRB knowledge base to answer this question reliably.";
}
