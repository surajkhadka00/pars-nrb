package np.org.nrb.pars.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/** Provider-independent beans shared by both OpenAI and Ollama adapters. */
@Configuration
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class AiCommonConfiguration {

    @Bean
    public VectorStore aiVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel,
                                     ParsAiProperties properties) {
        // Fail fast instead of silently indexing with a mismatched vector column.
        validateVectorDimension(jdbcTemplate, properties.getEmbeddingDimensions());
        // Flyway V14/V16 own the schema; no DDL is executed at startup.
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(properties.getEmbeddingDimensions())
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.NONE)
                .batchingStrategy(new TokenCountBatchingStrategy(EncodingType.CL100K_BASE, 512, 0.9))
                .initializeSchema(false)
                .schemaName("ai")
                .vectorTableName("vector_store")
                .build();
    }

    private void validateVectorDimension(JdbcTemplate jdbc, int configured) {
        Integer typmod = jdbc.queryForObject("""
                SELECT a.atttypmod FROM pg_attribute a
                WHERE a.attrelid = 'ai.vector_store'::regclass
                  AND a.attname = 'embedding' AND a.attnum > 0
                """, Integer.class);
        if (typmod == null) {
            throw new IllegalStateException("ai.vector_store.embedding column is missing");
        }
        int actual = typmod; // pgvector stores the declared dimension directly in atttypmod
        if (actual != configured) {
            throw new IllegalStateException(
                    "Embedding dimension mismatch: ai.vector_store is vector(" + actual
                            + ") but pars.ai.embedding-dimensions=" + configured
                            + ". Set the correct value and reindex before starting AI.");
        }
    }

    @Bean
    public ChatClient aiChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
