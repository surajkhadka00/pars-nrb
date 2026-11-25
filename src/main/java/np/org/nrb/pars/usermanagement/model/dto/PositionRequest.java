package np.org.nrb.pars.usermanagement.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PositionRequest(
        @NotBlank @Size(max = 20) String value,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 20) String limitType,
        BigDecimal limitValue,
        Boolean active,
        Long version
) {
}
