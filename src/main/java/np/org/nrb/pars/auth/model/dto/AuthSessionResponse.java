package np.org.nrb.pars.auth.model.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthSessionResponse(
        UUID id,
        UUID userId,
        String username,
        UUID familyId,
        String ipAddress,
        String userAgent,
        Instant issuedAt,
        Instant lastActivityAt,
        Instant expiresAt,
        Instant revokedAt
) {
}
