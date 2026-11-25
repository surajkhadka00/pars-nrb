package np.org.nrb.pars.deal.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CounterpartyResponse(
        UUID id,
        String value,
        String name,
        String description,
        String address,
        UUID userId,
        String bloombergCode,
        UUID countryId,
        String comments,
        String contactName,
        String email,
        String investmentGrade,
        boolean ownBank,
        String limitType,
        BigDecimal limitValue,
        String location,
        String phone,
        String position,
        String reutersDealingCode,
        boolean active,
        Long version
) {
}
