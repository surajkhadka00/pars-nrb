package np.org.nrb.pars.deal.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CountryRequest(
        @NotBlank @Size(max = 20) String value,
        @NotBlank @Size(max = 120) String name,
        Boolean active,
        Long version
) {
}
