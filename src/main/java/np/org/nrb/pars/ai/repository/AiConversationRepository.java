package np.org.nrb.pars.ai.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import np.org.nrb.pars.ai.domain.AiDtos;
import np.org.nrb.pars.ai.domain.AiDtos.AiConversationResponse;
import np.org.nrb.pars.ai.domain.AiDtos.AiMessageResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AiConversationRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public AiConversationRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    private static final RowMapper<AiConversationResponse> MAPPER = (rs, row) -> new AiConversationResponse(
            UUID.fromString(rs.getString("id")), rs.getString("title"),
            toInstant(rs, "created_at"), toInstant(rs, "updated_at"), rs.getInt("message_count"));

    private static Instant toInstant(ResultSet rs, String column) throws SQLException {
        var ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toInstant();
    }

    public UUID create(UUID ownerId, String title, java.time.Duration retention) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO ai.ai_conversations (id, owner_id, title, expires_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, now(), now())
                """, id, ownerId, title, java.sql.Timestamp.from(Instant.now().plus(retention)));
        return id;
    }

    public List<AiConversationResponse> listForOwner(UUID ownerId, int page, int size) {
        return jdbc.query("""
                SELECT c.*, (SELECT COUNT(*) FROM ai.ai_messages m WHERE m.conversation_id = c.id) AS message_count
                FROM ai.ai_conversations c
                WHERE c.owner_id = ? AND (c.expires_at IS NULL OR c.expires_at > now())
                ORDER BY c.updated_at DESC LIMIT ? OFFSET ?
                """, MAPPER, ownerId, size, page * size);
    }

    /** Ownership is checked in SQL — a guessed UUID belonging to another user is never returned. */
    public Optional<UUID> findOwnedId(UUID id, UUID ownerId) {
        return jdbc.query("""
                SELECT id FROM ai.ai_conversations
                WHERE id = ? AND owner_id = ? AND (expires_at IS NULL OR expires_at > now())
                """, (rs, row) -> UUID.fromString(rs.getString("id")), id, ownerId).stream().findFirst();
    }

    public void touch(UUID id) {
        jdbc.update("UPDATE ai.ai_conversations SET updated_at = now() WHERE id = ?", id);
    }

    public void rename(UUID id, String title) {
        jdbc.update("UPDATE ai.ai_conversations SET title = ?, updated_at = now() WHERE id = ?", title, id);
    }

    public void delete(UUID id) {
        jdbc.update("DELETE FROM ai.ai_conversations WHERE id = ?", id);
    }

    public void purgeExpired() {
        jdbc.update("DELETE FROM ai.ai_conversations WHERE expires_at IS NOT NULL AND expires_at < now()");
    }

    public void insertMessage(UUID messageId, UUID conversationId, String role, String content,
                              String sourcesJson, boolean insufficient, String traceId) {
        jdbc.update("""
                INSERT INTO ai.ai_messages (id, conversation_id, role, content, sources, insufficient_evidence, trace_id, created_at)
                VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, now())
                """, messageId, conversationId, role, content, sourcesJson, insufficient, traceId);
    }

    public List<AiMessageResponse> messages(UUID conversationId) {
        return jdbc.query("""
                SELECT id, role, content, sources::text AS sources_json, insufficient_evidence, created_at
                FROM ai.ai_messages WHERE conversation_id = ? ORDER BY created_at
                """, (rs, row) -> new AiMessageResponse(
                        UUID.fromString(rs.getString("id")), rs.getString("role"), rs.getString("content"),
                        parseSources(rs.getString("sources_json")), rs.getBoolean("insufficient_evidence"),
                        toInstant(rs, "created_at")), conversationId);
    }

    private List<AiDtos.AiSourceResponse> parseSources(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return List.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<List<AiDtos.AiSourceResponse>>() {});
        } catch (Exception e) {
            // Stored citations are audit data: never fail a conversation because of one bad row.
            return List.of();
        }
    }

    public Optional<UUID> findOwnedMessage(UUID messageId, UUID ownerId) {
        return jdbc.query("""
                SELECT m.id FROM ai.ai_messages m
                JOIN ai.ai_conversations c ON c.id = m.conversation_id
                WHERE m.id = ? AND c.owner_id = ?
                """, (rs, row) -> UUID.fromString(rs.getString("id")), messageId, ownerId).stream().findFirst();
    }

    public void saveFeedback(UUID id, UUID messageId, UUID userId, int rating, String comment) {
        jdbc.update("""
                INSERT INTO ai.ai_feedback (id, message_id, user_id, rating, comment, created_at)
                VALUES (?, ?, ?, ?, ?, now())
                """, id, messageId, userId, rating, comment);
    }
}
