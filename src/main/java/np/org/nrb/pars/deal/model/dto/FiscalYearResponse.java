package np.org.nrb.pars.deal.model.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FiscalYearResponse(
        UUID id,
        String value,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        UUID createdBy,
        UUID updatedBy,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
