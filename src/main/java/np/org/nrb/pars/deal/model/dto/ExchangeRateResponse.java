package np.org.nrb.pars.deal.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExchangeRateResponse(
        UUID id,
        UUID baseCurrencyId,
        String baseCurrencyIsoCode,
        int unit,
        UUID currencyId,
        String currencyIsoCode,
        String currencyDescription,
        BigDecimal rate,
        LocalDate rateDate,
        Long version
) {
}
