package np.org.nrb.pars.nrb.controller;

import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.nrb.crawler.NrbDocumentDownloader;
import np.org.nrb.pars.nrb.ingest.NrbRagIngestionService;
import np.org.nrb.pars.nrb.repository.NrbSourceRepository;
import np.org.nrb.pars.nrb.service.NrbDocumentSyncScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/nrb")
@ConditionalOnExpression("${pars.ai.enabled:false} and ${nrb.crawler.enabled:false}")
public class NrbAdminController {

    private final NrbDocumentSyncScheduler syncScheduler;
    private final NrbSourceRepository repository;
    private final NrbDocumentDownloader downloader;
    private final NrbRagIngestionService ingestion;

    public NrbAdminController(NrbDocumentSyncScheduler syncScheduler, NrbSourceRepository repository,
                              NrbDocumentDownloader downloader, NrbRagIngestionService ingestion) {
        this.syncScheduler = syncScheduler;
        this.repository = repository;
        this.downloader = downloader;
        this.ingestion = ingestion;
    }

    @PostMapping("/sources/{id}/sync")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_SYNC')")
    public ApiResponse<Map<String, Object>> syncSource(@PathVariable UUID id) {
        int indexed = syncScheduler.syncSource(id);
        return ApiResponse.ok(Map.of("indexedDocuments", indexed));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_SYNC')")
    public ApiResponse<Map<String, Object>> syncAll() {
        int indexed = 0;
        for (NrbSourceRepository.SourceRow source : repository.listSources()) {
            if (source.enabled() && !"API".equals(source.type())) {
                indexed += syncScheduler.syncSource(source.id());
            }
        }
        return ApiResponse.ok(Map.of("indexedDocuments", indexed));
    }

    @PostMapping("/documents/{id}/reindex")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_SYNC')")
    public ApiResponse<Map<String, Object>> reindex(@PathVariable UUID id) {
        NrbSourceRepository.DocumentRow document = repository.listDocuments(0, 1000).stream()
                .filter(d -> d.id().equals(id)).findFirst()
                .orElseThrow(() -> Exceptions.notFound("NRB document not found"));
        try {
            var downloaded = downloader.download(URI.create(document.storageReference()));
            boolean indexed = ingestion.ingest(document.id(), downloaded, document.category());
            return ApiResponse.ok(Map.of("reindexed", indexed));
        } catch (Exception e) {
            throw Exceptions.badRequest("Reindex failed: " + e.getMessage());
        }
    }
}
