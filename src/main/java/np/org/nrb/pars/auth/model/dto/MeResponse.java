package np.org.nrb.pars.auth.model.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MeResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        UUID sessionId,
        Set<String> roles,
        Set<String> permissions,
        Instant lastSuccessfulLoginAt
) {
}
