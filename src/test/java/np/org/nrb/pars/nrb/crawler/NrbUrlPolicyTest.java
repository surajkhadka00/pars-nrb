package np.org.nrb.pars.nrb.crawler;

import np.org.nrb.pars.nrb.config.NrbProperties;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NrbUrlPolicyTest {

    private final NrbProperties properties = new NrbProperties();
    private final NrbUrlPolicy policy = new NrbUrlPolicy(properties);

    @Test
    void allowsOfficialDomainsOnly() {
        assertTrue(policy.isAllowedHost("www.nrb.org.np"));
        assertTrue(policy.isAllowedHost("nrb.org.np"));
        assertFalse(policy.isAllowedHost("evil.example.com"));
        assertFalse(policy.isAllowedHost("www.nrb.org.np.evil.example.com"));
    }

    @Test
    void rejectsUnsafeSchemesCredentialsAndOffDomainUrls() {
        assertThrows(IllegalArgumentException.class, () -> policy.validate(URI.create("file:///etc/passwd")));
        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(URI.create("http://user:pass@www.nrb.org.np/")));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(URI.create("http://localhost/admin")));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(URI.create("https://evil.example.com/")));
    }

    @Test
    void normalizesRelativeLinksAgainstBase() {
        URI base = URI.create("https://www.nrb.org.np/category/circulars/");
        URI resolved = policy.normalize(base, "../notices/?department=bfr#section");
        assertEquals("https://www.nrb.org.np/category/notices/?department=bfr", resolved.toString());
    }

    @Test
    void robotsRulesControlAccess() {
        URI seed = URI.create("https://www.nrb.org.np/");
        policy.parseRobots(seed.getHost(), """
                User-agent: *
                Disallow: /private/
                Allow: /public/
                Crawl-delay: 1
                """);
        assertFalse(policy.isAllowedByRobots(URI.create("https://www.nrb.org.np/private/doc.pdf")));
        assertTrue(policy.isAllowedByRobots(URI.create("https://www.nrb.org.np/public/doc.pdf")));
        assertTrue(policy.isAllowedByRobots(URI.create("https://www.nrb.org.np/other/page")));
        assertEquals(2000, policy.crawlDelayMs("www.nrb.org.np"));
    }

    @Test
    void anchorsAndMalformedLinksAreRejectedSafely() {
        assertThrows(IllegalArgumentException.class,
                () -> policy.normalize(URI.create("https://www.nrb.org.np/"), "#"));
        assertThrows(IllegalArgumentException.class,
                () -> policy.normalize(URI.create("https://www.nrb.org.np/"), "   "));
    }

    @Test
    void robotsNotFetchedMeansNoCrawlPermission() {
        assertFalse(policy.isAllowedByRobots(URI.create("https://www.nrb.org.np/category/circulars/")));
    }

    @Test
    void configuredAllowedDomainsAreUsed() {
        properties.setAllowedDomains(List.of("nrb.gov.np"));
        NrbUrlPolicy custom = new NrbUrlPolicy(properties);
        assertTrue(custom.isAllowedHost("www.nrb.gov.np"));
        assertFalse(custom.isAllowedHost("www.nrb.org.np"));
    }
}
