package np.org.nrb.pars.deal.model.dto;

import java.time.Instant;
import java.util.UUID;

public record DealAttachmentResponse(
        UUID id,
        UUID dealId,
        String originalFileName,
        String storedFileName,
        String contentType,
        long fileSize,
        UUID uploadedById,
        String uploadedByUsername,
        Instant uploadedAt
) {
}
