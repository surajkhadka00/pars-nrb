package np.org.nrb.pars.ai.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import np.org.nrb.pars.ai.config.ParsAiProperties;
import np.org.nrb.pars.ai.service.AiPermissionService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Hybrid retrieval: pgvector similarity + PostgreSQL full-text, fused with RRF. */
@Service
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class HybridRetrievalService {

    public record Evidence(String text, UUID documentId, String title, int version, int page,
                           String section, UUID chunkId, double score, String status,
                           double similarity, String sourceUrl) {}

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final ParsAiProperties properties;
    private final AiPermissionService permissions;

    public HybridRetrievalService(VectorStore vectorStore, JdbcTemplate jdbc, ObjectMapper mapper,
                                  ParsAiProperties properties, AiPermissionService permissions) {
        this.vectorStore = vectorStore;
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.properties = properties;
        this.permissions = permissions;
    }

    public List<Evidence> retrieve(String question) {
        var filter = buildFilter();
        var search = SearchRequest.builder()
                .query(question)
                .topK(properties.getRetrievalTopK())
                .similarityThreshold(properties.getSimilarityThreshold())
                .filterExpression(filter)
                .build();

        List<Document> vectorResults = vectorStore.similaritySearch(search);
        List<Evidence> vectorEvidence = new ArrayList<>();
        int rank = 0;
        for (Document document : vectorResults) {
            rank++;
            Evidence evidence = toEvidence(document, 1.0 / (60 + rank));
            if (evidence != null) {
                vectorEvidence.add(evidence);
            }
        }

        List<Evidence> keywordEvidence = keywordSearch(question);
        return fuse(vectorEvidence, keywordEvidence, properties.getFinalPassages());
    }

    /** Filter is derived exclusively from the authenticated JWT context. */
    private Filter.Expression buildFilter() {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<Object> tokens = new ArrayList<>(permissions.accessTokens());
        var access = b.in("access", tokens);
        var published = b.eq("status", "PUBLISHED");
        var active = b.eq("active", true);
        var notDenied = b.ne("deny", permissions.denyToken());
        return b.and(b.and(access, published), b.and(active, notDenied)).build();
    }

    private List<Evidence> keywordSearch(String question) {
        List<String> tokens = permissions.accessTokens();
        String placeholders = String.join(",", tokens.stream().map(t -> "?").toList());
        List<Object> args = new ArrayList<>(tokens);
        args.add(permissions.denyToken());
        args.add(properties.getRetrievalTopK());
        String sql = """
                SELECT content, metadata::text AS metadata_json,
                       ts_rank(to_tsvector('simple', content), websearch_to_tsquery('simple', ?)) AS rank
                FROM ai.vector_store
                WHERE to_tsvector('simple', content) @@ websearch_to_tsquery('simple', ?)
                  AND metadata->>'status' = 'PUBLISHED'
                  AND metadata->>'active' = 'true'
                  AND metadata->>'access' IN (%s)
                  AND metadata->>'deny' <> ?
                ORDER BY rank DESC
                LIMIT ?
                """.formatted(placeholders);
        List<Object> queryArgs = new ArrayList<>();
        queryArgs.add(question);
        queryArgs.add(question);
        queryArgs.addAll(args);
        return jdbc.query(sql, (rs, row) -> {
            try {
                Map<String, Object> metadata = mapper.readValue(rs.getString("metadata_json"), new TypeReference<>() {});
                String id = String.valueOf(metadata.get("docId"));
                if (id == null || id.equals("null")) {
                    return null;
                }
                return new Evidence(rs.getString("content"), UUID.fromString(id),
                        String.valueOf(metadata.getOrDefault("title", "")),
                        Integer.parseInt(String.valueOf(metadata.getOrDefault("version", 1))),
                        Integer.parseInt(String.valueOf(metadata.getOrDefault("page", 0))),
                        String.valueOf(metadata.getOrDefault("section", "")),
                            UUID.nameUUIDFromBytes((id + rs.getString("content")).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                        rs.getDouble("rank"), "PUBLISHED", rs.getDouble("rank"),
                        String.valueOf(metadata.getOrDefault("sourceUrl", "")));
            } catch (Exception e) {
                throw new IllegalStateException("Unable to parse vector metadata", e);
            }
        }, queryArgs.toArray());
    }

    private Evidence toEvidence(Document document, double fusedScore) {
        Map<String, Object> metadata = document.getMetadata();
        Object docId = metadata.get("docId");
        if (docId == null) {
            return null;
        }
        return new Evidence(document.getText(), UUID.fromString(docId.toString()),
                String.valueOf(metadata.getOrDefault("title", "")),
                Integer.parseInt(String.valueOf(metadata.getOrDefault("version", 1))),
                Integer.parseInt(String.valueOf(metadata.getOrDefault("page", 0))),
                String.valueOf(metadata.getOrDefault("section", "")),
                UUID.fromString(String.valueOf(metadata.getOrDefault("id", UUID.randomUUID()))),
                fusedScore, "PUBLISHED", document.getScore() == null ? 0 : document.getScore(),
                String.valueOf(metadata.getOrDefault("sourceUrl", "")));
    }

    /** Reciprocal Rank Fusion merges two independent rankings without score calibration. */
    List<Evidence> fuse(List<Evidence> vector, List<Evidence> keyword, int limit) {
        Map<UUID, Evidence> merged = new LinkedHashMap<>();
        Map<UUID, Double> scores = new LinkedHashMap<>();
        rankInto(vector, merged, scores);
        rankInto(keyword, merged, scores);
        List<Evidence> all = new ArrayList<>(vector);
        all.addAll(keyword);
        return scores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(e -> {
                    Evidence base = merged.get(e.getKey());
                    double bestSimilarity = all.stream()
                            .filter(x -> x.chunkId().equals(base.chunkId()))
                            .mapToDouble(Evidence::similarity).max().orElse(0);
                    return new Evidence(base.text(), base.documentId(), base.title(), base.version(), base.page(),
                            base.section(), base.chunkId(), e.getValue(), base.status(), bestSimilarity,
                            base.sourceUrl());
                })
                .toList();
    }

    private void rankInto(List<Evidence> list, Map<UUID, Evidence> merged, Map<UUID, Double> scores) {
        for (int i = 0; i < list.size(); i++) {
            Evidence e = list.get(i);
            UUID key = e.chunkId();
            merged.putIfAbsent(key, e);
            scores.merge(key, 1.0 / (60 + i + 1), Double::sum);
        }
    }
}
