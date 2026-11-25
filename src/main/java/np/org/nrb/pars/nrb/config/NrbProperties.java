package np.org.nrb.pars.nrb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/** Configuration for official NRB external sources. No secrets are stored here. */
@Data
@Component
@ConfigurationProperties(prefix = "nrb")
public class NrbProperties {

    private String forexApiBaseUrl = "https://www.nrb.org.np/api/forex/v1/";
    private String websiteBaseUrl = "https://www.nrb.org.np/";

    /** Crawler is OFF by default: must be explicitly enabled after policy approval. */
    private boolean crawlerEnabled = false;
    private String crawlerUserAgent = "PARS-NRB-KnowledgeBot/1.0 (+https://www.nrb.org.np/)";
    private int crawlerDelayMs = 2000;
    private int crawlerMaxPages = 50;
    private int crawlerMaxDepth = 2;
    private boolean respectRobots = true;
    private String syncCron = "0 0 6 * * *";

    private Duration forexCacheTtl = Duration.ofMinutes(10);
    private long maxDownloadSize = 20L * 1024 * 1024;
    private List<String> allowedDomains = List.of("www.nrb.org.np", "nrb.org.np");

    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration responseTimeout = Duration.ofSeconds(20);
    private int maxRetries = 3;
    private long retryBackoffMs = 500;
    private int maxPerPage = 100;
    private int maxPagesPerQuery = 20;
    private int lookbackDays = 7;
}
