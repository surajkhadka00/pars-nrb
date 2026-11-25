package np.org.nrb.pars.nrb.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Typed views returned by our own PARS endpoints (not NRB's raw envelope). */
public final class NrbForexViews {
    private NrbForexViews() {}

    public record ForexRateView(
            String iso3,
            String name,
            int unit,
            BigDecimal buy,
            BigDecimal sell,
            String rawBuy,
            String rawSell
    ) {}

    public record ForexDayView(
            LocalDate date,
            String publishedOn,
            String modifiedOn,
            Instant fetchedAt,
            String sourceUrl,
            List<ForexRateView> rates
    ) {}
}
