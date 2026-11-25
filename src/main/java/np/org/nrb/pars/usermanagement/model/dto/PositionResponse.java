package np.org.nrb.pars.usermanagement.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PositionResponse(
        UUID id,
        String value,
        String name,
        String limitType,
        BigDecimal limitValue,
        boolean active,
        UUID createdBy,
        Instant createdAt,
        UUID updatedBy,
        Instant updatedAt,
        Long version
) {
}
