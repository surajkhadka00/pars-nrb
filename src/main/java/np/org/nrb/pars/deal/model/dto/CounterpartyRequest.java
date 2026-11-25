package np.org.nrb.pars.deal.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CounterpartyRequest(
        @NotBlank @Size(max = 20) String value,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2000) String description,
        @Size(max = 2000) String address,
        UUID userId,
        @Size(max = 40) String bloombergCode,
        UUID countryId,
        @Size(max = 2000) String comments,
        @Size(max = 120) String contactName,
        @Size(max = 100) String email,
        @Size(max = 40) String investmentGrade,
        Boolean ownBank,
        @Size(max = 20) String limitType,
        BigDecimal limitValue,
        @Size(max = 120) String location,
        @Size(max = 30) String phone,
        @Size(max = 100) String position,
        @Size(max = 40) String reutersDealingCode,
        Boolean active,
        Long version
) {
}
