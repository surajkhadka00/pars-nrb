package np.org.nrb.pars.ai.ingestion;

import np.org.nrb.pars.ai.config.ParsAiProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** Private, non-public storage for original AI documents. Never under static resources. */
@Service
public class DocumentStorageService {

    private final ParsAiProperties properties;
    private final FileValidationService validation;

    public DocumentStorageService(ParsAiProperties properties, FileValidationService validation) {
        this.properties = properties;
        this.validation = validation;
    }

    public String store(MultipartFile file, String extension) {
        Path root = Path.of(properties.getStoragePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
            String storedName = UUID.randomUUID() + "." + extension;
            Path target = validation.resolveSafe(root, storedName);
            try (var in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return storedName;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store AI document", e);
        }
    }

    public byte[] read(String storageName) {
        Path root = Path.of(properties.getStoragePath()).toAbsolutePath().normalize();
        try {
            Path target = validation.resolveSafe(root, storageName);
            if (!Files.isRegularFile(target)) {
                throw new IllegalStateException("Stored document not found");
            }
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read AI document", e);
        }
    }

    public void delete(String storageName) {
        Path root = Path.of(properties.getStoragePath()).toAbsolutePath().normalize();
        try {
            Path target = validation.resolveSafe(root, storageName);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to delete AI document", e);
        }
    }
}
