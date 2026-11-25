package np.org.nrb.pars.deal.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExchangeRateRequest(
        UUID baseCurrencyId,
        @NotNull @Positive Integer unit,
        @NotNull UUID currencyId,
        @NotNull @Positive BigDecimal rate,
        @NotNull LocalDate rateDate,
        Long version
) {
}
