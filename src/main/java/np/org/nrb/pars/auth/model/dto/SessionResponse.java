package np.org.nrb.pars.auth.model.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID familyId,
        String ipAddress,
        String userAgent,
        Instant issuedAt,
        Instant lastActivityAt,
        Instant expiresAt,
        Instant revokedAt,
        boolean current
) {
}
