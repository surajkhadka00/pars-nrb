package np.org.nrb.pars.ai.repository;

import np.org.nrb.pars.ai.domain.AiDtos.AiDocumentResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JDBC repository for schema ai.ai_documents (no second persistence implementation for vectors). */
@Repository
public class AiDocumentRepository {

    private final JdbcTemplate jdbc;

    public AiDocumentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<AiDocumentResponse> MAPPER = (rs, row) -> new AiDocumentResponse(
            UUID.fromString(rs.getString("id")), rs.getString("title"), rs.getString("original_filename"),
            rs.getString("mime_type"), rs.getString("checksum"), rs.getLong("file_size"),
            rs.getString("category"), rs.getString("access_classification"), rs.getString("publication_status"),
            rs.getInt("doc_version"), rs.getBoolean("active_version"),
            toInstant(rs, "uploaded_at"), toInstant(rs, "published_at"),
            rs.getString("embedding_model"), toInstant(rs, "last_indexed_at"));

    private static Instant toInstant(ResultSet rs, String column) throws SQLException {
        var ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toInstant();
    }

    public void insert(UUID id, String title, String description, String originalFilename, String mimeType,
                       String checksum, long fileSize, String category, String classification, int version,
                       String storageReference, UUID uploadedBy) {
        jdbc.update("""
                INSERT INTO ai.ai_documents
                 (id, title, description, original_filename, mime_type, checksum, file_size, category,
                  access_classification, publication_status, doc_version, uploaded_by, storage_reference,
                  active_version, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT', ?, ?, ?, FALSE, now(), now())
                """, id, title, description, originalFilename, mimeType, checksum, fileSize, category,
                classification, version, uploadedBy, storageReference);
    }

    public Optional<AiDocumentResponse> findById(UUID id) {
        return jdbc.query("SELECT * FROM ai.ai_documents WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public List<AiDocumentResponse> list(int page, int size) {
        return jdbc.query("SELECT * FROM ai.ai_documents ORDER BY uploaded_at DESC LIMIT ? OFFSET ?",
                MAPPER, size, page * size);
    }

    public long count() {
        Long value = jdbc.queryForObject("SELECT COUNT(*) FROM ai.ai_documents", Long.class);
        return value == null ? 0 : value;
    }

    public void updateStatus(UUID id, String status) {
        jdbc.update("UPDATE ai.ai_documents SET publication_status = ?, updated_at = now() WHERE id = ?", status, id);
    }

    public void markIndexed(UUID id, String model, String modelVersion, int dimensions) {
        jdbc.update("""
                UPDATE ai.ai_documents
                SET last_indexed_at = now(), embedding_model = ?, embedding_model_version = ?,
                    embedding_dimensions = ?, publication_status = 'PENDING_REVIEW', updated_at = now()
                WHERE id = ?
                """, model, modelVersion, dimensions, id);
    }

    public void publish(UUID id, UUID approvedBy) {
        jdbc.update("""
                UPDATE ai.ai_documents
                SET publication_status = 'PUBLISHED', active_version = TRUE, published_at = now(),
                    approved_by = ?, updated_at = now()
                WHERE id = ?
                """, approvedBy, id);
    }

    public void archive(UUID id) {
        jdbc.update("""
                UPDATE ai.ai_documents
                SET publication_status = 'ARCHIVED', active_version = FALSE, updated_at = now()
                WHERE id = ?
                """, id);
    }

    public void deactivateOtherVersions(UUID id) {
        jdbc.update("""
                UPDATE ai.ai_documents SET active_version = FALSE
                WHERE id <> ? AND title = (SELECT title FROM ai.ai_documents WHERE id = ?)
                """, id, id);
    }

    public void delete(UUID id) {
        jdbc.update("DELETE FROM ai.ai_documents WHERE id = ?", id);
    }

    public String storageReference(UUID id) {
        return jdbc.queryForObject("SELECT storage_reference FROM ai.ai_documents WHERE id = ?", String.class, id);
    }

    public boolean existsByChecksum(String checksum) {
        Long n = jdbc.queryForObject("SELECT COUNT(*) FROM ai.ai_documents WHERE checksum = ?", Long.class, checksum);
        return n != null && n > 0;
    }

    public void setVersionAndStatus(UUID id, int version, String status) {
        jdbc.update("UPDATE ai.ai_documents SET doc_version = ?, publication_status = ?, updated_at = now() WHERE id = ?",
                version, status, id);
    }
}
