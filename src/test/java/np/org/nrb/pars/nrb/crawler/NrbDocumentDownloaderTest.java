package np.org.nrb.pars.nrb.crawler;

import np.org.nrb.pars.nrb.config.NrbProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NrbDocumentDownloaderTest {

    private static final String URL = "https://www.nrb.org.np/contents/uploads/policy.pdf";

    private MockRestServiceServer server;
    private NrbDocumentDownloader downloader;

    @BeforeEach
    void setUp() {
        NrbProperties properties = new NrbProperties();
        properties.setMaxDownloadSize(64);
        RestClient.Builder builder = RestClient.builder().baseUrl("https://www.nrb.org.np/");
        server = MockRestServiceServer.bindTo(builder).build();
        NrbUrlPolicy policy = new NrbUrlPolicy(properties);
        policy.parseRobots("www.nrb.org.np", "User-agent: *\nAllow: /\n");
        downloader = new NrbDocumentDownloader(builder.build(), properties, policy);
    }

    @Test
    void downloadsValidPdfWithSignatureAndHash() {
        server.expect(requestTo(URL)).andRespond(withSuccess("%PDF-1.4 sample".getBytes(), MediaType.APPLICATION_PDF));

        var result = downloader.download(URI.create(URL));

        assertEquals("pdf", result.extension());
        assertEquals(64, result.sha256().length());
        server.verify();
    }

    @Test
    void rejectsOversizedFile() {
        byte[] big = new byte[200];
        java.util.Arrays.fill(big, (byte) 'A');
        server.expect(requestTo(URL)).andRespond(withSuccess(big, MediaType.APPLICATION_PDF));

        assertThrows(IllegalArgumentException.class, () -> downloader.download(URI.create(URL)));
        server.verify();
    }

    @Test
    void rejectsInvalidPdfSignature() {
        server.expect(requestTo(URL)).andRespond(withSuccess("NOT-A-PDF".getBytes(), MediaType.APPLICATION_PDF));

        assertThrows(IllegalArgumentException.class, () -> downloader.download(URI.create(URL)));
        server.verify();
    }
}
