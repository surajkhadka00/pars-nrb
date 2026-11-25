package np.org.nrb.pars.rbac.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import np.org.nrb.pars.rbac.model.dto.RoleRequest;
import np.org.nrb.pars.rbac.model.dto.RoleResponse;
import np.org.nrb.pars.rbac.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.ROLE_READ + "')")
    public ApiResponse<List<RoleResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<RoleResponse> result = roleService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ROLE_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(roleService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ROLE_READ + "')")
    public ApiResponse<RoleResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(roleService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.ROLE_WRITE + "')")
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.ok("Role created", roleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ROLE_WRITE + "')")
    public ApiResponse<RoleResponse> update(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        return ApiResponse.ok("Role updated", roleService.update(id, request));
    }
}
