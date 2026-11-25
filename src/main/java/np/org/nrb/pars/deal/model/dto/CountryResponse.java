package np.org.nrb.pars.deal.model.dto;

import java.time.Instant;
import java.util.UUID;

public record CountryResponse(
        UUID id,
        String value,
        String name,
        boolean active,
        UUID createdBy,
        Instant createdAt,
        UUID updatedBy,
        Instant updatedAt,
        Long version
) {
}
