package np.org.nrb.pars.deal.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InstrumentCodeRequest(
        @NotNull UUID dealTypeId,
        @NotBlank @Size(max = 20) String value,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2000) String description,
        Boolean active,
        Long version
) {
}
