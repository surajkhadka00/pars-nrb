package np.org.nrb.pars.nrb.service;

import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.nrb.client.NrbForexApiClient;
import np.org.nrb.pars.nrb.config.NrbProperties;
import np.org.nrb.pars.nrb.domain.NrbForexDtos;
import np.org.nrb.pars.nrb.domain.NrbForexViews.ForexDayView;
import np.org.nrb.pars.nrb.domain.NrbForexViews.ForexRateView;
import np.org.nrb.pars.nrb.repository.NrbForexRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Application service for official NRB forex data: live cache plus durable history. */
@Service
public class NrbForexService {

    private final NrbForexApiClient client;
    private final NrbForexRepository repository;
    private final NrbProperties properties;

    public NrbForexService(NrbForexApiClient client, NrbForexRepository repository, NrbProperties properties) {
        this.client = client;
        this.repository = repository;
        this.properties = properties;
    }

    /** Latest published day within the configured lookback. The returned date is authoritative. */
    public ForexDayView latest() {
        NrbForexDtos.NrbForexDay day = client.latest()
                .orElseThrow(() -> Exceptions.notFound("No NRB forex rates are available in the configured lookback window"));
        persist(day);
        return toView(day);
    }

    /** Historical query from stored snapshots; fetched from NRB only when the DB has no data. */
    @Transactional
    public List<ForexDayView> historical(LocalDate from, LocalDate to) {
        validateRange(from, to);
        List<ForexDayView> stored = repository.findRange(from, to);
        if (!stored.isEmpty()) {
            return stored;
        }
        for (NrbForexDtos.NrbForexDay day : client.historical(from, to)) {
            persist(day);
        }
        return repository.findRange(from, to);
    }

    public ForexDayView byCurrency(String iso3, LocalDate date) {
        if (iso3 == null || iso3.isBlank()) {
            throw Exceptions.badRequest("Currency code is required");
        }
        Optional<ForexDayView> stored = repository.findByCurrencyAndDate(iso3, date);
        if (stored.isPresent()) {
            return stored.get();
        }
        NrbForexDtos.NrbForexRate rate = client.rateOn(iso3, date)
                .orElseThrow(() -> Exceptions.notFound("NRB has no published rate for " + iso3 + " on " + date));
        persistSingle(date, rate);
        return repository.findByCurrencyAndDate(iso3, date)
                .orElseThrow(() -> Exceptions.notFound("NRB forex rate could not be stored"));
    }

    /** Administrator sync: fetch and persist; returns the number of newly stored days. */
    @Transactional
    public int sync(LocalDate from, LocalDate to) {
        validateRange(from, to);
        int saved = 0;
        for (NrbForexDtos.NrbForexDay day : client.historical(from, to)) {
            saved += persist(day);
        }
        return saved;
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw Exceptions.badRequest("A valid from/to date range is required");
        }
    }

    private int persist(NrbForexDtos.NrbForexDay day) {
        return repository.saveDay(sourceUrl(), day);
    }

    private void persistSingle(LocalDate date, NrbForexDtos.NrbForexRate rate) {
        NrbForexDtos.NrbForexDay day = new NrbForexDtos.NrbForexDay(
                date.toString(), null, null, List.of(rate));
        persist(day);
    }

    private String sourceUrl() {
        String base = properties.getForexApiBaseUrl();
        return base.endsWith("/") ? base + "rates" : base + "/rates";
    }

    private ForexDayView toView(NrbForexDtos.NrbForexDay day) {
        LocalDate date;
        try {
            date = LocalDate.parse(day.date());
        } catch (Exception e) {
            throw Exceptions.badRequest("NRB returned an invalid rate date: " + day.date());
        }
        List<ForexRateView> rates = day.safeRates().stream().map(r -> new ForexRateView(
                r.iso3(), r.name(), r.unit(), r.buyAmount(), r.sellAmount(), r.buy(), r.sell())).toList();
        return new ForexDayView(date, day.publishedOn(), day.modifiedOn(),
                java.time.Instant.now(), sourceUrl(), rates);
    }
}
