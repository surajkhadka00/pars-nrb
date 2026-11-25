package np.org.nrb.pars.monitoring.model.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String method,
        String path,
        int status,
        String payload,
        long durationMs,
        UUID userId,
        Instant createdAt
) {
}
