package np.org.nrb.pars.deal.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CurrencyResponse(
        UUID id,
        String isoCode,
        String curSymbol,
        String description,
        int stdPrecision,
        int costingPrecision,
        boolean euro,
        boolean emuMember,
        Instant emuEntryDate,
        BigDecimal emuRate,
        BigDecimal roundOffFactor,
        boolean active,
        Long version
) {
}
