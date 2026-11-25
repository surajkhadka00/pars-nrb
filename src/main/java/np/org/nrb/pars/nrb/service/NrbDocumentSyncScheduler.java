package np.org.nrb.pars.nrb.service;

import np.org.nrb.pars.nrb.crawler.NrbDocumentDownloader;
import np.org.nrb.pars.nrb.crawler.NrbUrlPolicy;
import np.org.nrb.pars.nrb.crawler.NrbWebsiteCrawler;
import np.org.nrb.pars.nrb.ingest.NrbRagIngestionService;
import np.org.nrb.pars.nrb.repository.NrbSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** Controlled NRB document synchronization. Runs only when explicitly enabled. */
@Component
@ConditionalOnProperty(prefix = "nrb", name = "crawler-enabled", havingValue = "true")
public class NrbDocumentSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(NrbDocumentSyncScheduler.class);

    private final NrbSourceRepository repository;
    private final NrbWebsiteCrawler crawler;
    private final NrbDocumentDownloader downloader;
    private final NrbRagIngestionService ingestion;
    private final NrbUrlPolicy policy;

    public NrbDocumentSyncScheduler(NrbSourceRepository repository, NrbWebsiteCrawler crawler,
                                    NrbDocumentDownloader downloader, NrbRagIngestionService ingestion,
                                    NrbUrlPolicy policy) {
        this.repository = repository;
        this.crawler = crawler;
        this.downloader = downloader;
        this.ingestion = ingestion;
        this.policy = policy;
    }

    @Scheduled(cron = "${nrb.sync-cron:0 0 6 * * *}", zone = "Asia/Kathmandu")
    public void syncAllEnabled() {
        for (NrbSourceRepository.SourceRow source : repository.listSources()) {
            if (!source.enabled() || "API".equals(source.type())) {
                continue; // API sources are handled by the forex scheduler
            }
            syncSource(source.id());
        }
    }

    /** Synchronous administrative sync of one source (bounded by crawler limits). */
    public int syncSource(UUID sourceId) {
        NrbSourceRepository.SourceRow source = repository.findSource(sourceId).orElse(null);
        if (source == null) {
            return 0;
        }
        UUID jobId = UUID.randomUUID();
        repository.insertJob(jobId, sourceId, "DOCUMENT_SYNC");
        int discovered = 0;
        int indexed = 0;
        int failed = 0;
        try {
            List<NrbWebsiteCrawler.DiscoveredLink> links = crawler.discoverDocuments(URI.create(source.url()));
            discovered = links.size();
            for (NrbWebsiteCrawler.DiscoveredLink link : links) {
                try {
                    URI uri = policy.normalize(URI.create(source.url()), link.url().toString());
                    var downloaded = downloader.download(uri);
                    if (ingestion.ingest(sourceId, downloaded, source.category())) {
                        indexed++;
                    }
                } catch (Exception e) {
                    failed++;
                    repository.insertError(sourceId, null, link.url().toString(), "DOWNLOAD_INDEX", e.getMessage());
                }
            }
            repository.updateSyncResult(sourceId, failed == 0, "SYNCED", null);
            repository.finishJob(jobId, "FINISHED", discovered, indexed, failed, null);
        } catch (Exception e) {
            repository.updateSyncResult(sourceId, false, "FAILED", e.getMessage());
            repository.finishJob(jobId, "FAILED", discovered, indexed, failed, e.getMessage());
            log.warn("PARS NRB source {} sync failed", source.name(), e);
        }
        return indexed;
    }
}
