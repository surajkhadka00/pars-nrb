package np.org.nrb.pars.ai.service;

import np.org.nrb.pars.ai.domain.AiDtos;
import np.org.nrb.pars.ai.ingestion.DocumentStorageService;
import np.org.nrb.pars.ai.ingestion.FileValidationService;
import np.org.nrb.pars.ai.repository.AiDocumentRepository;
import np.org.nrb.pars.ai.repository.AiJobRepository;
import np.org.nrb.pars.common.exception.Exceptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class AiDocumentService {

    private final AiDocumentRepository documents;
    private final AiJobRepository jobs;
    private final DocumentStorageService storage;
    private final FileValidationService validation;
    private final JdbcTemplate jdbc;

    public AiDocumentService(AiDocumentRepository documents, AiJobRepository jobs, DocumentStorageService storage,
                             FileValidationService validation, JdbcTemplate jdbc) {
        this.documents = documents;
        this.jobs = jobs;
        this.storage = storage;
        this.validation = validation;
        this.jdbc = jdbc;
    }

    public AiDtos.AiDocumentResponse upload(MultipartFile file, AiDtos.AiDocumentRequest request, UUID userId) {
        String extension = validation.validate(file);
        String checksum = checksum(file);
        if (documents.existsByChecksum(checksum)) {
            throw Exceptions.conflict("A document with this checksum already exists");
        }
        String storageName = storage.store(file, extension);
        UUID id = UUID.randomUUID();
        try {
            documents.insert(id, request.title(), request.description(), file.getOriginalFilename(),
                    file.getContentType() == null ? "application/octet-stream" : file.getContentType(), checksum,
                    file.getSize(), request.category(), request.accessClassification(),
                    request.docVersion() == null ? 1 : request.docVersion(), storageName, userId);
            jobs.enqueue(UUID.randomUUID(), id);
        } catch (RuntimeException e) {
            storage.delete(storageName);
            throw e;
        }
        return documents.findById(id).orElseThrow();
    }

    private String checksum(MultipartFile file) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(file.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute checksum", e);
        }
    }

    public List<AiDtos.AiDocumentResponse> list(int page, int size) {
        return documents.list(page, size);
    }

    public long count() {
        return documents.count();
    }

    public AiDtos.AiDocumentResponse get(UUID id) {
        return documents.findById(id).orElseThrow(() -> Exceptions.notFound("AI document not found"));
    }

    public AiDtos.AiDocumentResponse publish(UUID id, UUID approver) {
        AiDtos.AiDocumentResponse doc = get(id);
        if (!"PENDING_REVIEW".equals(doc.publicationStatus())) {
            throw Exceptions.conflict("Only documents in PENDING_REVIEW can be published");
        }
        documents.deactivateOtherVersions(id);
        documents.publish(id, approver);
        updateVectorMetadata(id, "PUBLISHED", true);
        return get(id);
    }

    public AiDtos.AiDocumentResponse archive(UUID id) {
        get(id);
        documents.archive(id);
        updateVectorMetadata(id, "ARCHIVED", false);
        return get(id);
    }

    public AiDtos.AiDocumentResponse reindex(UUID id) {
        AiDtos.AiDocumentResponse doc = get(id);
        jobs.enqueue(UUID.randomUUID(), id);
        documents.updateStatus(id, "DRAFT");
        return get(id);
    }

    public void delete(UUID id) {
        AiDtos.AiDocumentResponse doc = get(id);
        jdbc.update("DELETE FROM ai.vector_store WHERE metadata->>'docId' = ?", id.toString());
        String storageName = documents.storageReference(id);
        documents.delete(id);
        storage.delete(storageName);
    }

    /** Metadata-only update: avoids re-embedding and preserves the working index. */
    private void updateVectorMetadata(UUID id, String status, boolean active) {
        jdbc.update("""
                UPDATE ai.vector_store
                SET metadata = (jsonb_set(
                      jsonb_set(metadata::jsonb, '{status}', ?::jsonb),
                      '{active}', ?::jsonb))::json
                WHERE metadata->>'docId' = ?
                """, "\"" + status + "\"", String.valueOf(active), id.toString());
    }

    public List<AiDtos.AiJobResponse> jobs(int page, int size) {
        return jobs.list(page, size);
    }

    public long jobCount() {
        return jobs.count();
    }
}
