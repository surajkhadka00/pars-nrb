package np.org.nrb.pars.nrb.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Typed DTOs for the documented NRB Forex API. Field names mirror the actual JSON. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class NrbForexDtos {
    private NrbForexDtos() {}

    public record NrbForexResponse(
            @JsonProperty("status") NrbStatus status,
            @JsonProperty("errors") NrbErrors errors,
            @JsonProperty("params") Map<String, Object> params,
            @JsonProperty("data") NrbForexData data,
            @JsonProperty("pagination") NrbPagination pagination
    ) {}

    public record NrbStatus(@JsonProperty("code") int code) {}

    public record NrbErrors(@JsonProperty("validation") Map<String, List<String>> validation) {}

    public record NrbForexData(@JsonProperty("payload") List<NrbForexDay> payload) {
        public List<NrbForexDay> safePayload() {
            return payload == null ? List.of() : payload;
        }
    }

    public record NrbForexDay(
            @JsonProperty("date") String date,
            @JsonProperty("published_on") String publishedOn,
            @JsonProperty("modified_on") String modifiedOn,
            @JsonProperty("rates") List<NrbForexRate> rates
    ) {
        public List<NrbForexRate> safeRates() {
            return rates == null ? List.of() : rates;
        }
    }

    public record NrbForexRate(
            @JsonProperty("currency") NrbCurrency currency,
            @JsonProperty("buy") String buy,
            @JsonProperty("sell") String sell
    ) {
        public String iso3() {
            return currency == null ? null : currency.iso3();
        }

        public String name() {
            return currency == null ? null : currency.name();
        }

        public int unit() {
            return currency == null ? 0 : currency.unit();
        }

        /** Returns null when the source value is absent — never a fabricated zero. */
        public BigDecimal buyAmount() {
            return decimal(buy);
        }

        public BigDecimal sellAmount() {
            return decimal(sell);
        }

        private BigDecimal decimal(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return new BigDecimal(value.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public record NrbCurrency(
            @JsonProperty("unit") int unit,
            @JsonProperty("name") String name,
            @JsonProperty("iso3") @JsonAlias("ISO3") String iso3
    ) {}

    public record NrbPagination(
            @JsonProperty("page") Integer page,
            @JsonProperty("pages") Integer pages,
            @JsonProperty("per_page") Integer perPage,
            @JsonProperty("total") Integer total,
            @JsonProperty("links") NrbLinks links
    ) {}

    public record NrbLinks(
            @JsonProperty("prev") String prev,
            @JsonProperty("next") String next
    ) {}
}
