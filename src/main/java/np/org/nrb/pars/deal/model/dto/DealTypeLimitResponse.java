package np.org.nrb.pars.deal.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DealTypeLimitResponse(
        UUID id,
        UUID dealTypeId,
        String dealTypeName,
        UUID currencyId,
        String currencyIsoCode,
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
