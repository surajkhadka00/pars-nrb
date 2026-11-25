package np.org.nrb.pars.rbac.model.dto;

import np.org.nrb.pars.rbac.model.AccessEffect;

import java.util.UUID;

public record UserApiAccessResponse(
        UUID id,
        UUID permissionId,
        String permissionCode,
        AccessEffect effect,
        Long version
) {
}
