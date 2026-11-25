package np.org.nrb.pars.deal.repository;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class DealDocumentNumbersTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-10-12T04:30:00Z"),
            DealDocumentNumbers.ZONE
    );

    @Test
    void documentNoJoinsFiscalYearDealTypeAndSerial() {
        assertThat(DealDocumentNumbers.documentNo("2082/83", "FX", 1)).isEqualTo("2082/83_FX_1");
        assertThat(DealDocumentNumbers.documentNo("2082/83", "FX", 12)).isEqualTo("2082/83_FX_12");
    }

    @Test
    void likePatternTreatsUnderscoresAsLiteral() {
        assertThat(DealDocumentNumbers.likePattern("2082/83_FX_")).isEqualTo("2082/83\\_FX\\_%");
        assertThat(DealDocumentNumbers.suffixStart("2082/83_FX_")).isEqualTo(12);
    }

    @Test
    void fiscalDateUsesDealDateInKathmandu() {
        Instant dealDate = Instant.parse("2026-07-16T18:20:00Z");

        assertThat(DealDocumentNumbers.fiscalDate(dealDate, CLOCK)).isEqualTo(LocalDate.of(2026, 7, 17));
    }

    @Test
    void missingDealDateUsesTodayInKathmandu() {
        assertThat(DealDocumentNumbers.fiscalDate(null, CLOCK)).isEqualTo(LocalDate.of(2026, 10, 12));
        assertThat(CLOCK.getZone()).isEqualTo(ZoneId.of("Asia/Kathmandu"));
    }
}
