package np.org.nrb.pars.usermanagement.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import np.org.nrb.pars.usermanagement.model.dto.PositionRequest;
import np.org.nrb.pars.usermanagement.model.dto.PositionResponse;
import np.org.nrb.pars.usermanagement.service.PositionService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.POSITION_READ + "')")
    public ApiResponse<List<PositionResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<PositionResponse> result = positionService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.POSITION_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(positionService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.POSITION_READ + "')")
    public ApiResponse<PositionResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(positionService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.POSITION_WRITE + "')")
    public ApiResponse<PositionResponse> create(@Valid @RequestBody PositionRequest request) {
        return ApiResponse.ok("Position created", positionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.POSITION_WRITE + "')")
    public ApiResponse<PositionResponse> update(@PathVariable UUID id, @Valid @RequestBody PositionRequest request) {
        return ApiResponse.ok("Position updated", positionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.POSITION_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        positionService.delete(id);
        return ApiResponse.ok("Position deleted", null);
    }
}
