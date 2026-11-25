package np.org.nrb.pars.nrb.ingest;

import np.org.nrb.pars.ai.config.ParsAiProperties;
import np.org.nrb.pars.ai.ingestion.ChunkingService;
import np.org.nrb.pars.ai.ingestion.DocumentExtractionService;
import np.org.nrb.pars.nrb.config.NrbProperties;
import np.org.nrb.pars.nrb.crawler.NrbDocumentDownloader;
import np.org.nrb.pars.nrb.repository.NrbSourceRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Indexes downloaded official NRB documents into the SINGLE existing vector store. */
@Service
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class NrbRagIngestionService {

    private final DocumentExtractionService extraction;
    private final ChunkingService chunking;
    private final NrbDocumentMetadataService metadataService;
    private final VectorStore vectorStore;
    private final NrbSourceRepository repository;
    private final ParsAiProperties aiProperties;
    private final NrbProperties nrbProperties;

    public NrbRagIngestionService(DocumentExtractionService extraction, ChunkingService chunking,
                                  NrbDocumentMetadataService metadataService, VectorStore vectorStore,
                                  NrbSourceRepository repository, ParsAiProperties aiProperties,
                                  NrbProperties nrbProperties) {
        this.extraction = extraction;
        this.chunking = chunking;
        this.metadataService = metadataService;
        this.vectorStore = vectorStore;
        this.repository = repository;
        this.aiProperties = aiProperties;
        this.nrbProperties = nrbProperties;
    }

    /** Returns true when a new document was indexed; false when unchanged (idempotent). */
    public boolean ingest(UUID sourceId, NrbDocumentDownloader.Downloaded downloaded, String category) {
        if (repository.findByHash(downloaded.sha256()).isPresent()) {
            return false;
        }
        var extracted = extraction.extract(downloaded.bytes(), downloaded.extension());
        if (!extracted.hasText()) {
            throw new IllegalStateException("NRB document contains no extractable text (OCR not enabled)");
        }
        var meta = metadataService.extract(downloaded.bytes(), downloaded.extension(),
                downloaded.fileName(), extracted.text());
        UUID documentId = UUID.randomUUID();
        List<Document> chunks = chunking.chunk(extracted.text()).stream().map(chunk -> {
            Map<String, Object> map = new HashMap<>();
            map.put("docId", documentId.toString());
            map.put("title", meta.title());
            map.put("version", 1);
            map.put("page", chunk.page() == null ? 0 : chunk.page());
            map.put("section", chunk.section() == null ? "" : chunk.section());
            map.put("classification", "PUBLIC");
            map.put("access", "PUBLIC");
            map.put("deny", "");
            map.put("status", "PUBLISHED");
            map.put("active", true);
            map.put("sourceUrl", downloaded.url().toString());
            map.put("source", "NRB");
            map.put("category", category == null ? "GENERAL" : category);
            map.put("publicationDate", meta.publicationDate() == null ? "" : meta.publicationDate().toString());
            map.put("embeddingModel", aiProperties.getEmbeddingModel());
            map.put("embeddingModelVersion", aiProperties.getEmbeddingModelVersion());
            return new Document(chunk.text(), map);
        }).toList();

        if (chunks.isEmpty()) {
            throw new IllegalStateException("Chunking produced no chunks");
        }
        vectorStore.delete("sourceUrl == '" + downloaded.url() + "'");
        vectorStore.add(chunks);

        repository.insertDocument(documentId, sourceId, downloaded.url().toString(), meta.title(),
                category, meta.language(), meta.publicationDate(), downloaded.sha256(),
                downloaded.fileName(), downloaded.contentType(), downloaded.bytes().length,
                downloaded.url().toString());
        return true;
    }

    public NrbProperties properties() {
        return nrbProperties;
    }
}
