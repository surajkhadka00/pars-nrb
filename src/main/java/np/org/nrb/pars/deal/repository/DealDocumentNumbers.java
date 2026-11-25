package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.common.exception.Exceptions;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

final class DealDocumentNumbers {

    static final int MAX_LENGTH = 40;
    static final ZoneId ZONE = ZoneId.of("Asia/Kathmandu");

    private DealDocumentNumbers() {
    }

    static LocalDate fiscalDate(Instant dealDate, Clock clock) {
        if (dealDate == null) {
            return LocalDate.now(clock);
        }
        return dealDate.atZone(clock.getZone()).toLocalDate();
    }

    static String documentNo(String fiscalYearValue, String dealTypeValue, int serial) {
        String documentNo = fiscalYearValue + "_" + dealTypeValue + "_" + serial;
        if (documentNo.length() > MAX_LENGTH) {
            throw Exceptions.badRequest("Generated document number exceeds 40 characters");
        }
        return documentNo;
    }

    static String likePattern(String prefix) {
        return escapeLike(prefix) + "%";
    }

    static int suffixStart(String prefix) {
        return prefix.length() + 1;
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
