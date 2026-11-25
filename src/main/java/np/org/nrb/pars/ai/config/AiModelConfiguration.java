package np.org.nrb.pars.ai.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Paid/hosted provider adapter. Active when provider=openai (default). */
@Configuration
@ConditionalOnExpression("${pars.ai.enabled:false} and '${pars.ai.provider:openai}' == 'openai'")
public class AiModelConfiguration {

    @Bean
    public OpenAiApi aiOpenAiApi(ParsAiProperties properties) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "PARS_AI_API_KEY is required when provider=openai. Use provider=ollama for free local models.");
        }
        OpenAiApi.Builder builder = OpenAiApi.builder().apiKey(properties.getApiKey());
        if (properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank()) {
            builder.baseUrl(properties.getBaseUrl());
        }
        return builder.build();
    }

    @Bean
    public ChatModel aiChatModel(OpenAiApi api, ParsAiProperties properties) {
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getChatModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .build())
                .build();
    }

    @Bean
    public EmbeddingModel aiEmbeddingModel(OpenAiApi api, ParsAiProperties properties) {
        return new OpenAiEmbeddingModel(api, MetadataMode.EMBED, OpenAiEmbeddingOptions.builder()
                .model(properties.getEmbeddingModel())
                .dimensions(properties.getEmbeddingDimensions())
                .build());
    }
}
