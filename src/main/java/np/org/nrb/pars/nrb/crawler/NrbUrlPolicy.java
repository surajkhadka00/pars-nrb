package np.org.nrb.pars.nrb.crawler;

import np.org.nrb.pars.nrb.config.NrbProperties;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** SSRF guard and robots.txt policy. Only approved official hosts are ever contacted. */
@Component
public class NrbUrlPolicy {

    private final NrbProperties properties;
    private final java.util.Map<String, RobotsRules> robotsCache = new java.util.concurrent.ConcurrentHashMap<>();

    public NrbUrlPolicy(NrbProperties properties) {
        this.properties = properties;
    }

    public void validate(URI uri) {
        if (uri == null || uri.getScheme() == null) {
            throw new IllegalArgumentException("URL must be absolute");
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException("Only http/https URLs are allowed");
        }
        if (uri.getUserInfo() != null) {
            throw new IllegalArgumentException("URL must not contain credentials");
        }
        String host = uri.getHost();
        if (host == null || !isAllowedHost(host)) {
            throw new IllegalArgumentException("Host is not in the NRB allow-list: " + host);
        }
        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isAnyLocalAddress()) {
                throw new IllegalArgumentException("Private/loopback addresses are blocked");
            }
        } catch (java.net.UnknownHostException e) {
            throw new IllegalArgumentException("Unable to resolve host: " + host);
        }
    }

    public boolean isAllowedHost(String host) {
        if (host == null) {
            return false;
        }
        String normalized = host.toLowerCase(Locale.ROOT);
        return properties.getAllowedDomains().stream()
                .map(d -> d.toLowerCase(Locale.ROOT))
                .anyMatch(d -> normalized.equals(d) || normalized.endsWith("." + d));
    }

    public URI normalize(URI base, String rawHref) {
        if (rawHref == null || rawHref.isBlank()) {
            throw new IllegalArgumentException("Empty link");
        }
        int hash = rawHref.indexOf('#');
        String href = (hash >= 0 ? rawHref.substring(0, hash) : rawHref).trim();
        if (href.isEmpty()) {
            throw new IllegalArgumentException("Anchor-only link");
        }
        try {
            href = URLDecoder.decode(href, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed link", e);
        }
        URI resolved = base.resolve(href).normalize();
        validate(resolved);
        return resolved;
    }

    /** Parses a minimal robots.txt subset: User-agent, Allow, Disallow, Crawl-delay. */
    public void parseRobots(String host, String body) {
        RobotsRules rules = new RobotsRules();
        String agent = null;
        for (String line : body.split("\\r?\\n")) {
            String clean = line.split("#")[0].trim();
            if (clean.toLowerCase(Locale.ROOT).startsWith("user-agent:")) {
                agent = clean.substring(clean.indexOf(':') + 1).trim().toLowerCase(Locale.ROOT);
            } else if (clean.toLowerCase(Locale.ROOT).startsWith("disallow:") && agent != null) {
                String path = clean.substring(clean.indexOf(':') + 1).trim();
                if (!path.isEmpty()) {
                    rules.disallowed.add(path);
                }
            } else if (clean.toLowerCase(Locale.ROOT).startsWith("allow:") && agent != null) {
                rules.allowed.add(clean.substring(clean.indexOf(':') + 1).trim());
            } else if (clean.toLowerCase(Locale.ROOT).startsWith("crawl-delay:")) {
                try {
                    rules.crawlDelayMs = (long) (Double.parseDouble(
                            clean.substring(clean.indexOf(':') + 1).trim()) * 1000);
                } catch (NumberFormatException ignored) {
                    // ignore malformed delay
                }
            }
        }
        robotsCache.put(host, rules);
    }

    public boolean isAllowedByRobots(URI uri) {
        if (!properties.isRespectRobots()) {
            return true;
        }
        RobotsRules rules = robotsCache.get(uri.getHost());
        if (rules == null) {
            return false; // not fetched / unavailable -> skip, never assume permission
        }
        String path = uri.getPath() == null || uri.getPath().isEmpty() ? "/" : uri.getPath();
        for (String allow : rules.allowed) {
            if (path.startsWith(allow)) {
                return true;
            }
        }
        for (String disallow : rules.disallowed) {
            if (path.startsWith(disallow)) {
                return false;
            }
        }
        return true;
    }

    public long crawlDelayMs(String host) {
        RobotsRules rules = robotsCache.get(host);
        if (rules != null && rules.crawlDelayMs > 0) {
            return Math.max(rules.crawlDelayMs, properties.getCrawlerDelayMs());
        }
        return properties.getCrawlerDelayMs();
    }

    private static class RobotsRules {
        private final List<String> disallowed = new java.util.ArrayList<>();
        private final List<String> allowed = new java.util.ArrayList<>();
        private long crawlDelayMs = 0;
    }
}
