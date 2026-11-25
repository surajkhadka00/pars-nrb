package np.org.nrb.pars.auth.model.dto;

import java.time.Instant;

public record ForgotPasswordStatusResponse(
        boolean valid,
        Instant expiresAt
) {
}
