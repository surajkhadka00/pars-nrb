package np.org.nrb.pars.deal.model.dto;

import java.util.UUID;

public record NostroResponse(
        UUID id,
        UUID counterpartyId,
        UUID currencyId,
        String currencyIsoCode,
        String value,
        String name,
        String description,
        String glAcNo,
        String iban,
        String swiftCode,
        boolean active,
        Long version
) {
}
