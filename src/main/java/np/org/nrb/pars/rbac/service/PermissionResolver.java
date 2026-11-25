package np.org.nrb.pars.rbac.service;

import np.org.nrb.pars.rbac.model.AccessEffect;
import np.org.nrb.pars.rbac.model.Permission;
import np.org.nrb.pars.rbac.model.Role;
import np.org.nrb.pars.rbac.model.UserApiAccess;
import np.org.nrb.pars.rbac.repository.PermissionRepository;
import np.org.nrb.pars.rbac.repository.UserApiAccessRepository;
import np.org.nrb.pars.usermanagement.model.AppUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionResolver {

    private final UserApiAccessRepository userApiAccessRepository;
    private final PermissionRepository permissionRepository;

    public PermissionResolver(UserApiAccessRepository userApiAccessRepository, PermissionRepository permissionRepository) {
        this.userApiAccessRepository = userApiAccessRepository;
        this.permissionRepository = permissionRepository;
    }

    public Set<String> resolve(AppUser user) {
        Set<String> granted = new HashSet<>();
        for (Role role : user.getRoles()) {
            if ("ADMIN".equalsIgnoreCase(role.getCode())) {
                // ADMIN always retains full access, regardless of accidental DB edits.
                permissionRepository.findAll().forEach(permission -> granted.add(permission.getCode()));
            } else {
                for (Permission permission : role.getPermissions()) {
                    granted.add(permission.getCode());
                }
            }
        }
        List<UserApiAccess> overrides = userApiAccessRepository.findByUserId(user.getId());
        for (UserApiAccess override : overrides) {
            String code = override.getPermission().getCode();
            if (override.getEffect() == AccessEffect.GRANT) {
                granted.add(code);
            } else if (override.getEffect() == AccessEffect.DENY) {
                granted.remove(code);
            }
        }
        return granted;
    }

    public Set<String> roleCodes(AppUser user) {
        return user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet());
    }
}
