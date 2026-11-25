package np.org.nrb.pars.deal.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CounterpartyLimitRequest(
        @NotNull UUID counterpartyId,
        Instant dateFrom,
        Instant dateTo,
        @Size(max = 255) String description,
        @Size(max = 40) String limitCriteria,
        @Size(max = 20) String limitType,
        BigDecimal limitValue,
        Boolean active,
        Long version
) {
}
