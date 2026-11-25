package np.org.nrb.pars.ai.repository;

import np.org.nrb.pars.ai.domain.AiDtos.AiJobResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AiJobRepository {

    private final JdbcTemplate jdbc;

    public AiJobRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<AiJobResponse> MAPPER = (rs, row) -> new AiJobResponse(
            UUID.fromString(rs.getString("id")), UUID.fromString(rs.getString("document_id")),
            rs.getString("status"), rs.getString("stage"), rs.getInt("progress"), rs.getInt("attempts"),
            rs.getString("error_message"),
            rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("finished_at") == null ? null : rs.getTimestamp("finished_at").toInstant());

    public void enqueue(UUID jobId, UUID documentId) {
        jdbc.update("""
                INSERT INTO ai.ai_ingestion_jobs (id, document_id, status, stage, progress, attempts)
                VALUES (?, ?, 'QUEUED', 'UPLOADED', 0, 0)
                """, jobId, documentId);
    }

    /** Atomic multi-worker claim with SKIP LOCKED. */
    public Optional<UUID> claimOne(String workerId) {
        List<UUID> ids = jdbc.query("""
                SELECT id FROM ai.ai_ingestion_jobs
                WHERE status IN ('QUEUED','FAILED') AND attempts < max_attempts
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED
                LIMIT 1
                """, (rs, row) -> UUID.fromString(rs.getString("id")));
        if (ids.isEmpty()) {
            return Optional.empty();
        }
        UUID id = ids.get(0);
        jdbc.update("""
                UPDATE ai.ai_ingestion_jobs
                SET status = 'EXTRACTING', claimed_by = ?, claimed_at = now(),
                    attempts = attempts + 1, error_message = NULL, row_version = row_version + 1
                WHERE id = ?
                """, workerId, id);
        return Optional.of(id);
    }

    public void stage(UUID id, String stage, int progress, String status) {
        jdbc.update("""
                UPDATE ai.ai_ingestion_jobs SET stage = ?, progress = ?, status = ?, row_version = row_version + 1
                WHERE id = ?
                """, stage, progress, status, id);
    }

    public void complete(UUID id) {
        jdbc.update("""
                UPDATE ai.ai_ingestion_jobs
                SET status = 'PENDING_REVIEW', stage = 'INDEXING', progress = 100,
                    finished_at = now(), row_version = row_version + 1
                WHERE id = ?
                """, id);
    }

    public void fail(UUID id, String reason) {
        String message = reason == null ? "unknown error" : reason.substring(0, Math.min(1000, reason.length()));
        jdbc.update("""
                UPDATE ai.ai_ingestion_jobs
                SET status = 'FAILED', error_message = ?, finished_at = now(), row_version = row_version + 1
                WHERE id = ?
                """, message, id);
    }

    public List<AiJobResponse> list(int page, int size) {
        return jdbc.query("SELECT * FROM ai.ai_ingestion_jobs ORDER BY created_at DESC LIMIT ? OFFSET ?",
                MAPPER, size, page * size);
    }

    public long count() {
        Long value = jdbc.queryForObject("SELECT COUNT(*) FROM ai.ai_ingestion_jobs", Long.class);
        return value == null ? 0 : value;
    }

    public Optional<AiJobResponse> findById(UUID id) {
        return jdbc.query("SELECT * FROM ai.ai_ingestion_jobs WHERE id = ?", MAPPER, id).stream().findFirst();
    }
}
