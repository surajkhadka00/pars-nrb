package np.org.nrb.pars.deal.model.dto;

import java.util.UUID;

public record InstrumentCodeResponse(
        UUID id,
        UUID dealTypeId,
        String dealTypeName,
        String value,
        String name,
        String description,
        boolean active,
        Long version
) {
}
