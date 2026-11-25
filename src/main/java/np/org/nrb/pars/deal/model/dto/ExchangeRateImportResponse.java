package np.org.nrb.pars.deal.model.dto;

import java.time.LocalDate;

public record ExchangeRateImportResponse(
        LocalDate rateDate,
        int imported
) {
}
