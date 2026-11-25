package np.org.nrb.pars.rbac.model.dto;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String code,
        String name,
        String description,
        Long version
) {
}
