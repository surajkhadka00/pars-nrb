package np.org.nrb.pars.nrb.crawler;

import np.org.nrb.pars.nrb.config.NrbProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

/** Safe downloader: allow-list, robots, size cap, MIME/magic validation, no execution. */
@Component
@ConditionalOnProperty(prefix = "nrb", name = "crawler-enabled", havingValue = "true")
public class NrbDocumentDownloader {

    public record Downloaded(byte[] bytes, String contentType, URI url, String sha256,
                             String fileName, String extension) {}

    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "docx", "xlsx", "xls", "csv", "txt", "html");
    private static final java.util.Map<String, byte[]> MAGIC = java.util.Map.of(
            "pdf", new byte[]{'%', 'P', 'D', 'F', '-'},
            "docx", new byte[]{'P', 'K', 3, 4},
            "xlsx", new byte[]{'P', 'K', 3, 4});

    private final RestClient restClient;
    private final NrbProperties properties;
    private final NrbUrlPolicy policy;

    public NrbDocumentDownloader(RestClient nrbRestClient, NrbProperties properties, NrbUrlPolicy policy) {
        this.restClient = nrbRestClient;
        this.properties = properties;
        this.policy = policy;
    }

    public Downloaded download(URI uri) {
        policy.validate(uri);
        if (!policy.isAllowedByRobots(uri)) {
            throw new IllegalArgumentException("robots.txt disallows this URL");
        }
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        int dot = path.lastIndexOf('.');
        String ext = dot < 0 ? "" : path.substring(dot + 1);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Unsupported file type: " + ext);
        }

        return restClient.get().uri(uri).exchange((request, response) -> {
            long length = response.getHeaders().getContentLength();
            if (length > properties.getMaxDownloadSize()) {
                throw new IllegalArgumentException("Downloaded file exceeds the size limit");
            }
            byte[] bytes;
            try (InputStream in = response.getBody(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int total = 0;
                int read;
                while ((read = in.read(buffer)) != -1) {
                    total += read;
                    if (total > properties.getMaxDownloadSize()) {
                        throw new IllegalArgumentException("Downloaded file exceeds the size limit");
                    }
                    out.write(buffer, 0, read);
                }
                bytes = out.toByteArray();
            }
            if (bytes.length == 0) {
                throw new IllegalArgumentException("Downloaded file is empty");
            }
            validateMagic(ext, bytes);
            String hash;
            try {
                hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            } catch (Exception e) {
                throw new IllegalStateException("Unable to hash document", e);
            }
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            if (fileName.isBlank()) {
                fileName = "document." + ext;
            }
            return new Downloaded(bytes, String.valueOf(response.getHeaders().getContentType()),
                    uri, hash, fileName, ext);
        });
    }

    private void validateMagic(String ext, byte[] bytes) {
        byte[] signature = MAGIC.get(ext);
        if (signature == null) {
            return;
        }
        if (bytes.length < signature.length) {
            throw new IllegalArgumentException("File is truncated");
        }
        for (int i = 0; i < signature.length; i++) {
            if (bytes[i] != signature[i]) {
                throw new IllegalArgumentException("Invalid file signature for " + ext);
            }
        }
    }

    public static String readUtf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
