package np.org.nrb.pars.usermanagement.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record UpdateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 30) String mobile,
        @Size(max = 500) String address,
        UUID positionId,
        boolean enabled,
        Set<UUID> roleIds,
        @NotNull Long version
) {
}
