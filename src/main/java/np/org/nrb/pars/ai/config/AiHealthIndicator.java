package np.org.nrb.pars.ai.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Exposed as /actuator/health/ai even when AI is disabled. */
@Component("ai")
public class AiHealthIndicator implements HealthIndicator {

    private final ParsAiProperties properties;

    public AiHealthIndicator(ParsAiProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        if (!properties.isEnabled()) {
            return Health.up()
                    .withDetail("status", "DISABLED")
                    .withDetail("message", "AI module is disabled (pars.ai.enabled=false)")
                    .build();
        }
        // A key is only required by hosted providers. Ollama runs locally with no key.
        if ("openai".equals(properties.getProvider())
                && (properties.getApiKey() == null || properties.getApiKey().isBlank())) {
            return Health.down()
                    .withDetail("status", "MISCONFIGURED")
                    .withDetail("message", "PARS_AI_API_KEY is required when provider=openai")
                    .build();
        }
        return Health.up()
                .withDetail("status", "CONFIGURED")
                .withDetail("provider", properties.getProvider())
                .withDetail("chatModel", properties.getChatModel())
                .withDetail("embeddingModel", properties.getEmbeddingModel())
                .withDetail("embeddingDimensions", properties.getEmbeddingDimensions())
                .build();
    }
}
