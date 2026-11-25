package np.org.nrb.pars.nrb.crawler;

import np.org.nrb.pars.nrb.config.NrbProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

/**
 * Controlled crawler. Only approved official URLs, robots.txt respected, bounded depth
 * and page count, crawl delay, no bypass of access controls.
 */
@Component
@ConditionalOnProperty(prefix = "nrb", name = "crawler-enabled", havingValue = "true")
public class NrbWebsiteCrawler {

    public record DiscoveredLink(URI url, String title, String type) {}

    private static final Set<String> DOCUMENT_EXTENSIONS =
            Set.of("pdf", "docx", "xlsx", "xls", "csv", "txt");

    private final RestClient restClient;
    private final NrbProperties properties;
    private final NrbUrlPolicy policy;

    public NrbWebsiteCrawler(RestClient nrbRestClient, NrbProperties properties, NrbUrlPolicy policy) {
        this.restClient = nrbRestClient;
        this.properties = properties;
        this.policy = policy;
    }

    /** Returns discovered document links. Returns empty if robots.txt cannot be verified. */
    public List<DiscoveredLink> discoverDocuments(URI seed) {
        policy.validate(seed);
        if (!loadRobots(seed)) {
            return List.of();
        }

        List<DiscoveredLink> documents = new ArrayList<>();
        Set<URI> visited = new LinkedHashSet<>();
        Queue<URI> queue = new ArrayDeque<>();
        queue.add(seed);
        int depth = 0;
        int crawled = 0;

        while (!queue.isEmpty() && depth < properties.getCrawlerMaxDepth()
                && crawled < properties.getCrawlerMaxPages()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize && crawled < properties.getCrawlerMaxPages(); i++) {
                URI current = queue.poll();
                if (!visited.add(current) || !policy.isAllowedByRobots(current)) {
                    continue;
                }
                String html = fetchHtml(current);
                if (html == null) {
                    continue;
                }
                crawled++;
                Document page = Jsoup.parse(html, current.toString());
                for (Element link : page.select("a[href]")) {
                    URI resolved;
                    try {
                        resolved = policy.normalize(current, link.attr("href"));
                    } catch (IllegalArgumentException e) {
                        continue; // off-domain or unsafe link — silently ignored
                    }
                    String ext = extension(resolved.getPath());
                    if (DOCUMENT_EXTENSIONS.contains(ext)) {
                        if (policy.isAllowedByRobots(resolved)) {
                            documents.add(new DiscoveredLink(resolved,
                                    link.text().isBlank() ? resolved.getPath() : link.text(), ext));
                        }
                    } else if (!visited.contains(resolved)) {
                        queue.add(resolved);
                    }
                }
                delay(current.getHost());
            }
            depth++;
        }
        return documents;
    }

    private boolean loadRobots(URI seed) {
        try {
            URI robots = new URI(seed.getScheme() + "://" + seed.getAuthority() + "/robots.txt");
            policy.validate(robots);
            String body = restClient.get().uri(robots).header(HttpHeaders.ACCEPT, "text/plain")
                    .retrieve().body(String.class);
            if (body == null) {
                return false;
            }
            policy.parseRobots(seed.getHost(), body);
            return true;
        } catch (Exception e) {
            return false; // unavailable/blocked -> do not crawl
        }
    }

    private String fetchHtml(URI uri) {
        try {
            return restClient.get().uri(uri).header(HttpHeaders.ACCEPT, "text/html")
                    .retrieve().body(String.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    private void delay(String host) {
        try {
            Thread.sleep(policy.crawlDelayMs(host));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String extension(String path) {
        String clean = path == null ? "" : path.toLowerCase(Locale.ROOT);
        int dot = clean.lastIndexOf('.');
        return dot < 0 ? "" : clean.substring(dot + 1);
    }
}
