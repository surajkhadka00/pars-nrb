package np.org.nrb.pars.ai.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FREE local provider adapter (Ollama). Active when PARS_AI_PROVIDER=ollama.
 * No API key, no external data transfer — suitable for institutional data.
 */
@Configuration
@ConditionalOnExpression("${pars.ai.enabled:false} and '${pars.ai.provider:openai}' == 'ollama'")
public class OllamaModelConfiguration {

    @Bean
    public OllamaApi aiOllamaApi(ParsAiProperties properties) {
        String baseUrl = (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank())
                ? "http://localhost:11434" : properties.getBaseUrl();
        return OllamaApi.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public ChatModel aiChatModel(OllamaApi api, ParsAiProperties properties) {
        OllamaOptions options = OllamaOptions.builder()
                .model(properties.getChatModel())
                .temperature(properties.getTemperature())
                .numPredict(properties.getMaxTokens())
                .build();
        return OllamaChatModel.builder().ollamaApi(api).defaultOptions(options).build();
    }

    @Bean
    public EmbeddingModel aiEmbeddingModel(OllamaApi api, ParsAiProperties properties) {
        OllamaOptions options = OllamaOptions.builder()
                .model(properties.getEmbeddingModel())
                .build();
        return OllamaEmbeddingModel.builder().ollamaApi(api).defaultOptions(options).build();
    }
}
