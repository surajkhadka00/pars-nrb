package np.org.nrb.pars.rbac.model.dto;

import jakarta.validation.constraints.NotNull;
import np.org.nrb.pars.rbac.model.AccessEffect;

import java.util.UUID;

public record UserApiAccessRequest(
        @NotNull UUID permissionId,
        @NotNull AccessEffect effect
) {
}
