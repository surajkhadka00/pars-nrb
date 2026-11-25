package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.context.CurrentUserProvider;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.config.ParsStorageProperties;
import np.org.nrb.pars.deal.model.Deal;
import np.org.nrb.pars.deal.model.DealAttachment;
import np.org.nrb.pars.deal.model.dto.DealAttachmentResponse;
import np.org.nrb.pars.deal.repository.DealAttachmentRepository;
import np.org.nrb.pars.deal.repository.DealRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class DealAttachmentService {

    private static final ZoneId KATHMANDU = ZoneId.of("Asia/Kathmandu");
    private static final DateTimeFormatter STORED_NAME_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int SIGNATURE_LENGTH = 16;
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp",
            "bmp", "image/bmp",
            "pdf", "application/pdf",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "xls", "application/vnd.ms-excel",
            "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final DealRepository dealRepository;
    private final DealAttachmentRepository attachmentRepository;
    private final DealLookups lookups;
    private final CurrentUserProvider currentUserProvider;
    private final ParsStorageProperties storageProperties;

    public DealAttachmentService(
            DealRepository dealRepository,
            DealAttachmentRepository attachmentRepository,
            DealLookups lookups,
            CurrentUserProvider currentUserProvider,
            ParsStorageProperties storageProperties
    ) {
        this.dealRepository = dealRepository;
        this.attachmentRepository = attachmentRepository;
        this.lookups = lookups;
        this.currentUserProvider = currentUserProvider;
        this.storageProperties = storageProperties;
    }

    @Transactional(readOnly = true)
    public List<DealAttachmentResponse> list(UUID dealId) {
        requireDeal(dealId);
        return attachmentRepository.findByDeal_IdOrderByCreatedAtDesc(dealId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DealAttachmentResponse upload(UUID dealId, MultipartFile file) {
        Deal deal = requireDeal(dealId);
        if (file == null || file.isEmpty()) {
            throw Exceptions.badRequest("File is required");
        }
        String originalFileName = originalFileName(file);
        String extension = extensionOf(originalFileName);
        String contentType = ALLOWED_TYPES.get(extension);
        if (contentType == null) {
            throw Exceptions.badRequest("Allowed files are images, PDF, DOCX, and Excel");
        }
        byte[] signature = readSignature(file);
        if (!matchesSignature(extension, signature)) {
            throw Exceptions.badRequest("File content does not match its extension");
        }

        String storedFileName = storedFileName(extension);
        Path directory = attachmentDirectory();
        Path target = directory.resolve(storedFileName).normalize();
        if (!target.startsWith(directory)) {
            throw Exceptions.badRequest("Invalid stored file name");
        }

        try {
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw Exceptions.badRequest("Could not store the file");
        }

        try {
            DealAttachment attachment = new DealAttachment();
            attachment.setDeal(deal);
            attachment.setOriginalFileName(originalFileName);
            attachment.setStoredFileName(storedFileName);
            attachment.setContentType(contentType);
            attachment.setFileSize(file.getSize());
            attachment.setUploadedBy(lookups.user(currentUserProvider.require().userId()));
            attachmentRepository.save(attachment);
            return toResponse(attachment);
        } catch (RuntimeException ex) {
            deleteQuietly(target);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(UUID dealId, UUID attachmentId) {
        if (dealId == null) {
            throw Exceptions.badRequest("dealId is required");
        }
        DealAttachment attachment = attachmentRepository.findDetailedById(attachmentId)
                .orElseThrow(() -> Exceptions.notFound("Attachment not found"));
        if (!attachment.getDeal().getId().equals(dealId)) {
            throw Exceptions.notFound("Attachment not found");
        }
        Path directory = attachmentDirectory();
        Path target = directory.resolve(attachment.getStoredFileName()).normalize();
        if (!target.startsWith(directory) || !Files.isRegularFile(target)) {
            throw Exceptions.notFound("Attachment file is missing");
        }
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(attachment.getOriginalFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .contentLength(attachment.getFileSize())
                .body(new FileSystemResource(target));
    }

    @Transactional
    public void delete(UUID dealId, UUID id) {
        if (dealId == null) {
            throw Exceptions.badRequest("dealId is required");
        }
        DealAttachment attachment = attachmentRepository.findDetailedById(id)
                .orElseThrow(() -> Exceptions.notFound("Attachment not found"));
        if (!attachment.getDeal().getId().equals(dealId)) {
            throw Exceptions.notFound("Attachment not found");
        }
        Path directory = attachmentDirectory();
        Path target = directory.resolve(attachment.getStoredFileName()).normalize();
        attachmentRepository.delete(attachment);
        if (target.startsWith(directory)) {
            deleteQuietly(target);
        }
    }

    private Deal requireDeal(UUID dealId) {
        if (dealId == null) {
            throw Exceptions.badRequest("dealId is required");
        }
        return dealRepository.findById(dealId).orElseThrow(() -> Exceptions.notFound("Deal not found"));
    }

    private Path attachmentDirectory() {
        String configured = storageProperties.getDealAttachments();
        if (configured == null || configured.isBlank()) {
            throw Exceptions.badRequest("Deal attachment folder is not configured");
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    private static String storedFileName(String extension) {
        String time = ZonedDateTime.now(KATHMANDU).format(STORED_NAME_TIME);
        String unique = UUID.randomUUID().toString().substring(0, 8);
        return time + "-" + unique + "." + extension;
    }

    private static String originalFileName(MultipartFile file) {
        String raw = file.getOriginalFilename();
        String name = raw == null ? "" : Path.of(raw).getFileName().toString().replaceAll("[\\r\\n\\t]", "").trim();
        if (name.isBlank() || ".".equals(name) || "..".equals(name)) {
            throw Exceptions.badRequest("File name is required");
        }
        if (name.length() > 255) {
            name = name.substring(name.length() - 255);
        }
        return name;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            throw Exceptions.badRequest("Allowed files are images, PDF, DOCX, and Excel");
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static byte[] readSignature(MultipartFile file) {
        byte[] header = new byte[SIGNATURE_LENGTH];
        try (InputStream input = file.getInputStream()) {
            int read = input.read(header);
            if (read <= 0) {
                throw Exceptions.badRequest("File is empty");
            }
            return read == header.length ? header : Arrays.copyOf(header, read);
        } catch (IOException ex) {
            throw Exceptions.badRequest("Could not read the file");
        }
    }

    private static boolean matchesSignature(String extension, byte[] header) {
        return switch (extension) {
            case "jpg", "jpeg" -> startsWith(header, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            case "png" -> startsWith(header, new byte[]{(byte) 0x89, 'P', 'N', 'G'});
            case "gif" -> startsWith(header, new byte[]{'G', 'I', 'F', '8'});
            case "bmp" -> startsWith(header, new byte[]{'B', 'M'});
            case "webp" -> header.length >= 12
                    && startsWith(header, new byte[]{'R', 'I', 'F', 'F'})
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
            case "pdf" -> startsWith(header, new byte[]{'%', 'P', 'D', 'F'});
            case "docx", "xlsx" -> startsWith(header, new byte[]{'P', 'K'});
            case "xls" -> startsWith(header, new byte[]{(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0});
            default -> false;
        };
    }

    private static boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // The database row was not saved. A leftover file can be removed later.
        }
    }

    private DealAttachmentResponse toResponse(DealAttachment attachment) {
        return new DealAttachmentResponse(
                attachment.getId(),
                attachment.getDeal().getId(),
                attachment.getOriginalFileName(),
                attachment.getStoredFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUploadedBy().getId(),
                attachment.getUploadedBy().getUsername(),
                attachment.getCreatedAt()
        );
    }
}
