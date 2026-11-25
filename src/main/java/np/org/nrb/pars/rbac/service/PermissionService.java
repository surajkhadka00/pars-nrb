package np.org.nrb.pars.rbac.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.rbac.model.Permission;
import np.org.nrb.pars.rbac.model.UserApiAccess;
import np.org.nrb.pars.rbac.model.dto.PermissionResponse;
import np.org.nrb.pars.rbac.model.dto.UserApiAccessRequest;
import np.org.nrb.pars.rbac.model.dto.UserApiAccessResponse;
import np.org.nrb.pars.rbac.repository.PermissionRepository;
import np.org.nrb.pars.rbac.repository.UserApiAccessRepository;
import np.org.nrb.pars.usermanagement.model.AppUser;
import np.org.nrb.pars.usermanagement.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserApiAccessRepository userApiAccessRepository;
    private final AppUserRepository userRepository;
    private final ODataQuery oDataQuery;

    public PermissionService(
            PermissionRepository permissionRepository,
            UserApiAccessRepository userApiAccessRepository,
            AppUserRepository userRepository,
            ODataQuery oDataQuery
    ) {
        this.permissionRepository = permissionRepository;
        this.userApiAccessRepository = userApiAccessRepository;
        this.userRepository = userRepository;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(Permission.class, page, size, sort);
        return oDataQuery.findAll(permissionRepository, Permission.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return permissionRepository.findAll().stream()
                .map(permission -> new OptionItem(permission.getId(), permission.getCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserApiAccessResponse> listUserAccess(UUID userId) {
        return userApiAccessRepository.findByUserId(userId).stream().map(this::toAccessResponse).toList();
    }

    @Transactional
    public List<UserApiAccessResponse> replaceUserAccess(UUID userId, List<UserApiAccessRequest> requests) {
        AppUser user = userRepository.findById(userId).orElseThrow(() -> Exceptions.notFound("User not found"));
        userApiAccessRepository.deleteByUserId(userId);
        userApiAccessRepository.flush();
        for (UserApiAccessRequest request : requests) {
            Permission permission = permissionRepository.findById(request.permissionId())
                    .orElseThrow(() -> Exceptions.notFound("Permission not found"));
            UserApiAccess access = new UserApiAccess();
            access.setUser(user);
            access.setPermission(permission);
            access.setEffect(request.effect());
            userApiAccessRepository.save(access);
        }
        return listUserAccess(userId);
    }

    private PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDescription(),
                permission.getVersion()
        );
    }

    private UserApiAccessResponse toAccessResponse(UserApiAccess access) {
        return new UserApiAccessResponse(
                access.getId(),
                access.getPermission().getId(),
                access.getPermission().getCode(),
                access.getEffect(),
                access.getVersion()
        );
    }
}
