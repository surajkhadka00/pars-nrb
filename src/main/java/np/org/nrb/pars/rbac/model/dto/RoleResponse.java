package np.org.nrb.pars.rbac.model.dto;

import java.util.Set;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String code,
        String name,
        String description,
        Set<String> permissions,
        Long version
) {
}
