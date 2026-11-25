package np.org.nrb.pars.nrb.client;

import np.org.nrb.pars.nrb.config.NrbProperties;
import np.org.nrb.pars.nrb.domain.NrbForexDtos;
import np.org.nrb.pars.nrb.domain.NrbForexDtos.NrbForexDay;
import np.org.nrb.pars.nrb.domain.NrbForexDtos.NrbForexRate;
import np.org.nrb.pars.nrb.domain.NrbForexDtos.NrbForexResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Typed client for the ONLY documented NRB endpoint: GET /rates.
 * Uses the documented parameters only; no invented endpoints. Bounded retries,
 * short TTL cache, and no substitution of missing values with zero.
 */
public class NrbForexApiClient {

    private static final ZoneId NEPAL = ZoneId.of("Asia/Kathmandu");

    private final RestClient restClient;
    private final NrbProperties properties;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public NrbForexApiClient(RestClient restClient, NrbProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    private record CacheEntry(NrbForexResponse body, java.time.Instant expiresAt) {
        boolean expired() {
            return java.time.Instant.now().isAfter(expiresAt);
        }
    }

    public NrbForexResponse fetchRates(LocalDate from, LocalDate to, int page, int perPage) {
        validate(from, to, page, perPage);
        String key = from + "|" + to + "|" + page + "|" + perPage;
        CacheEntry cached = cache.get(key);
        if (cached != null && !cached.expired()) {
            return cached.body();
        }
        NrbForexResponse response = executeWithRetry(from, to, page, perPage);
        Duration ttl = properties.getForexCacheTtl();
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            cache.put(key, new CacheEntry(response, java.time.Instant.now().plus(ttl)));
        }
        return response;
    }

    private void validate(LocalDate from, LocalDate to, int page, int perPage) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from date must not be after to date");
        }
        if (page < 1) {
            throw new IllegalArgumentException("page must be at least 1");
        }
        if (perPage < 1 || perPage > properties.getMaxPerPage()) {
            throw new IllegalArgumentException("per_page must be between 1 and " + properties.getMaxPerPage());
        }
    }

    private NrbForexResponse executeWithRetry(LocalDate from, LocalDate to, int page, int perPage) {
        int attempts = Math.max(1, properties.getMaxRetries());
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                NrbForexResponse body = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("rates")
                                .queryParam("page", page)
                                .queryParam("per_page", perPage)
                                .queryParam("from", from.toString())
                                .queryParam("to", to.toString())
                                .build())
                        .retrieve()
                        .body(NrbForexResponse.class);
                if (body == null) {
                    throw new NrbForexApiException("NRB Forex API returned an empty body");
                }
                if (body.status() == null || body.status().code() != 200) {
                    int code = body.status() == null ? 0 : body.status().code();
                    String details = body.errors() != null && body.errors().validation() != null
                            ? body.errors().validation().toString() : null;
                    throw new NrbForexApiException(
                            "NRB Forex API returned status " + code, code, details);
                }
                return body;
            } catch (NrbForexApiException e) {
                // Validation/API errors are deterministic: never retry them.
                throw e;
            } catch (RestClientException e) {
                if (attempt == attempts) {
                    throw new NrbForexApiException("NRB Forex API request failed: " + e.getMessage());
                }
                backoff(attempt);
            }
        }
        throw new NrbForexApiException("NRB Forex API request failed");
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(properties.getRetryBackoffMs() * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NrbForexApiException("NRB Forex API request interrupted");
        }
    }

    /** Latest published day within a bounded lookback window. Never pretends an old rate is current. */
    public Optional<NrbForexDay> latest() {
        LocalDate today = LocalDate.now(NEPAL);
        for (int i = 0; i < Math.max(1, properties.getLookbackDays()); i++) {
            LocalDate date = today.minusDays(i);
            NrbForexResponse response = fetchRates(date, date, 1, properties.getMaxPerPage());
            for (NrbForexDay day : response.data() == null ? List.<NrbForexDay>of() : response.data().safePayload()) {
                if (!day.safeRates().isEmpty()) {
                    return Optional.of(day);
                }
            }
        }
        return Optional.empty();
    }

    /** Historical range across all pages, bounded to avoid runaway requests. */
    public List<NrbForexDay> historical(LocalDate from, LocalDate to) {
        NrbForexResponse first = fetchRates(from, to, 1, properties.getMaxPerPage());
        List<NrbForexDay> all = new ArrayList<>(first.data() == null ? List.of() : first.data().safePayload());
        Integer totalPages = first.pagination() == null ? null : first.pagination().pages();
        int pages = totalPages == null ? 1 : Math.min(totalPages, properties.getMaxPagesPerQuery());
        for (int page = 2; page <= pages; page++) {
            NrbForexResponse next = fetchRates(from, to, page, properties.getMaxPerPage());
            all.addAll(next.data() == null ? List.of() : next.data().safePayload());
        }
        return all;
    }

    public Optional<NrbForexRate> rateOn(String iso3, LocalDate date) {
        if (iso3 == null || iso3.isBlank()) {
            return Optional.empty();
        }
        NrbForexResponse response = fetchRates(date, date, 1, properties.getMaxPerPage());
        for (NrbForexDay day : response.data() == null ? List.<NrbForexDay>of() : response.data().safePayload()) {
            for (NrbForexRate rate : day.safeRates()) {
                if (iso3.equalsIgnoreCase(rate.iso3())) {
                    return Optional.of(rate);
                }
            }
        }
        return Optional.empty();
    }

    public void clearCache() {
        cache.clear();
    }
}
