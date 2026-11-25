package np.org.nrb.pars.deal.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CurrencyRequest(
        @NotBlank @Size(min = 3, max = 3) String isoCode,
        @Size(max = 10) String curSymbol,
        @NotBlank @Size(max = 255) String description,
        Integer stdPrecision,
        Integer costingPrecision,
        Boolean euro,
        Boolean emuMember,
        Instant emuEntryDate,
        BigDecimal emuRate,
        BigDecimal roundOffFactor,
        Boolean active,
        Long version
) {
}
