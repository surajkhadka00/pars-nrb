package np.org.nrb.pars.rbac.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import np.org.nrb.pars.rbac.model.dto.PermissionResponse;
import np.org.nrb.pars.rbac.model.dto.UserApiAccessRequest;
import np.org.nrb.pars.rbac.model.dto.UserApiAccessResponse;
import np.org.nrb.pars.rbac.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('" + PermissionCodes.PERMISSION_READ + "')")
    public ApiResponse<List<PermissionResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<PermissionResponse> result = permissionService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/permissions/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.PERMISSION_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(permissionService.options());
    }

    @GetMapping("/users/{userId}/api-access")
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_READ + "')")
    public ApiResponse<List<UserApiAccessResponse>> listAccess(@PathVariable UUID userId) {
        return ApiResponse.ok(permissionService.listUserAccess(userId));
    }

    @PutMapping("/users/{userId}/api-access")
    @PreAuthorize("hasAuthority('" + PermissionCodes.API_ACCESS_WRITE + "')")
    public ApiResponse<List<UserApiAccessResponse>> replaceAccess(
            @PathVariable UUID userId,
            @Valid @RequestBody List<UserApiAccessRequest> requests
    ) {
        return ApiResponse.ok("API access updated", permissionService.replaceUserAccess(userId, requests));
    }
}
