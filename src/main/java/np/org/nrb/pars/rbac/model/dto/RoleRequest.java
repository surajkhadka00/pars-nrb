package np.org.nrb.pars.rbac.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record RoleRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        Set<UUID> permissionIds,
        Long version
) {
}
