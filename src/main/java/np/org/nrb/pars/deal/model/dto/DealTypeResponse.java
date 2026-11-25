package np.org.nrb.pars.deal.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DealTypeResponse(
        UUID id,
        String value,
        String name,
        String description,
        String contactMode,
        boolean instrumentCodeApplicable,
        String limitType,
        BigDecimal limitValue,
        boolean active,
        Long version
) {
}
