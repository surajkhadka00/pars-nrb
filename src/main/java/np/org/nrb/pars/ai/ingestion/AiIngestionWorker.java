package np.org.nrb.pars.ai.ingestion;

import np.org.nrb.pars.ai.config.ParsAiProperties;
import np.org.nrb.pars.ai.domain.AiDtos;
import np.org.nrb.pars.ai.repository.AiDocumentRepository;
import np.org.nrb.pars.ai.repository.AiJobRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Background ingestion worker. Never runs inside a chat request. */
@Component
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class AiIngestionWorker {

    private final AiJobRepository jobs;
    private final AiDocumentRepository documents;
    private final DocumentStorageService storage;
    private final DocumentExtractionService extraction;
    private final ChunkingService chunking;
    private final VectorStore vectorStore;
    private final ParsAiProperties properties;
    private final JdbcTemplate jdbc;

    public AiIngestionWorker(AiJobRepository jobs, AiDocumentRepository documents, DocumentStorageService storage,
                             DocumentExtractionService extraction, ChunkingService chunking, VectorStore vectorStore,
                             ParsAiProperties properties, JdbcTemplate jdbc) {
        this.jobs = jobs;
        this.documents = documents;
        this.storage = storage;
        this.extraction = extraction;
        this.chunking = chunking;
        this.vectorStore = vectorStore;
        this.properties = properties;
        this.jdbc = jdbc;
    }

    @Scheduled(fixedDelayString = "${pars.ai.worker-delay-ms:10000}")
    public void processOne() {
        String workerId = "worker-" + ProcessHandle.current().pid();
        var claimed = jobs.claimOne(workerId);
        if (claimed.isEmpty()) {
            return;
        }
        UUID jobId = claimed.get();
        AiDtos.AiJobResponse job = jobs.findById(jobId).orElseThrow();
        UUID documentId = job.documentId();
        try {
            var info = jdbc.queryForObject("""
                    SELECT original_filename, mime_type, access_classification, doc_version,
                           uploaded_by::text, storage_reference
                    FROM ai.ai_documents WHERE id = ?
                    """, (rs, row) -> new String[]{
                    rs.getString(1), rs.getString(2), rs.getString(3), String.valueOf(rs.getInt(4)),
                    rs.getString(5), rs.getString(6)}, documentId);
            if (info == null) {
                throw new IllegalStateException("Document metadata missing");
            }

            jobs.stage(jobId, "EXTRACTING", 20, "EXTRACTING");
            String extension = info[0].substring(info[0].lastIndexOf('.') + 1).toLowerCase();
            var extracted = extraction.extract(storage.read(info[5]), extension);
            if (!extracted.hasText()) {
                throw new IllegalStateException("Document contains no extractable text (scanned PDFs require OCR)");
            }

            jobs.stage(jobId, "CHUNKING", 50, "CHUNKING");
            var chunks = chunking.chunk(extracted.text());
            if (chunks.isEmpty()) {
                throw new IllegalStateException("Chunking produced no chunks");
            }

            jobs.stage(jobId, "EMBEDDING", 75, "EMBEDDING");
            String access = switch (info[2]) {
                case "PUBLIC", "INTERNAL" -> info[2];
                case "ROLE_BASED" -> "ROLE_ADMIN";
                case "RESTRICTED" -> "USER_" + info[4];
                default -> "INTERNAL";
            };
            List<Document> documentsToIndex = chunks.stream().map(chunk -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("docId", documentId.toString());
                metadata.put("title", documents.findById(documentId).map(AiDtos.AiDocumentResponse::title).orElse(""));
                metadata.put("version", info[3]);
                metadata.put("page", chunk.page() == null ? 0 : chunk.page());
                metadata.put("section", chunk.section() == null ? "" : chunk.section());
                metadata.put("classification", info[2]);
                metadata.put("access", access);
                metadata.put("deny", "");
                metadata.put("status", "PENDING_REVIEW");
                metadata.put("active", false);
                return new Document(chunk.text(), metadata);
            }).toList();

            // Replace any previous index for this document before adding the new version.
            vectorStore.delete("docId == '" + documentId + "'");
            vectorStore.add(documentsToIndex);

            documents.markIndexed(documentId, properties.getEmbeddingModel(),
                    properties.getEmbeddingModelVersion(), properties.getEmbeddingDimensions());
            jobs.complete(jobId);
        } catch (Exception e) {
            jobs.fail(jobId, e.getMessage());
            documents.updateStatus(documentId, "FAILED");
        }
    }
}
