package np.org.nrb.pars.auth.model.dto;

import java.time.Instant;
import java.util.UUID;

public record FailedAttemptResponse(
        UUID id,
        String username,
        String ipAddress,
        Instant attemptedAt,
        String reason
) {
}
