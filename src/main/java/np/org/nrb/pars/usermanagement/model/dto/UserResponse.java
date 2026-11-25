package np.org.nrb.pars.usermanagement.model.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        String mobile,
        String address,
        UUID positionId,
        String positionName,
        boolean hasImage,
        boolean enabled,
        boolean expired,
        Instant lockedUntil,
        Instant lastSuccessfulLoginAt,
        Instant inactiveExpireAt,
        Set<String> roles,
        Long version
) {
}
