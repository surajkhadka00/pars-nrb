package np.org.nrb.pars.deal.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CounterpartyLimitResponse(
        UUID id,
        UUID counterpartyId,
        String counterpartyName,
        Instant dateFrom,
        Instant dateTo,
        String description,
        String limitCriteria,
        String limitType,
        BigDecimal limitValue,
        boolean active,
        Long version
) {
}
