package np.org.nrb.pars.nrb.service;

import np.org.nrb.pars.nrb.client.NrbForexApiClient;
import np.org.nrb.pars.nrb.config.NrbProperties;
import np.org.nrb.pars.nrb.domain.NrbForexDtos;
import np.org.nrb.pars.nrb.domain.NrbForexViews.ForexDayView;
import np.org.nrb.pars.nrb.repository.NrbForexRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NrbForexServiceTest {

    private NrbForexDtos.NrbForexDay day() {
        var currency = new NrbForexDtos.NrbCurrency(1, "U.S. Dollar", "USD");
        var rate = new NrbForexDtos.NrbForexRate(currency, "149.26", "149.86");
        return new NrbForexDtos.NrbForexDay("2026-11-18", "2026-11-18 00:00:19",
                "2026-11-17 15:51:13", List.of(rate));
    }

    @Test
    void latestUsesRealPublishedDateAndPersists() {
        FakeClient client = new FakeClient(Optional.of(day()));
        FakeRepository repository = new FakeRepository();
        NrbForexService service = new NrbForexService(client, repository, new NrbProperties());

        ForexDayView view = service.latest();

        assertEquals(LocalDate.of(2026, 11, 18), view.date());
        assertEquals(new BigDecimal("149.26"), view.rates().get(0).buy());
        assertTrue(view.sourceUrl().endsWith("/rates"));
        assertEquals(1, repository.saved);
    }

    @Test
    void historicalUsesStoredDataWithoutRefetching() {
        FakeClient client = new FakeClient(Optional.of(day()));
        var stored = new ForexDayView(LocalDate.of(2026, 11, 18), null, null, null,
                "https://www.nrb.org.np/api/forex/v1/rates", List.of());
        FakeRepository repository = new FakeRepository(List.of(stored));
        NrbForexService service = new NrbForexService(client, repository, new NrbProperties());

        List<ForexDayView> result = service.historical(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 18));

        assertEquals(1, result.size());
        assertEquals(0, client.historicalCalls);
    }

    @Test
    void latestFailsClearlyWhenNothingPublished() {
        NrbForexService service = new NrbForexService(
                new FakeClient(Optional.empty()), new FakeRepository(), new NrbProperties());
        assertThrows(RuntimeException.class, service::latest);
        assertEquals(0, ((FakeRepository) new FakeRepository()).saved);
    }

    private static class FakeClient extends NrbForexApiClient {
        private final Optional<NrbForexDtos.NrbForexDay> latest;
        private int historicalCalls;

        FakeClient(Optional<NrbForexDtos.NrbForexDay> latest) {
            super(null, new NrbProperties());
            this.latest = latest;
        }

        @Override
        public Optional<NrbForexDtos.NrbForexDay> latest() {
            return latest;
        }

        @Override
        public List<NrbForexDtos.NrbForexDay> historical(LocalDate from, LocalDate to) {
            historicalCalls++;
            return latest.map(List::of).orElseGet(List::of);
        }
    }

    private static class FakeRepository extends NrbForexRepository {
        private final List<ForexDayView> stored;
        private int saved;

        FakeRepository() {
            this(List.of());
        }

        FakeRepository(List<ForexDayView> stored) {
            super(null, null);
            this.stored = stored;
        }

        @Override
        public int saveDay(String sourceUrl, NrbForexDtos.NrbForexDay day) {
            saved++;
            return 1;
        }

        @Override
        public List<ForexDayView> findRange(LocalDate from, LocalDate to) {
            return stored;
        }
    }
}
