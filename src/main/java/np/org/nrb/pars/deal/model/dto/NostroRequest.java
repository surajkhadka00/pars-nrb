package np.org.nrb.pars.deal.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record NostroRequest(
        UUID counterpartyId,
        @NotNull UUID currencyId,
        @NotBlank @Size(max = 40) String value,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String description,
        @Size(max = 16) String glAcNo,
        @Size(max = 40) String iban,
        @Size(max = 11) String swiftCode,
        Boolean active,
        Long version
) {
}
