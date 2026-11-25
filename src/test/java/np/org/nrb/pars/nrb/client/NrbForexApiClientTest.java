package np.org.nrb.pars.nrb.client;

import np.org.nrb.pars.nrb.config.NrbProperties;
import np.org.nrb.pars.nrb.domain.NrbForexDtos.NrbForexDay;
import np.org.nrb.pars.nrb.domain.NrbForexDtos.NrbForexResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NrbForexApiClientTest {

    private static final String URL =
            "https://www.nrb.org.np/api/forex/v1/rates?page=1&per_page=100&from=2026-11-18&to=2026-11-18";

    private MockRestServiceServer server;
    private NrbForexApiClient client;

    @BeforeEach
    void setUp() {
        NrbProperties properties = new NrbProperties();
        properties.setMaxRetries(1);
        properties.setForexCacheTtl(Duration.ZERO);
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://www.nrb.org.np/api/forex/v1/");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new NrbForexApiClient(builder.build(), properties);
    }

    private static final String SUCCESS_JSON = """
            {
              "status":{"code":200},
              "errors":{"validation":null},
              "params":{"page":"1","per_page":"100","from":"2026-11-18","to":"2026-11-18"},
              "data":{"payload":[{
                "date":"2026-11-18",
                "published_on":"2026-11-18 00:00:19",
                "modified_on":"2026-11-17 15:51:13",
                "rates":[
                  {"currency":{"unit":100,"name":"Indian Rupee","iso3":"INR"},"buy":"160.00","sell":"160.15"},
                  {"currency":{"unit":1,"name":"U.S. Dollar","iso3":"USD"},"buy":"149.26","sell":"149.86"}
                ]
              }]},
              "pagination":{"page":1,"pages":1,"per_page":100,"total":1,
                "links":{"prev":null,"next":null}}
            }
            """;

    @Test
    void parsesActualResponseShapePreservingOriginalFields() {
        server.expect(requestTo(URL)).andRespond(withSuccess(SUCCESS_JSON, MediaType.APPLICATION_JSON));

        NrbForexResponse response = client.fetchRates(
                LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 18), 1, 100);

        assertEquals(200, response.status().code());
        NrbForexDay day = response.data().safePayload().get(0);
        assertEquals("2026-11-18", day.date());
        assertEquals("2026-11-18 00:00:19", day.publishedOn());
        assertEquals(2, day.safeRates().size());
        var usd = day.safeRates().stream().filter(r -> "USD".equals(r.iso3())).findFirst().orElseThrow();
        assertEquals("149.26", usd.buy());
        assertEquals(new BigDecimal("149.26"), usd.buyAmount());
        var inr = day.safeRates().stream().filter(r -> "INR".equals(r.iso3())).findFirst().orElseThrow();
        assertEquals(100, inr.unit());
        assertEquals(1, response.pagination().total());
        server.verify();
    }

    @Test
    void reportsApiValidationErrorsInsteadOfSilentlyFailing() {
        String body = """
                {"status":{"code":400},
                 "errors":{"validation":{"per_page":["Per Page must be no more than 100"]}},
                 "data":{"payload":null},"pagination":{"page":null,"pages":null,"per_page":null,"total":null}}
                """;
        server.expect(requestTo(URL)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        NrbForexApiException error = assertThrows(NrbForexApiException.class,
                () -> client.fetchRates(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 18), 1, 100));
        assertEquals(400, error.statusCode());
        assertTrue(error.validationDetails().contains("Per Page"));
        server.verify();
    }

    @Test
    void validatesParametersWithoutCallingTheApi() {
        assertThrows(IllegalArgumentException.class,
                () -> client.fetchRates(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 18), 1, 100));
        assertThrows(IllegalArgumentException.class,
                () -> client.fetchRates(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 18), 1, 101));
        assertThrows(IllegalArgumentException.class,
                () -> client.fetchRates(null, LocalDate.of(2026, 11, 18), 1, 100));
        server.verify();
    }

    @Test
    void missingRateIsNullNeverZero() {
        String body = SUCCESS_JSON.replace("\"buy\":\"149.26\"", "\"buy\":null");
        server.expect(requestTo(URL)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        NrbForexResponse response = client.fetchRates(
                LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 18), 1, 100);
        Optional<BigDecimal> buy = response.data().safePayload().get(0).safeRates().stream()
                .filter(r -> "USD".equals(r.iso3())).findFirst().map(r -> r.buyAmount());
        assertTrue(buy.isEmpty());
        server.verify();
    }

    @Test
    void cachesSuccessfulResponsesForConfiguredTtl() {
        NrbProperties props = new NrbProperties();
        props.setMaxRetries(1);
        props.setForexCacheTtl(Duration.ofMinutes(5));
        RestClient.Builder builder = RestClient.builder().baseUrl("https://www.nrb.org.np/api/forex/v1/");
        MockRestServiceServer cachingServer = MockRestServiceServer.bindTo(builder).build();
        NrbForexApiClient cachingClient = new NrbForexApiClient(builder.build(), props);
        cachingServer.expect(requestTo(URL)).andRespond(withSuccess(SUCCESS_JSON, MediaType.APPLICATION_JSON));

        cachingClient.fetchRates(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 18), 1, 100);
        cachingClient.fetchRates(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 18), 1, 100);
        cachingServer.verify();
    }

    @Test
    void retriesTransientServerErrorsWithBoundedAttempts() {
        NrbProperties props = new NrbProperties();
        props.setMaxRetries(2);
        props.setRetryBackoffMs(1);
        props.setForexCacheTtl(Duration.ZERO);
        RestClient.Builder builder = RestClient.builder().baseUrl("https://www.nrb.org.np/api/forex/v1/");
        MockRestServiceServer retryServer = MockRestServiceServer.bindTo(builder).build();
        NrbForexApiClient retryClient = new NrbForexApiClient(builder.build(), props);
        retryServer.expect(requestTo(URL)).andRespond(withServerError());
        retryServer.expect(requestTo(URL)).andRespond(withSuccess(SUCCESS_JSON, MediaType.APPLICATION_JSON));

        NrbForexResponse response = retryClient.fetchRates(
                LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 18), 1, 100);
        assertEquals(200, response.status().code());
        retryServer.verify();
    }
}
