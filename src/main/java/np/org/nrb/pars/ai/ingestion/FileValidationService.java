package np.org.nrb.pars.ai.ingestion;

import np.org.nrb.pars.ai.config.ParsAiProperties;
import np.org.nrb.pars.common.exception.Exceptions;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/** Validates file extension, MIME type and magic bytes before any content is stored. */
@Service
public class FileValidationService {

    private static final Map<String, byte[]> MAGIC = Map.of(
            "pdf", new byte[]{'%', 'P', 'D', 'F', '-'},
            "docx", new byte[]{'P', 'K', 3, 4},
            "xlsx", new byte[]{'P', 'K', 3, 4},
            "txt", new byte[]{},
            "html", new byte[]{}
    );
    private static final Set<String> ALLOWED = Set.of("pdf", "docx", "xlsx", "txt", "html");

    private final ParsAiProperties properties;
    private final Tika tika = new Tika();

    public FileValidationService(ParsAiProperties properties) {
        this.properties = properties;
    }

    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw Exceptions.badRequest("Uploaded file is empty");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw Exceptions.badRequest("File exceeds the maximum allowed size");
        }
        String original = file.getOriginalFilename();
        if (original == null || original.contains("..") || original.contains("/") || original.contains("\\")) {
            throw Exceptions.badRequest("Invalid file name");
        }
        int dot = original.lastIndexOf('.');
        String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase();
        if (!ALLOWED.contains(ext)) {
            throw Exceptions.badRequest("Unsupported file type: " + ext);
        }

        // Read the bytes ONCE. Tika and the magic-byte check must not compete for
        // the same InputStream (detect() consumes it).
        byte[] bytes;
        try {
            bytes = file.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw Exceptions.badRequest("Unable to read uploaded file");
        }

        String detected;
        try {
            detected = tika.detect(new ByteArrayInputStream(bytes), original);
        } catch (IOException e) {
            throw Exceptions.badRequest("Unable to detect file content type");
        }
        if (!mimeMatches(ext, detected)) {
            throw Exceptions.badRequest("File content does not match its extension (detected: " + detected + ")");
        }

        byte[] signature = MAGIC.get(ext);
        if (signature != null && signature.length > 0) {
            if (bytes.length < signature.length) {
                throw Exceptions.badRequest("File is truncated");
            }
            for (int i = 0; i < signature.length; i++) {
                if (bytes[i] != signature[i]) {
                    throw Exceptions.badRequest("Invalid file signature for a " + ext + " file");
                }
            }
        }
        return ext;
    }

    private boolean mimeMatches(String ext, String mime) {
        if (mime == null) {
            return false;
        }
        return switch (ext) {
            case "pdf" -> mime.contains("pdf");
            case "docx" -> mime.contains("officedocument.wordprocessingml") || mime.contains("zip");
            case "xlsx" -> mime.contains("officedocument.spreadsheetml") || mime.contains("zip");
            case "txt" -> mime.contains("text/plain");
            case "html" -> mime.contains("html");
            default -> false;
        };
    }

    public Path resolveSafe(Path root, String relativeName) {
        Path target = root.resolve(relativeName).normalize();
        if (!target.startsWith(root.normalize())) {
            throw Exceptions.badRequest("Path traversal detected");
        }
        return target;
    }
}
