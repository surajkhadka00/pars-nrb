package np.org.nrb.pars.rbac.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.rbac.model.Permission;
import np.org.nrb.pars.rbac.model.Role;
import np.org.nrb.pars.rbac.model.dto.PermissionResponse;
import np.org.nrb.pars.rbac.model.dto.RoleRequest;
import np.org.nrb.pars.rbac.model.dto.RoleResponse;
import np.org.nrb.pars.rbac.repository.PermissionRepository;
import np.org.nrb.pars.rbac.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ODataQuery oDataQuery;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, ODataQuery oDataQuery) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(Role.class, page, size, sort);
        return oDataQuery.findAll(roleRepository, Role.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return roleRepository.findAll().stream()
                .map(role -> new OptionItem(role.getId(), role.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsByCode(request.code())) {
            throw Exceptions.conflict("Role code already exists");
        }
        if ("ADMIN".equalsIgnoreCase(request.code())) {
            throw Exceptions.forbidden("The ADMIN role is reserved and cannot be created");
        }
        Role role = new Role();
        role.setCode(request.code());
        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(resolvePermissions(request.permissionIds()));
        roleRepository.save(role);
        return toResponse(role);
    }

    @Transactional
    public RoleResponse update(UUID id, RoleRequest request) {
        Role role = load(id);
        if (request.version() != null && !Objects.equals(role.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        boolean isAdminRole = "ADMIN".equalsIgnoreCase(role.getCode());
        if (isAdminRole && !"ADMIN".equalsIgnoreCase(request.code())) {
            throw Exceptions.forbidden("The ADMIN role cannot be renamed");
        }
        if (!isAdminRole && "ADMIN".equalsIgnoreCase(request.code())) {
            throw Exceptions.conflict("The ADMIN code is reserved");
        }
        if (!role.getCode().equals(request.code()) && roleRepository.existsByCode(request.code())) {
            throw Exceptions.conflict("Role code already exists");
        }
        role.setCode(request.code());
        role.setName(request.name());
        role.setDescription(request.description());
        if (isAdminRole) {
            // ADMIN always retains full access to every permission known to the system.
            Set<Permission> all = new HashSet<>(permissionRepository.findAll());
            role.setPermissions(all);
        } else {
            role.setPermissions(resolvePermissions(request.permissionIds()));
        }
        roleRepository.save(role);
        return toResponse(role);
    }

    private Role load(UUID id) {
        return roleRepository.findWithPermissionsById(id).orElseGet(() ->
                roleRepository.findById(id).orElseThrow(() -> Exceptions.notFound("Role not found")));
    }

    private Set<Permission> resolvePermissions(Set<UUID> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw Exceptions.badRequest("One or more permissions were not found");
        }
        return new HashSet<>(permissions);
    }

    private RoleResponse toResponse(Role role) {
        Set<String> codes = role.getPermissions() == null
                ? Set.of()
                : role.getPermissions().stream().map(Permission::getCode).collect(Collectors.toSet());
        return new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getDescription(), codes, role.getVersion());
    }
}
