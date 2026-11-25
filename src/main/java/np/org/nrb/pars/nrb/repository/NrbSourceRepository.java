package np.org.nrb.pars.nrb.repository;

import np.org.nrb.pars.nrb.crawler.NrbUrlPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JDBC repository for the NRB source registry and discovered documents. */
@Repository
public class NrbSourceRepository {

    public record SourceRow(UUID id, String name, String url, String type, String category,
                            boolean enabled, String lastSync, String status, String error) {}

    public record DocumentRow(UUID id, String canonicalUrl, String title, String category,
                              String language, LocalDate publicationDate, String contentHash,
                              String status, boolean published, int version, String storageReference) {}

    private final JdbcTemplate jdbc;

    public NrbSourceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SourceRow> listSources() {
        return jdbc.query("""
                SELECT id, source_name, official_url, source_type, category, enabled,
                       last_successful_sync::text, ingestion_status, error_details
                FROM ai.nrb_source_registry ORDER BY source_name
                """, (rs, row) -> new SourceRow(
                UUID.fromString(rs.getString("id")), rs.getString("source_name"),
                rs.getString("official_url"), rs.getString("source_type"), rs.getString("category"),
                rs.getBoolean("enabled"), rs.getString("last_successful_sync"),
                rs.getString("ingestion_status"), rs.getString("error_details")));
    }

    public Optional<SourceRow> findSource(UUID id) {
        return listSources().stream().filter(s -> s.id().equals(id)).findFirst();
    }

    public void updateSyncResult(UUID id, boolean success, String status, String error) {
        jdbc.update("""
                UPDATE ai.nrb_source_registry
                SET last_attempted_sync = now(),
                    last_successful_sync = CASE WHEN ? THEN now() ELSE last_successful_sync END,
                    ingestion_status = ?, error_details = ?, updated_at = now()
                WHERE id = ?
                """, success, status, error, id);
    }

    public void setEnabled(UUID id, boolean enabled) {
        jdbc.update("UPDATE ai.nrb_source_registry SET enabled = ?, updated_at = now() WHERE id = ?", enabled, id);
    }

    public void insertJob(UUID jobId, UUID sourceId, String type) {
        jdbc.update("""
                INSERT INTO ai.nrb_source_sync_jobs (id, source_id, job_type, status, started_at)
                VALUES (?, ?, ?, 'RUNNING', now())
                """, jobId, sourceId, type);
    }

    public void finishJob(UUID jobId, String status, int discovered, int downloaded, int failed, String error) {
        jdbc.update("""
                UPDATE ai.nrb_source_sync_jobs
                SET status = ?, discovered = ?, downloaded = ?, failed = ?, error_message = ?, finished_at = now()
                WHERE id = ?
                """, status, discovered, downloaded, failed, error, jobId);
    }

    public List<DocumentRow> listDocuments(int page, int size) {
        return jdbc.query("""
                SELECT id, canonical_url, title, category, language, publication_date,
                       content_hash, ingestion_status, published, version, storage_reference
                FROM ai.nrb_documents ORDER BY updated_at DESC LIMIT ? OFFSET ?
                """, NrbSourceRepository::mapDocument, size, page * size);
    }

    public Optional<DocumentRow> findByHash(String hash) {
        return jdbc.query("""
                SELECT id, canonical_url, title, category, language, publication_date,
                       content_hash, ingestion_status, published, version, storage_reference
                FROM ai.nrb_documents WHERE content_hash = ?
                """, NrbSourceRepository::mapDocument, hash).stream().findFirst();
    }

    /** Returns the document id when inserted, empty when the hash already exists (idempotent). */
    public Optional<UUID> insertDocument(UUID id, UUID sourceId, String url, String title, String category,
                                         String language, LocalDate publicationDate, String hash,
                                         String fileName, String mimeType, long fileSize,
                                         String storageReference) {
        List<UUID> inserted = jdbc.query("""
                INSERT INTO ai.nrb_documents
                 (id, source_id, canonical_url, title, category, language, publication_date,
                  content_hash, file_name, mime_type, file_size, storage_reference,
                  ingestion_status, published, version, fetched_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'INDEXED', TRUE, 1, now())
                ON CONFLICT (content_hash) DO NOTHING
                RETURNING id
                """, (rs, row) -> UUID.fromString(rs.getString(1)),
                id, sourceId, url, title, category, language, publicationDate,
                hash, fileName, mimeType, fileSize, storageReference);
        return inserted.stream().findFirst();
    }

    public void insertError(UUID sourceId, UUID documentId, String url, String stage, String message) {
        jdbc.update("""
                INSERT INTO ai.nrb_ingestion_errors (id, source_id, document_id, url, stage, error_message)
                VALUES (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), sourceId, documentId, url, stage,
                message == null ? null : message.substring(0, Math.min(1000, message.length())));
    }

    private static DocumentRow mapDocument(ResultSet rs, int row) throws SQLException {
        var date = rs.getDate("publication_date");
        return new DocumentRow(UUID.fromString(rs.getString("id")), rs.getString("canonical_url"),
                rs.getString("title"), rs.getString("category"), rs.getString("language"),
                date == null ? null : date.toLocalDate(), rs.getString("content_hash"),
                rs.getString("ingestion_status"), rs.getBoolean("published"), rs.getInt("version"),
                rs.getString("storage_reference"));
    }
}
