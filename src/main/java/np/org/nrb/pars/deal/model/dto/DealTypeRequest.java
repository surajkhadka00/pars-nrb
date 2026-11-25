package np.org.nrb.pars.deal.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DealTypeRequest(
        @NotBlank @Size(max = 20) String value,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2000) String description,
        @Size(max = 20) String contactMode,
        Boolean instrumentCodeApplicable,
        @Size(max = 20) String limitType,
        BigDecimal limitValue,
        Boolean active,
        Long version
) {
}
